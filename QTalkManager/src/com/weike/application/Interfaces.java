package com.weike.application;

public class Interfaces {

	public static String url = "http://qtalkmanager.jd-app.com/index.php/api/client?action=";

	public static String getLoginUrl(String account, String pass) {
		return new String(url + "login" + "&account=" + account + "&password="
				+ pass);
	}

	public static String getWaitSellUrl(String account, String money, int n,
			int size) {
		return new String(url + "phonecard_of_salesman" + "&account=" + account
				+ "&money=" + money + "&sold=false&order=desc" + "&page=" + n
				+ "&page_size=" + size);
	}

	public static String getSelledUrl(String account, String money, int n,
			int size) {
		return new String(url + "phonecard_of_salesman" + "&account=" + account
				+ "&money=" + money + "&sold=true&order=desc" + "&page=" + n
				+ "&page_size=" + size);
	}

	public static String getCustomerUrl(String account, int n, int size) {

		return new String(url + "customer_list_with_alipay" + "&account="
				+ account + "&page=" + n + "&page_size=" + size);
	}

	public static String getRecordOrCommUrl(String account, int n, int size) {

		return new String(url + "cash_log" + "&account=" + account + "&page="
				+ n + "&page_size=" + size);
	}

	public static String getModifyUrl(String account, String pin, boolean sold) {

		return new String(url + "change_sell_status" + "&account=" + account + "&pin="
				+ pin + "&sold=" + sold);
	}
}
