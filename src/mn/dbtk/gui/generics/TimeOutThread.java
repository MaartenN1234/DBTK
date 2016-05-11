package mn.dbtk.gui.generics;

public abstract class TimeOutThread extends Thread {
	long timeout;
	
	public TimeOutThread(long i) {
		super();
		setDelay(i);
		start();
	}

	public void setDelay(long i) {
		timeout = System.currentTimeMillis() + i;
	}
	public void run(){
		while(timeout > System.currentTimeMillis()){
			try {
				sleep(timeout - System.currentTimeMillis());
			} catch (InterruptedException e) {
				interrupt();
				return;
			}
		}
		runSub();
	}
	
	abstract protected void runSub();

}
