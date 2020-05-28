package com.firefly.videonameparser.bean;

import android.util.Log;

import com.firefly.videonameparser.utils.StringUtils;

public class AudioCodec {

	public static final String[] MP3_REGEX = {"MP3", "LAME", "LAME(?:\\d)+-?(?:\\d)"};
	public static final String[] DOLBY_DIGITAL_REGEX = {"Dolby", "DolbyDigital", "Dolby-Digital", "DD", "AC3D?"};
	public static final String[] DOLBY_ATMOS_REGEX = {"Dolby-?Atmos", "Atmos"};
	public static final String[] AAC_REGEX = {"AAC"};
	public static final String[] DOLBY_DIGITAL_PLUS_REGEX = {"EAC3", "DDP", "DD+"};
	public static final String[] FLAC_REGEX = {"Flac"};
	
	public static final String[] DTS_REGEX = {"DTS"};
	public static final String[] DTS_HD_REGEX = {"DTS-?HD", "DTS(?=-?MA)"};
	public static final String[] DOLBY_TRUEHD_REGEX = {"True-?HD"};
	
	public static final String[] OPUS_REGEX = {"Opus"};
	public static final String[] VORBIS_REGEX = {"Vorbis"};
	public static final String[] PCM_REGEX = {"PCM"};
	public static final String[] LPCM_REGEX = {"LPCM"};
	
//    rebulk.string("AAC", value="AAC")
//    rebulk.string("EAC3", "DDP", "DD+", value="Dolby Digital Plus")
//    rebulk.string("Flac", value="FLAC")
//    rebulk.string("DTS", value="DTS")
//    rebulk.regex("DTS-?HD", "DTS(?=-?MA)", value="DTS-HD",
//                 conflict_solver=lambda match, other: other if other.name == "audio_codec" else "__default__")
//    rebulk.regex("True-?HD", value="Dolby TrueHD")
//    rebulk.string("Opus", value="Opus")
//    rebulk.string("Vorbis", value="Vorbis")
//    rebulk.string("PCM", value="PCM")
//    rebulk.string("LPCM", value="LPCM")
	public String codec;
	
	
	public AudioCodec(String codec) {
		super();
		this.codec = codec;
	}

	public static AudioCodec parser(String input){
		String codec = null;
		//Log.v("sjfq", "VideoCodec parser :"+input);
		if(StringUtils.matchListFind(MP3_REGEX, input))
		{
			codec = "MP3";
		}else if(StringUtils.matchListFind(DOLBY_DIGITAL_REGEX, input))
		{
			codec = "Dolby Digital";
		}else if(StringUtils.matchListFind(DOLBY_ATMOS_REGEX, input))
		{
			codec = "Dolby Atmos";
		}else if(StringUtils.matchListFind(AAC_REGEX, input))
		{
			codec = "AAC";
		}else if(StringUtils.matchListFind(DOLBY_DIGITAL_PLUS_REGEX, input))
		{
			codec = "Dolby Digital Plus";
		}else if(StringUtils.matchListFind(FLAC_REGEX, input))
		{
			codec = "Flac";
		}else if(StringUtils.matchListFind(DTS_REGEX, input))
		{
			codec = "DTS";
		}else if(StringUtils.matchListFind(DTS_HD_REGEX, input))
		{
			codec = "DTS-HD";
		}else if(StringUtils.matchListFind(DOLBY_TRUEHD_REGEX, input))
		{
			codec = "Dolby TrueHD";
		}else if(StringUtils.matchListFind(OPUS_REGEX, input))
		{
			codec = "Opus";
		}else if(StringUtils.matchListFind(VORBIS_REGEX, input))
		{
			codec = "Vorbis";
		}else if(StringUtils.matchListFind(PCM_REGEX, input))
		{
			codec = "PCM";
		}else if(StringUtils.matchListFind(LPCM_REGEX, input))
		{
			codec = "LPCM";
		}
		
		//Log.v("sjfq", "codec:"+codec);

		if(codec != null)
			return new AudioCodec(codec);
		return null;
		
	}
}

