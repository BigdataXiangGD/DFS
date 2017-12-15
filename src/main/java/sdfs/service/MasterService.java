package sdfs.service;

import sdfs.message.ElectionMessage;
import sdfs.message.FSMessage;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import sdfs.Pid;
import sdfs.ServerMain;
import sdfs.message.ElectionMessageType;
import sdfs.message.ElectionMessage.MessageBuilder;
import sdfs.FailureDetector.FailureDetector;

/**
 * Class for Master Service
 */
public class MasterService {
    private int REPLICATION_TIMEOUT = 3000;
    private int REPLICATION_UNIT = 3;
    private int SERVER_TIMEOUT = 100;
    private FailureDetector FD;
    private ServerSocket welcomeSocket;
    private HashMap<String, MutablePair<Set<String>, Long>> filemap;


    /**
     * Update file meta data
     *
     * @param serverid
     * @param filename
     */
    private void updateFileMap(String serverid, String filename) {
        if (filemap.containsKey(filename)) {
            filemap.get(filename).getLeft().add(serverid);
            filemap.get(filename).setRight(new Long(System.currentTimeMillis()));
        } else {
            Set<String> s = new HashSet<String>();
            s.add(serverid);
            Long timestamp = new Long(System.currentTimeMillis());
            filemap.put(filename, new MutablePair<Set<String>, Long>(s, timestamp));
        }
    }

    /**
     * Update files in local FS
     *
     * @param filenames
     */
    public void updateSelfFiles(List<String> filenames) {
        for (String filename : filenames) {
            updateFileMap(FD.getSelfID().toString(), filename);
        }
    }

    public MasterService(int port) {
        FD = ServerMain.FD;
        filemap = new HashMap<String, MutablePair<Set<String>, Long>>();
        try {
            welcomeSocket = new ServerSocket(port);
            welcomeSocket.setSoTimeout(SERVER_TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("[MASTER]: Unable to start master server socket");
            System.exit(-1);
        }
    }

    /**
     * Send file to clientSocket
     *
     * @param clientSocket
     * @param msg
     * @return
     */
    private String sendMessage(Socket clientSocket, String msg) {
        String response = null;
        try {
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.writeBytes(msg + '\n');
            System.out.println("[DEBUG][MASTER]: ElectionMessage sent " + msg);
            response = inFromServer.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[ERROR]: Sending ElectionMessage");
        }
        return response;
    }

    /**
     * Handle reply from clientSocket
     *
     * @param msg
     * @param clientSocket
     * @throws IOException
     */
    private void handleMessage(String msg, Socket clientSocket) throws IOException {
        System.out.println("[DEBUG][MASTER]: ElectionMessage Recieved " + msg + " at time " + String.valueOf(System.currentTimeMillis()));
        ElectionMessage m = ElectionMessage.extractMessage(msg);
        if (m.type == ElectionMessageType.OK) {

        } else if (m.type == ElectionMessageType.GET) {
            String filename = m.messageParams[0];
            if (!filemap.containsKey(filename)) {
                String replymsg = MessageBuilder.buildGetReplyMessage("NOT_OK", new String[0]).toString();
                sendMessage(clientSocket, replymsg);
            } else {
                int num = filemap.get(filename).getLeft().size();
                String servers[] = new String[num];
                int idx = 0;
                for (String server : filemap.get(filename).getLeft()) {
                    servers[idx] = server;
                    idx++;
                }
                String replymsg = MessageBuilder.buildGetReplyMessage("OK", servers).toString();
                sendMessage(clientSocket, replymsg);
            }
        } else {
            throw new IOException("ElectionMessage not recognized");
        }
    }

    /**
     * Communicate info to other process in group
     *
     * @param add
     * @param port
     * @param msg
     */
    void communicate(String add, int port, String msg) {
        try {
            Socket clientSocket = new Socket(add, port);
            String response = sendMessage(clientSocket, msg);
            if (response != null) {
                handleMessage(response, clientSocket);
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[ERROR][Election]: Unable to communicate with " + add + " on port " + port);
        }
    }

    /**
     * Inform SDFSProxy about current master
     */
    private void informIntro() {
        String msg = MessageBuilder.buildCoordMessage(FD.getSelfID().pidStr).toString();
        communicate(ServerMain.intro_address,
                ServerMain.intro_port + ServerMain.ESPortDelta,
                msg);
    }

    /**
     * Check if files need replication
     */
    private void checkReplication() {
        System.out.println("[DEBUG][MASTER]: Checking Replication");
        Random rn = new Random();
        for (Iterator<Map.Entry<String, MutablePair<Set<String>, Long>>> it = filemap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, MutablePair<Set<String>, Long>> entry = it.next();
            String filename = entry.getKey();
            System.out.println("[DEBUG][MASTER] Checking replication for " + filename);
            if ((System.currentTimeMillis() - filemap.get(filename).getRight().longValue()) > REPLICATION_TIMEOUT) {
                System.out.println("[DEBUG][MASTER] Replication timedout for " + filename);
                Set<String> replicaServers = filemap.get(filename).getLeft();
                for (String serverid : new HashSet<String>(replicaServers)) {
                    if (!FD.isAlive(serverid)) {
                        replicaServers.remove(serverid);
                    }
                }
                if (replicaServers.size() == 0) {
                    it.remove();
                } else {
                    List<String> memlist = FD.getMemlistSkipIntroducerWithSelf();
                    Collections.shuffle(memlist);
                    List<String> replicaServersList = new ArrayList<String>(replicaServers);
                    if (replicaServers.size() < REPLICATION_UNIT && (memlist.size() > replicaServers.size())) {
                        System.out.println("[DEBUG][MASTER] Replication True for file " + filename);
                        int count = replicaServers.size();
                        for (String serverid : memlist) {
                            if (count < REPLICATION_UNIT) {
                                if (!replicaServers.contains(serverid)) {
                                    try {
                                        Pid source = Pid.getPid(replicaServersList.get(rn.nextInt(replicaServers.size())));
                                        String msg = FSMessage.createReplicateMessage
                                                (filename, source.hostname, source.port + ServerMain.FSPortDelta).toString();
                                        Socket clientSocket = new Socket(Pid.getPid(serverid).hostname, Pid.getPid(serverid).port + ServerMain.FSPortDelta);
                                        sendMessage(clientSocket, msg);
                                        clientSocket.close();
                                        count++;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        System.out.println("[ERROR] [MASTER]: Error sending replicate message to " + serverid);
                                    }
                                }
                            }
                        }
                        filemap.get(filename).setRight(new Long(System.currentTimeMillis()));
                    }
                }
            }
        }
    }

    /**
     * Launch Master service
     */
    public void startMS() {
        boolean introducer_state = true;
        while (true) {
            if (!introducer_state && FD.isAlive(FD.getIntroID().toString())) {
                informIntro();
            }
            introducer_state = FD.isAlive(FD.getIntroID().toString());
            checkReplication();
            try {
                System.out.println("[DEBUG][MASTER]: Waiting to accept connection");
                Socket connectionSocket = welcomeSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                String msg = inFromClient.readLine();
                if (msg != null) {
                    handleMessage(msg, connectionSocket);
                }
                connectionSocket.close();
            } catch (SocketTimeoutException e) {
                System.out.println("[DEBUG][MASTER]: Socket Timeout");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[ERROR][MASTER]: Connection Error");
            }
        }
    }
}
