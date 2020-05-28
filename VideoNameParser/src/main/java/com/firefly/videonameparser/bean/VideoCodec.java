package com.firefly.videonameparser.bean;

import android.util.Log;

import com.firefly.videonameparser.utils.StringUtils;

public class VideoCodec {

	public static final String[] MPEG_2_REGEX = {"Mpe?g-?2", "[hx]-?262"};
	public static final String[] DIVX_REGEX = {"DVDivX", "DivX"};
	public static final String[] XVID_REGEX = {"XviD"};
	public static final String[] VC_1_REGEX = {"VC-?1"};
	public static final String[] VP7_REGEX = {"VP7"};
	public static final String[] VP8_REGEX = {"VP8", "VP80"};
	
	public static final String[] VP9_REGEX = {"VP9"};
	public static final String[] H263_REGEX = {"[hx]-?263"};
	public static final String[] H264_REGEX = {"[hx]-?264(?:-?AVC(?:HD)?)?(?:-?SC)?", "MPEG-?4(?:-?AVC(?:HD)?)", "AVC(?:HD)?(?:-?SC)?"};
	
	public static final String[] H265_REGEX = {"[hx]-?265(?:-?HEVC)?", "HEVC"};
	
	
	public String codec;
	
	
	public VideoCodec(String codec) {
		super();
		this.codec = codec;
	}

	public static VideoCodec parser(String input){
		String codec = null;
		//Log.v("sjfq", "VideoCodec parser :"+input);
		if(StringUtils.matchListFind(MPEG_2_REGEX, input))
		{
			codec = "MPEG-2";
		}else if(StringUtils.matchListFind(DIVX_REGEX, input))
		{
			codec = "DivX";
		}else if(StringUtils.matchListFind(XVID_REGEX, input))
		{
			codec = "XviD";
		}else if(StringUtils.matchListFind(VC_1_REGEX, input))
		{
			codec = "VC-1";
		}else if(StringUtils.matchListFind(VP7_REGEX, input))
		{
			codec = "VP7";
		}else if(StringUtils.matchListFind(VP8_REGEX, input))
		{
			codec = "VP8";
		}else if(StringUtils.matchListFind(VP9_REGEX, input))
		{
			codec = "VP9";
		}else if(StringUtils.matchListFind(H263_REGEX, input))
		{
			codec = "H263";
		}else if(StringUtils.matchListFind(H264_REGEX, input))
		{
			codec = "H264";
		}else if(StringUtils.matchListFind(H265_REGEX, input))
		{
			codec = "H265";
		}
		//Log.v("sjfq", "codec:"+codec);

		if(codec != null)
			return new VideoCodec(codec);
		return null;
		
	}
}

//rebulk.regex('(?P<video_codec>hevc)(?P<color_depth>10)', value={'video_codec': 'H.265', 'color_depth': '10-bit'},
//tags=['video-codec-suffix'], children=True)
//
//# http://blog.mediacoderhq.com/h264-profiles-and-levels/
//# http://fr.wikipedia.org/wiki/H.264
//rebulk.defaults(name="video_profile",
//   validator=seps_surround,
//   disabled=lambda context: is_disabled(context, 'video_profile'))
//
//rebulk.string('BP', value='Baseline', tags='video_profile.rule')
//rebulk.string('XP', 'EP', value='Extended', tags='video_profile.rule')
//rebulk.string('MP', value='Main', tags='video_profile.rule')
//rebulk.string('HP', 'HiP', value='High', tags='video_profile.rule')
//rebulk.regex('Hi422P', value='High 4:2:2')
//rebulk.regex('Hi444PP', value='High 4:4:4 Predictive')
//rebulk.regex('Hi10P?', value='High 10')  # no profile validation is required
//
//rebulk.string('DXVA', value='DXVA', name='video_api',
// disabled=lambda context: is_disabled(context, 'video_api'))
//
//rebulk.defaults(name='color_depth',
//   validator=seps_surround,
//   disabled=lambda context: is_disabled(context, 'color_depth'))
//rebulk.regex('12.?bits?', value='12-bit')
//rebulk.regex('10.?bits?', 'YUV420P10', 'Hi10P?', value='10-bit')
//rebulk.regex('8.?bits?', value='8-bit')
//
//rebulk.rules(ValidateVideoCodec, VideoProfileRule)
