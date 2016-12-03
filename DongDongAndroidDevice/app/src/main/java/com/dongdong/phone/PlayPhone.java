//package com.dongdong.phone;
//
//import java.io.IOException;
//import java.util.ArrayList;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.widget.Toast;
//
//import com.dongdong.Prompt.KeyEventDialogManager;
//import com.dongdong.Prompt.CountTimeRunnable;
//import com.dongdong.AppConfig;
//import com.dongdong.data.DeviceApplication;
//import com.jr.door.R;
//import com.jr.gs.JRService;
//import com.reason.UcsReason;
//import com.yzx.api.CallType;
//import com.yzx.api.UCSCall;
//import com.yzx.api.UCSService;
//import com.yzx.listenerInterface.CallStateListener;
//import com.yzx.listenerInterface.ConnectionListener;
//
//import com.dongdong.devservicelib.DSPacket;
//import com.dongdong.devservicelib.UdpClientSocket;
//
//public class PlayPhone implements ConnectionListener, CallStateListener {
//	private Context context = null;
//	private String roomnumber;
//	private AccountMessage accountMessage = null;
//	private static final int DFINE_CALL = 0;
//	private static final int DFINE_ANSWER = 1;
//	private static final int DFINE_TIME = 2;
//	private static final int DFINE_ERROR = 3;
//	private static final int DFINE_SUCC = 4;
//	private static final int DFINE_HANDUP = 5;
//
//	private String phoneNum = "";
//	private KeyEventDialogManager promptDialog;
//	// private boolean sevSucc = false;
//
//	/**
//	 * 拨打电话回应
//	 *
//	 * @param roomnumber
//	 *            房号
//	 * @param phonenumber
//	 *            电话号码
//	 * @param remaincount
//	 *            剩余未拨打电话个数
//	 * @param result
//	 *            3-通话结束 4-呼叫超时 5-通话超时
//	 */
//	private Handler handler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case 0:
//
//				promptDialog.showPhoneToast(context, "对方正在响铃");
//				new PhoneCallSocket(roomnumber, phoneNum, 0, 1).start();
//				break;
//			case 1:
//				CountTimeRunnable.startTalkingOrMonitoring();
//				// Toast.makeText(context, "请通话", Toast.LENGTH_SHORT).show();
//				promptDialog.showNormalDialog(R.mipmap.talking);
//				new PhoneCallSocket(roomnumber, phoneNum, 0, 2).start();
//				break;
//			case 2:
//				// 数字
//				if ((msg.getData().getInt("num") + "").equals("11")) {// #号键开门
//					JRService.JRUnlock();
//					promptDialog.showToast(context, R.mipmap.open_door);
//				}
//				break;
//			case 3:
//				// promptDialog.dismissDialog();
//				System.out
//						.println("PlayPhone.class hanler 3----------overtime");
//				promptDialog.showPhoneToast(context, getErrorMess(msg.getData()
//						.getInt("arg0")));
//				new PhoneCallSocket(roomnumber, phoneNum, 0, 4).start();
//				CountTimeRunnable.timeOuthandUp();
//				DeviceApplication.isYTXPhoneCall = false;
//				hangUp();
//				break;
//			case 4:
//				Toast.makeText(context, "电话服务器连接成功！", Toast.LENGTH_LONG).show();
//				break;
//			case 5:
//				CountTimeRunnable.stopTalkingOrMonitoring();
//				promptDialog.showPhoneToast(context, "对方正在响铃");
//				promptDialog.dismissDialog();
//				System.out.println("java---------------呼叫被hadle释放");
//				DeviceApplication.isYTXPhoneCall = false;
//				hangUp();
//				new PhoneCallSocket(roomnumber, phoneNum, 0, 3).start();
//				break;
//			default:
//				break;
//			}
//
//		}
//	};
//
//	private BroadcastReceiver br = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (intent.getAction().equals(UCSService.ACTION_INIT_SUCCESS)) {
//				System.out.println("kkkkkkkkkk sdk初始化成功");
//				loginServer();
//			} else {
//				// 其他
//			}
//		}
//	};
//
//	public PlayPhone(Context context, KeyEventDialogManager promptDialog) {
//		this.context = context;
//		this.promptDialog = promptDialog;
//
//		// 添加连接监听器
//		UCSService.addConnectionListener(this);
//		// 添加电话监听器
//		UCSCall.addCallStateListener(this);
//		// 初始化SDK
//		UCSService.initAction(context);
//		UCSService.init(context, true);
//
//		IntentFilter ift = new IntentFilter();
//		ift.addAction(UIDfineAction.ACTION_LOGIN);
//		ift.addAction(UIDfineAction.ACTION_DIAL);
//		// ift.addAction(UIDfineAction.ACTION_START_TIME);
//		ift.addAction(UCSService.ACTION_INIT_SUCCESS);
//		ift.addAction("android.intent.action.ACTION_SHUTDOWN"); // 系统关机广播
//		context.registerReceiver(br, ift);
//	}
//
//	public void unRegisterReceiver(Context context) {
//		context.unregisterReceiver(br);
//	}
//
//	public void setMessageObject(AccountMessage accountMessage) {
//		this.accountMessage = accountMessage;
//	}
//
//	public void loginServer() {
//		if (!UCSService.isConnected()) {
//			System.out.println("PlayPhone.class start loginserver    "
//					+ accountMessage.getSid() + "   " + accountMessage.getPwd()
//					+ "  " + accountMessage.getAccountID() + "  "
//					+ accountMessage.getAccountPwd() + "  " + phoneNum);
//			UCSService.connect(accountMessage.getSid(),
//					accountMessage.getPwd(), accountMessage.getAccountID(),
//					accountMessage.getAccountPwd());
//			UCSCall.setSpeakerphone(true);
//		}
//	}
//
//	public void call(String roomnumber, String phonenumber) {
//
//		if (roomnumber.equals("")) {
//			promptDialog.showPhoneToast(context, "房号或者手机号不存在");
//			DeviceApplication.OnStatus = AppConfig.DEVICE_FREE;
//			return;
//		}
//
//		if (!UCSService.isConnected()) {
//			Toast.makeText(context, "电话服务未启动成功！", Toast.LENGTH_SHORT).show();
//			DeviceApplication.OnStatus = AppConfig.DEVICE_FREE;
//			return;
//		}
//
//		this.phoneNum = phonenumber;
//		this.roomnumber = roomnumber;
//		promptDialog.showNormalDialog(R.mipmap.phonecall);
//		DeviceApplication.OnStatus = AppConfig.DEVICE_WORKING;
//		System.out.println("PlayPhone.class call:   " + phonenumber);
//		DeviceApplication.isYTXPhoneCall = true;
//		UCSCall.dial(context, CallType.DIRECT, phoneNum);
//		new PhoneCallSocket(roomnumber, phoneNum, 0, 0).start();
//	}
//
//	public void hangUp() {
//		promptDialog.dismissDialog();
//		UCSCall.hangUp("");
//		DeviceApplication.OnStatus = AppConfig.DEVICE_FREE;//
//		System.out.println("call hangup");
//	}
//
//	@Override
//	public void onAlerting(String callid) {
//		System.out.println("PlayPhone.class onAlerting......." + callid);
//		Message msg = new Message();
//		msg.what = DFINE_CALL;
//		Bundle bundle = new Bundle();
//		msg.setData(bundle);// mes利用Bundle传递数据
//		handler.sendMessage(msg);// 用activity中的handler发送消息
//
//	}
//
//	@Override
//	public void onAnswer(String arg0) {
//		System.out.println("PlayPhone.class onAnswer is hearing...." + arg0);
//		Message msg = new Message();
//		msg.what = DFINE_ANSWER;
//		Bundle bundle = new Bundle();
//		bundle.putString("arg0", arg0);
//		msg.setData(bundle);// mes利用Bundle传递数据
//		handler.sendMessage(msg);// 用activity中的handler发送消息
//
//	}
//
//	@Override
//	public void onCallBackSuccess() {
//	}
//
//	@Override
//	public void onChatRoomIncomingCall(String arg0, String arg1, String arg2,
//			String arg3, String arg4) {
//	}
//
//	@Override
//	public void onChatRoomModeConvert(String arg0) {
//	}
//
//	@Override
//	public void onChatRoomState(String arg0, ArrayList arg1) {
//	}
//
//	@Override
//	public void onConferenceModeConvert(String arg0) {
//	}
//
//	@Override
//	public void onConferenceState(String arg0, ArrayList arg1) {
//	}
//
//	@Override
//	public void onDTMF(int num) {
//		System.out.println("PlayPhone.class onDTMF :" + num);
//		Message msg = new Message();
//		msg.what = DFINE_TIME;
//		Bundle bundle = new Bundle();
//		bundle.putInt("num", num);
//		msg.setData(bundle);// mes利用Bundle传递数据
//		handler.sendMessage(msg);// 用activity中的handler发送消息
//	}
//
//	@Override
//	public void onDialFailed(String callid, UcsReason reason) {
//		System.out.println("PlayPhone.class  onDialFailed: " + reason.getMsg()
//				+ "   " + reason.getReason());
//		Message msg = new Message();
//		msg.what = DFINE_ERROR;
//		Bundle bundle = new Bundle();
//		bundle.putInt("arg0", reason.getReason());
//		msg.setData(bundle);// mes利用Bundle传递数据
//		handler.sendMessage(msg);// 用activity中的handler发送消息
//	}
//
//	@Override
//	public void onHangUp(String callid, UcsReason reason) {
//		Message msg = new Message();
//		msg.what = DFINE_HANDUP;
//		handler.sendMessage(msg);// 用activity中的handler发送消息
//		System.out.println("PlayPhone.class onHangUp 子线程呼叫被释放"
//				+ reason.getMsg() + "   " + reason.getReason());
//	}
//
//	@Override
//	public void onIncomingCall(String arg0, String arg1, String arg2,
//			String arg3, String arg4) {
//	}
//
//	@Override
//	public void onNetworkState(int arg0) {
//	}
//
//	// ////////////////
//	@Override
//	public void onConnectionFailed(UcsReason arg0) {
//		// 登陆失败
//		System.out.println("PlayPhone.class onConnectionFailed:  "
//				+ arg0.getMsg() + "  " + arg0.getReason());
//
//	}
//
//	@Override
//	public void onConnectionSuccessful() {
//		// 登陆成功
//		Message msg = new Message();
//		msg.what = DFINE_SUCC;
//		Bundle bundle = new Bundle();
//		msg.setData(bundle);// mes利用Bundle传递数据
//		handler.sendMessage(msg);// 用activity中的handler发送消息
//
//	}
//
//	private class PhoneCallSocket extends Thread {
//		String roomnumber = "";
//		String phonenumber = "";
//		int remaincount;
//		int result;
//
//		public PhoneCallSocket(String roomnumber, String phonenumber,
//				int remaincount, int result) {
//			this.roomnumber = roomnumber;
//			this.phonenumber = phonenumber;
//			this.remaincount = remaincount;
//			this.result = result;
//
//		}
//
//		@Override
//		public void run() {
//			UdpClientSocket client = null;
//			try {
//				client = new UdpClientSocket();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			String serverHost = AppConfig.SERVER_HOST_IP;
//			int serverPort = 45611;
//			try {
//				DSPacket packet = new DSPacket();
//				byte[] callpkt = packet.phoneCallResult(0, roomnumber,
//						phonenumber, result);
//				if (callpkt == null) {
//					return;
//				}
//				client.send(serverHost, serverPort, callpkt, callpkt.length);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	public String getErrorMess(int error) {
//
//		if (error == 300210) {
//			return "平台服务器错误(错误码" + error + ")";
//		} else if (error == 300211) {
//			return "余额不足(错误码" + error + ")";
//		} else if (error == 300212) {
//			return "对方正忙(错误码" + error + ")";
//		} else if (error == 300213) {
//			return "对方拒绝接听(错误码" + error + ")";
//		} else if (error == 300214) {
//			return "该用户不在线(错误码" + error + ")";
//		} else if (error == 300215) {
//			return "被叫号码错误(错误码" + error + ")";
//		} else if (error == 300216) {
//			return "被叫号码冻结(错误码" + error + ")";
//		} else if (error == 300217) {
//			return "主叫号码冻结(错误码" + error + ")";
//		} else if (error == 300218) {
//			return "主叫账号过期(错误码" + error + ")";
//		} else if (error == 300220) {
//			return "呼叫请求超时(错误码" + error + ")";
//		} else if (error == 300221) {
//			return "对方无人应答(错误码" + error + ")";
//		} else if (error == 300223) {
//			return "鉴权失败(错误码" + error + ")";
//		} else {
//			return "未知错误(错误码" + error + ")";
//		}
//
//	}
//}
