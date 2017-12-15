package sdfs.service;

import sdfs.message.ElectionMessage;
import sdfs.message.FSMessage;
import sdfs.message.FSMessageType;
import sdfs.Pid;
import sdfs.ServerMain;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Class for local File Server
 */
public class FileServer extends Thread {
    static final String baseDir = System.getProperty("user.home") + "/Desktop/CS425Project/"
            + ServerMain.FD.getSelfID().toString() + "/";
    private Set<String> sdfsfilenames;
    private final int port;

    public FileServer(int port) {
        this.port = port;
        sdfsfilenames = Collections.synchronizedSet(new HashSet<String>());
        try {
            FileUtils.deleteDirectory(new File(baseDir));
            FileUtils.forceMkdir(new File(baseDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter method to get meta data of local files
     *
     * @return
     */
    public ArrayList<String> getFilesInServer() {
        ArrayList<String> ret = new ArrayList<String>();
        for (String s : sdfsfilenames)
            ret.add(s);

        return ret;
    }

    @Override
    public void run() {
        ServerSocket listener;
        try {
            listener = new ServerSocket(this.port);
            System.out.println(listener.getLocalSocketAddress() + ":" + listener.getLocalPort());
            while (true) {
                Socket connection = listener.accept();
                connection.setSoTimeout(2000);
                new FileServerThread(connection, sdfsfilenames).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class FileServerThread extends Thread {
        private static final int MasterPortDelta = 3;
        Socket socket;
        Set<String> sdfsFiles;
        private final Map<FSMessageType, HandleRequestStrategy> handleRequestStrategyMap = new HashMap<FSMessageType, HandleRequestStrategy>();

        {
            handleRequestStrategyMap.put(FSMessageType.GET, new HandleRequestStrategy() {
                public void handle(PrintWriter out, String fileName, FSMessage messageRequest) {
                    if (sdfsFiles.contains(fileName)) {
                        out.println(FSMessage.createOkayMessage());
                        out.flush();
                        sendSDFSFile(fileName);
                    } else {
                        out.println(FSMessage.createNayMessage());
                        out.flush();
                    }
                    closeSocket();
                }
            });
            handleRequestStrategyMap.put(FSMessageType.REP, new HandleRequestStrategy() {
                public void handle(PrintWriter out, String fileName, FSMessage messageRequest) {
                    closeSocket();
                    replicateSDFSFile(fileName, messageRequest.ipAddress, messageRequest.port);
                }
            });

        }

        FileServerThread(Socket sock, Set<String> sdfsfiles) {
            socket = sock;
            sdfsFiles = sdfsfiles;
        }

        @Override
        public void run() {
            handleRequest();
        }

        private void closeSocket() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Replicate file from source
         *
         * @param fileName
         * @param ipAddress
         * @param port
         * @return
         */
        private boolean replicateSDFSFile(String fileName, String ipAddress, int port) {
            Socket socket = null;
            Scanner soIn = null;
            try {
                socket = new Socket(ipAddress, port);
                socket.setSoTimeout(2000);
                soIn = new Scanner(new InputStreamReader(socket.getInputStream()));
                soIn.useDelimiter("\n");
                PrintWriter soOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                soOut.println(FSMessage.createGetMessage(fileName));
                soOut.flush();
                if (FSMessage.retrieveMessage(soIn.next()).type.equals(FSMessageType.YES)) {
                    createSDFSFile(socket, fileName);
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    socket.close();
                    soIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        /**
         * Send file to other process
         *
         * @param filename
         */
        private void sendSDFSFile(String filename) {
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                byte[] buffer = new byte[1024];
                FileInputStream fileIn = new FileInputStream(FileServer.baseDir + filename);
                int readlen;
                while ((readlen = fileIn.read(buffer)) != -1) {
                    out.write(buffer, 0, readlen);
                }

                out.flush();
                fileIn.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Handle fileOp requests from other processes
         */
        private void handleRequest() {
            try {
                Scanner in = new Scanner(new InputStreamReader(socket.getInputStream()));
                in.useDelimiter("\n");
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                FSMessage messageRequest = FSMessage.retrieveMessage(in.next());
                String fname = messageRequest.fileName;
                handleRequestStrategyMap.get(messageRequest.type).handle(out, fname, messageRequest);
                in.close();
            } catch (IOException e) {
                closeSocket();
                e.printStackTrace();
            }
        }

        /**
         * Receive SDFS file
         *
         * @param socket
         * @param fileName
         * @throws InterruptedIOException
         */
        private void createSDFSFile(Socket socket, String fileName) throws InterruptedIOException {
            try {
                FileOutputStream fs = new FileOutputStream(FileServer.baseDir + fileName);
                byte[] buffer = new byte[1024];
                DataInputStream in = new DataInputStream(socket.getInputStream());
                int readlen;
                while ((readlen = in.read(buffer)) > 0) {
                    fs.write(buffer, 0, readlen);
                }

                fs.close();
                System.out.println("[DEBUG][FILE_SERVER] new file in server : " + fileName);
                sdfsFiles.add(fileName);
                notifyFileAdd(fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Notify new file to Master
         *
         * @param fileName
         */
        private void notifyFileAdd(String fileName) {
            Pid master = ServerMain.ES.getMasterPid();
            try {
                Socket sock = new Socket(master.hostname, master.port + MasterPortDelta);
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
                String[] filenames = {fileName};
                out.println(ElectionMessage
                        .MessageBuilder
                        .buildNewfilesMessage(ServerMain.FD.getSelfID().toString(), filenames)
                        .toString());
                out.flush();
                System.out.println("[DEBUG][FILE_SERVER]: notified master");
                in.readLine();
                System.out.println("[DEBUG][FILE_SERVER]: notify ack master");

                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}

interface HandleRequestStrategy {
    void handle(PrintWriter out, String fileName, FSMessage messageRequest);
}
