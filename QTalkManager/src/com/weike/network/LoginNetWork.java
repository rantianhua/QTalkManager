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
 * @author Rth ������ʺ�̨�ӿڣ���ȡJson����
 */

public class LoginNetWork extends Thread{
	
	private Handler han = null;	//UI����
	private String url = null; 	//���ʵ�����
	private HttpClient client = null;
	private HttpGet get = null;
	private HttpResponse response = null;
	private Message message = null;
	private HttpEntity entity = null;	//��Ӧʵ��
	private String content =null;	//�õ�������
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
		//����5�볬ʱ
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5*1000);
		message = han.obtainMessage();
		get = new HttpGet(url);	//��get����ʽ����
		try{
			response = client.execute(get);	//��Ӧ����
			entity = response.getEntity();	//��ȡ��Ӧʵ��
			if(entity != null) {
				content = EntityUtils.toString(entity);
			}
			if(content != null) {
				dataMap = new AnalyzeData().getLoginData(content);		//����JSon����
				if(dataMap != null) {
					if(dataMap.get("status").equals("success")) {
						//��¼�ɹ�
						message.what = 0;
						message.obj = dataMap;
					}else {
						if(dataMap.get("message").equals("account")) {
							//�˺Ŵ���
							message.what = 1;
						}else {
							//�������
							message.what = 2;
						}
					}
				}
			}
			//���ٶ�ȡ��Ϣ
		    get.abort();
		}catch(Exception e) {
			e.printStackTrace();
			message.what = 3;
			content = null;
		}finally {
			//�����̷߳�����Ϣ
			han.sendMessage(message);
			//�ر�HttpClient�����ͷ���Դ
			client.getConnectionManager().shutdown();
		}	
	}
	
}
