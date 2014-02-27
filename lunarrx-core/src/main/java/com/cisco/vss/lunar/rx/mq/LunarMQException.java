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

	public enum StreamingError {
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

        private String m_message;

        private StreamingError(String msg) {
            m_message = msg;
        }

        public String GetMessage() {
            return m_message;
        }

        public static StreamingError FromMessage(String msg) {
            for (StreamingError err : StreamingError.values())
                if (err.m_message.equals(msg))
                    return err;
            return null;
        }
    }

    public boolean m_isRecoverable;
    public StreamingError m_code;

    public LunarMQException(boolean isRecoverable) {
        this(StreamingError.LMQ_UNKNOWN.GetMessage(), isRecoverable, StreamingError.LMQ_UNKNOWN);
    }
    
    public LunarMQException(boolean isRecoverable, StreamingError code) {
        this(code.GetMessage(), isRecoverable, code);
    }

    public LunarMQException(String message, boolean isRecoverable, StreamingError code)
    {
        super(message);
        m_isRecoverable = isRecoverable;
        m_code = code;
    }

    public LunarMQException(String message, boolean isRecoverable)
    {
        super(message);
        m_isRecoverable = isRecoverable;
        m_code = StreamingError.LMQ_UNKNOWN;
    }

    public boolean isRecoverable()
    {
        return m_isRecoverable;
    }

    public StreamingError getCode() {
        return m_code;
    }
}
