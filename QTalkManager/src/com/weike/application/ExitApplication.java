package com.weike.application;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;


public class ExitApplication extends Application {

	private List<Activity> mList = new LinkedList<Activity>();	//存放Activity对像
	private static ExitApplication instance = null;	//ExitApplication对象
	
	public ExitApplication() {
		
	}
	
	public synchronized static ExitApplication getInstance() {
		if(instance == null) {
			instance = new ExitApplication();
		}
		return instance;
	}
	
	//添加Activity对象
	public void addActivity(Activity activity) {
		mList.add(activity);
	}
	
	public void exit() {
		try{
			for(Activity activity : mList) {
				if(activity != null) {
					activity.finish();
				}
			}
			System.exit(0);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
