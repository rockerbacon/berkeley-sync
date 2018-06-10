package tests;

public class TestClock {

	private long timestamp;
	private long updateInterval;
	private long updateIncrement;
	private boolean isRunning;

	public TestClock (long beginningTime, long updateInterval, long updateIncrement) {
		this.timestamp = beginningTime;
		this.updateInterval = updateInterval;
		this.updateIncrement = updateIncrement;
		this.isRunning = false;
	}
	
	public long getTime () {
		return this.timestamp;
	}
	
	public void adjustTime (long newTime) {
		this.timestamp = newTime;
	}
	
	public void start () {
		this.isRunning = true;
		new Thread ( new Runnable () { @Override public void run () {
			while (TestClock.this.isRunning) {
				try {
					Thread.sleep(TestClock.this.updateInterval);
					TestClock.this.timestamp += TestClock.this.updateIncrement;
				} catch (InterruptedException e) {
					System.err.println("Clock interrupted");
				}
			}
		}}).start();
	}
	
	public void stop () {
		this.isRunning = false;
	}

}
