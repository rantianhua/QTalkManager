package com.weike.adapter;

import java.util.ArrayList;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weike.qtalkmanager.R;

public class CustomerListViewAdapter extends BaseAdapter {

	private static ArrayList<Map<String, String>> mData; // 数据源，每个item中的数据
	private Context context;
	private static CustomerListViewAdapter thisAdapter;

	public static CustomerListViewAdapter getInstance() {
		return thisAdapter;
	}

	public CustomerListViewAdapter(ArrayList<Map<String, String>> mData,
			Context context) {
		CustomerListViewAdapter.mData = mData;
		this.context = context;
		thisAdapter = this;
	}

	public void refreshData(ArrayList<Map<String, String>> mData) {
		CustomerListViewAdapter.mData = mData;
		this.notifyDataSetChanged(); // 更新数据
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
			convertView = LayoutInflater.from(context).inflate(R.layout.item_2,
					null);
			hodler = new ViewHolder();
			hodler.userName = (TextView) convertView
					.findViewById(R.id.item2_firstContent);
			hodler.counts = (TextView) convertView
					.findViewById(R.id.item2_secondContent);
			convertView.setTag(hodler);
		} else {
			hodler = (ViewHolder) convertView.getTag();
		}
		String name = (String) mData.get(arg0).get("customeraccount");
		try {
			name = name.substring(0, 7) + "****" + name.substring(11);
			hodler.userName.setText(name);
		} catch (Exception e) {
			hodler.userName.setText(mData.get(arg0).get("customeraccount"));
		}

		hodler.counts.setText((CharSequence) mData.get(arg0).get("money"));

		// 为每个listView设置高度
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 70);
		convertView.setLayoutParams(lp);

		return convertView;
	}

	static class ViewHolder {
		TextView userName, counts;
	}

	public ArrayList<Map<String, String>> getData() {
		return CustomerListViewAdapter.mData;
	}
}
