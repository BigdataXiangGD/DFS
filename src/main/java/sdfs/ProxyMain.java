package sdfs;

import java.io.IOException;

import sdfs.FailureDetector.FDIntroducer;
import sdfs.service.MasterTracker;
import sdfs.FailureDetector.FailureDetector;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import sdfs.thread.FailureDetectorThread;
import sdfs.thread.InputProcessorIntroThread;
import sdfs.thread.MasterTrackerThread;

/**
 * Main class for SDFSProxy
 */
public class ProxyMain {
    private static int FDport = 0;
    public static FailureDetector FD;
    public static MasterTracker MT;

    /**
     * Formats commandLine inputs and flags
     */
    private static void FormatCommandLineInputs(String[] args) {
        Options op = createOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        try {
            line = parser.parse(op, args);
        } catch (ParseException e) {
            printHelp(op);
            e.printStackTrace();
        }
        ProxyMain.FDport = Integer.parseInt(line.getOptionValue("port"));
    }

    /**
     * Creates the required options to look for in command line arguments
     *
     * @return Options object
     */
    private static Options createOptions() {
        Option port = Option.builder("port").argName("serverPort").hasArg().desc("Port to run faliure detector server")
                .required().build();
        Options op = new Options();
        op.addOption(port);
        return op;
    }

    /**
     * print helper for usage
     *
     * @param op options
     */
    private static void printHelp(Options op) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("failureDetector", op);
    }

    /**
     * Setup Failure Detector and Master Tracker
     *
     * @return
     * @throws IOException
     */
    public static void setupServices() throws IOException {
        FD = new FDIntroducer(ProxyMain.FDport);
        MT = new MasterTracker(FDport + 1);
    }

    /**
     * Main function for launching SDFSProxy
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        FormatCommandLineInputs(args);
        setupServices();
        //Start Failure Detector
        FailureDetectorThread FDThread = new FailureDetectorThread(FD);
        FDThread.setDaemon(true);
        FDThread.start();
        //Start Failure Detector
        MasterTrackerThread MTThread = new MasterTrackerThread(MT);
        MTThread.setDaemon(true);
        MTThread.start();
        //Start User input Processor
        InputProcessorIntroThread InputThread = new InputProcessorIntroThread();
        InputThread.setDaemon(true);
        InputThread.start();
        FDThread.join();
    }
}
