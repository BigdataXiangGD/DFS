package sdfs.message;

import java.io.IOException;
import java.io.Serializable;

/**
 * Annum for Type of messages
 */
public enum FSMessageType implements Serializable {
    GET("G"),
    PUT("P"),
    REP("R"),
    YES("Y"),
    DEL("D"),
    NO("N");

    public final String messagePrefix;

    FSMessageType(String n) {
        messagePrefix = n;
    }

    @Override
    public String toString() {
        return messagePrefix;
    }

    /**
     * @param string
     * @return
     * @throws IOException
     */
    public static FSMessageType fromString(String string) throws IOException {
        if (string.equals("G"))
            return FSMessageType.GET;
        else if (string.equals("P"))
            return FSMessageType.PUT;
        else if (string.equals("R"))
            return FSMessageType.REP;
        else if (string.equals("Y"))
            return FSMessageType.YES;
        else if (string.equals("D"))
            return FSMessageType.DEL;
        else if (string.equals("N"))
            return FSMessageType.NO;
        else
            throw new IOException("Type not found");
    }
}
