package com.firefly.videonameparser.bean;

import com.firefly.videonameparser.utils.StringUtils;

import android.text.TextUtils;
import android.util.Log;

public class Source {
	
	String name;
	
	public Source(String name) {
		super();
		this.name = name;
	}

	public static final String RIP_PREFIX =  "(Rip)-?";
    public static final String RIP_SUFFIX =  "-?(Rip)";
    public static final String RIP_OPTIONAL_SUFFIX = "(?:" + RIP_SUFFIX + ")?";
	private  static String[] build_source_pattern(String[] regexs,String prefix,String  suffix){
		String[] build_regexs = new String[regexs.length];
		String format =  (TextUtils.isEmpty(prefix)?"":prefix)+ "(%s)" + (TextUtils.isEmpty(suffix)?"":suffix);
		for(int i=0;i<regexs.length;i++){
			String regex = String.format(format, regexs[i]);
			build_regexs[i] = regex;
			Log.v("sjfqq","regex:"+regex );
		}
		
		return build_regexs;
		
	} 
	
	public static final String VHS_NAME = "VHS";
	public final static  String[] VHS_REGEX = build_source_pattern(new String[]{"VHS"},null,RIP_OPTIONAL_SUFFIX);

	public static final String CAM_NAME = "Camera";
	public final static  String[] CAM_REGEX = build_source_pattern(new String[]{"CAM"},null,RIP_OPTIONAL_SUFFIX);
	
	public static final String HD_CAM_NAME = "HD Camera";
	public final static  String[] HD_CAM_REGEX = build_source_pattern(new String[]{"HD-?CAM"},null,RIP_OPTIONAL_SUFFIX);
	
	public static final String TS_NAME = "TS";
	public final static  String[] TS_REGEX = build_source_pattern(new String[]{"TELESYNC", "TS"},null,RIP_OPTIONAL_SUFFIX);
	
	public static final String HD_TS_NAME = "HD TS";
	public final static  String[] HD_TS_REGEX = build_source_pattern(new String[]{"HD-?TELESYNC", "HD-?TS"},null,RIP_OPTIONAL_SUFFIX);
	
	public static final String WP_NAME = "Workprint";//WORKPRITN (WP)是从未完成的电影拷贝转制而成，可能会缺失镜头和音乐。质量可能从最好到很差
	public final static  String[] WP_REGEX = build_source_pattern(new String[]{"WORKPRINT", "WP"},null,null);
	
	public static final String TC_NAME = "TC";
	public final static  String[] TC_REGEX = build_source_pattern(new String[]{"TELECINE", "TC"},null,RIP_OPTIONAL_SUFFIX);
	
	public static final String HD_TC_NAME = "HD TC";
	public final static  String[] HD_TC_REGEX = build_source_pattern(new String[]{"HD-?TELECINE", "HD-?TC"},null,RIP_OPTIONAL_SUFFIX);
	
	public static final String PPV_NAME = "Pay-per-view";	
	public final static  String[] PPV_REGEX = build_source_pattern(new String[]{"PPV"},null,RIP_OPTIONAL_SUFFIX);

	public static final String TV1_NAME = "TV";	
	public final static  String[] TV1_REGEX = build_source_pattern(new String[]{"SD-?TV"},null,RIP_OPTIONAL_SUFFIX);

	public static final String TV2_NAME = "TV";	
	public final static  String[] TV2_REGEX = build_source_pattern(new String[]{"TV"},null,RIP_SUFFIX);

	public static final String TV3_NAME = "TV";	
	public final static  String[] TV3_REGEX = build_source_pattern(new String[]{"TV", "SD-?TV"},RIP_PREFIX,null);

	public static final String TV4_NAME = "TV";	
	public final static  String[] TV4_REGEX = build_source_pattern(new String[]{"TV-?(?=Dub)"},null,null);

	/***
	 * Digital TV
	 */
	public static final String DVB_NAME = "DVB";
	public final static  String[] DVB_REGEX = build_source_pattern(new String[]{"DVB", "PD-?TV"},null,RIP_OPTIONAL_SUFFIX);

	public static final String DVD_NAME = "DVD";	
	public final static  String[] DVD_REGEX = build_source_pattern(new String[]{"DVD"},null,RIP_OPTIONAL_SUFFIX);

	public static final String DM_NAME = "Digital Master";	
	public final static  String[] DM_REGEX = build_source_pattern(new String[]{"DM"},null,RIP_OPTIONAL_SUFFIX);

