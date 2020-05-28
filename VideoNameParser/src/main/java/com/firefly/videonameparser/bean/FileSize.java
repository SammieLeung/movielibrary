package com.firefly.videonameparser.bean;

import com.firefly.videonameparser.utils.StringUtils;

import android.util.Log;

public class FileSize {
	
	public String size;
	
	
	public static final String MATCH_REGEX="([1-9]\\d*.\\d*|0.\\d*[1-9]\\d*)\\s?(GB|MB)";//[15.6GB]/[1000MB]
	
	
	public FileSize(String size) {
		super();
		this.size = size;
	}


	public static FileSize parser(String str){
		String size = null;		
		if(StringUtils.matchFind(MATCH_REGEX, str))
		{
			String[] dotStampMatch =StringUtils.matcher(MATCH_REGEX, str);

			if(dotStampMatch != null && dotStampMatch.length > 0)
			{
				size = dotStampMatch[0];
			}
	   
		}
		if(size != null){
			return new FileSize(size);
		}
//	    if (str.matches("(.*)1080(p|P)(.*)")) { 
//	    	tag = "1080p";
//	    }else if (str.matches("(.*)720(p|P)(.*)")) { 
//	    	tag = "720p";
//	    }else if (str.matches("(.*)480[p|P](.*)")) { 
//	    	tag = "480p";
//	    }else if(str.matches("(\\d{3,4})?(X|x|:|\\*)?(\\d{3,4})")) //<width>x<height>
//	    {
//	    }
		return null;
		
	}

}
