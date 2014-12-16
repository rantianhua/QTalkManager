package com.weike.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.weike.application.Interfaces;
import com.weike.network.LoginNetWork;
import com.weike.qtalkmanager.MyDialog;
import com.weike.qtalkmanager.R;
import com.weike.util.ConnectionDetector;

public class WaitListViewAdapter extends BaseAdapter {

	private static ArrayList<Map<String, String>> mData; // ����Դ��ÿ��item�е�����
	private Context context;

	// ����
	private AnimationSet animationSet;
	private AlphaAnimation alpha; // ���뵭������
	private TranslateAnimation translate; // ƽ�ƶ���
	private static WaitListViewAdapter waitAdapter = null;
	private static Handler han = null;

	// TabSelled�Ļص��ӿ�
	private AddSelledItemLisenter addItem;
	private MyDialog md = null;

	public static WaitListViewAdapter getInstances() {
		return waitAdapter;
	}

	public WaitListViewAdapter(ArrayList<Map<String, String>> mData2,
			Context context, final AddSelledItemLisenter addItem) {
		WaitListViewAdapter.mData = mData2;
		this.context = context;
		this.addItem = addItem;
		waitAdapter = this;
	}

	@SuppressLint("HandlerLeak")
	private void initHandler(final int item, final View v) {
		han = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 0) {
					Map<String, Object> map = new HashMap<String, Object>();
					map = (Map<String, Object>) msg.obj;
					if (map.get("status").equals("success")) {

						md.dismiss();
						Map<String, String> mapData = new HashMap<String, String>();
						mapData = mData.get(item);
						deleteItem(v, item);
						addItem.setBadgeView(mapData, 1);

					} else {
						md.dismiss();
						Toast.makeText(context, "����������Ժ����ԣ�",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					md.dismiss();
					Toast.makeText(context, "�޸�ʧ�ܣ���ˢ�º����ԣ�",
							Toast.LENGTH_SHORT).show();
				}
			}
		};
	}

	// ��ʼ�������ķ���
	private void initAnimation() {
		animationSet = new AnimationSet(true);
		alpha = new AlphaAnimation(1, 0); // ��������
		animationSet.addAnimation(alpha);
		translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF,
				0f, Animation.RELATIVE_TO_SELF, 0f); // ����ƽ�ƶ���
		animationSet.addAnimation(translate);
		animationSet.setDuration(500);
	}

	public synchronized ArrayList<Map<String, String>> getData() {
		return mData;
	}

	public void refreshData(ArrayList<Map<String, String>> mData) {
		WaitListViewAdapter.mData = mData;
	}

	@Override
	public int getCount() {

		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {

		return null;
	}

	@Override
	public long getItemId(int arg0) {

		return 0;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		ViewHolder hodler = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_4,
					null);
			hodler = new ViewHolder();
			hodler.cardNumber = (TextView) convertView
					.findViewById(R.id.item4_firstContent);
			hodler.status = (TextView) convertView
					.findViewById(R.id.item4_thirdContent);
			hodler.operation = (TextView) convertView
					.findViewById(R.id.item4_forthContent);
			hodler.pagevalue = (TextView) convertView
					.findViewById(R.id.item4_secondContent);
			convertView.setTag(hodler);
		} else {
			hodler = (ViewHolder) convertView.getTag();
		}
		hodler.cardNumber.setText((CharSequence) mData.get(arg0).get("pin"));
		hodler.pagevalue.setText((CharSequence) mData.get(arg0).get("money"));
		hodler.status.setText("δʹ��");
		hodler.operation.setText("�۳�");
		hodler.operation.setBackgroundColor(Color.GREEN);
		final int item = arg0;
		final View v = convertView;
		hodler.operation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				md = new MyDialog(context, R.layout.dialog_layout,
						R.style.Theme_myDialog);
				TextView title = (TextView) md.findViewById(R.id.tv_title_dialog);
				TextView info = (TextView) md.findViewById(R.id.tv_info_dialog);
				Button sure = (Button) md.findViewById(R.id.btn_sure_dialog);
				Button cancel = (Button) md
						.findViewById(R.id.btn_cancel_dialog);
				sure.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						initHandler(item, v);
						if (new ConnectionDetector(context)
								.isConnectingToInternet()) {
							SharedPreferences sp = context
									.getSharedPreferences("userInfo",
											Context.MODE_PRIVATE);
							String account = sp.getString("account", "");							
							try {
								new LoginNetWork(han, Interfaces.getModifyUrl(
										account, (String) mData.get(item)
												.get("pin"), true)).start();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							md.dismiss();
						}
					}
				});
				cancel.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						md.dismiss();
					}
				});
				title.setText(mData.get(item).get("pin"));
				info.setText("ȷ���۳��ÿ���");
				md.show();
			}
		});
		// Ϊÿ��listView���ø߶�
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 70);
		convertView.setLayoutParams(lp);

		return convertView;
	}

	static class ViewHolder {
		TextView cardNumber, pagevalue, status, operation;
	}

	// ɾ����view�ķ���
	private void deleteItem(final View v, final int item) {
		if (animationSet == null) {
			initAnimation();
		}
		animationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				refreshData(mData);
				getInstances().notifyDataSetChanged();
			}
		});
		v.startAnimation(animationSet);

	}

	// ����ӿڣ�������ҳ�����BadgeView
	public interface AddSelledItemLisenter {
		public void setBadgeView(Map<String, String> mapData, int num);
	}
}
