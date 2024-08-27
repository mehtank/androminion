package com.mehtank.androminion.util;

import android.app.Application;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ThisApplication extends Application
{
	private String errorPath;
	private final Thread.UncaughtExceptionHandler defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler();	
	public void onCreate() {
		super.onCreate();
		errorPath = getExternalCacheDir() + "/errors.txt";
		ThemeSetter.setTheme(this, true);
		ThemeSetter.setLanguage(this);
		Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
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
		});
	}
}
