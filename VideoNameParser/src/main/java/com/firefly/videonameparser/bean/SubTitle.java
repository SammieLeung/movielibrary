package com.firefly.videonameparser.bean;

public class SubTitle {
	
	private static final String[] KEY_WORDS = {"中文","英文","中英","简体","繁体","GB","BIG5","字幕","双语"};
	public static boolean parser(String seq){
		for (String word : KEY_WORDS) {
			if(seq.toUpperCase().indexOf(word) > -1)
				return true;
		}
		
		
		return false;
		
	}
	
}
