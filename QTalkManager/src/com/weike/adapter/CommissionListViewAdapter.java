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

public class CommissionListViewAdapter extends BaseAdapter {

	private static ArrayList<Map<String, String>> mData; // 数据源，每个item中的数据
	private Context context;
	public static CommissionListViewAdapter thisAdapter = null;

	public CommissionListViewAdapter(ArrayList<Map<String, String>> mData,
			Context context) {
		CommissionListViewAdapter.mData = mData;
		this.context = context;
		thisAdapter = this;
	}

	public static CommissionListViewAdapter getInstance() {
		return thisAdapter;
	}

	public void refreshData(ArrayList<Map<String, String>> mData) {
		CommissionListViewAdapter.mData = mData;
		this.notifyDataSetChanged(); // 更新数据
	}

	public ArrayList<Map<String, String>> getData() {
		return CommissionListViewAdapter.mData;
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
			convertView = LayoutInflater.from(context).inflate(R.layout.item_3,
					null);
			hodler = new ViewHolder();
			hodler.countsDate = (TextView) convertView
					.findViewById(R.id.item3_firstContent);
			hodler.preMoney = (TextView) convertView
					.findViewById(R.id.item3_secondContent);
			hodler.commMoney = (TextView) convertView
					.findViewById(R.id.item3_thirdContent);
			convertView.setTag(hodler);
		} else {
			hodler = (ViewHolder) convertView.getTag();
		}
		hodler.countsDate.setText((CharSequence) mData.get(arg0).get("time"));
		hodler.countsDate.setTextSize(12);
		hodler.preMoney.setText((CharSequence) mData.get(arg0).get("real_money"));
		hodler.commMoney.setText((CharSequence) mData.get(arg0).get("commi_money"));

		// 为每个listView设置高度
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 70);
		convertView.setLayoutParams(lp);
		return convertView;
	}

	static class ViewHolder {
		TextView countsDate, preMoney,commMoney;
	}
}
