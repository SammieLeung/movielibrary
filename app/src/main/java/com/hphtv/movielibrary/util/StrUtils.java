package com.hphtv.movielibrary.util;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.UnsupportedEncodingException;
import org.apache.http.NameValuePair;

import com.hphtv.movielibrary.sqlite.bean.scraperBean.Celebrity;

import android.text.TextUtils;

public class StrUtils {

	//utf-8转码
	public static String encode(String paramDecStr) {
		//return URLEncoder.encode(paramDecStr);
		if ( paramDecStr == null || paramDecStr.equals("") ) {
			return "";
		}
		try {
			return URLEncoder.encode(paramDecStr, "UTF-8").replace("+", "%20")
					.replace("*", "%2A").replace("%7E", "~")
					.replace("#", "%23");
		} catch (UnsupportedEncodingException e) {
			//throw new RuntimeException(e.getMessage(), e);
			return paramDecStr;
		}
	}

	//得到String格式的参数字符串
	public static String getStringParams(List<NameValuePair> paramsList) {
		StringBuffer sb = new StringBuffer();
		for(NameValuePair param : paramsList){
			sb.append('&');
			sb.append(param.getName());
			sb.append('=');
			sb.append(encode(param.getValue()));
		}
		//去掉第一个&号
		return sb.toString().substring(1);
	}
	
	public static String formathScraperUrl(String url)
	{
		if(TextUtils.isEmpty(url))return url;
		
		return url.replaceAll("&amp;", "&").replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">")
				.replaceAll("&quot;", "\"").replaceAll("&apos;", "'");
	}
	public static String arrayToString(Celebrity[] array)
	{
		if(array == null || array.length == 0)
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		int length = array.length;
		for (int i = 0; i < 2&&i<length; i++) {
			sb.append(array[i].getName());
			if(i < length -1)sb.append("  ");
		}
		
		return sb.toString();
	}
	
	public static String arrayToString(String[] array)
	{
		if(array == null || array.length == 0)
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		int length = array.length;
		for (int i = 0; i < length; i++) {
			sb.append(array[i]);
			if(i < length -1)sb.append(" ");
		}
		
		return sb.toString();
	}
	
	public  static String[] matcher(String regex,String input) {
		return matcher(regex,Pattern.CASE_INSENSITIVE, input);
	}
	public  static String[] matcher(String regex,int flag,String input) {
		Pattern pattern = Pattern.compile(regex,flag);
		Matcher matcher = pattern.matcher(input);
		List<String> list = new ArrayList<String>();
		while (matcher.find()) {
			for (int i = 0; i <= matcher.groupCount(); i++) {
				list.add(matcher.group(i));
			}
		}
		return list.toArray(new String[0]);
	}
	
	public static boolean isNull(String data){
		return data == null || data.equals("") || data.equals("null");
	}

	public static float byteToGigabyte(long n){
		DecimalFormat df=new DecimalFormat("0.00");
		return Float.valueOf(df.format(n/(float)(1024*1024*1024)));
	}

	public static boolean contains(String[] stringArray, String source) {
		// 转换为list
		List<String> tempList = Arrays.asList(stringArray);

		// 利用list的包含方法,进行判断
		if(tempList.contains(source))
		{
			return true;
		} else {
			return false;
		}
	}

	public static String[] concat(String[] a, String[] b) {
		String[] c= new String[a.length+b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
}
