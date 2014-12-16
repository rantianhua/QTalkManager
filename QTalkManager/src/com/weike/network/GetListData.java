package com.weike.network;

/**
 */

import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.weike.util.AnalyzeData;

public class GetListData extends Thread {

	private Handler han = null; // UI����
	private String url = null; // ���ʵ�����
	private HttpClient client = null;
	private HttpGet get = null;
	private HttpResponse response = null;
	private Message message = null;
	private HttpEntity entity = null; // ��Ӧʵ��
	private String content = null; // �õ�������

	public GetListData() {

	}

	public GetListData(Handler han, String url) {
		this.han = han;
		this.url = url;
	}

	@Override
	public void run() {
		super.run();
		client = new DefaultHttpClient();
		message = han.obtainMessage();
		get = new HttpGet(url); // ��get����ʽ����
		try {
			response = client.execute(get); // ��Ӧ����
			entity = response.getEntity(); // ��ȡ��Ӧʵ��
			if (entity != null) {
				content = EntityUtils.toString(entity);
				if (content != null) {
					ArrayList<Map<String, String>> dataSet = new AnalyzeData()
							.getCutJsonData(content);
					message.what = 0;
					message.obj = dataSet;
				} else {
					message.what = 1;
				}
			} else {
				message.what = 1;
			}
			// ���ٶ�ȡ��Ϣ
			get.abort();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("GetListData", "GetListData�����������", new Throwable(e));
		} finally {
			// �����̷߳�����Ϣ
			han.sendMessage(message);
			// �ر�HttpClient�����ͷ���Դ
			client.getConnectionManager().shutdown();
		}
	}
}
