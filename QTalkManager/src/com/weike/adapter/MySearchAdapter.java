package com.weike.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filterable;

public class MySearchAdapter extends ArrayAdapter<String> implements Filterable{

	public MySearchAdapter(Context context, int resource,
			int textViewResourceId, String[] objects) {
		super(context, resource, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}
	
	

}
