package com.dongdong.phone;

public class AccountMessage {
	//应用程序ID
	private String appid = "0";
	private String sid = "0";
	private String pwd = "0";

	//开发者帐号ID
	private String accountID = "0";
	private String accountPwd = "0";

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getAccountID() {
		return accountID;
	}

	public void setAccountID(String accountID) {
		this.accountID = accountID;
	}

	public String getAccountPwd() {
		return accountPwd;
	}

	public void setAccountPwd(String accountPwd) {
		this.accountPwd = accountPwd;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public boolean getEffect() {

		if (sid.equals("0") || pwd.equals("0") || accountID.equals("0")
				|| accountPwd.equals("0")) {
			return false;
		}

		if (sid.equals("") || pwd.equals("") || accountID.equals("")
				|| accountPwd.equals("")) {
			return false;
		}
		return true;

	}

}
