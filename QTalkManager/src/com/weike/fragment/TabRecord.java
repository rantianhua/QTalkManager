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

import com.weike.adapter.RecordListViewAdapter;
import com.weike.application.Interfaces;
import com.weike.db.OperationDB;
import com.weike.network.GetListData;
import com.weike.qtalkmanager.R;
import com.weike.util.ConnectionDetector;

public class TabRecord extends Fragment implements
		SwipeRefreshLayout.OnRefreshListener, OnScrollListener {

	private ListView listView = null;
	private RecordListViewAdapter adapter = null;
	private static ArrayList<Map<String, String>> mData = new ArrayList<Map<String, String>>(); // 数据源
	private View mainView = null;
	private SwipeRefreshLayout refreshLayout = null; // 刷新控件
	private static Handler han = null, sHan = null;
	private TextView tvThirty, tvHundred, tvYear, tvTime;
	private final static String TABLE = "record_tb";
	private ProgressBar pbHead, pbFooter;
	private static OperationDB db = null;
	private int lastItem = 0, totalItem = 0;

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

	private void getMessage() {
		// 检查网络状态
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
					if (sHan == null) {
						sHan = initSaveHandler();
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
	private Handler initSaveHandler() {
		Handler han = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 0) {
					ArrayList<Map<String, String>> showData = getListData(
							"phonecard", null);
					showList(showData);
					refreshSum();
				}
			}
		};
		return han;
	}

	class newWork extends Thread {
		private ArrayList<Map<String, String>> sData = null;
		private Handler sHandler = null;

		public newWork(Handler han, ArrayList<Map<String, String>> sData) {
			this.sData = sData;
			sHandler = han;
		}

		@Override
		public void run() {
			super.run();
			try {
				// 将所有数据存入数据库
				saveData(sData);
				Message m = sHandler.obtainMessage();
				m.what = 0;
				sHandler.sendMessage(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected static void saveData(ArrayList<Map<String, String>> mData2) {
		ArrayList<ContentValues> list = new ArrayList<ContentValues>();
		for (int i = 0; i < mData.size(); i++) {
			ContentValues content = new ContentValues();
			String hh = null;
			try {
				content.put("time", mData.get(i).get("time"));
				String type = mData.get(i).get("type");
				content.put("type", type);
				if (type.equals("phonecard")) {
					hh = mData.get(i).get("memo");
					content.put(
							"thirty_sum",
							Integer.valueOf(
									hh.substring(hh.indexOf("30元卡") + 4,
											hh.indexOf("张"))).intValue());
					hh = hh.substring(hh.indexOf(",") + 1);
					content.put(
							"hundred_sum",
							Integer.valueOf(
									hh.substring(hh.indexOf("100元卡") + 5,
											hh.indexOf("张"))).intValue());
					hh = hh.substring(hh.indexOf(",") + 1);
					content.put(
							"year_sum",
							Integer.valueOf(
									hh.substring(hh.indexOf("365元卡") + 5,
											hh.indexOf("张"))).intValue());
					content.put("real_money", 0.00);
					content.put("commi_money", 0.00);
				} else {
					content.put("thirty_sum", 0);
					content.put("hundred_sum", 0);
					content.put("year_sum", 0);
					DecimalFormat format = new DecimalFormat("#0.00");
					float realMoney = Float.valueOf(mData.get(i).get("memo"))
							.floatValue();
					float commiMoney = Float.valueOf(mData.get(i).get("money"))
							.floatValue();
					content.put("real_money",
							Float.valueOf(format.format(realMoney))
									.floatValue());
					content.put("commi_money",
							Float.valueOf(format.format(commiMoney))
									.floatValue());
				}
				list.add(content);
				content = null;
			} catch (NumberFormatException e) {
				content.put(
						"hundred_sum",
						Integer.valueOf(
								hh.substring(hh.indexOf("110元卡") + 5,
										hh.indexOf("张"))).intValue());
			}
		}
		try {
			db.clearDB(TABLE);
			db.saveData(TABLE, list);
			list = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("InlinedApi")
	private void initView() {
		LayoutInflater inflate = getActivity().getLayoutInflater();
		mainView = inflate.inflate(R.layout.tab_record,
				(ViewGroup) getActivity().findViewById(R.layout.item_3), false);
		listView = (ListView) mainView.findViewById(R.id.record_list);

		// google官方的下拉刷新控件
		refreshLayout = (SwipeRefreshLayout) mainView
				.findViewById(R.id.refresh_record_ll);
		refreshLayout.setOnRefreshListener(this);
		// 设置控件的颜色变化
		refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		tvThirty = (TextView) mainView.findViewById(R.id.tv_thirdCards_record);
		tvHundred = (TextView) mainView
				.findViewById(R.id.tv_hundredCards_record);
		tvYear = (TextView) mainView.findViewById(R.id.tv_yearCards_record);
		tvTime = (TextView) mainView.findViewById(R.id.tv_time_record);
		pbHead = (ProgressBar) mainView.findViewById(R.id.record_progressbar);
		pbFooter = (ProgressBar) mainView
				.findViewById(R.id.record_footer_progressbar);
		db = new OperationDB(getActivity());
	}

	// 展示ListView
	public void showList(ArrayList<Map<String, String>> mData) {
		if (adapter == null) {
			adapter = new RecordListViewAdapter(mData, getActivity());
			pbHead.setVisibility(View.GONE);
			listView.setAdapter(adapter);
			listView.setOnScrollListener(this);
		} else {
			if (refreshLayout.isRefreshing()) {
				// 正在刷新
				tvTime.setVisibility(View.GONE);
				adapter.refreshData(mData);
				refreshLayout.setRefreshing(false);
				adapter.notifyDataSetChanged();
			} else {
				// 上拉加载
				adapter.getData().addAll(mData);
				pbFooter.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
			}
		}
	}

	// 下拉刷新
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onRefresh() {
		SharedPreferences sp = getActivity().getSharedPreferences(
				"record_refresh", Context.MODE_PRIVATE);
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
			public void run() {
				getMessage();
			}
		}, 2000);
	}

	private void refreshSum() {
		String sql1 = "select total(thirty_sum) from " + TABLE;
		String sql2 = "select total(hundred_sum) from " + TABLE;
		String sql3 = "select total(year_sum) from " + TABLE;
		int thirty = 0, hundred = 0, year = 0;
		Cursor cursorT = null, cursorH = null, cursorY = null;
		try {
			cursorT = db.getMessage(sql1);
			if (cursorT.moveToFirst()) {
				do {
					thirty = cursorT.getInt(0);
				} while (cursorT.moveToNext());
			}
			tvThirty.setText(thirty + "");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!cursorT.isClosed() && cursorT != null) {
				cursorT.close();
				cursorT = null;
			}
		}
		try {
			cursorH = db.getMessage(sql2);
			if (cursorH.moveToFirst()) {
				do {
					hundred = cursorH.getInt(0);
				} while (cursorH.moveToNext());
				tvHundred.setText(hundred + "");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (!cursorH.isClosed() && cursorH != null) {
				cursorH.close();
				cursorH = null;
			}
		}
		try {
			cursorY = db.getMessage(sql3);
			if (cursorY.moveToFirst()) {
				do {
					year = cursorY.getInt(0);
				} while (cursorY.moveToNext());
			}
			tvYear.setText(year + "");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!cursorY.isClosed() && cursorY != null) {
				cursorY.close();
				cursorY = null;
			}
		}
		sql1 = null;
		sql2 = null;
		sql3 = null;
	}

	private ArrayList<Map<String, String>> getListData(String type, String time) {
		ArrayList<Map<String, String>> rData = new ArrayList<Map<String, String>>(); // 待返回的数据源
		String sql = null;
		sql = "select time,type,thirty_sum,hundred_sum,year_sum from " + TABLE;
		Cursor cursor = db.getMessage(sql);
		boolean tag = false, bag = false;
		if (cursor != null) {
			while (cursor.moveToNext()) {
				if (tag) {
					bag = true;
				}
				String t = cursor.getString(cursor.getColumnIndex("type"));
				if (t.equals("phonecard")) {
					if (time == null) {
						Map<String, String> map = new HashMap<String, String>();
						map.put("time",
								cursor.getString(cursor.getColumnIndex("time")));
						map.put("thirty_sum", String.valueOf(cursor
								.getInt(cursor.getColumnIndex("thirty_sum"))));
						map.put("hundred_sum", String.valueOf(cursor
								.getInt(cursor.getColumnIndex("hundred_sum"))));
						map.put("year_sum", String.valueOf(cursor.getInt(cursor
								.getColumnIndex("year_sum"))));
						rData.add(map);
					} else {
						String tm = cursor.getString(cursor
								.getColumnIndex("time"));
						if (t.equals("phonecard") && tm.equals(time)) {
							tag = true;
						}
						if (bag) {
							Map<String, String> map = new HashMap<String, String>();
							map.put("time", cursor.getString(cursor
									.getColumnIndex("time")));
							map.put("thirty_sum",
									String.valueOf(cursor.getInt(cursor
											.getColumnIndex("thirty_sum"))));
							map.put("hundred_sum", String.valueOf(cursor
									.getInt(cursor
											.getColumnIndex("hundred_sum"))));
							map.put("year_sum", String.valueOf(cursor
									.getInt(cursor.getColumnIndex("year_sum"))));
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
		}
		return rData;
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
						addData = getListData(
								adapter.getData().get(lastItem - 1).get("type"),
								adapter.getData().get(lastItem).get("time"));
						showList(addData);
					}
				}, 2000);
			}
		}
	}

}
