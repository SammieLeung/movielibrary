package com.firefly.videonameparser.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;

public class StringUtils {


	/**
	 * 检查字符串中是否包含中文字符
	 * @param str
	 * @return
	 */
	public static boolean   checkChina(String str) {  
		String exp="^[\u4E00-\u9FA5|\\！|\\,|\\。|\\（|\\）|\\《|\\》|\\“|\\”|\\？|\\：|\\；|\\【|\\】]$";  
		Pattern pattern=Pattern.compile(exp);  
		for (int i = 0; i < str.length(); i++) {// 遍历字符串每一个字符  
			char c = str.charAt(i);  
			Matcher matcher=pattern.matcher(c + "");  
			if(matcher.matches()) {  
				return true;
			}  
		}  
		return false;  
	}  

	private static String[] ChineseInterpunction = { "“", "”", "‘", "’", "。", "，", "；", "：", "？", "！", "……", "—", "～", "（", "）", "《", "》","【","】" };   
	private static String[] EnglishInterpunction = {"\"", "\"", "'", "'", ".", ",", ";", ":", "?", "!", "…", "-", "~", "(", ")", "<", ">","[","]" };   
	public static String  ChineseToEnglish(String str)   
	{   
		if(str == null || str.length() == 0) return str;
		for (int i = 0; i < ChineseInterpunction.length; i++)   
		{   
			str = str.replaceAll(ChineseInterpunction[i], EnglishInterpunction[i]);
		}  
		return str; 
	}

	public static boolean hasGB2312(String str) {
		for (int i = 0; i < str.length(); i++) {
			String bb = str.substring(i, i + 1);
			// 生成一个Pattern,同时编译一个正则表达式,其中的u4E00("一"的unicode编码)-\u9FA5("龥"的unicode编码)
			boolean cc = java.util.regex.Pattern.matches("[\u4E00-\u9FA5]", bb);
			if(cc)
				return true;
		}
		return false;
	}

	public static String getOnlyGB2312(String str){
		StringBuffer sb=new StringBuffer();
		for(char c:str.toCharArray()){
			if('\u4E00'<=c&&c<='\u9F45'){
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static boolean hasHttpUrl(String str) {
		if(TextUtils.isEmpty(str)) return false;
		//把中文替换为#  
		str = str.replaceAll("[\u4E00-\u9FA5]", "#");  
		System.out.println(str);  
		String url[]=str.split("#");  
		//转换为小写  
		if(url!=null&&url.length>0){  
			for(String tempurl:url){   
				tempurl = tempurl.toLowerCase();  
				String regex = "^((https|http|ftp|rtsp|mms)?://)"    
						+ "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" //ftp的user@    
						+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184    
						+ "|" // 允许IP和DOMAIN（域名）   
						+ "([0-9a-z_!~*'()-]+\\.)*" // 域名- www.    
						+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // 二级域名    
						+ "[a-z]{2,6})" // first level domain- .com or .museum    
						+ "(:[0-9]{1,4})?" // 端口- :80    
						+ "((/?)|" // a slash isn't required if there is no file name    
						+ "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";   

				Pattern p = Pattern.compile(regex);  
				Matcher matcher = p.matcher(tempurl);  
				if(matcher.find())
					return true;
			}  
		}  
		return false;
	}




	public static boolean contain(String[] array,String str)
	{
		if(array == null || array.length ==0) return false;
		List<String> list=Arrays.asList(array);
		return list.contains(str);
	}

	public static boolean matchListFind(String[] regexs,String input){

		for (String regex : regexs) {
			if(matchFind(regex,input))
			{
				Log.v("sjfqq","regex:"+regex);
				return true;
			}
		}
		return false;
	}


	public static boolean matchFind(String regex,String input){
		Pattern p = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(input);
		return m.find();
	}

	public static  String[] matcher(String regex,String input) {
		return matcher(regex,Pattern.CASE_INSENSITIVE, input);
	}

	public static  String[] matcher(String regex,int flag,String input) {
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

	public static  String[] matcher2(String regex,String input) {
		return matcher2(regex,Pattern.CASE_INSENSITIVE, input);
	}
	 
	public static  String[] matcher2(String regex,int flag,String input) {
		Pattern pattern = Pattern.compile(regex,flag);
		Matcher matcher = pattern.matcher(input);
		List<String> list = new ArrayList<String>();
		while (matcher.find()) {
			Log.v("sjfqq","matcher.groupCount():"+matcher.groupCount()+","+matcher.group());
			if(matcher.groupCount() > 0)
			{
				list.add(matcher.group(0));
			}
		}

		return list.toArray(new String[0]);
	}
	public static String removeAll(String str, ArrayList<String> removeWords){
		if(TextUtils.isEmpty(str) || removeWords == null || removeWords.size() == 0) return str;
		for (String string : removeWords) {
			str = str.replace( string,"");
		}
		return str;

	}

	public static String deleteSubString(String str,String sub_str){
		if(TextUtils.isEmpty(str) || TextUtils.isEmpty(sub_str)) return str;

		int index = str.indexOf(sub_str);
		if(index > -1)
		{
			//	str.
		}

		return str;

	}
	/**
	 * 转义正则特殊字符 （$()*+.[]?\^{},|）
	 * 
	 * @param keyword
	 * @return
	 */
	public static String escapeExprSpecialWord(String keyword) {
		if (!TextUtils.isEmpty(keyword)) {
			String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
			for (String key : fbsArr) {
				if (keyword.contains(key)) {
					keyword = keyword.replace(key, "\\" + key);
				}
			}
		}
		return keyword;
	}
	public static void debug(String[] matches){
		Log.v("sjfqq","**********************************");
		for (String string : matches) {
			Log.v("sjfqq","debug string："+string);
		}
		Log.v("sjfqq","**********************************");
	}

}
