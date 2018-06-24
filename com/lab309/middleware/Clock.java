package com.lab309.middleware;

public interface Clock {
	public long getTimeMillis ();
	
	public void adjustTime (long timeOffset);
}
