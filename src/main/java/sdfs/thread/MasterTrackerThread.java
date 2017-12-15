package sdfs.thread;

import sdfs.service.MasterTracker;

/** Thread Class for Tracking Master as SDFSProxy
 *
 */
public class MasterTrackerThread extends Thread{
	private MasterTracker MT;
	public MasterTrackerThread(MasterTracker MT){
		this.MT=MT;
	}
	@Override
	public void run(){
		MT.startMT();
	}
}
