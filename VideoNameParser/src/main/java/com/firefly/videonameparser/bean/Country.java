package com.firefly.videonameparser.bean;

import android.util.Log;

import com.firefly.videonameparser.utils.StringUtils;

public class Country {
	public final static String[] COUNTRY_CODE_ES ={"ES","españa"};
	
	public final static String[] COUNTRY_CODE_GB ={"GB","UK"};
	
	public final static String[] COUNTRY_CODE_BR ={"BR","brazilian","bra"};
	
	public final static String[] COUNTRY_CODE_CA ={"CA","québec","quebec","qc"};
	
	public final static String[] COUNTRY_CODE_MX ={"MX","Latinoamérica","latin america"};
	
	
	public String code = null;
	
	public Country(String code) {
		super();
		this.code = code;
	}

	public static Country parser(String seg){
		String[] segSplit = seg.split("\\.| |-|;|_");
		for (String string : segSplit) {
			if(StringUtils.contain(COUNTRY_CODE_ES, string))
			{
				return new Country("ES");
			}else if(StringUtils.contain(COUNTRY_CODE_GB, string))
			{
				return new Country("GB");
			}else if(StringUtils.contain(COUNTRY_CODE_BR, string))
			{
				return new Country("BR");
			}else if(StringUtils.contain(COUNTRY_CODE_CA, string))
			{
				return new Country("CA");
			}else if(StringUtils.contain(COUNTRY_CODE_MX, string))
			{
				return new Country("MX");
			}
		}
		return null;
		
	}

}
