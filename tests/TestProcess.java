package tests;

import com.lab309.middleware.BerkeleyLeader;
import com.lab309.middleware.BerkeleySlave;
import com.lab309.middleware.Clock;
import com.lab309.middleware.BullyElector;

import java.util.Random;

import java.io.IOException;

public class TestProcess {

	private static int port = 50050;
	private static int electionPort = 51050;
	private static long answerLimit = 300;
	private static long inactivityLimit = 2300;
	private static long minUpdateInterval = 1900, maxUpdateInterval = 2100;
	private static float minUpdateIncrement = 0.9f, maxUpdateIncrement = 1.1f;
	private static long syncInterval = 2000;

	private TestClock clock;
	private BerkeleySlave synchronizer;
	private BerkeleyLeader syncLeader;
	private BullyElector elector;
	private boolean monitoring;
	
	private static long randomBetween (long min, long max) {
		Random rnd = new Random();
		return rnd.nextLong() % (max-min) + min;
	}
	private static float randomBetween (float min, float max) {
		Random rnd = new Random();
		return rnd.nextFloat()*(max-min)+min;
	}

	public TestProcess (boolean isLeader) {
		try {
			long updateInterval = randomBetween(minUpdateInterval, maxUpdateInterval);
			this.clock = new TestClock(0, updateInterval, (long)(updateInterval*randomBetween(minUpdateIncrement, maxUpdateIncrement)));
			this.synchronizer = new BerkeleySlave(TestProcess.port, this.clock);
			if (isLeader) {
				this.syncLeader = new BerkeleyLeader(TestProcess.port, TestProcess.answerLimit, this.clock);
			} else {
				this.syncLeader = null;
			}
			this.elector = new BullyElector(TestProcess.electionPort, inactivityLimit, answerLimit, this.clock);
			
			this.monitoring = true;
			new Thread(new Runnable() { @Override public void run () {
				try {
					while (TestProcess.this.monitoring) {
						if (TestProcess.this.elector.becomesLeaderUponElection()) {
							TestProcess.this.syncLeader = new BerkeleyLeader(TestProcess.port, TestProcess.answerLimit, TestProcess.this.clock);
						} else {
							TestProcess.this.syncLeader = null;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}}).start();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Clock getClock () {
		return this.clock;
	}
	
	public void start () {
		this.clock.start();
		this.synchronizer.startSyncing();
		new Thread( new Runnable () { @Override public void run () {
			try {
				while (TestProcess.this.syncLeader != null) {
					TestProcess.this.syncLeader.sync();
					Thread.sleep(TestProcess.syncInterval);
				}
			} catch (InterruptedException e) {
				System.err.println("Thread sleep interrupted");
			}
		}}).start();
	}
	
	public void stop () {
		this.clock.stop();
		this.synchronizer.stopSyncing();
		this.monitoring = false;
		this.syncLeader = null;
		this.elector.stopAllActivity();
	}

}
