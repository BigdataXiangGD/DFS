package sdfs.message;

import java.io.IOException;
import java.io.Serializable;

/**
 * Class ElectionMessage
 */
public class FSMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String EOF = "$$$";
    private static final String ARG_SEPARATOR = " ";
    public final FSMessageType type;
    protected final String fileName;
    protected final String ipAddress;
    protected final int port;

    private FSMessage(FSMessageType t) {
        type = t;
        fileName = "";
        ipAddress = "";
        port = 0;
    }

    private FSMessage(FSMessageType t, String filename) {
        type = t;
        fileName = filename;
        ipAddress = "";
        port = 0;
    }

    private FSMessage(FSMessageType t, String filename, String ipAddress, int port) {
        type = t;
        fileName = filename;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    /** Gets String from message
     * @return String of the message
     */
    public String toString() {
        return type.toString() + ARG_SEPARATOR + fileName + ARG_SEPARATOR + ipAddress + ARG_SEPARATOR + String.valueOf(port);
    }

    public static FSMessage createGetMessage(String filename) {
        return new FSMessage(FSMessageType.GET, filename);
    }

    public static FSMessage createPutMessage(String filename) {
        return new FSMessage(FSMessageType.PUT, filename);
    }

    public static FSMessage createDelMessage(String filename) {
        return new FSMessage(FSMessageType.DEL, filename);
    }

    public static FSMessage createReplicateMessage(String filename, String ipAddress, int port) {
        return new FSMessage(FSMessageType.REP, filename, ipAddress, port);
    }

    public static FSMessage createOkayMessage() {
        return new FSMessage(FSMessageType.YES);
    }

    public static FSMessage createNayMessage() {
        return new FSMessage(FSMessageType.NO);
    }

    /**
     * @param message
     * @return
     * @throws IOException
     */
    public static FSMessage retrieveMessage(String message) throws IOException {
        String[] args = message.split(ARG_SEPARATOR);
        return new FSMessage(FSMessageType.fromString(args[0]), args[1], args[2], Integer.parseInt(args[3]));
    }
}
