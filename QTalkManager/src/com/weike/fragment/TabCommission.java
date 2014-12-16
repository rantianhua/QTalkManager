package com.weike.fragment;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.weike.adapter.CommissionListViewAdapter;
import com.weike.application.Interfaces;
import com.weike.db.OperationDB;
import com.weike.network.GetListData;
import com.weike.qtalkmanager.R;
import com.weike.util.ConnectionDetector;

public class TabCommission extends Fragment implements
		SwipeRefreshLayout.OnRefreshListener, OnScrollListener {

	private ListView listView = null;
	private ArrayList<Map<String, String>> mData = null;
	private CommissionListViewAdapter adapter = null;
	private View mainView = null;
	private static Handler han = null;
	private SwipeRefreshLayout refreshlayout = null;
	private TextView tvSumPre, tvSumComm, tvTime;
	private OperationDB db = null;
	private ProgressBar pbHead, pbFooter;
	private final String TABLE = "record_tb";
	private int lastItem, totalItem;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup p = (ViewGroup) mainView.getParent();
		if (p != null) {
			p.removeAllViewsInLayout();
		}
		return mainView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initHandler();
		pbHead.setVisibility(View.VISIBLE);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				mData = getListData("alipay", null);
				showList(mData);
				refeshSum();
			}
		}, 4000);

	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		han = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 0) {
					mData.clear();
					mData = (ArrayList<Map<String, String>>) msg.obj;
					// 存储数据
					TabRecord.saveData(mData);
					ArrayList<Map<String, String>> showData = new ArrayList<Map<String, String>>();
					showData = getListData("alipay", null);
					showList(showData);
					refeshSum();
				} else {
					Toast.makeText(getActivity(), "网络不给力，请稍后再试",
							Toast.LENGTH_SHORT).show();
				}
			}
		};
	}

	protected ArrayList<Map<String, String>> getListData(String type,
			String time) {
		ArrayList<Map<String, String>> rData = new ArrayList<Map<String, String>>(); // 待返回的数据源
		String sql = null;
		sql = "select time,type,real_money,commi_money from " + TABLE;
		Cursor cursor = db.getMessage(sql);
		boolean tag = false, bag = false;
		while (cursor.moveToNext()) {
			if (tag) {
				bag = true;
			}
			String t = cursor.getString(cursor.getColumnIndex("type"));
			if (t.equals("alipay")) {
				if (time == null) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("time",
							cursor.getString(cursor.getColumnIndex("time")));
					map.put("real_money", String.valueOf(cursor.getFloat(cursor
							.getColumnIndex("real_money"))));
					map.put("commi_money", String.valueOf(cursor
							.getFloat(cursor.getColumnIndex("commi_money"))));

					rData.add(map);
				} else {
					String tm = cursor.getString(cursor.getColumnIndex("time"));
					if (tm.equals(time)) {
						tag = true;
					}
					if (bag) {
						Map<String, String> map = new HashMap<String, String>();
						map.put("time",
								cursor.getString(cursor.getColumnIndex("time")));
						map.put("real_money", String.valueOf(cursor
								.getFloat(cursor.getColumnIndex("real_money"))));
						map.put("commi_money",
								String.valueOf(cursor.getFloat(cursor
										.getColumnIndex("commi_money"))));

						rData.add(map);
					}
				}

			}
			if (rData.size() == 20) {
				break;
			}
		}
		try {
			if (!cursor.isClosed()) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("getListData", "查询数据库失败！");
		}
		return rData;
	}

	// 初始化控件
	@SuppressLint("InlinedApi")
	private void initView() {
		LayoutInflater inflate = getActivity().getLayoutInflater();
		mainView = inflate.inflate(R.layout.tab_commission,
				(ViewGroup) getActivity().findViewById(R.layout.item_3), false);
		listView = (ListView) mainView.findViewById(R.id.commission_list);
		refreshlayout = (SwipeRefreshLayout) mainView
				.findViewById(R.id.refresh_commission_ll);
		refreshlayout.setOnRefreshListener(this);
		// 设置控件的颜色变化
		refreshlayout.setColorSchemeResources(android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		tvSumPre = (TextView) mainView.findViewById(R.id.tv_preSum_commission);
		tvSumComm = (TextView) mainView
				.findViewById(R.id.tv_commiSum_commission);
		tvTime = (TextView) mainView.findViewById(R.id.tv_time_commi);
		pbHead = (ProgressBar) mainView.findViewById(R.id.commi_progressbar);
		pbFooter = (ProgressBar) mainView
				.findViewById(R.id.commi_footer_progressbar);
		db = new OperationDB(getActivity());
	}

	protected void showList(ArrayList<Map<String, String>> mData2) {
		if (adapter == null) {
			adapter = new CommissionListViewAdapter(mData, getActivity());
			pbHead.setVisibility(View.GONE);
			listView.setAdapter(adapter);
			listView.setOnScrollListener(this);
		} else {
			if (refreshlayout.isRefreshing()) {
				tvTime.setVisibility(View.GONE);
				adapter.refreshData(mData2);
				refreshlayout.setRefreshing(false);
				adapter.notifyDataSetChanged();
			} else {
				adapter.getData().addAll(mData);
				pbFooter.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
			}
		}

	}

	// 访问网络获取数据
	private void getMessage() {
		if (new ConnectionDetector(getActivity()).isConnectingToInternet()) {
			getActivity();
			SharedPreferences sp = getActivity().getSharedPreferences(
					"userInfo", Context.MODE_PRIVATE);
			String account = sp.getString("account", "");
			new GetListData(han, Interfaces.getRecordOrCommUrl(account, 1,
					10000)).start();
		} else {
			Toast.makeText(getActivity(), "网络异常，稍后重试！", Toast.LENGTH_SHORT)
					.show();
		}
	}

	// 下拉刷新
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onRefresh() {
		SharedPreferences sp = getActivity().getSharedPreferences(
				"commi_refresh", Context.MODE_PRIVATE);
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String date = sDateFormat.format(new java.util.Date());
		String preDate = sp.getString("time", "");
		if (preDate != null) {
			tvTime.setText("上次刷新   " + preDate);
		} else {
			tvTime.setText("暂未刷新过");
		}
		tvTime.setVisibility(View.VISIBLE);
		Editor editor = sp.edit();
		editor.putString("time", date);
		editor.commit();
		// 下拉刷新数据
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				getMessage();
			}
		}, 2000);
	}

	private void refeshSum() {
		DecimalFormat format = new DecimalFormat("#0.00");
		String sql = "select total(real_money) from " + TABLE;
		Cursor cursor = null;
		float sum = 0;
		try {
			cursor = db.getMessage(sql);
			if (cursor.moveToFirst()) {
				do {
					sum = cursor.getFloat(0);
				} while (cursor.moveToNext());
			}
			tvSumPre.setText(format.format(sum));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		String sql2 = "select total(commi_money) from " + TABLE;
		Cursor cursor2 = null;
		float sum2 = 0;
		try {
			cursor2 = db.getMessage(sql2);
			if (cursor2.moveToFirst()) {
				do {
					sum2 = cursor2.getFloat(0);
				} while (cursor2.moveToNext());
			}

			tvSumComm.setText(format.format(sum2));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor2.close();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		db.closeDB();
	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount;
		totalItem = totalItemCount;

	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int state) {
		if (lastItem == totalItem && state == SCROLL_STATE_IDLE) {
			if (pbFooter.getVisibility() == View.GONE) {
				// 上拉加载更多
				pbFooter.setVisibility(View.VISIBLE);
				// 推迟2秒执行，确保新数据已经插入数据库
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						ArrayList<Map<String, String>> addData = null;
						addData = getListData(adapter.getData().get(lastItem)
								.get("type"), adapter.getData().get(lastItem)
								.get("time"));
						showList(addData);
					}
				}, 2000);

			}

		}
	}

}
