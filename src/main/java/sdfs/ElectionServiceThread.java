package sdfs;
import java.io.IOException;
import sdfs.ElectionService.ElectionService;

/**
 * Thread Class for launching Election Service
 *
 */
public class ElectionServiceThread extends Thread{
	private ElectionService ES;
	public ElectionServiceThread(ElectionService ES){
		this.ES=ES;
	}
	@Override
	public void run(){
		try {
			ES.startES();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
