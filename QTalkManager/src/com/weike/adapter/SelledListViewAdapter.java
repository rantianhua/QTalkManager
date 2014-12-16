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

public class SelledListViewAdapter extends BaseAdapter {

	private static ArrayList<Map<String, String>> mData; // 数据源，每个item中的数据
	private Context context;
	private static SelledListViewAdapter selledAdapter = null;

	// 动画
	private AnimationSet animationSet;
	private AlphaAnimation alpha; // 淡入淡出动画
	private TranslateAnimation translate; // 平移动画
	private AddItemWaitListener addItem;
	private MyDialog md = null;
	private static Handler han = null;

	public static SelledListViewAdapter getInstance() {
		return selledAdapter;
	}

	public SelledListViewAdapter(ArrayList<Map<String, String>> mData,
			Context context, final AddItemWaitListener addItem) {
		SelledListViewAdapter.mData = mData;
		this.context = context;
		this.addItem = addItem;
		selledAdapter = this;
	}

	@SuppressLint("HandlerLeak")
	private void initHandler(final int item, final View v) {
		han = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 0) {
					Map<String, String> map = new HashMap<String, String>();
					map = (Map<String, String>) msg.obj;
					if (map.get("status").equals("success")) {

						md.dismiss();
						Map<String, String> mapData = new HashMap<String, String>();
						mapData = mData.get(item);
						deleteItem(v, item);
						addItem.setBageView(mapData, 1);

					} else {
						md.dismiss();
						Toast.makeText(context, "网络出错，请稍后再试！",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					md.dismiss();
					Toast.makeText(context, "修改失败，请刷新后再试！", Toast.LENGTH_SHORT)
							.show();
				}
			}
		};
	}

	public void refreshData(ArrayList<Map<String, String>> mData) {
		SelledListViewAdapter.mData = mData;
	}

	public ArrayList<Map<String, String>> getData() {
		return mData;
	}

	// 初始化动画的方法
	private void initAnimation() {
		animationSet = new AnimationSet(true);
		alpha = new AlphaAnimation(1, 0); // 淡出动画
		animationSet.addAnimation(alpha);
		translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF,
				0f, Animation.RELATIVE_TO_SELF, 0f); // 向左平移动画
		animationSet.addAnimation(translate);
		animationSet.setDuration(500);
	}

	@Override
	public int getCount() {

		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {

		return arg0;
	}

	@Override
	public long getItemId(int arg0) {

		return arg0;
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
		if (!mData.get(arg0).get("usedaccount").equals("none")) {
			hodler.status.setText("已使用");
		} else {
			hodler.status.setText("未使用");
			hodler.status.setTextColor(Color.RED);
		}
		hodler.operation.setText("退回");
		hodler.operation.setBackgroundColor(Color.RED);

		final int item = arg0;
		final View v = convertView;
		hodler.operation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				md = new MyDialog(context, R.layout.dialog_layout,
						R.style.Theme_myDialog);
				TextView title = (TextView)md.findViewById(R.id.tv_title_dialog);
				TextView info = (TextView)md.findViewById(R.id.tv_info_dialog);
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
										account,
										(String) mData.get(item).get("pin"),
										false)).start();
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
				info.setText("确定退回该卡？");
				md.show();
			}
		});

		// 为每个listView设置高度
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 70);
		convertView.setLayoutParams(lp);

		return convertView;
	}

	static class ViewHolder {
		TextView cardNumber, pagevalue, status, operation;
	}

	// 删除子view的方法
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
				getInstance().notifyDataSetChanged();
			}
		});
		v.startAnimation(animationSet);
	}

	public interface AddItemWaitListener {
		public void setBageView(Map<String, String> map, int num);
	}
}