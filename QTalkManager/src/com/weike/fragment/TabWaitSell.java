package com.weike.fragment;

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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jauker.widget.BadgeView;
import com.weike.adapter.MyArrayAdapter;
import com.weike.adapter.WaitListViewAdapter;
import com.weike.adapter.WaitListViewAdapter.AddSelledItemLisenter;
import com.weike.application.ExitApplication;
import com.weike.application.Interfaces;
import com.weike.db.OperationDB;
import com.weike.network.GetListData;
import com.weike.qtalkmanager.MyDialog;
import com.weike.qtalkmanager.R;
import com.weike.util.ConnectionDetector;

@SuppressLint("SimpleDateFormat")
public class TabWaitSell extends Fragment implements
		SwipeRefreshLayout.OnRefreshListener, OnClickListener, OnScrollListener {

	private final static String TABLE = "wait_tb";
	public ListView listView = null;
	private TextView sum = null, refreshTime = null;
	public WaitListViewAdapter adapter = null;
	private SwipeRefreshLayout refreshLayout = null; // 刷新控件
	public BadgeView bvButtom = null;
	public LinearLayout ll_sell = null; // 底部导航栏的父布局
	private View mainView = null; // 主视图
	private AutoCompleteTextView searchText = null; // 用于搜索控件
	private Handler han = null;
	private ProgressBar loadingHead = null, loadingFooter = null;
	private Button thirty, hundred, year, quit;
	private MyArrayAdapter<String> searchAdapter = null;
	public ArrayList<Map<String, String>> sumData = new ArrayList<Map<String, String>>();
	public ArrayList<Map<String, String>> thirtyData = new ArrayList<Map<String, String>>();
	public ArrayList<Map<String, String>> hundredData = new ArrayList<Map<String, String>>();
	public ArrayList<Map<String, String>> yearData = new ArrayList<Map<String, String>>();
	private String[] data = null; // 搜所数据源
	private OperationDB db = null;
	private int totalItem = 0, lastItem = 0;
	private static TabWaitSell tw = null;
	private TabSelled sellTab = null;

	// 初始化除了视图以外的东西
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		loadingHead.setVisibility(View.VISIBLE);
		initHandler();
		tw = this;
		getMessage();
	}

	public static TabWaitSell getInstance() {
		return tw;
	}

	// 访问网络获取数据
	private void getMessage() {
		// 先检查网络是否连接
		if (new ConnectionDetector(getActivity()).isConnectingToInternet()) {
			try {
				getActivity();
				SharedPreferences sp = getActivity().getSharedPreferences(
						"userInfo", Context.MODE_PRIVATE);
				String account = sp.getString("account", "");
				new GetListData(han, Interfaces.getWaitSellUrl(account, "all",
						1, 10000)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// 没有连接网络
			Toast.makeText(getActivity(), "网络未连接,请连接网络再试！", Toast.LENGTH_SHORT)
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
					sumData = (ArrayList<Map<String, String>>) msg.obj;
					// 先显示前20条数据
					switch ((String) listView.getTag()) {
					case "30":
						for (int i = 0; i < sumData.size(); i++) {
							if (sumData.get(i).get("money").equals("30")) {
								thirtyData.add(sumData.get(i));
							}
							if (thirtyData.size() == 20) {
								break;
							}
						}
						showList(thirtyData);
						break;

					case "110":
						for (int i = 0; i < sumData.size(); i++) {
							if (sumData.get(i).get("money").equals("110")) {
								hundredData.add(sumData.get(i));
							}
							if (hundredData.size() == 20) {
								break;
							}
						}
						showList(hundredData);
						break;
					case "365":
						for (int i = 0; i < sumData.size(); i++) {
							if (sumData.get(i).get("money").equals("365")) {
								yearData.add(sumData.get(i));
							}
							if (yearData.size() == 20) {
								break;
							}
						}
						showList(yearData);
						break;
					default:
						break;
					}
					new mutiTask(sumData).start();
				} else {
					Toast.makeText(getActivity(), "网络不给力，请稍后再试",
							Toast.LENGTH_SHORT).show();
				}
			}
		};
	}

	class mutiTask extends Thread {
		private ArrayList<Map<String, String>> mutiData = null;

		public mutiTask(ArrayList<Map<String, String>> mutiData) {
			this.mutiData = mutiData;
		}

		@Override
		public void run() {
			super.run();
			try {
				// 将新数据存入数据库
				saveWaitData(mutiData);
				// 更新搜索数据源
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 将数据存入数据库
	protected void saveWaitData(ArrayList<Map<String, String>> sumData2) {
		// 判断sumData2是否有数据
		if (sumData2.size() != 0) {
			try {
				// 先清空数据库
				db.clearDB(TABLE);
				ArrayList<ContentValues> values = new ArrayList<ContentValues>();
				for (int i = 0; i < sumData.size(); i++) {
					ContentValues content = new ContentValues();
					content.put("pin", sumData.get(i).get("pin"));
					content.put("money", sumData.get(i).get("money"));
					values.add(content);
				}
				db.saveData(TABLE, values);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@SuppressLint("InlinedApi")
	private void initView() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mainView = inflater.inflate(R.layout.tab_waitsell,
				(ViewGroup) getActivity().findViewById(R.layout.item_4), false);
		listView = (ListView) mainView.findViewById(R.id.wait_list);
		listView.setTag("30"); // 标记当前显示的是什么类型的卡
		sum = (TextView) mainView.findViewById(R.id.tv_waitOrselled_buttom);
		refreshTime = (TextView) mainView.findViewById(R.id.tv_wait_time);
		// 初始化自动搜索控件
		searchText = (AutoCompleteTextView) mainView
				.findViewById(R.id.auto_search);

		// google官方的下拉刷新控件
		refreshLayout = (SwipeRefreshLayout) mainView
				.findViewById(R.id.refresh_waitsell_ll);
		refreshLayout.setOnRefreshListener(this);
		// 设置控件的颜色变化
		refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		loadingHead = (ProgressBar) mainView
				.findViewById(R.id.wait_progressbar);
		loadingFooter = (ProgressBar) mainView
				.findViewById(R.id.wait_footer_progressbar);
		thirty = (Button) mainView.findViewById(R.id.btn_thirty);
		hundred = (Button) mainView.findViewById(R.id.btn_hundred);
		year = (Button) mainView.findViewById(R.id.btn_year);
		quit = (Button) mainView.findViewById(R.id.btn_quit);
		thirty.setOnClickListener(this);
		hundred.setOnClickListener(this);
		year.setOnClickListener(this);
		quit.setOnClickListener(this);
		db = new OperationDB(getActivity());
	}

	// 初始化搜索数据的适配器
	void initSearchAdapter() {
		data = new String[thirtyData.size() + hundredData.size()
				+ yearData.size()];
		for (int i = 0; i < thirtyData.size(); i++) {
			data[i] = thirtyData.get(i).get("pin");
		}
		for (int i = 0; i < hundredData.size(); i++) {
			data[thirtyData.size() + i] = hundredData.get(i).get("pin");
		}

		for (int i = 0; i < yearData.size(); i++) {
			data[thirtyData.size() + hundredData.size() + i] = yearData.get(i)
					.get("pin");
		}
		// 实例化自动搜索的适配器
		searchAdapter = new MyArrayAdapter<>(getActivity(),
				R.layout.search_item, data);

		searchText.setAdapter(searchAdapter);
		searchText
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {

						// 隐藏键盘
						InputMethodManager imm = (InputMethodManager) getActivity()
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.toggleSoftInput(0,
								InputMethodManager.HIDE_NOT_ALWAYS);
						TextView tv = (TextView) arg1;
						String pin = tv.getText().toString();
						String money = null;
						int pos = 0;
						for (int i = 0; i < thirtyData.size(); i++) {
							if (pin.equals(thirtyData.get(i).get("pin"))) {
								money = "30";
								pos = i;
							}
						}
						if (pos == 0 && money == null) {
							for (int i = 0; i < hundredData.size(); i++) {
								if (pin.equals(hundredData.get(i).get("pin"))) {
									money = "110";
									pos = i;
								}
							}
						}
						if (pos == 0 && money == null) {
							for (int i = 0; i < yearData.size(); i++) {
								if (pin.equals(yearData.get(i).get("pin"))) {
									money = "365";
									pos = i;
								}
							}
						}
						switch (money) {
						case "30":
							if (listView.getTag().equals("30")) {
								listView.setSelection(pos);
							} else {
								changMessage("30", thirtyData);
								listView.setSelection(pos);
							}
							break;

						case "110":
							if (listView.getTag().equals("110")) {
								listView.setSelection(pos);
							} else {
								changMessage("110", hundredData);
								listView.setSelection(pos);
							}
							break;
						case "365":
							if (listView.getTag().equals("365")) {
								listView.setSelection(pos);
							} else {
								changMessage("365", yearData);
								listView.setSelection(pos);
							}
							break;
						default:
							Toast.makeText(getActivity(), "还没浏览到该项，继续往下划吧！",
									Toast.LENGTH_SHORT).show();
							break;
						}
						searchText.setText("");
					}
				});
	}

	// 初始化视图
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		ViewGroup p = (ViewGroup) mainView.getParent();
		if (p != null) {
			p.removeAllViewsInLayout();
		}
		return mainView;
	}

	@Override
	public void onRefresh() {
		SharedPreferences sp = getActivity().getSharedPreferences(
				"wait_refresh", Context.MODE_PRIVATE);
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String date = sDateFormat.format(new java.util.Date());
		String preDate = sp.getString("time", "");
		if (preDate != null) {
			refreshTime.setText("上次刷新   " + preDate);
		} else {
			refreshTime.setText("暂未更新");
		}
		refreshTime.setVisibility(View.VISIBLE);
		Editor editor = sp.edit();
		editor.putString("time", date);
		editor.commit();
		date = null;
		sDateFormat = null;
		preDate = null;
		sp = null;
		new Handler().postDelayed(new Runnable() {
			public void run() {
				sumData.clear();
				thirtyData.clear();
				hundredData.clear();
				yearData.clear();
				getMessage();
			}
		}, 2000);
	}

	// 展示ListView
	private void showList(ArrayList<Map<String, String>> freshData) {
		if (adapter == null) {
			ll_sell = (LinearLayout) getActivity().findViewById(
					R.id.buttom_ll_sell);
			bvButtom = new BadgeView(getActivity());
			bvButtom.setBadgeGravity(Gravity.TOP | Gravity.RIGHT);
			ll_sell.addView(bvButtom);

			AddSelledItemLisenter addItem = new AddSelledItemLisenter() {

				@Override
				public void setBadgeView(Map<String, String> map, int num) {
					// 底部脚标
					if (bvButtom.getVisibility() != View.VISIBLE) {
						bvButtom.setVisibility(View.VISIBLE);
					}
					bvButtom.setBadgeCount(bvButtom.getBadgeCount() + num);
					String mTag = (String) map.get("money");
					// 修改数据库
					// 先在wait_tb离删除该项
					db.delectData(TABLE, map.get("pin"));
					// 在sell_tb中插入
					ArrayList<ContentValues> list = new ArrayList<ContentValues>();
					ContentValues content = new ContentValues();
					content.put("pin", Integer.valueOf(map.get("pin"))
							.intValue());
					content.put("money", map.get("money"));
					content.put("usedaccount", "none");
					list.add(content);
					try {
						db.saveData("sell_tb", list);
					} catch (Exception e) {
						e.printStackTrace();
					}

					Map<String, String> map2 = new HashMap<String, String>();
					map2 = map;
					map2.put("usedaccount", "none");
					if (sellTab == null) {
						sellTab = TabSelled.getInstance();
					}
					switch (mTag) {
					case "30":
						if (thirtyData.contains(map)) {
							thirtyData.remove(map);
						}
						if (sellTab.thirtyData.size() == 0) {
							sellTab.thirtyData.add(map2);
						} else {
							for (int i = 0; i < sellTab.thirtyData.size(); i++) {
								if (Integer.valueOf((String) map.get("pin"))
										.intValue() < Integer.valueOf(
										sellTab.thirtyData.get(i).get("pin"))
										.intValue()) {
									sellTab.thirtyData.add(i, map2);
									if (sellTab.listView.getTag().equals("30")) {
										sellTab.adapter
												.refreshData(sellTab.thirtyData);
									}
									break;
								}
							}
						}
						break;
					case "110":
						if (hundredData.contains(map)) {
							hundredData.remove(map);
						}

						if (sellTab.hundredData.size() == 0) {
							sellTab.hundredData.add(map2);
						} else {
							for (int i = 0; i < sellTab.hundredData.size(); i++) {
								if (Integer.valueOf((String) map.get("pin"))
										.intValue() < Integer.valueOf(
										sellTab.hundredData.get(i).get("pin"))
										.intValue()) {
									sellTab.hundredData.add(i, map2);
									if (sellTab.listView.getTag().equals("110")) {

										sellTab.adapter
												.refreshData(sellTab.hundredData);
									}
									break;
								}
							}
						}
						break;
					case "365":
						if (yearData.contains(map)) {
							yearData.remove(map);
						}
						if (sellTab.yearData.size() == 0) {
							sellTab.yearData.add(map2);
						} else {
							for (int i = 0; i < sellTab.yearData.size(); i++) {
								if (Integer.valueOf((String) map.get("pin"))
										.intValue() < Integer.valueOf(
										sellTab.yearData.get(i).get("pin"))
										.intValue()) {
									sellTab.yearData.add(i, map2);
									if (sellTab.listView.getTag().equals("365")) {
										sellTab.adapter
												.refreshData(sellTab.yearData);
									}
									break;
								}
							}
						}

						break;
					default:
						break;
					}
					refreshSum();
					initSearchAdapter();
					sellTab.adapter.notifyDataSetChanged();
					sellTab.refreshSum();
					sellTab.initSearchAdapter();
				}
			};
			// listView的适配器
			adapter = new WaitListViewAdapter(freshData, this.getActivity(),
					addItem);
			loadingHead.setVisibility(View.GONE);
			listView.setAdapter(adapter);
			listView.setOnScrollListener(this);
			refreshSum();
			initSearchAdapter();
		} else {
			if (refreshLayout.isRefreshing()
					|| loadingHead.getVisibility() == View.VISIBLE) {
				refreshTime.setVisibility(View.GONE);
				// 下拉刷新或点击按钮操作
				if (freshData.size() == 0) {
					refreshLayout.setRefreshing(false);
					loadingHead.setVisibility(View.GONE);
					Toast.makeText(getActivity(),
							"暂时没有" + listView.getTag() + "元卡的数据！",
							Toast.LENGTH_SHORT).show();
				} else {
					adapter.refreshData(freshData);
					listView.setVisibility(View.VISIBLE);
					refreshLayout.setRefreshing(false);
					loadingHead.setVisibility(View.GONE);
					adapter.notifyDataSetChanged();
				}
			} else {
				// 上拉加载更多
				adapter.getData().addAll(freshData);
				loadingFooter.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
			}
			initSearchAdapter();
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btn_hundred:
			// 判断当前显示的数据种类
			if (!listView.getTag().equals("110")) {
				if (hundredData.size() == 0) {
					hundredData = getListData("110", null);
				}
				changMessage("110", hundredData);
			}
			break;

		case R.id.btn_thirty:
			// 判断当前显示的数据种类
			if (!listView.getTag().equals("30")) {
				if (thirtyData.size() == 0) {
					thirtyData = getListData("30", null);
				}
				changMessage("30", thirtyData);
			}
			break;
		case R.id.btn_year:
			// 判断当前显示的数据种类
			if (!listView.getTag().equals("365")) {
				if (yearData.size() == 0) {
					yearData = getListData("365", null);
				}
				changMessage("365", yearData);
			}
			break;
		case R.id.btn_quit:
			// 退出应用
			final MyDialog quitDialog = new MyDialog(getActivity(), 180, 120,
					R.layout.dialog_layout, R.style.Theme_myDialog);
			TextView tvMessage = (TextView) quitDialog
					.findViewById(R.id.tv_info_dialog);
			TextView title = (TextView) quitDialog
					.findViewById(R.id.tv_title_dialog);
			title.setText("退出应用");
			tvMessage.setText("确定退出应用？");
			Button sure = (Button) quitDialog
					.findViewById(R.id.btn_sure_dialog);
			Button cancel = (Button) quitDialog
					.findViewById(R.id.btn_cancel_dialog);
			sure.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					quitDialog.dismiss();
					// 退出应用
					ExitApplication.getInstance().exit();
				}
			});
			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					quitDialog.dismiss();
				}
			});
			quitDialog.show();
			break;
		default:
			break;
		}
	}

	public void refreshSum() {
		String sql = null;
		switch ((String) listView.getTag()) {
		case "30":
			sql = "select * from " + TABLE + " where money=30";
			break;
		case "110":
			sql = "select * from " + TABLE + " where money=110";
			break;
		case "365":
			sql = "select * from " + TABLE + " where money=365";
			break;
		default:
			break;
		}
		int sumNumber;
		Cursor cursor = null;
		try {
			cursor = db.getMessage(sql);
			sumNumber = cursor.getCount();
			// 更新总数
			sum.setText("总计：" + sumNumber);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				cursor = null;
			}
		}
	}

	private void changMessage(String tag,
			ArrayList<Map<String, String>> showData) {
		// 点击按钮
		listView.setTag(tag);
		listView.setVisibility(View.GONE);
		loadingHead.setVisibility(View.VISIBLE);
		showList(showData);
		refreshSum();
	}

	private ArrayList<Map<String, String>> getListData(String money, String pin) {
		// 从数据库获取前十五条数据
		ArrayList<Map<String, String>> rData = new ArrayList<Map<String, String>>(); // 待返回的数据源
		String sql = "select * from " + TABLE + " where money=" + money
				+ " order by pin asc";
		Cursor cursor = db.getMessage(sql);
		while (cursor.moveToNext()) {
			if (pin == null) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("pin", String.valueOf(cursor.getInt(cursor
						.getColumnIndex("pin"))));
				map.put("money",
						cursor.getString(cursor.getColumnIndex("money")));
				rData.add(map);
				map = null;
			} else {
				int pin2 = cursor.getInt(cursor.getColumnIndex("pin"));
				if (pin2 > Integer.valueOf(pin).intValue()) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("pin", String.valueOf(pin2));
					map.put("money",
							cursor.getString(cursor.getColumnIndex("money")));
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
		return rData;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 关闭数据库
		db.closeDB();
	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.totalItem = totalItemCount;
		this.lastItem = firstVisibleItem + visibleItemCount;
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int state) {
		if (totalItem == lastItem && state == SCROLL_STATE_IDLE) {
			if (loadingFooter.getVisibility() == View.GONE) {
				// 上拉加载更多
				loadingFooter.setVisibility(View.VISIBLE);
				// 推迟2秒执行，确保新数据已经插入数据库
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						ArrayList<Map<String, String>> addData = null;
						String pin = adapter.getData()
								.get(adapter.getData().size() - 1).get("pin");
						addData = getListData((String) listView.getTag(), pin);
						showList(addData);
					}
				}, 2000);
			}
		}
	}
}
