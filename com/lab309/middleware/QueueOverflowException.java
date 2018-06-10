package com.lab309.middleware;

public class QueueOverflowException extends Exception {
	public QueueOverflowException () {
		super("Queue for RPC is full and cannot answer another command. Increase queue size or check that all calls are returning properly");
	}
}