	public static final String DVD2_NAME = "DVD";	
	public final static  String[] DVD2_REGEX = build_source_pattern(new String[]{"VIDEO-?TS", "DVD-?R(?:$|(?!E))",  
          "DVD-?9", "DVD-?5"},null,RIP_OPTIONAL_SUFFIX);

	
	public static final String HDTV_NAME = "HDTV";	
	public final static  String[] HDTV_REGEX = build_source_pattern(new String[]{"HD-?TV"},null,RIP_OPTIONAL_SUFFIX);

	
	public static final String HDTV2_NAME = "HDTV";	
	public final static  String[] HDTV2_REGEX = build_source_pattern(new String[]{"TV-?HD"},null,RIP_SUFFIX);

	
	public static final String HDTV3_NAME = "HDTV";	
	public final static  String[] HDTV3_REGEX = build_source_pattern(new String[]{"TV"},null,"-?(Rip-?HD)");

	
	public static final String VOD_NAME = "VOD";	//"Video on Demand"
	public final static  String[] VOD_REGEX  = build_source_pattern(new String[]{"VOD"},null,RIP_OPTIONAL_SUFFIX);

	public static final String WEB_NAME = "Web";
	public final static  String[] WEB_REGEX  = build_source_pattern(new String[]{"WEB", "WEB-?DL"},null,RIP_SUFFIX);

	// WEBCap is a synonym to WEBRip, mostly used by non english
	public static final String WEBCAP_NAME = "Web";	
	public final static  String[] WEBCAP_REGEX  = build_source_pattern(new String[]{"WEB-?(Cap)"},null,RIP_OPTIONAL_SUFFIX);

	public static final String WEB2_NAME = "Web";	
	public final static  String[] WEB2_REGEX  = build_source_pattern(new String[]{"WEB-?DL", "WEB-?U?HD", "WEB", "DL-?WEB", "DL(?=-?Mux)"},null,null);


	public static final String HD_DVD_NAME = "HD-DVD";	
	public final static  String[] HD_DVD_REGEX  = build_source_pattern(new String[]{"HD-?DVD"},null,RIP_OPTIONAL_SUFFIX);

	public static final String BD_NAME = "BD";	//Blu-ray
	public final static  String[] BD_REGEX  = build_source_pattern(new String[]{"Blu-?ray", "BD", "BD[59]", "BD25", "BD50"},null,RIP_OPTIONAL_SUFFIX);

	public static final String BD2_NAME = "BD";// BRRip	
	public final static  String[] BD2_REGEX  = build_source_pattern(new String[]{"(BR)-?(?=Scr(?:eener)?)", "(BR)-?(?=Mux)"},null,null);

	public static final String BD3_NAME = "BD";	
	public final static  String[] BD3_REGEX  = build_source_pattern(new String[]{"(BR)"},null,RIP_SUFFIX);

	public static final String ULTRA_HD_BD_NAME = "Ultra HD Blu-ray";	
	public final static  String[] ULTRA_HD_BD_REGEX  = build_source_pattern(new String[]{"Ultra-?Blu-?ray", "Blu-?ray-?Ultra"},null,null);

	public static final String AHDTV_NAME = "Analog HDTV";	//Analog HDTV
	public final static  String[] AHDTV_REGEX  = build_source_pattern(new String[]{"AHDTV"},null,null);

	public static final String ULTRA_HDTV_NAME = "Ultra HDTV";	
	public final static  String[] ULTRA_HDTV_REGEX  = build_source_pattern(new String[]{"UHD-?TV"},null,RIP_OPTIONAL_SUFFIX);

	public static final String ULTRA_HDTV2_NAME = "Ultra HDTV";	
	public final static  String[] ULTRA_HDTV2_REGEX  = build_source_pattern(new String[]{"UHD"},null,RIP_SUFFIX);

	public static final String DSR_NAME = "Satellite";	//卫星频道
	public final static  String[] DSR_REGEX  = build_source_pattern(new String[]{"DSR", "DTH"},null,RIP_OPTIONAL_SUFFIX);

