package sdfs.message;

import sdfs.FailureDetector.Info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class ElectionMessage
 */
public class FDMessage {
    private final char PARAM_DELIM=' ';
    private final char INFO_DELIM=';';
    public final FDMessageType type;
    private final String [] messageParams;
    private List<Info> infoAttached;

    /**
     * Fields of Parameters
     *
     */
    private enum ParamsFields {
        messageKey (0),
        senderID (1),
        destinationID (2);

        public final int index;
        ParamsFields(int index) {
            this.index=index;
        }
    }

    private FDMessage(FDMessageType type, String [] params) {
        this.type=type;
        messageParams=params;
        infoAttached=new ArrayList<Info>();
    }

    /** Gets bytes from message
     * @return bytearray of the message
     */
    public byte [] toByteArray() {
        StringBuilder builder=new StringBuilder();
        builder.append(type.getMessagePrefix());
        for (String param : messageParams)
            builder.append(PARAM_DELIM).append(param);

        for (Info i : infoAttached) {
            builder.append(INFO_DELIM).append(i.toString());
        }

        byte [] ret = builder.toString().getBytes();
        assert ret.length<1024 : "FATAL ERROR ! byte overflow";
        return ret;
    }

    /** Extract mesage info
     * @param messageBytes
     * @param byteLen
     * @return
     */
    public static FDMessage extractMessage(byte[] messageBytes, int byteLen) {
        String[] tokens=new String(messageBytes,0,byteLen).split(";");
        String[] mStr=tokens[0].split(" ");
        FDMessageType type = null;
        try {
            type= FDMessageType.getMessageType(mStr[0].charAt(0));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        String [] params = new String[mStr.length-1];
        for (int i=1;i<params.length+1;i++)
            params[i-1]=mStr[i];

        FDMessage ret=new FDMessage(type,params);
        for (int i=1;i<tokens.length;i++) {
            try {
                ret.infoAttached.add(Info.fromString(tokens[i]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    /** Helper function
     * @return Sender PID
     */
    public String getMessageSenderID() {
        assert messageParams.length>ParamsFields.senderID.index : "This type of message " +
                "does not have senderID as param";
        return messageParams[ParamsFields.senderID.index];
    }

    /** Helper function
     * @return ElectionMessage key
     */
    public String getMessageKey() {
        assert messageParams.length>ParamsFields.messageKey.index : "This type of message " +
                "does not have senderID as param";
        return messageParams[ParamsFields.messageKey.index];
    }

    /** Helper function
     * @return Destination of message
     */
    public String getReqDestination() {
        assert messageParams.length>ParamsFields.destinationID.index : "This type of message " +
                "does not have senderID as param";
        return messageParams[ParamsFields.destinationID.index];
    }

    /** Get info from message
     * @return list of parameters from information
     */
    public List<Info> getInfoList() {
        return this.infoAttached;
    }

    /**
     * Class for building message
     *
     */
    public static class MessageBuilder {
        private FDMessage m;
        public MessageBuilder(FDMessageType type, String [] args) {
            m=new FDMessage(type,args);
        }
        /** Construct ping message
         * @param messageKey
         * @param senderID
         * @return
         */
        public static MessageBuilder buildPingMessage(String messageKey,String senderID) {
            String [] args=new String[2];
            args[ParamsFields.messageKey.index]=messageKey;
            args[ParamsFields.senderID.index]=senderID;

            MessageBuilder newInstance=new MessageBuilder(FDMessageType.PING,args);
            return newInstance;
        }

        /** Build ping requqest message
         * @param messageKey
         * @param senderID
         * @param destinationID
         * @return
         */
        public static MessageBuilder buildPingReqMessage(String messageKey,String senderID,String destinationID) {
            String [] args=new String[3];
            args[ParamsFields.messageKey.index]=messageKey;
            args[ParamsFields.senderID.index]=senderID;
            args[ParamsFields.destinationID.index]=destinationID;

            MessageBuilder newInstance=new MessageBuilder(FDMessageType.PING_REQUEST,args);
            return newInstance;
        }

        /** Build ping ack request message 
         * @param messageKey
         * @param senderID
         * @return
         */
        public static MessageBuilder buildAckReqMessage(String messageKey,String senderID) {
            String [] args=new String[2];
            args[ParamsFields.messageKey.index]=messageKey;
            args[ParamsFields.senderID.index]=senderID;

            MessageBuilder newInstance=new MessageBuilder(FDMessageType.ACK_REQUEST,args);
            return newInstance;
        }

        /** Build Ack message
         * @param ackID
         * @return
         */
        public static MessageBuilder buildAckMessage(String ackID) {
            String [] args=new String[1];
            args[ParamsFields.messageKey.index]=ackID;

            MessageBuilder newInstance=new MessageBuilder(FDMessageType.ACK,args);
            return newInstance;
        }

        /** Build not in memlist message
         * @return
         */
        public static MessageBuilder buildMissingNoticeMessage() {
            String [] args=new String[0];

            MessageBuilder newInstance=new MessageBuilder(FDMessageType.MISSING_NOTICE,args);
            return newInstance;
        }

        /** ElectionMessage to end reciever
         * @return
         */
        public static MessageBuilder buildEndNoticeMessage() {
            String [] args=new String[0];

            MessageBuilder newInstance=new MessageBuilder(FDMessageType.END,args);
            return newInstance;
        }

        /** Build parameter to infolist
         * @param infos
         * @return
         */
        public MessageBuilder addInfoFromList(Collection<Info> infos) {
            for (Info i : infos) {
                this.m.infoAttached.add(i);
            }

            return this;
        }
        
        /** Add join info to info list
         * @param joinerID
         * @return
         */
        public MessageBuilder addJoinInfo(String joinerID) {
            this.m.infoAttached.add(new Info(Info.InfoType.JOIN, joinerID));
            return this;
        }

        /** Add leave info to info list
         * @param leaverID
         * @return
         */
        public MessageBuilder addLeaveInfo(String leaverID) {
            this.m.infoAttached.add(new Info(Info.InfoType.LEAVE, leaverID));
            return this;
        }

        /** Get message
         * @return
         */
        public final FDMessage getMessage() {
            return m;
        }
    }
}
