package com.mehtank.androminion.comms;


public interface EventHandler {
	public boolean handle(Event e);
	public void debug(String s);
}
