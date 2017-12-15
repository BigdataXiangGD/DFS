package sdfs.FailureDetector;

import java.io.IOException;

/**
 * Annum for Type of messages
 */
public enum FDMessageType {
    PING ('P'),
    PING_REQUEST ('Q'),
    ACK ('A'),
    ACK_REQUEST ('B'),
    MISSING_NOTICE ('M'),
    END ('E');

    private final char messagePrefix;
    FDMessageType(char p) {
        messagePrefix =p;
    }

    /** Get message prefix from message
     * @return
     */
    public char getMessagePrefix() {
        return messagePrefix;
    }

    /** Get message type from message
     * @param prefix
     * @return
     * @throws IOException
     */
    public static FDMessageType getMessageType(char prefix) throws IOException {
        if (prefix=='P')
            return PING;
        else if (prefix=='Q')
            return PING_REQUEST;
        else if (prefix=='A')
            return ACK;
        else if (prefix=='B')
            return ACK_REQUEST;
        else if (prefix=='M')
            return MISSING_NOTICE;
        else if (prefix=='E')
            return END;
        else
            throw new IOException("ElectionMessage prefix supplied is not recognized!");
    }
}
