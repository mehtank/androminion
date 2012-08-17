package com.mehtank.androminion.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DominionServer extends Service {
	VDomServer vds;
	private final static String stopped = "Server stopped";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		if (!test().equals(stopped))
			return;
		
        Strings.context = this;
		VDomServer.main(new String[] {
				"Drew (AI)", "com.vdom.players.VDomPlayerDrew",
				"Earl (AI)", "com.vdom.players.VDomPlayerEarl",
                "Mary (AI)", "com.vdom.players.VDomPlayerMary",
                "Chuck (AI)", "com.vdom.players.VDomPlayerChuck",
                "Sarah (AI)", "com.vdom.players.VDomPlayerSarah",
				"-debug"
		});
		vds = VDomServer.me;
	}

	@Override
	public void onDestroy() {
		if (vds != null)
			try {
				vds.quit();
			} catch (NullPointerException e) {
				// whatever.
			}
		vds = null;
		super.onDestroy();
	}
	
	String test() {
		if (vds == null)
			return stopped;
		
		int port = vds.getPort();
		if (port == 0)
			return stopped;
		
		String host = getLocalIpAddress();
		return "Server started on " + (host == null ? "localhost" : host) + ":" + port;
	}
	
	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
					en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
						enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("DroidServer", ex.toString());
		}
		return null;
	}
	
}
