package tests;

import com.lab309.middleware.BerkeleyLeader;
import com.lab309.middleware.BerkeleySlave;
import com.lab309.middleware.Clock;

import java.util.Random;

import java.io.IOException;

public class TestProcess {

	private static int port = 50050;
	private static long answerLimit = 300;
	private static long inactivityLimit = 700;
	private static long minUpdateInterval = 350, maxUpdateInterval = 400;
	private static long minUpdateIncrement = 330, maxUpdateIncrement = 420;
	private static long syncInterval = 500;

	private TestClock clock;
	private BerkeleySlave synchronizer;
	private BerkeleyLeader syncLeader;
	
	private static long randomBetween (long min, long max) {
		Random rnd = new Random();
		return rnd.nextLong() % (max-min) + min;
	}

	public TestProcess (boolean isLeader) {
		try {
			this.clock = new TestClock(0, randomBetween(minUpdateInterval, maxUpdateInterval), randomBetween(minUpdateIncrement, maxUpdateIncrement));
			this.synchronizer = new BerkeleySlave(TestProcess.port, TestProcess.inactivityLimit, this.clock);
			if (isLeader) {
				this.syncLeader = new BerkeleyLeader(TestProcess.port, TestProcess.answerLimit, this.clock);
			} else {
				this.syncLeader = null;
			}
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
		this.syncLeader = null;
	}

}
