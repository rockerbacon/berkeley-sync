package tests;

import java.util.Scanner;

public class TestMain {

	public static void main (String[] args) {
		TestProcess process;
		Scanner scan = new Scanner(System.in);
		if (args.length > 1 && args[1] == "leader") {
			process = new TestProcess(true);
		} else {
			process = new TestProcess(false);
		}
		
		System.out.println("Process ready to be started, press enter to continue");
		scan.nextLine();
		process.start();
		
		class ThreadedSection implements Runnable {
			public boolean running;
			
			@Override public void run () {
				running = true;
				try {
					while (running) {
						System.out.println(process.getClock().getTimeMillis());
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					System.err.println("Thread sleep interrupted");
				}
			}
			
		};
		ThreadedSection r = new ThreadedSection();
		new Thread(r).start();
		
		System.out.println("Process started, press enter again to stop");
		scan.nextLine();
		r.running = false;
		process.stop();
	}

}
