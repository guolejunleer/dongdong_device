package com.dongdong.phone;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpUtils {
	private String TAG = "HttpUtils";
	private String m_apikey = "P4VenrKw5aam6sREotky";
	private String m_secretkey = "1bvNOGryv3IPq0fwICBH";
	private String m_baseurl = "http://wuye.dd121.com/dd/wuye_api/2.0/";

	public HttpUtils() {
	}

	/*
	 * Function : 获取云之讯账号信息 Return :
	 */
	public Map<String, String> getUcpaasInfo(String deviceid) {
		Calendar calendar = Calendar.getInstance();
		Map<String, String> params = new HashMap<String, String>();
		params.put("apikey", "P4VenrKw5aam6sREotky");
		params.put("timestamp", "" + calendar.getTimeInMillis() / 1000);
		params.put("id", "0");
		params.put("method", "getUcpaasInfo");
		// params.put("sn", "wuye123");
		params.put("deviceid", deviceid);
		String jsonstr = submitPostData(m_baseurl, params, "utf-8");
		return ParseJson(jsonstr);
	}

	/*
	 * Function : 获取云之讯子账号信息 Return :
	 */
	public Map<String, String> getUcpaasClientInfo(String sid, String token,
			String appid) {
		Calendar calendar = Calendar.getInstance();
		Map<String, String> params = new HashMap<String, String>();
		params.put("apikey", "P4VenrKw5aam6sREotky");
		params.put("timestamp", "" + calendar.getTimeInMillis() / 1000);
		params.put("id", "0");
		params.put("method", "getUcpaasClientInfo");
		params.put("sid", sid);
		params.put("token", token);
		params.put("appid", appid);
		String jsonstr = submitPostData(m_baseurl, params, "utf-8");
		return ParseJson(jsonstr);
	}

	/*
	 * Function : 发送Post请求到服务器 Param : params请求体内容，encode编码格式
	 */
	private String submitPostData(String strUrlPath,
			Map<String, String> params, String encode) {
		String sign = genSign(strUrlPath, params, encode);
		params.put("sign", sign);
		byte[] data = getRequestData(params, encode).toString().getBytes();// 获得请求体
		System.out.println(data);
		try {

			// String urlPath = "http://192.168.1.9:80/JJKSms/RecSms.php";
			URL url = new URL(strUrlPath);

			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			httpURLConnection.setConnectTimeout(3000); // 设置连接超时时间
			httpURLConnection.setDoInput(true); // 打开输入流，以便从服务器获取数据
			httpURLConnection.setDoOutput(true); // 打开输出流，以便向服务器提交数据
			httpURLConnection.setRequestMethod("POST"); // 设置以Post方式提交数据
			httpURLConnection.setUseCaches(false); // 使用Post方式不能使用缓存
			// 设置请求体的类型是文本类型
			httpURLConnection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			// 设置请求体的长度
			httpURLConnection.setRequestProperty("Content-Length",
					String.valueOf(data.length));
			// 获得输出流，向服务器写入数据
			OutputStream outputStream = httpURLConnection.getOutputStream();
			outputStream.write(data);

			int response = httpURLConnection.getResponseCode(); // 获得服务器的响应码
			if (response == HttpURLConnection.HTTP_OK) {
				InputStream inptStream = httpURLConnection.getInputStream();
				return dealResponseResult(inptStream); // 处理服务器的响应结果
			}
		} catch (IOException e) {
			// no_room.printStackTrace();
			return "err: " + e.getMessage().toString();
		}
		return "-1";
	}

	/*
	 * Function : 封装请求体信息 Param : params请求体内容，encode编码格式
	 */
	private StringBuffer getRequestData(Map<String, String> params,
			String encode) {

		StringBuffer stringBuffer = new StringBuffer(); // 存储封装好的请求体信息
		try {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				stringBuffer.append(entry.getKey()).append("=")
						.append(URLEncoder.encode(entry.getValue(), encode))
						.append("&");
			}
			stringBuffer.deleteCharAt(stringBuffer.length() - 1); // 删除最后的一个"&"
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringBuffer;
	}

	private String genSign(String url, Map<String, String> params, String encode) {
		String baseStr = "POST" + url;
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("POST").append(url);
		Map<String, String> sortparams = sortMapByKey(params);
		try {
			for (Map.Entry<String, String> entry : sortparams.entrySet()) {
				stringBuffer.append(entry.getKey()).append("=")
						.append(URLEncoder.encode(entry.getValue(), encode));
			}
			stringBuffer.append(m_secretkey);
			return getMd5(URLEncoder.encode(stringBuffer.toString(), encode));
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private String getMd5(String plainText) {
		try {
			StringBuffer buf = new StringBuffer("");
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			// 32位加密
			return buf.toString();
			// 16位的加密
			// return buf.toString().substring(8, 24);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		}

	}

	/*
	 * Function : 处理服务器的响应结果（将输入流转化成字符串） Param : inputStream服务器的响应输入流
	 */
	private String dealResponseResult(InputStream inputStream) {
		String resultData = null; // 存储处理结果
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		int len = 0;
		try {
			while ((len = inputStream.read(data)) != -1) {
				byteArrayOutputStream.write(data, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		resultData = new String(byteArrayOutputStream.toByteArray());
		return resultData;
	}

	class MapKeyComparator implements Comparator<String> {
		@Override
		public int compare(String str1, String str2) {
			return str1.compareTo(str2);
		}
	}

	/**
	 * 使用 Map按key进行排序
	 * 
	 * @param map
	 * @return
	 */
	private Map<String, String> sortMapByKey(Map<String, String> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		Map<String, String> sortMap = new TreeMap<String, String>(
				new MapKeyComparator());
		sortMap.putAll(map);
		return sortMap;
	}

	/**
	 * 解析Json数据
	 * 
	 * @param jsonString
	 *            Json数据字符串
	 */
	private Map<String, String> ParseJson(String jsonString) {
		Map<String, String> map = new HashMap<String, String>();
		if (0 == jsonString.compareTo("")) {
			System.out.println("jsonString is null");
			return map;
		}
		try {
			JSONObject jsonRoot = new JSONObject(jsonString);
			int error_code = jsonRoot.optInt("error_code");
			String error_msg = jsonRoot.optString("error_msg");
			map.put("error_code", "" + error_code);
			map.put("error_msg", error_msg);
			System.out.println("error_code:" + error_code + " error_msg:"
					+ error_msg);

			if (jsonRoot.has("response_params")) {
				String response_params = jsonRoot.optString("response_params");
				System.out.println("response_params:" + response_params);
				JSONObject jsonParams = new JSONObject(response_params);
				if (jsonParams.has("sid")) {
					map.put("sid", jsonParams.optString("sid"));
				}
				if (jsonParams.has("sid")) {
					map.put("token", jsonParams.optString("token"));
				}
				if (jsonParams.has("sid")) {
					map.put("appid", jsonParams.optString("appid"));
				}
				if (jsonParams.has("client")) {
					map.put("client", jsonParams.optString("client"));
				}
				if (jsonParams.has("clientpwd")) {
					map.put("clientpwd", jsonParams.optString("clientpwd"));
				}
			}
			return map;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return map;
	}

}
