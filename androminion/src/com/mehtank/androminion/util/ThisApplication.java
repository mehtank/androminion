package com.mehtank.androminion.util;

import android.app.Application;
import android.util.Log;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.mehtank.androminion.activities.GameActivity;

public class ThisApplication extends Application
{
	private final String errorPath = GameActivity.BASEDIR + "/errors.txt";
	private final Thread.UncaughtExceptionHandler defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler();	
	public void onCreate() {
		Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException (Thread thread, Throwable e)
			{
				try {
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date();
					FileWriter fw = new FileWriter(errorPath, true);
					fw.write("==========================================\r\n");
					fw.write(dateFormat.format(date) + "\r\n");
					fw.write("Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "\r\n");
					fw.write(e.getMessage() + "\r\n");
					for (StackTraceElement stack : e.getStackTrace()) {
						fw.write(stack.toString() + "\r\n");
					}
					fw.close();
				} catch (Exception exception) {
				}
				defaultUncaughtHandler.uncaughtException(thread, e);
			}
		});
	}
}
