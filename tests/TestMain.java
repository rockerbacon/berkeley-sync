package tests;

import java.util.Scanner;

public class TestMain {

	public static void main (String[] args) {
		TestProcess process;
		Scanner scan = new Scanner(System.in);
		if (args.length > 0 && args[0].equals("leader")) {
			process = new TestProcess(true);
		} else {
			process = new TestProcess(false);
		}
		
		System.out.println("Process ready to be started, press enter to continue");
		scan.nextLine();
		process.start();
		
		System.out.println("Process started, press enter again to stop");
		scan.nextLine();
		process.stop();
	}

}
