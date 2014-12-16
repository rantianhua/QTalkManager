package com.weike.util;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import com.google.gson.stream.JsonReader;

public class AnalyzeData {

	private HashMap<String, String> dataMap = null; // ��½����װ��HashMap������
	private Map<String, String> map = null; // "����"���ݷ�װ��map��ArrayList������
	private JsonReader reader = null;

	public AnalyzeData() {

	}

	public HashMap<String, String> getLoginData(String data) {

		dataMap = new HashMap<String, String>();
		try {
			reader = new JsonReader(new StringReader(data)); // �õ�Jsonreader����
			reader.beginObject();
			while (reader.hasNext()) {
				String keyName = reader.nextName();
				if (keyName.equals("salesman")) {
					reader.beginObject();
					while (reader.hasNext()) {
						String key = reader.nextName();
						String values = reader.nextString();
						dataMap.put(key, values);
					}

					reader.endObject();
				} else {
					String value = reader.nextString();
					dataMap.put(keyName, value);
				}
			}
			reader.endObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dataMap;
	}

	// �������ݲ�����������ݿ�
	public ArrayList<Map<String,String>> getCutJsonData(String data) {
		System.gc();
		ArrayList<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
		// ������ݿ�
		try {
			data = data.replaceAll(":null", ":\"none\""); // �滻��ֵΪnull����
			reader = new JsonReader(new StringReader(data)); // �õ�Jsonreader����
			reader.beginObject();
			while (reader.hasNext()) {
				String keyName = reader.nextName();
				if (keyName.equals("phonecard") || keyName.equals("customer")
						|| keyName.equals("cash_log")) {
					reader.beginArray();
					while (reader.hasNext()) {
						map = new HashMap<String, String>();
						reader.beginObject();
						while (reader.hasNext()) {
							String key = reader.nextName();
							String values = reader.nextString();
							if(keyName.equals("customer") && key.equals("money")) {
								DecimalFormat format = new DecimalFormat("#0.00");
								float money  = Float.valueOf(values).floatValue();
								map.put(key, format.format(money));
							}else {
								map.put(key, values);
							}
							key = null;
							values = null;
						}
						reader.endObject();
						dataList.add(map);
						map = null;
					}
					reader.endArray();
				} else {
					reader.nextString();
				}
				keyName = null;
			}
			reader.endObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dataList;
	}

	
}
