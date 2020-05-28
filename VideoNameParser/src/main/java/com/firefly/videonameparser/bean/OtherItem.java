package com.firefly.videonameparser.bean;

import android.util.Log;

import com.firefly.videonameparser.utils.StringUtils;

public class OtherItem {

	public static final String[] AUDIO_FIXED_REGEX = {"Audio-?Fix", "Audio-?Fixed"};
	public static final String[] SYNC_FIXED_REGEX = {"Sync-?Fix", "Sync-?Fixed"};
	public static final String[] DUAL_AUDIO_REGEX = {"Dual", "Dual-?Audio"};
	public static final String[] WIDE_SCREEN_REGEX = {"ws", "wide-?screen"};
	public static final String[] REENCODED_REGEX = {"Re-?Enc(?:oded)?"};
	public static final String[] ENCODED_REGEX = {"Enc(?:oded)?"};
	public static final String[] PROPER_REGEX = {"Real", "Fix", "Fixed","Proper", "Repack", "Rerip", "Dirfix", "Nfofix", "Prooffix"};
	
	public static final String[] PROPER2_REGEX = {"(?:Proof-?)?Sample-?Fix"};
	public static final String[] FAN_SUBTITLED__REGEX = {"Fansub"};
	public static final String[] FAST_SUBTITLED__REGEX = {"Fastsub"};

	public static final String[] TRANSLATION_GROUPS_REGEX={"^[^aeiou]+$"};
	
	
	public String tag;
	
	
	public OtherItem(String tag) {
		super();
		this.tag = tag;
	}

	public static OtherItem parser(String input){
		String tag = null;
		Log.v("sjfq", "OtherItem parser :"+input);
		if(StringUtils.matchListFind(AUDIO_FIXED_REGEX, input))
		{
			tag = "audio_fixed";
		}else if(StringUtils.matchListFind(SYNC_FIXED_REGEX, input))
		{
			tag = "Sync Fixed";
		}else if(StringUtils.matchListFind(DUAL_AUDIO_REGEX, input))
		{
			tag = "Dual Audio";
		}else if(StringUtils.matchListFind(WIDE_SCREEN_REGEX, input))
		{
			tag = "Widescreen";
		}else if(StringUtils.matchListFind(REENCODED_REGEX, input))
		{
			tag = "Reencoded";
		}else if(StringUtils.matchListFind(ENCODED_REGEX, input))
		{
			tag = "Encoded";
		}else if(StringUtils.matchListFind(PROPER_REGEX, input))
		{
			tag = "Proper";
		}else if(StringUtils.matchListFind(PROPER2_REGEX, input))
		{
			tag = "Proper";
		}else if(StringUtils.matchListFind(FAN_SUBTITLED__REGEX, input))
		{
			tag = "Fan Subtitled";
		}else if(StringUtils.matchListFind(FAN_SUBTITLED__REGEX, input))
		{
			tag = "Fast Subtitled";
		}else if(StringUtils.matchListFind(TRANSLATION_GROUPS_REGEX,input)){
			tag="Translate Groups";
		}
		Log.v("sjfq", "OtherItem tag :"+tag);
		if(tag != null)
			return new OtherItem(tag);
		return null;
		
	}
}

//rebulk.regex("(?P<video_codec>hevc)(?P<color_depth>10)", value={"video_codec": "H.265", "color_depth": "10-bit"},
//tags=["video-codec-suffix"], children=True)
//
//# http://blog.mediacoderhq.com/h264-profiles-and-levels/
//# http://fr.wikipedia.org/wiki/H.264
//rebulk.defaults(name="video_profile",
//   validator=seps_surround,
//   disabled=lambda context: is_disabled(context, "video_profile"))
//
//rebulk.string("BP", value="Baseline", tags="video_profile.rule")
//rebulk.string("XP", "EP", value="Extended", tags="video_profile.rule")
//rebulk.string("MP", value="Main", tags="video_profile.rule")
//rebulk.string("HP", "HiP", value="High", tags="video_profile.rule")
//rebulk.regex("Hi422P", value="High 4:2:2")
//rebulk.regex("Hi444PP", value="High 4:4:4 Predictive")
//rebulk.regex("Hi10P?", value="High 10")  # no profile validation is required
//
//rebulk.string("DXVA", value="DXVA", name="video_api",
// disabled=lambda context: is_disabled(context, "video_api"))
//
//rebulk.defaults(name="color_depth",
//   validator=seps_surround,
//   disabled=lambda context: is_disabled(context, "color_depth"))
//rebulk.regex("12.?bits?", value="12-bit")
//rebulk.regex("10.?bits?", "YUV420P10", "Hi10P?", value="10-bit")
//rebulk.regex("8.?bits?", value="8-bit")
//
//rebulk.rules(ValidateVideoCodec, VideoProfileRule)
