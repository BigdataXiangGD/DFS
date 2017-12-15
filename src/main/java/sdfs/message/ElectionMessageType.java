package sdfs.message;

import java.io.IOException;

/**
 * Annum for Type of messages
 */
public enum ElectionMessageType {
    MASTER ('M'),
    MASTER_REPLY ('R'),
    COORDINATOR('C'),
	OK('K'),
	GET('G'),
	GET_REPLY('E');
	
    private final char messagePrefix;
    ElectionMessageType(char p) {
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
    public static ElectionMessageType getMessageType(char prefix) throws IOException {
        if (prefix=='M')
            return MASTER;
        else if (prefix=='R')
            return MASTER_REPLY;
        else if (prefix=='C')
            return COORDINATOR;
        else if (prefix=='K')
            return OK;
        else if (prefix=='G')
        	return GET;
        else if (prefix=='E')
        	return GET_REPLY;
            throw new IOException("ElectionMessage prefix supplied is not recognized!");
    }
}
