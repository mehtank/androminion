package com.vdom.comms;

import java.io.Serializable;

public class Event implements Serializable{
	private static final long serialVersionUID = -590316466543954582L;

	public static class EventObject implements Serializable {
		private static final long serialVersionUID = 6832528588406201248L;
	
		public GameStatus gs;
		public String[] ss;
		public int[] is;
		public NewGame ng;
		public SelectCardOptions sco;
		
		public EventObject(GameStatus o) {
			this.gs = o;
		}
		public EventObject(String[] o) {
			this.ss = o;
		}
		public EventObject(int[] o) {
			this.is = o;
		}
		public EventObject(NewGame o) {
			this.ng = o;
		}
		public EventObject(SelectCardOptions o) {
			this.sco = o;
		}
	}
	
	public enum EType {
		GETSERVER, SERVER, 
		
		HELLO, GAMESTATS, NEWGAME,
		
		STARTGAME, JOINGAME,
		
		STATUS,
		
		GETNAME, SETNAME, SETHOST,

		GETCARD, CARD,
		GETSTRING, STRING, 
		ORDERCARDS, CARDORDER,
		
		CARDOBTAINED, CARDTRASHED, CARDREVEALED,
		SAY, CHAT, 
		
		Success,
		QUIT,
		
		ACHIEVEMENT,
		
		DEBUG, 
		PING, PONG, DISCONNECT, 
	}
	public EType t;
	public String s;
	public boolean b;
	public int i;
	public EventObject o;
	
	public Event(EType t) {
		this.t = t;
	}
	
	public Event setType(EType r) {
		this.t = r;
		return this;
	}
	public Event setString(String s) {
		this.s = s;
		return this;
	}
	public Event setBoolean(boolean b) {
		this.b = b;
		return this;
	}
	public Event setInteger(int i) {
		this.i = i;
		return this;
	}
	public Event setObject(EventObject o) {
		this.o = o;
		return this;
	}
	
	public String toString() {
		String str = t.toString();
		
		str += ", int = " + i;
		str += ", str =  " + s;
		str += ", bool = " + b;
		if (o != null) 
			str += ", obj = " + o.toString();
		
		return str;
	}
}