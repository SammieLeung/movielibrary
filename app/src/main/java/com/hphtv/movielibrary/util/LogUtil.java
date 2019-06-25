package com.hphtv.movielibrary.util;

import android.util.Log;

public class LogUtil {

	public static boolean VISIABLE=true;
	    // 使用Log来显示调试信息,因为log在实现上每个message有4k字符长度限制  
	    // 所以这里使用自己分节的方式来输出足够长度的message  
	public static void v(String tag,String str) {
		if(VISIABLE){
			str = str.trim();
			int index = 0;
			int maxLength = 4000;
			String sub;
			while (index < str.length()) {
				// java的字符不允许指定超过总的长度end
				if (str.length() <= index + maxLength) {
					sub = str.substring(index);
				} else {
					sub = str.substring(index, index+maxLength);
				}

				index += maxLength;
				Log.v(tag, sub.trim());
			}
		}

	}

	public static void d(String tag,String str){
		if(VISIABLE){
			str = str.trim();
			int index = 0;
			int maxLength = 4000;
			String sub;
			while (index < str.length()) {
				// java的字符不允许指定超过总的长度end
				if (str.length() <= index + maxLength) {
					sub = str.substring(index);
				} else {
					sub = str.substring(index, index+maxLength);
				}

				index += maxLength;
				Log.d(tag, sub.trim());
			}
		}
	}

	public static void e(String tag,String str){
		String[] infos = getAutoJumpLogInfos();
		if(VISIABLE){
			str = str.trim();
			int index = 0;
			int maxLength = 4000;
			String sub;
			Log.e(infos[0],infos[1]+infos[2]);
			while (index < str.length()) {
				// java的字符不允许指定超过总的长度end
				if (str.length() <= index + maxLength) {
					sub = str.substring(index);
				} else {
					sub = str.substring(index, index+maxLength);
				}

				index += maxLength;
				Log.e(tag, sub.trim());
			}
		}
	}

	/**
	 * 获取打印信息所在方法名，行号等信息
	 * @return
	 */
	private static String[] getAutoJumpLogInfos() {
		String[] infos = new String[]{"", "", ""};
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		if (elements.length < 5) {
			Log.e("MyLogger", "Stack is too shallow!!!");
			return infos;
		} else {
			infos[0] = elements[4].getClassName().substring(
					elements[4].getClassName().lastIndexOf(".") + 1);
			infos[1] = elements[4].getMethodName() + "()";
			infos[2] = " at (" + elements[4].getClassName() + ".java:"
					+ elements[4].getLineNumber() + ")";
			return infos;
		}

	}
}
