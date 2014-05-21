
package com.cisco.vss.lunar.rx.mq;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class LunarMQSocket
{   
	private final static Logger        LOGGER = LogManager.getLogger();
	private final String               ip;
	private final int                  port;
	private final String               message;
    private       Socket               socket;
    private       BufferedInputStream  inputStream;
    private       OutputStream         outputStream;
    private int                        incomingMessageNum;
    private int                        outgoingMessageNum;
    
    static LunarMQSocket createSocket(final String ip, int port, final String message) throws IOException, LunarMQException
    {
        final LunarMQSocket lmqSocket = new LunarMQSocket(ip, port, message);
        
        return lmqSocket;
    }

    //TODO: why not to use some more efficient binary protocol?
    //TODO: there is no such a thing a bidirectional communication. This class still violates SRP
    byte[] read() throws IOException, LunarMQException
    {
    	final MessageHeader header = MessageHeader.read(inputStream);
    	
    	incomingMessageNum = header.checkSequence(incomingMessageNum);
    	return header.readBody(inputStream);
    }     
    
    void write(final String message) throws IOException, LunarMQException
    {   
        write(message.getBytes());
    }     
    
    void write(byte[] buffer) throws IOException, LunarMQException
    {   
        /* @todo async write can only be done with NIO - not for now */
    	final MessageHeader header = new MessageHeader(buffer.length, outgoingMessageNum++);
    	for(int retries=0; retries < 3; retries++)
	    	try {
	    		header.write(outputStream);
	    		outputStream.write(buffer);
	    		return; //how ugly!!!
	    	} catch(SocketException exp) {
	    		if("Broken pipe".equals(exp.getMessage())) {
	    			try {
	    				LOGGER.warn("Got Broken pipe exception. Retries count: {}", retries+1);
	    				close();
	    				open();
	    			} catch(Throwable exp1) {
	    				throw exp1;
	    			}
	    		} else throw exp;
	    	} catch (IOException exp) {
	    		final String responseMsg = readResponse();
	            LunarMQException.StreamingError lmqe = LunarMQException.StreamingError.FromMessage(responseMsg);
	            if (lmqe != null) 
	                throw new LunarMQException(false, lmqe);
	
	            // this so some unknown error, throw it...
	            throw exp;
	    	}
    }       
    
    void close() throws IOException 
    {       
       inputStream.close();
       outputStream.close();
       socket.close();
    }          
    
    private LunarMQSocket(final String ip, final int port, final String message) throws IOException, LunarMQException
    {   
    	this.ip            = ip;
    	this.port          = port;
    	this.message       = message;
    	open();
    }
    
    private void open() throws IOException, LunarMQException {
    	init();
    	handShake(message);    	
    }
    
    private void init() throws UnknownHostException, IOException {
        socket             = new Socket(ip, port);
        inputStream        = new BufferedInputStream(socket.getInputStream());
        outputStream       = socket.getOutputStream();
        outgoingMessageNum = 0;
        incomingMessageNum = -1;    	
    }
    
    private void handShake(final String message) throws IOException, LunarMQException {
        write(message);
        final String response = readResponse();
        if(null==response)
        	throw new LunarCannotReadHandshakeResponseException();
        if (!LunarMQException.StreamingError.LMQ_OK.GetMessage().equals(response))
        	throw new LunarHandshakeFailureException(response);
    }
    
    private String readResponse() throws IOException, LunarMQException {
    	for(int retries = 0; retries < 3; retries++)
	        try
	        {
	            return new String(read());
	        } catch (LunarEndOfStreamException ex)
	        {
				LOGGER.warn("Got an error {} while reading MQ response. Retries count: {}", ex.toString(), retries+1);
	        	try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
	        }
        return null;
    }
}
