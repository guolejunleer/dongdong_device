package com.dongdong.phone;

public class UIDfineAction {

	public static String ACTION_LOGIN = "com.yzx.login";
	public static String ACTION_DIAL = "com.yzx.dial";
	public static String ACTION_LOGOUT = "com.yzx.logout";
	/*
	 * 
	 * 
	 * 
	 * public static final HashMap<Integer,String> dialState = new
	 * HashMap<Integer,String>(); static{
	 * //dialState.put(UCSCall.CALL_VOIP_ANSWER, "接听电话");
	 * dialState.put(UCSCall.CALL_VOIP_NOT_ENOUGH_BALANCE, "余额不足");
	 * dialState.put(UCSCall.CALL_VOIP_BUSY, "对方正忙");
	 * dialState.put(UCSCall.CALL_VOIP_REFUSAL, "对方拒绝");
	 * dialState.put(UCSCall.CALL_VOIP_NUMBER_ERROR, "被叫号码不在线");
	 * dialState.put(UCSCall.CALL_VOIP_REJECT_ACCOUNT_FROZEN, "被号账户被冻结");
	 * dialState.put(UCSCall.CALL_VOIP_ACCOUNT_FROZEN, "账户被冻结");
	 * dialState.put(UCSCall.CALL_VOIP_ACCOUNT_EXPIRED, "主号账户过期");
	 * dialState.put(UCSCall.CALL_VOIP_CALLYOURSELF, "不能拨打自己绑定号码");
	 * dialState.put(UCSCall.CALL_VOIP_NETWORK_TIMEOUT, "呼叫请求超时");
	 * dialState.put(UCSCall.CALL_VOIP_NOT_ANSWER, "对方无人应答");
	 * dialState.put(UCSCall.CALL_VOIP_TRYING_183, "被叫不在线,转直拨");
	 * dialState.put(UCSCall.CALL_VOIP_RINGING_180, "对方正在响铃");
	 * dialState.put(UCSCall.CALL_VOIP_SESSION_EXPIRATION, "鉴权失败(TCP未认证)");
	 * dialState.put(UCSCall.CALL_VOIP_ERROR, "服务器错误");
	 * dialState.put(UCSCall.HUNGUP_NOT_ANSWER, "被叫方没有应答");
	 * dialState.put(UCSCall.HUNGUP_MYSELF, "自己挂断电话");
	 * dialState.put(UCSCall.HUNGUP_OTHER, "对方挂断电话");
	 * //dialState.put(UCSCall.HUNGUP_WHILE_2G, "2G不能拨打免费、直拨、视频和智能通话");
	 * dialState.put(UCSCall.HUNGUP_REFUSAL, "对方拒绝接听");
	 * dialState.put(UCSCall.HUNGUP_NOT_ENOUGH_BALANCE, "余额不足");
	 * dialState.put(UCSCall.HUNGUP_MYSELF_REFUSAL, "自己拒绝接听");
	 * dialState.put(UCSCall.CALL_VIDEO_DOES_NOT_SUPPORT, "该设备不支持视频通话");
	 * dialState.put(UCSCall.CALL_TRANSFER_DO_NOT_ALLOW_CALLS,
	 * "本机开启呼叫转移时不允许使用回拔"); dialState.put(UCSCall.CALL_FAIL_BLACKLIST,
	 * "呼叫失败（频繁呼叫已被列入黑名单）"); }
	 * 
	 * 
	 * public static String FROM_NUM_KEY = "fromsernum"; public static String
	 * TO_NUM_KEY = "tosernum"; public static String CALL_UID= "call_uid";
	 * public static String CALL_PHONE= "call_phone"; public static String
	 * RESULT_KEY = "result"; public static String REASON_KEY = "reason"; public
	 * static String ACTION_LOGIN = "com.yzx.login"; public static String
	 * ACTION_LOGOUT = "com.yzx.logout"; public static String
	 * ACTION_TCP_LOGIN_RESPONSE = "com.yzx.tcp_login_response"; public static
	 * String ACTION_TCP_LOGIN_CLIENT_RESPONSE =
	 * "com.yzx.tcp_login_client_response"; public static String
	 * ACTION_SEND_FILE_PROGRESS = "com.yzx.send_file"; public static String
	 * ACTION_NETWORK_STATE = "com.yzx.network.state";
	 * 
	 * public static String ACTION_DIAL = "com.yzx.dial"; public static String
	 * ACTION_DIAL_STATE = "com.yzx.dial.state"; public static String
	 * ACTION_DIAL_HANGUP = "com.yzx.dial.hangup"; public static String
	 * ACTION_ANSWER = "com.yzx.answer"; public static String ACTION_CALL_BACK =
	 * "com.yzx.callback"; public static String ACTION_CALL_TIME =
	 * "com.yzx.call_time"; //public static String ACTION_START_TIME =
	 * "com.yzx.start_time"; public static String ACTION =
	 * "com.yzxproject.resetList"; public static String ACTION_MSG =
	 * "com.yzxproject.end_failed"; public static String ACTION_STATUS =
	 * "com.yzxproject.status"; public static String ACTION_ADDPERSON =
	 * "com.yzxproject.conference.addperson"; public static String
	 * ACTION_MEETING_STATES = "com.yzx.meeting.states"; public static String
	 * ACTION_MEETING_ADDPERSONS = "com.yzx.meeting.addpersons";
	 * 
	 * public static final int LOCATION = 10; //自定义消息类型 位置 public static final
	 * int FILE = 20; //自定义消息类型 附件
	 * 
	 * public static void initAction(String packName){
	 * if(!ACTION_LOGIN.startsWith(packName)){ ACTION_LOGIN = packName + "_" +
	 * ACTION_LOGIN; ACTION_LOGOUT = packName + "_" + ACTION_LOGOUT;
	 * ACTION_TCP_LOGIN_RESPONSE = packName + "_" + ACTION_TCP_LOGIN_RESPONSE;
	 * ACTION_TCP_LOGIN_CLIENT_RESPONSE = packName + "_" +
	 * ACTION_TCP_LOGIN_CLIENT_RESPONSE; ACTION_SEND_FILE_PROGRESS = packName +
	 * "_" + ACTION_SEND_FILE_PROGRESS; ACTION_DIAL = packName + "_" +
	 * ACTION_DIAL; ACTION_DIAL_STATE = packName + "_" + ACTION_DIAL_STATE;
	 * ACTION_DIAL_HANGUP = packName + "_" + ACTION_DIAL_HANGUP;
	 * ACTION_NETWORK_STATE = packName + "_" + ACTION_NETWORK_STATE;
	 * ACTION_ANSWER = packName + "_" + ACTION_ANSWER; ACTION_CALL_BACK =
	 * packName + "_" + ACTION_CALL_BACK; ACTION_CALL_TIME = packName + "_" +
	 * ACTION_CALL_TIME; //ACTION_START_TIME = packName + "_" +
	 * ACTION_START_TIME; ACTION = packName + "_" + ACTION; ACTION_MSG =
	 * packName + "_" + ACTION_MSG; ACTION_STATUS = packName + "_" +
	 * ACTION_STATUS; ACTION_ADDPERSON = packName + "_" + ACTION_ADDPERSON;
	 * ACTION_MEETING_STATES = packName + "_" + ACTION_MEETING_STATES;
	 * ACTION_MEETING_ADDPERSONS = packName + "_" + ACTION_MEETING_ADDPERSONS; }
	 * }
	 */
}
