package com.firefly.videonameparser.bean;

import android.text.TextUtils;

import com.firefly.videonameparser.utils.StringUtils;

public class Episode {
		public int season = 0;
		public int episode = 0;
		public Episode(int season, int episode) {
			super();
			this.season = season;
			this.episode = episode;
		}
		
		public static Episode parser(String str){
			if(TextUtils.isEmpty(str)) return null;
			if(StringUtils.matchFind("^\\d{1,3}$",str))//[HYSUB]ONE PUNCH MAN[10][GB_MP4][1280X720].mp4 
			{
				String[] dotStampMatch =StringUtils.matcher("^\\d{1,3}$", str);
				if(dotStampMatch != null && dotStampMatch.length > 0)
				{
					return new Episode(1,  Integer.parseInt(dotStampMatch[0]));
				}
			}else{
				
			}
			return null;
			
		}
		
}
