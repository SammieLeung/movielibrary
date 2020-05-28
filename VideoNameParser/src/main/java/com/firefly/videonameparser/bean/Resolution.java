package com.firefly.videonameparser.bean;

import com.firefly.videonameparser.utils.StringUtils;

import android.util.Log;

public class Resolution {
	
	public String tag;
	
	
//	  Resolution of video.
//
//	  - ``1280x720``, ``360i``, ``360p``, ``368p``, ``480i``, ``480p``, ``576i``, ``576p``, ``720p``, ``900i``,
//	    ``900p``, ``1080i``, ``1080p``, ``1440p``, ``2160p``, ``4320p``
//     <width>x<height> 或是　<height>p|P|i
	public static final String MATCH_FORMAT1="\\d{3,4}\\s?[p|i]";//"\\d{3,4}(p|P|i|I)";//"(^|((.*)[^\\d])(\\d{3,4})(p|P|i|I)(.*)";// "(.*)[^\\d]+(\\d{3,4})(p|P|i|I)(.*)";;//"(\\d{3,4})(p|P|i|I)";
	public static final String MATCH_FORMAT2 = "(\\d{3,4})\\s?[X|:|\\*]\\s?(\\d{3,4})";//"((\\d{3,4})?(X|x|:)?(\\d{3,4}))";
	
	
	public Resolution(String tag) {
		super();
		this.tag = tag;
	}


	public static Resolution parser(String str){
		String tag = null;		
		if(StringUtils.matchFind(MATCH_FORMAT1, str))
		{
			String[] dotStampMatch =StringUtils.matcher(MATCH_FORMAT1, str);
			if(dotStampMatch != null && dotStampMatch.length > 0)
			{
				tag = dotStampMatch[0].toUpperCase();
			}
	   
		}else if(StringUtils.matchFind(MATCH_FORMAT2, str)) //<width>x<height>
		{
			String[] dotStampMatch =StringUtils.matcher(MATCH_FORMAT2, str);
			if(dotStampMatch != null && dotStampMatch.length >= 3)
			{
				//String width = dotStampMatch[1];
				String height = dotStampMatch[2];
				tag = height+"P";
			}
		 }
		if(tag != null){
			return new Resolution(tag);
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
