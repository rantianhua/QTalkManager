package com.weike.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class OperationDB {

	private SetupDB dbHelper = null;

	public OperationDB(Context context) {
		dbHelper = SetupDB.getInstance(context);
	}

	// ��������
	public synchronized void saveData(String table,
			ArrayList<ContentValues> values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			// ����
			db.beginTransaction();
			for (int i = 0; i < values.size(); i++) {
				db.insert(table, null, values.get(i));
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e("saveData", "�������ݴ���", new Throwable(e));
		} finally {
			db.endTransaction();
			values = null;
		}
	}

	// //�޸�����
	// public void modifyData(String pin,ContentValues values) {
	// if(dbGet.isOpen()) {
	// try {
	// dbGet.update("sell_tb", values, pin+"=?", new String[]{pin});
	// } catch (Exception e) {
	// Log.e("modifyData", "�޸����ݴ���",new Throwable(e));
	// }
	// }
	// }

	public void insertOne(String sql) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ��ѯ����
	public synchronized Cursor getMessage(String sql) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			try {
				Cursor cursor = db.rawQuery(sql, null);
				return cursor;
			} catch (Exception e) {
				Log.e("modifyData", "��ѯʧ��", new Throwable(e));
			}
		}
		return null;
	}

	// ������ݿ�
	public void clearDB(String table) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			String sql = "delete from " + table;
			db.execSQL(sql);
		} catch (Exception e) {
			Log.e("clearDB", "������ݿ�ʧ��", new Throwable(e));
		}
	}

	public void delectData(String table, String pin) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.delete(table, "pin=?", new String[] { pin });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeDB() {
		dbHelper.closeDB();
	}
}
