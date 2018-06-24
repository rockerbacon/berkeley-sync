package tests;

import com.lab309.middleware.Clock;

public class TestClock implements Clock{

	private long timestamp;
	private long updateInterval;
	private long updateIncrement;
	private boolean isRunning;
	private Thread updateThread;

	public TestClock (long beginningTime, long updateInterval, long updateIncrement) {
		this.timestamp = beginningTime;
		this.updateInterval = updateInterval;
		this.updateIncrement = updateIncrement;
		this.isRunning = false;
	}
	
	@Override
	public long getTimeMillis () {
		return this.timestamp;
	}
	
	public void adjustTime (long timeOffset) {
		this.timestamp += timeOffset;
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
