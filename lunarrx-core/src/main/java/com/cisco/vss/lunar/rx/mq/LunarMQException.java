/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cisco.vss.lunar.rx.mq;


class LunarMQException extends Exception
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	enum StreamingError {
        LMQ_RETRY ("Retry"),
        LMQ_OK ("OK"),
        LMQ_EOF ("End of file"),
        LMQ_EOS ("End of stream"),
        LMQ_READ_ERR ("Error reading"),
        LMQ_WRITE_ERR ("Error writing"),
        LMQ_SOCK_ERR ("Socket error"),
        LMQ_CONNECT_ERR ("Failed to connect to server"),
        LMQ_SEND_ERR ("Failed to send request to server"),
        LMQ_BAD_URL ("Invalid URL"),
        LMQ_HEADER_TOO_LONG ("Header too long"),
        LMQ_BAD_HEADER_FORMAT ("Bad header format, should be in the form of 'size msg#'"),
        LMQ_REQUEST_TOO_LONG ("Request too long"),
        LMQ_BAD_REQUEST_FORMAT ("Bad request format, should be in the form of <mime>:<sourceID>"),
        LMQ_NO_SUCH_STREAM ("The requested stream was not found"),
        LMQ_STREAM_ALREADY_EXISTS ("Stream already exists"),
        LMQ_TOO_BIG ("Message too big"),
        LMQ_TOO_MANY_POSTS ("Too many posts per second"),
        LMQ_UNKNOWN ("Unknown");

        private String message;

        private StreamingError(String msg) {
            message = msg;
        }

        String GetMessage() {
            return message;
        }

        static StreamingError FromMessage(String msg) {
            for (StreamingError err : StreamingError.values())
                if (err.message.equals(msg))
                    return err;
            return null;
        }
    }

    private final boolean        isRecoverable;
    private final StreamingError code;

    LunarMQException(String message, boolean isRecoverable, StreamingError code)
    {
        super(message);
        this.isRecoverable = isRecoverable;
        this.code          = code;
    }
    
    LunarMQException(boolean isRecoverable, StreamingError code) {
        this(code.GetMessage(), isRecoverable, code);
    }


    LunarMQException(String message)
    {
    	this(message, false, StreamingError.LMQ_UNKNOWN);
    }

    boolean isRecoverable()
    {
        return this.isRecoverable;
    }

    StreamingError getCode() {
        return code;
    }
}
