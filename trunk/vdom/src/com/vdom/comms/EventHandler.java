package com.vdom.comms;

/**
 * These functions are called <b>FROM WITHIN NETWORK THREADS</b>
 * <ul>
 * <li> handle is called from the receiving thread </li>
 * <li> sendErrorHandler is called from the sending thread </li>
 * <li> debug is called from wherever. </li>
 * </ul>
 * ==> handle() and sendErrorHandler() are called from <b>DIFFERENT THREADS</b>
 */

public interface EventHandler {
	public boolean handle(Event e);
	public void debug(String s);
	public void sendErrorHandler(Exception e); // is called upon an IOException from the socket when sending.
}
