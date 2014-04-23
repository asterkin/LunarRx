
package com.cisco.vss.lunar.rx.mq;

import java.io.*;
import java.net.Socket;

class LunarMQSocket
{        
    private final Socket               socket;
    private final BufferedInputStream  inputStream;
    private final OutputStream         outputStream;
    private int                        incomingMessageNum;
    private int                        outgoingMessageNum;
    
    static LunarMQSocket createSocket(final String ip, int port, final String message) throws IOException, LunarMQException
    {
        final LunarMQSocket lmqSocket = new LunarMQSocket(new Socket(ip, port));
        
        lmqSocket.handShake(message);
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
    	try {
    		header.write(outputStream);
    		outputStream.write(buffer);
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
    
    private LunarMQSocket(final Socket s) throws IOException
    {        
        socket             = s;
        inputStream        = new BufferedInputStream(socket.getInputStream());
        outputStream       = socket.getOutputStream();
        outgoingMessageNum = 0;
        incomingMessageNum = -1;
    }
    
    private void handShake(final String message) throws IOException, LunarMQException {
        write(message);
        final String response = readResponse();
        if (!LunarMQException.StreamingError.LMQ_OK.GetMessage().equals(response))
        	throw new LunarHandshakeFailureException(response);
    }
    
    private String readResponse() throws IOException, LunarMQException {
    	int retries = 3;
    	while(retries > 0)
	        try
	        {
	            return new String(read());
	        } catch (LunarEndOfStreamException ex)
	        {
	        	retries--;
	        	try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
	        }
        throw new LunarCannotReadHandshakeResponseException();
    }
}
