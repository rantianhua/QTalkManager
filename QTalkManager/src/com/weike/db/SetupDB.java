package com.weike.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SetupDB extends SQLiteOpenHelper {
	private static String NAME = "qtalk_db";
	private static int VERSION = 1;
	public static SetupDB mInstance;

	public SetupDB(Context context) {
		super(context, NAME, null, VERSION);
	}

	public synchronized static SetupDB getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SetupDB(context);
		}
		return mInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 第一次实例化时调用,新建各类表
		String wait_tb = "CREATE TABLE wait_tb (pin int PRIMARY KEY,money txt)"; // 待售的表
		db.execSQL(wait_tb);
		String sell_tb = "CREATE TABLE sell_tb (pin int PRIMARY KEY,money txt,usedaccount txt)"; // 已售的表
		db.execSQL(sell_tb);
		String customer_tb = "CREATE TABLE customer_tb (customeraccount txt PRIMARY KEY,money float)"; // 客户的表
		db.execSQL(customer_tb);
		String record_tb = "CREATE TABLE record_tb (time txt not null,type txt not null,thirty_sum int,hundred_sum int,year_sum int,real_money float,commi_money float, PRIMARY KEY(time,type)) "; // 记录的表
		db.execSQL(record_tb);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

	}
	
	
	public void closeDB() {
	    if (mInstance != null) {
	        try {
	            SQLiteDatabase db = mInstance.getWritableDatabase();
	            db.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        mInstance = null;
	    }
	}
	

}
