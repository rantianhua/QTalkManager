package com.weike.network;

import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import com.weike.util.AnalyzeData;

import android.os.Handler;
import android.os.Message;

/**
 * 
 * @author Rth 该类访问后台接口，获取Json数据
 */

public class LoginNetWork extends Thread{
	
	private Handler han = null;	//UI交互
	private String url = null; 	//访问的链接
	private HttpClient client = null;
	private HttpGet get = null;
	private HttpResponse response = null;
	private Message message = null;
	private HttpEntity entity = null;	//响应实体
	private String content =null;	//得到的内容
	private HashMap<String,String> dataMap = null;
	
	public LoginNetWork() {

	}

	public LoginNetWork(Handler han,String url) {
		this.han = han;
		this.url = url;
	}
	
	@Override
	public void run() {
		super.run();
		client = new DefaultHttpClient();
		//设置5秒超时
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5*1000);
		message = han.obtainMessage();
		get = new HttpGet(url);	//以get的形式访问
		try{
			response = client.execute(get);	//响应请求
			entity = response.getEntity();	//获取响应实体
			if(entity != null) {
				content = EntityUtils.toString(entity);
			}
			if(content != null) {
				dataMap = new AnalyzeData().getLoginData(content);		//解析JSon数据
				if(dataMap != null) {
					if(dataMap.get("status").equals("success")) {
						//登录成功
						message.what = 0;
						message.obj = dataMap;
					}else {
						if(dataMap.get("message").equals("account")) {
							//账号错误
							message.what = 1;
						}else {
							//密码错误
							message.what = 2;
						}
					}
				}
			}
			//不再读取消息
		    get.abort();
		}catch(Exception e) {
			e.printStackTrace();
			message.what = 3;
			content = null;
		}finally {
			//向主线程发送消息
			han.sendMessage(message);
			//关闭HttpClient对像，释放资源
			client.getConnectionManager().shutdown();
		}	
	}
	
}
