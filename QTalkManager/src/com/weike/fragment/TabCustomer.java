package com.weike.fragment;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentValues;
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

import com.weike.adapter.CustomerListViewAdapter;
import com.weike.application.Interfaces;
import com.weike.db.OperationDB;
import com.weike.network.GetListData;
import com.weike.qtalkmanager.R;
import com.weike.util.ConnectionDetector;

public class TabCustomer extends Fragment implements
		SwipeRefreshLayout.OnRefreshListener, OnScrollListener {

	private ListView listView = null;
	private ArrayList<Map<String, String>> mData = null;
	private CustomerListViewAdapter adapter = null;
	private View mainView = null;
	private static Handler han = null;
	private TextView sumCustomer = null, sumMoney = null, tvTime;
	private SwipeRefreshLayout refreshLayout = null;
	private OperationDB db = null;
	private ProgressBar pbHead = null, pbFooter = null;
	private final String TABLE = "customer_tb";
	private int lastItem, totalItem;
	protected Handler sHan;

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
				getMessage();
			}
		}, 3000);
	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		han = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {

				super.handleMessage(msg);
				if (msg.what == 0) {
					if (mData.size() != 0) {
						mData.clear();
					}
					mData = (ArrayList<Map<String, String>>) msg.obj;
					ArrayList<Map<String, String>> showData = new ArrayList<Map<String, String>>();
					for (int i = 0; i < mData.size(); i++) {
						showData.add(mData.get(i));
						if (showData.size() == 20) {
							break;
						}
					}
					showList(showData);
					if (sHan == null) {
						initSaveHandler();
					}
					new newWork(sHan, mData).start();
				} else {
					Toast.makeText(getActivity(), "网络不给力，请稍后再试",
							Toast.LENGTH_SHORT).show();
				}
			}
		};

	}

	@SuppressLint("HandlerLeak")
	void initSaveHandler() {
		sHan = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 0) {
					refreshSum();
				}
			}
		};
	}

	class newWork extends Thread {
		private ArrayList<Map<String, String>> newData = null;
		private Handler han = null;

		public newWork(Handler han, ArrayList<Map<String, String>> newData) {
			this.newData = newData;
			this.han = han;
		}

		@Override
		public void run() {
			super.run();
			try {
				saveData(newData);
				Message m = han.obtainMessage();
				m.what = 0;
				han.sendMessage(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void saveData(ArrayList<Map<String, String>> mData2) {
		ArrayList<ContentValues> list = new ArrayList<ContentValues>();
		for (int i = 0; i < mData2.size(); i++) {
			ContentValues content = new ContentValues();
			content.put("customeraccount", mData2.get(i).get("customeraccount"));
			DecimalFormat format = new DecimalFormat("#0.00");
			float money = Float.valueOf(mData.get(i).get("money")).floatValue();
			content.put("money", Float.valueOf(format.format(money))
					.floatValue());
			list.add(content);
		}
		try {
			db.clearDB(TABLE);
			db.saveData(TABLE, list);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected ArrayList<Map<String, String>> getListData(String name) {

		ArrayList<Map<String, String>> rData = new ArrayList<Map<String, String>>(); // 待返回的数据源
		String sql = null;
		sql = "select * from " + TABLE;
		Cursor cursor = db.getMessage(sql);
		boolean tag = false, bag = false;
		if (cursor != null) {
			while (cursor.moveToNext()) {
				if (tag) {
					bag = true;
				}
				if (name == null) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("customeraccount", cursor.getString(cursor
							.getColumnIndex("customeraccount")));
					map.put("money", String.valueOf(cursor.getFloat(cursor
							.getColumnIndex("money"))));
					rData.add(map);
					map = null;
				} else {
					String na = cursor.getString(cursor
							.getColumnIndex("customeraccount"));
					if (na.equals(name)) {
						tag = true;
					}
					if (bag) {
						Map<String, String> map = new HashMap<String, String>();
						map.put("customeraccount", na);
						map.put("money", String.valueOf(cursor.getFloat(cursor
								.getColumnIndex("money"))));
						rData.add(map);
						map = null;
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
			sql = null;
		}
		return rData;
	}

	// 初始化视图
	@SuppressLint({ "InlinedApi" })
	private void initView() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mainView = inflater.inflate(R.layout.tab_customer,
				(ViewGroup) getActivity().findViewById(R.layout.item_2), false);
		listView = (ListView) mainView.findViewById(R.id.customer_list);

		sumCustomer = (TextView) mainView.findViewById(R.id.tv_sum_customer);
		sumMoney = (TextView) mainView.findViewById(R.id.tv_sumMoney_customer);
		refreshLayout = (SwipeRefreshLayout) mainView
				.findViewById(R.id.refresh_customer_ll);
		// 设置控件的颜色变化
		refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		refreshLayout.setOnRefreshListener(this);
		tvTime = (TextView) mainView.findViewById(R.id.tv_time_customer);
		pbHead = (ProgressBar) mainView.findViewById(R.id.customer_progressbar);
		pbFooter = (ProgressBar) mainView
				.findViewById(R.id.customer_footer_progressbar);
		db = new OperationDB(getActivity());
		mData = new ArrayList<Map<String, String>>();
	}

	// 访问网络获取数据
	private void getMessage() {
		// 检查网络
		if (new ConnectionDetector(getActivity()).isConnectingToInternet()) {
			try {
				getActivity();
				SharedPreferences sp = getActivity().getSharedPreferences(
						"userInfo", Context.MODE_PRIVATE);
				String account = sp.getString("account", "");
				new GetListData(han, Interfaces.getCustomerUrl(account, 1,
						10000)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// 没有连接网络
			Toast.makeText(getActivity(), "网络为连接，请检查网络！", Toast.LENGTH_SHORT)
					.show();
		}
	}

	// 展示ListView
	private void showList(ArrayList<Map<String, String>> mData) {
		if (adapter == null) {
			adapter = new CustomerListViewAdapter(mData, getActivity());
			pbHead.setVisibility(View.GONE);
			listView.setAdapter(adapter);
			listView.setOnScrollListener(this);
		} else {
			if (refreshLayout.isRefreshing()) {
				adapter.refreshData(mData);
				refreshLayout.setRefreshing(false);
				tvTime.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
			} else {
				adapter.getData().addAll(mData);
				pbFooter.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
			}
		}
		mData = null;
	}

	// 下拉刷新
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onRefresh() {
		SharedPreferences sp = getActivity().getSharedPreferences(
				"customer_refresh", Context.MODE_PRIVATE);
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
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				getMessage();
			}
		}, 2000);
	}

	private void refreshSum() {
		String sql = "select * from " + TABLE;
		Cursor cursor = null;
		try {
			cursor = db.getMessage(sql);
			int num = cursor.getCount();
			sumCustomer.setText("总计：" + num);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		String sql2 = "select total(money) from " + TABLE;
		float number = 0;
		Cursor c2 = null;
		try {
			c2 = db.getMessage(sql2);
			if (c2.moveToFirst()) {
				do {
					number = c2.getFloat(0);
				} while (c2.moveToNext());
			}
			DecimalFormat format = new DecimalFormat("#0.00");
			sumMoney.setText(format.format(number));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c2.close();
		}
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
						addData = getListData(adapter.getData()
								.get(adapter.getData().size() - 1)
								.get("customeraccount"));
						showList(addData);
					}
				}, 2000);

			}

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		db.closeDB();
	}
}
