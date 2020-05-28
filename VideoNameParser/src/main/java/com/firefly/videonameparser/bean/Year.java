package com.firefly.videonameparser.bean;

import android.util.Log;

import com.firefly.videonameparser.utils.StringUtils;

public class Year {
	private final static String MATCH_YEAR_REGEX = "(19[0-9][0-9]|20[0-1][0-9])";//匹配４位数字,范围为1900-2099
	public static int  parser(String seg){
		String[] dotStampMatch =StringUtils.matcher(MATCH_YEAR_REGEX, seg);
		if(dotStampMatch != null && dotStampMatch.length > 1)
		{
			return Integer.parseInt(dotStampMatch[1]);
		}
		
		return 0;
		
		
	}


}
