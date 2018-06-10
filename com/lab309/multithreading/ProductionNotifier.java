package com.lab309.multithreading;

public class ProductionNotifier {

	private Integer i_count;
	private int size_count;
	private Object lk_up;
	private Object lk_down;
	private boolean b_interrupted;
	
	public ProductionNotifier (int size_count) {
		this.i_count = 0;
		this.size_count = size_count;
		this.lk_up = new Object();
		this.lk_down = new Object();
		this.b_interrupted = false;
	}
	
	public void requestSpace () throws InterruptedException {
		boolean b_wait;
		
		//checks for access to critical area
		synchronized(this.i_count) {
			b_wait = this.i_count.intValue() == this.size_count;
		}
		//wait for access to critical area
		if (b_wait) {
			synchronized(this.lk_up) {
				this.lk_up.wait();
			}
			if (this.b_interrupted) {
				throw new InterruptedException();
			}
		}
		
		synchronized(this.i_count) {
			//accesses critical area
			this.i_count = this.i_count.intValue()+1;
		}
		synchronized (this.lk_down) {
			//wake a waiting thread
			this.lk_down.notify();
		}
	}
	
	public void requestProduct () throws InterruptedException {
		boolean b_wait;
		
		//checks for access to critical area
		synchronized(this.i_count) {
			b_wait = this.i_count.intValue() == 0;
		}
		//wait for access to critical area
		if (b_wait) {
			synchronized(this.lk_down) {
				this.lk_down.wait();
			}
			if (this.b_interrupted) {
				throw new InterruptedException();
			}
		}
		
		synchronized(this.i_count) {
			//accesses critical area
			this.i_count = this.i_count.intValue()-1;
		}
		synchronized (this.lk_up) {
			//wake a waiting thread
			this.lk_up.notify();
		}
	}
	
	public void interrupt () {
		this.b_interrupted = true;
		synchronized(this.lk_up) {
			this.lk_up.notifyAll();
		}
		synchronized(this.lk_down) {
			this.lk_down.notifyAll();
		}
	}
	
	public void reset () {
		this.b_interrupted = false;
	}
	
}
