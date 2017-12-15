package sdfs.ElectionService;

import java.io.IOException;

/**
 * Class ElectionMessage
 */
public class ElectionMessage {
    private final char PARAM_DELIM = ' ';
    public final ElectionMessageType type;
    public final String[] messageParams;

    /**
     * Fields of Parameters
     */
    private ElectionMessage(ElectionMessageType type, String[] params) {
        this.type = type;
        messageParams = params;
    }

    /**
     * Gets String from message
     *
     * @return String of the message
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type.getMessagePrefix());
        for (String param : messageParams)
            builder.append(PARAM_DELIM).append(param);
        String ret = builder.toString();
        return ret;
    }

    /**
     * Extract message info
     *
     * @param msg
     * @return
     */
    public static ElectionMessage extractMessage(String msg) {
        String[] mStr = msg.split(" ");
        ElectionMessageType type = null;
        try {
            type = ElectionMessageType.getMessageType(mStr[0].charAt(0));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        String[] params = new String[mStr.length - 1];
        for (int i = 1; i < params.length + 1; i++)
            params[i - 1] = mStr[i];

        ElectionMessage ret = new ElectionMessage(type, params);
        return ret;
    }

    /**
     * Class for building message
     */
    public static class MessageBuilder {
        /**
         * Builds Master ElectionMessage
         *
         * @param senderId
         * @return
         */
        public static ElectionMessage buildMasterMessage(String senderId) {
            String[] args = new String[1];
            args[0] = senderId;
            return new ElectionMessage(ElectionMessageType.MASTER, args);
        }

        /**
         * Builds reply to master message
         *
         * @param masterID
         * @return
         */
        public static ElectionMessage buildMasterReplyMessage(String masterID) {
            String[] args = new String[1];
            args[0] = masterID;
            return new ElectionMessage(ElectionMessageType.MASTER_REPLY, args);
        }

        /**
         * Builds coordinator message
         *
         * @param masterID
         * @return
         */
        public static ElectionMessage buildCoordMessage(String masterID) {
            String[] args = new String[1];
            args[0] = masterID;
            return new ElectionMessage(ElectionMessageType.COORDINATOR, args);
        }

        /**
         * Builds ok message
         *
         * @param senderID
         * @return
         */
        public static ElectionMessage buildOKMessage(String senderID) {
            String[] args = new String[1];
            args[0] = senderID;
            return new ElectionMessage(ElectionMessageType.OK, args);
        }

        /**
         * Builds put message
         *
         * @param filename
         * @return
         */
        public static ElectionMessage buildPutMessage(String filename) {
            String[] args = new String[1];
            args[0] = filename;
            return new ElectionMessage(ElectionMessageType.PUT, args);
        }

        /**
         * Builds reply to put message
         *
         * @param validity
         * @param servers
         * @return
         */
        public static ElectionMessage buildPutReplyMessage(String validity, String[] servers) {
            String[] args = new String[1 + servers.length];
            args[0] = validity;
            for (int i = 1; i < args.length; i++) {
                args[i] = servers[i - 1];
            }
            return new ElectionMessage(ElectionMessageType.PUT_REPLY, args);
        }

        /**
         * Builds get message
         *
         * @param filename
         * @return
         */
        public static ElectionMessage buildGetMessage(String filename) {
            String[] args = new String[1];
            args[0] = filename;
            return new ElectionMessage(ElectionMessageType.GET, args);
        }

        /**
         * Build reply to get message
         *
         * @param validity
         * @param servers
         * @return
         */
        public static ElectionMessage buildGetReplyMessage(String validity, String[] servers) {
            String[] args = new String[1 + servers.length];
            args[0] = validity;
            for (int i = 1; i < args.length; i++) {
                args[i] = servers[i - 1];
            }
            return new ElectionMessage(ElectionMessageType.GET_REPLY, args);
        }

        /**
         * Build delete message
         *
         * @param filename
         * @return
         */
        public static ElectionMessage buildDeleteMessage(String filename) {
            String[] args = new String[1];
            args[0] = filename;
            return new ElectionMessage(ElectionMessageType.DELETE, args);
        }

        /**
         * Build reply to delete message
         *
         * @param validity
         * @return
         */
        public static ElectionMessage buildDeleteReplyMessage(String validity) {
            String[] args = new String[1];
            args[0] = validity;
            return new ElectionMessage(ElectionMessageType.DELETE_REPLY, args);
        }

        /**
         * Build new files message
         *
         * @param serverID
         * @param files
         * @return
         */
        public static ElectionMessage buildNewfilesMessage(String serverID, String[] files) {
            String[] args = new String[1 + files.length];
            args[0] = serverID;
            for (int i = 1; i < args.length; i++) {
                args[i] = files[i - 1];
            }
            return new ElectionMessage(ElectionMessageType.NEWFILES, args);
        }

        /**
         * Builds list message
         *
         * @param senderID
         * @return
         */
        public static ElectionMessage buildListMessage(String senderID) {
            String[] args = new String[1];
            args[0] = senderID;
            return new ElectionMessage(ElectionMessageType.LIST, args);
        }

        /**
         * Build reply to list message
         *
         * @param senderID
         * @param files
         * @return
         */
        public static ElectionMessage buildListReplyMessage(String senderID, String[] files) {
            String[] args = new String[1 + files.length];
            args[0] = senderID;
            for (int i = 1; i < args.length; i++) {
                args[i] = files[i - 1];
            }
            return new ElectionMessage(ElectionMessageType.LIST_REPLY, args);
        }
    }
}
