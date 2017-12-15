package sdfs;

import sdfs.ElectionService.ElectionMessage;
import sdfs.FileServer.FSMessage;
import sdfs.FileServer.FSMessageType;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;


/**
 * Class for SDFSClient
 */
public class SDFSClient {
    private static String introIP;
    private static int introPort;
    private static final int ElectionPortDelta = 1;
    private static final int FSPortDelta = 2;
    private static final int MasterPortDelta = 3;

    /**
     * Main function for SDFSClient
     *
     * @param args
     */
    public static void main(String[] args) {
        assert args.length == 2 : "usage : String argument SDFSProxy hostname and int argument SDFSProxy port reqd!";
        introIP = args[0];
        introPort = Integer.parseInt(args[1]);
        System.out.println("SDFSProxy IP : " + introIP + ", SDFSProxy port : " + introPort);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean exit = false;
        while (!exit) {
            System.out.println("press exit to end, get sdfsfilename destfilename");
            try {
                String[] input = br.readLine().split(" ");
                if (input[0].equals("exit")) {
                    exit = true;
                } else if (input[0].equals("get")) {
                    fileOps(flattenFilename(input[1]), input[2], 'g');
                }

                System.out.println("request completed");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    /**
     * Flatten the file to avoid slashes in name
     *
     * @param fname
     * @return flattenedFilename
     */
    private static String flattenFilename(String fname) {
        String ret = fname.replace("/", "$");
        return ret;
    }

    /**
     * Get Master from SDFSProxy
     *
     * @return masterId
     */
    private static Pid getMaster() {
        while (true) {
            Socket sock;
            Scanner in;
            try {
                sock = new Socket(introIP, introPort + ElectionPortDelta);
                in = new Scanner(new InputStreamReader(sock.getInputStream()));
                in.useDelimiter("\n");
                PrintWriter out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
                out.println(ElectionMessage
                        .MessageBuilder
                        .buildMasterMessage(InetAddress.getLocalHost().getHostName())
                        .toString());
                out.flush();
                ElectionMessage reply = ElectionMessage
                        .extractMessage(in.next());
                if (!reply.messageParams[0].equals("NOT_SET")) {
                    return Pid.getPid(reply.messageParams[0]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Master not set! I will try again.");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles fileOperations in SDFS
     *
     * @param srcfname
     * @param destfname
     * @param op
     */
    private static void fileOps(String srcfname, String destfname, char op) {
        Pid master = getMaster();
        try {
            Socket sock = new Socket(master.hostname, master.port + MasterPortDelta);
            sock.setSoTimeout(2000);
            Scanner soIn = new Scanner(new InputStreamReader(sock.getInputStream()));
            soIn.useDelimiter("\n");
            PrintWriter soOut = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()), true);

            if (op == 'g') {
                getOperation(soIn, soOut, srcfname, destfname);
            } else {
                System.out.println("op not recognized!");
            }
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * get VM's having file from SDFS
     *
     * @param soIn
     * @param soOut
     * @param sdfsfname
     * @param destfname
     */
    private static void getOperation(Scanner soIn, PrintWriter soOut, String sdfsfname, String destfname) {
        soOut.println(ElectionMessage
                .MessageBuilder
                .buildGetMessage(sdfsfname)
                .toString());
        soOut.flush();
        ElectionMessage reply = ElectionMessage
                .extractMessage(soIn.next());
        if (reply.messageParams[0].equals("NOT_OK")) {
            System.out.println("Get operation cannot be completed. File does not exist in sdfs");
        } else {
            boolean got = false;
            for (int i = 1; i < reply.messageParams.length && !got; i++) {
                got = receiveFile(Pid.getPid(reply.messageParams[i]), sdfsfname, destfname);
            }

            if (!got)
                System.out.println("Get operation cannot be completed. " +
                        "All file servers rejected replying the file");
        }
    }

    /**
     * Receive File from SDFS to local
     *
     * @param pid
     * @param sdfsfname
     * @param destfname
     * @return
     */
    private static boolean receiveFile(Pid pid, String sdfsfname, String destfname) {
        try {
            Socket sock = new Socket(pid.hostname, pid.port + FSPortDelta);
            Scanner in = new Scanner(new InputStreamReader(sock.getInputStream()));
            in.useDelimiter("\n");
            PrintWriter out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            out.println(FSMessage.createGetMessage(sdfsfname));
            out.flush();
            FSMessage reply = FSMessage.retrieveMessage(in.next());
            if (reply.type.equals(FSMessageType.NO)) {
                sock.close();
                in.close();
                return false;
            }
            FileOutputStream fs = new FileOutputStream(destfname);
            byte[] buffer = new byte[1024];
            DataInputStream din = new DataInputStream(sock.getInputStream());
            int readlen;
            while ((readlen = din.read(buffer)) != -1) {
                fs.write(buffer, 0, readlen);
            }
            fs.close();
            sock.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
