package com.weike.adapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FragmentsAdapter extends FragmentPagerAdapter {

	private ArrayList<Fragment> mViewData = null;

	public FragmentsAdapter(FragmentManager fm, ArrayList<Fragment> mViewData) {
		super(fm);
		this.mViewData = mViewData;
	}

	@Override
	public Fragment getItem(int arg0) {
		return mViewData.get(arg0);
	}

	@Override
	public int getCount() {
		return mViewData.size();
	}

}
