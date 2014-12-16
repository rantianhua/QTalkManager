package com.weike.qtalkmanager;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jauker.widget.BadgeView;
import com.weike.application.ExitApplication;
import com.weike.fragment.TabCommission;
import com.weike.fragment.TabCustomer;
import com.weike.fragment.TabRecord;
import com.weike.fragment.TabSelled;
import com.weike.fragment.TabWaitSell;

public class MainActivity extends FragmentActivity implements OnClickListener {

	private ViewPager myViewPager = null;
	private FragmentPagerAdapter pagerAdapter = null;
	private ArrayList<Fragment> mViewData = null; // fragment集合

	private TextView tv_wait, tv_selled, tv_customer, tv_record, tv_commission;

	// 屏幕1/5宽度
	private int scrollbar = 0;
	private ImageView tabline = null;
	private int currentpage = 0; // 表示当前页的页数

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// setContentView(R.layout.activity_main);
		setContentView(R.layout.activity_main);
		ExitApplication.getInstance().addActivity(this);
		// 初始化滑动指示线
		initTabline();
		// 初始化View
		initView();
	}

	@SuppressLint("NewApi")
	private void initTabline() {
		tabline = (ImageView) findViewById(R.id.id_iv_tabline);
		// 得到屏幕的宽度
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		scrollbar = metrics.widthPixels / 5;
		android.view.ViewGroup.LayoutParams lp = tabline.getLayoutParams();
		lp.width = scrollbar;
		tabline.setLayoutParams(lp);
	}

	// 初始化控件
	private void initView() {
		myViewPager = (ViewPager) findViewById(R.id.id_viewpager);
		tv_wait = (TextView) findViewById(R.id.buttom_tv_waitsell);
		tv_selled = (TextView) findViewById(R.id.buttom_tv_selled);
		tv_customer = (TextView) findViewById(R.id.buttom_tv_customer);
		tv_record = (TextView) findViewById(R.id.buttom_tv_record);
		tv_commission = (TextView) findViewById(R.id.buttom_tv_commission);
		tv_wait.setOnClickListener(this);
		tv_selled.setOnClickListener(this);
		tv_customer.setOnClickListener(this);
		tv_record.setOnClickListener(this);
		tv_commission.setOnClickListener(this);

		// ll_waitsell = (LinearLayout) findViewById(R.id.buttom_ll_waitsell);

		mViewData = new ArrayList<Fragment>();
		// 得到Fragment对象
		TabWaitSell waitSell = new TabWaitSell();
		TabSelled selled = new TabSelled();
		TabRecord record = new TabRecord();
		TabCustomer customer = new TabCustomer();
		TabCommission commission = new TabCommission();
		mViewData.add(waitSell);
		mViewData.add(selled);
		mViewData.add(customer);
		mViewData.add(record);
		mViewData.add(commission);
		pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

			@Override
			public int getCount() {
				return mViewData.size();
			}

			@Override
			public Fragment getItem(int pos) {
				return mViewData.get(pos);
			}

		};
		myViewPager.setAdapter(pagerAdapter);

		// 为ViewPager设置事件
		myViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int pos) {
				resetTextView();
				switch (pos) {
				case 0:
					tv_wait.setTextColor(Color.parseColor("#008000"));
					hideBadgeView(TabSelled.getInstance().bvButtom);
					break;
				case 1:
					tv_selled.setTextColor(Color.parseColor("#008000"));
					hideBadgeView(TabWaitSell.getInstance().bvButtom);
					break;
				case 2:
					tv_customer.setTextColor(Color.parseColor("#008000"));
					break;
				case 3:
					tv_record.setTextColor(Color.parseColor("#008000"));
					break;
				case 4:
					tv_commission.setTextColor(Color.parseColor("#008000"));
					break;
				default:

					break;
				}
				currentpage = pos;
			}

			@Override
			public void onPageScrolled(int pos, float posoffset, int posffsetpx) {
				LinearLayout.LayoutParams layoutParams = (android.widget.LinearLayout.LayoutParams) tabline
						.getLayoutParams();
				if (currentpage == 0 && pos == 0) {
					// 0-〉1
					layoutParams.leftMargin = (int) (currentpage * scrollbar + posoffset
							* scrollbar);
				} else if (currentpage == 1 && pos == 0) {
					// 1-〉0
					layoutParams.leftMargin = (int) (currentpage * scrollbar + (posoffset - 1)
							* scrollbar);
				} else if (currentpage == 1 && pos == 1) {
					// 1-〉2
					layoutParams.leftMargin = (int) (currentpage * scrollbar + posoffset
							* scrollbar);
				} else if (currentpage == 2 && pos == 1) {
					// 2-〉1
					layoutParams.leftMargin = (int) (currentpage * scrollbar + (posoffset - 1)
							* scrollbar);
				}
				if (currentpage == 2 && pos == 2) {
					// 2-〉3
					layoutParams.leftMargin = (int) (currentpage * scrollbar + posoffset
							* scrollbar);
				} else if (currentpage == 3 && pos == 2) {
					// 3-〉2
					layoutParams.leftMargin = (int) (currentpage * scrollbar + (posoffset - 1)
							* scrollbar);
				} else if (currentpage == 3 && pos == 3) {
					// 3-〉4
					layoutParams.leftMargin = (int) (currentpage * scrollbar + posoffset
							* scrollbar);
				}
				if (currentpage == 4 && pos == 3) {
					// 4-〉3
					layoutParams.leftMargin = (int) (currentpage * scrollbar + (posoffset - 1)
							* scrollbar);
				} else if (currentpage == 4 && pos == 4) {
					// 4-〉5
					layoutParams.leftMargin = (int) (currentpage * scrollbar + posoffset
							* scrollbar);
				} else if (currentpage == 5 && pos == 4) {
					// 5-〉4
					layoutParams.leftMargin = (int) (currentpage * scrollbar + (posoffset - 1)
							* scrollbar);
				}
				tabline.setLayoutParams(layoutParams);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	// 把字体颜色全部重新设置成黑色
	protected void resetTextView() {
		tv_wait.setTextColor(Color.WHITE);
		tv_selled.setTextColor(Color.WHITE);
		tv_customer.setTextColor(Color.WHITE);
		tv_record.setTextColor(Color.WHITE);
		tv_commission.setTextColor(Color.WHITE);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.buttom_tv_waitsell:
			resetTextView();
			tv_wait.setTextColor(Color.parseColor("#008000"));
			myViewPager.setCurrentItem(0, true);
			hideBadgeView(TabSelled.getInstance().bvButtom);
			break;

		case R.id.buttom_tv_selled:
			resetTextView();
			tv_selled.setTextColor(Color.parseColor("#008000"));
			myViewPager.setCurrentItem(1, true);
			hideBadgeView(TabWaitSell.getInstance().bvButtom);
			break;
		case R.id.buttom_tv_customer:
			resetTextView();
			tv_customer.setTextColor(Color.parseColor("#008000"));
			myViewPager.setCurrentItem(2, true);
			break;
		case R.id.buttom_tv_record:
			resetTextView();
			tv_record.setTextColor(Color.parseColor("#008000"));
			myViewPager.setCurrentItem(3, true);
			break;
		case R.id.buttom_tv_commission:
			resetTextView();
			tv_commission.setTextColor(Color.parseColor("#008000"));
			myViewPager.setCurrentItem(4, true);
			break;
		default:

			break;
		}
	}

	// 将BageView消失
	private void hideBadgeView(BadgeView view) {
		if (view != null && view.getVisibility() != View.GONE) {
			view.setBadgeCount(0);
			view.setVisibility(View.GONE);
		}
	}

	// 按返回键时让程序在后台运行
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.moveTaskToBack(true);
		}
		return true;
	}
}
