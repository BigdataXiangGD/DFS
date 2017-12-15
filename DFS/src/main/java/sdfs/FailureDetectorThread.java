package sdfs;
import sdfs.FailureDetector.FailureDetector;

/**
 * Thread Class for launching Failure Detector service
 *
 */
public class FailureDetectorThread extends Thread{
	private FailureDetector FD;
	public FailureDetectorThread(FailureDetector FD){
		this.FD=FD;
	}
	@Override
	public void run(){
		FD.startFD();
	}
}
