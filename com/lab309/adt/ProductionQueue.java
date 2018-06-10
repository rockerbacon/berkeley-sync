package com.lab309.adt;

import com.lab309.multithreading.ProductionNotifier;

public class ProductionQueue<T> {

	private ConcurrentStaticQueue<T> sque;
	private ProductionNotifier pn;
	private Object lk_pop;
	private Object lk_push;
	
	public ProductionQueue (int size_queue) {
		this.sque = new ConcurrentStaticQueue<T>(size_queue);
		this.pn = new ProductionNotifier(size_queue);
	}
	
	public T pop () throws IndexOutOfBoundsException, InterruptedException {
		T obj;
		this.pn.requestProduct();
		obj = this.sque.pop();
		return obj;
	}
	
	public void push (T obj) throws IndexOutOfBoundsException, InterruptedException {
		this.pn.requestSpace();
		this.sque.push(obj);
	}
	
	public void interrupt () {
		this.pn.interrupt();
	}

}
