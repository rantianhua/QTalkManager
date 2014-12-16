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

public class RecordListViewAdapter extends BaseAdapter {

	private static ArrayList<Map<String, String>> mData; // 数据源，每个item中的数据
	private Context context;
	private static RecordListViewAdapter thisAdapter;
	

	public RecordListViewAdapter(ArrayList<Map<String, String>> mData,
			Context context) {
		RecordListViewAdapter.mData = mData;
		this.context = context;
		thisAdapter = this;
	}


	public void refreshData(ArrayList<Map<String, String>> mData) {
		RecordListViewAdapter.mData = mData;
		this.notifyDataSetChanged(); // 更新数据
	}

	public ArrayList<Map<String, String>> getData() {
		return RecordListViewAdapter.mData;
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
			hodler.countDate = (TextView) convertView
					.findViewById(R.id.item4_firstContent);
			hodler.thirdtyCard = (TextView) convertView
					.findViewById(R.id.item4_secondContent);
			hodler.hundredCard = (TextView) convertView
					.findViewById(R.id.item4_thirdContent);
			hodler.yearCard = (TextView) convertView
					.findViewById(R.id.item4_forthContent);
			convertView.setTag(hodler);
		} else {
			hodler = (ViewHolder) convertView.getTag();
		}
		
		hodler.countDate.setText((CharSequence) mData.get(arg0).get("time"));
		hodler.countDate.setTextSize(12);
		hodler.thirdtyCard.setText(mData.get(arg0).get("thirty_sum"));
		hodler.hundredCard.setText(mData.get(arg0).get("hundred_sum"));
		hodler.yearCard.setText(mData.get(arg0).get("year_sum"));
		// 为每个listView设置高度
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 70);
		convertView.setLayoutParams(lp);

		return convertView;
	}

	static class ViewHolder {
		TextView countDate, thirdtyCard, hundredCard,yearCard;
	}

	public static RecordListViewAdapter getInstance() {
		
		return thisAdapter;
	}
}
