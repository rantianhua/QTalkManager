package com.weike.qtalkmanager;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.weike.application.ExitApplication;
import com.weike.application.Interfaces;
import com.weike.network.LoginNetWork;
import com.weike.util.ConnectionDetector;

public class Login extends Activity implements OnClickListener {

	private EditText user = null;
	private EditText pass = null;
	private Button btn_login = null;
	private static Handler han = null;
	private SharedPreferences sp = null;
	private HashMap<String, String> dataMap = null;
	private Editor editor = null;
	private String username = null, password = null;
	private ProgressDialog mDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		ExitApplication.getInstance().addActivity(this);
		init();
	}

	@SuppressLint("HandlerLeak")
	private void init() {
		user = (EditText) findViewById(R.id.edit_login_user);
		pass = (EditText) findViewById(R.id.edit_login_pass);
		btn_login = (Button) findViewById(R.id.btn_login_login);
		btn_login.setOnClickListener(this);
		// �õ�SharedPrefrences����
		sp = this.getSharedPreferences("userInfo", MODE_PRIVATE);
		username = sp.getString("account", "");
		password = sp.getString("password", "");
		if (username != null && password != null) {
			user.setText(username);
			pass.setText(password);
		}
		editor = sp.edit();
		han = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 0:
					// ��¼�ɹ�
					dataMap = (HashMap<String, String>) msg.obj;
					// ����SheredPreaferences����Ϣ
					editor.clear();
					editor.putString("id",dataMap.get("id"));
					editor.putString("account", dataMap.get("account"));
					editor.putString("name", dataMap.get("name"));
					editor.putString("password", dataMap.get("password"));
					editor.putString("type", dataMap.get("type"));
					editor.putString("agent_account",
							dataMap.get("agent_account"));
					editor.putString("memo",dataMap.get("memo"));
					editor.putString("last_phonecard_time",
							dataMap.get("last_phonecard_time"));
					editor.putString("last_phonecard_money",
							dataMap.get("last_phonecard_money"));
					editor.putString("last_alipay_time",
							dataMap.get("last_alipay_time"));
					editor.putString("last_alipay_money",
							dataMap.get("last_alipay_money"));
					editor.commit();
					Intent intent = new Intent();
					intent.setClass(Login.this, MainActivity.class);
					mDialog.cancel();
					Login.this.startActivity(intent);
					break;
				case 1:
					mDialog.cancel();
					// ��¼ʧ�ܣ��˺Ŵ���
					Toast.makeText(Login.this, "�˺Ŵ���!", Toast.LENGTH_SHORT)
							.show();
					break;
				case 2:
					mDialog.cancel();
					// ��¼ʧ�ܣ��������
					Toast.makeText(Login.this, "�������!", Toast.LENGTH_SHORT)
							.show();
					break;
				case 3:
					mDialog.cancel();
					// ��¼ʧ�ܣ�δ֪����
					Toast.makeText(Login.this, "���粻���������Ժ�����!",
							Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}
		};
		mDialog = new ProgressDialog(Login.this);
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.setMessage("���ڵ�½...");
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btn_login_login:
			// ��������Ƿ�������
			if (new ConnectionDetector(Login.this).isConnectingToInternet()) {
				String username = user.getText().toString();
				String password = pass.getText().toString();
				System.out.println(username.equals(""));
				// �жϲ���������
				if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
					Toast.makeText(Login.this, "�˺Ż����벻��Ϊ��", Toast.LENGTH_SHORT)
							.show();
				} else {
					String url = Interfaces.getLoginUrl(username, password);
					new LoginNetWork(han, url).start();

					mDialog.show();
				}
			} else {
				Toast.makeText(Login.this, "����δ���ӣ��޷���¼��", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		default:
			break;
		}

	}

}
