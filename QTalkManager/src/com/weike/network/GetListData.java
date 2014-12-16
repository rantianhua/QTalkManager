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

	private Handler han = null; // UI交互
	private String url = null; // 访问的链接
	private HttpClient client = null;
	private HttpGet get = null;
	private HttpResponse response = null;
	private Message message = null;
	private HttpEntity entity = null; // 响应实体
	private String content = null; // 得到的内容

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
		get = new HttpGet(url); // 以get的形式访问
		try {
			response = client.execute(get); // 响应请求
			entity = response.getEntity(); // 获取响应实体
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
			// 不再读取消息
			get.abort();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("GetListData", "GetListData访问网络出错", new Throwable(e));
		} finally {
			// 向主线程发送消息
			han.sendMessage(message);
			// 关闭HttpClient对像，释放资源
			client.getConnectionManager().shutdown();
		}
	}
}