	public static final String DSR2_NAME = "Satellite";	
	public final static  String[] DSR2_REGEX  = build_source_pattern(new String[]{"DSR?", "SAT"},null,RIP_SUFFIX);

	
	public static Source parser(String input){
		Log.v("sjfqq","parser input:"+input );
		Log.v("sjfqq","xx:"+StringUtils.matchListFind(ULTRA_HD_BD_REGEX, input)+","+StringUtils.matchListFind(TS_REGEX, input));
		
		String name = null;
		if(StringUtils.matchListFind(VHS_REGEX, input))
		{
			name = VHS_NAME;
		}else if(StringUtils.matchListFind(CAM_REGEX, input))
		{
			name = CAM_NAME;
		}else if(StringUtils.matchListFind(HD_CAM_REGEX, input))
		{
			name = HD_CAM_NAME;
		}else if(StringUtils.matchListFind(TS_REGEX, input))
		{
			name = TS_NAME;
		}else if(StringUtils.matchListFind(HD_TS_REGEX, input))
		{
			name = HD_TS_NAME;
		}else if(StringUtils.matchListFind(WP_REGEX, input))
		{
			name = WP_NAME;
		}else if(StringUtils.matchListFind(TC_REGEX, input))
		{
			name = TC_NAME;
		}else if(StringUtils.matchListFind(HD_TC_REGEX, input))
		{
			name = HD_TC_NAME;
		}else if(StringUtils.matchListFind(PPV_REGEX, input))
		{
			name = PPV_NAME;
		}else if(StringUtils.matchListFind(WP_REGEX, input))
		{
			name = WP_NAME;
		}else if(StringUtils.matchListFind(TV1_REGEX, input))
		{
			name = TV1_NAME;
		}else if(StringUtils.matchListFind(TV2_REGEX, input))
		{
			name = TV2_NAME;
		}else if(StringUtils.matchListFind(TV3_REGEX, input))
		{
			name = TV3_NAME;
		}else if(StringUtils.matchListFind(TV4_REGEX, input))
		{
			name = TV4_NAME;
		}else if(StringUtils.matchListFind(DVB_REGEX, input))
		{
			name = DVB_NAME;
		}else if(StringUtils.matchListFind(DVD_REGEX, input))
		{
			name = DVD_NAME;
		}else if(StringUtils.matchListFind(DM_REGEX, input))
		{
			name = DM_NAME;
		}else if(StringUtils.matchListFind(DVD2_REGEX, input))
		{
			name = DVD2_NAME;
		}else if(StringUtils.matchListFind(HDTV_REGEX, input))
		{
			name = HDTV_NAME;
		}else if(StringUtils.matchListFind(HDTV2_REGEX, input))
		{
			name = HDTV2_NAME;
		}else if(StringUtils.matchListFind(HDTV3_REGEX, input))
		{
			name = HDTV3_NAME;
		}else if(StringUtils.matchListFind(VOD_REGEX, input))
		{
			name = VOD_NAME;
		}else if(StringUtils.matchListFind(WEB_REGEX, input))
		{
			name = WEB_NAME;
		}else if(StringUtils.matchListFind(WEBCAP_REGEX, input))
		{
			name = WEBCAP_NAME;
		}else if(StringUtils.matchListFind(WEB2_REGEX, input))
		{
			name = WEB2_NAME;
		}else if(StringUtils.matchListFind(HD_DVD_REGEX, input))
		{
			name = HD_DVD_NAME;
		}else if(StringUtils.matchListFind(BD_REGEX, input))
		{
			name = BD_NAME;
		}else if(StringUtils.matchListFind(BD2_REGEX, input))
		{
			name = BD2_NAME;
		}else if(StringUtils.matchListFind(BD3_REGEX, input))
		{
			name = BD3_NAME;
		}else if(StringUtils.matchListFind(ULTRA_HD_BD_REGEX, input))
		{
			name = ULTRA_HD_BD_NAME;
		}else if(StringUtils.matchListFind(AHDTV_REGEX, input))
		{
			name = AHDTV_NAME;
		}else if(StringUtils.matchListFind(ULTRA_HDTV_REGEX, input))
		{
			name = ULTRA_HDTV_NAME;
		}else if(StringUtils.matchListFind(ULTRA_HDTV2_REGEX, input))
		{
			name = ULTRA_HDTV2_NAME;
		}else if(StringUtils.matchListFind(DSR_REGEX, input))
		{
			name = DSR_NAME;
		}else if(StringUtils.matchListFind(DSR2_REGEX, input))
		{
			name = DSR2_NAME;
		}
		
		Log.v("sjfqq","name:"+name );
		if(!TextUtils.isEmpty(name))
			return new Source(name);
		
		return null;
	}
	 

	





//
//rebulk.regex(*build_source_pattern("DSR", "DTH", suffix=rip_optional_suffix),
//            value={"source": "Satellite", "other": "Rip"})
//rebulk.regex(*build_source_pattern("DSR?", "SAT", suffix=rip_suffix),
//            value={"source": "Satellite", "other": "Rip"})

}
