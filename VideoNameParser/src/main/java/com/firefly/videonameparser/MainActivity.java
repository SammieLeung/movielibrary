package com.firefly.videonameparser;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


public class MainActivity extends Activity {
	private static final String TAG = "Video-Parser";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_2);
//		debug("Death.Note.TV.2006.DVDRip.Ep03.Rev2.x264.AC3-Jina");
//		debug("[dmhy][Death_Note][32].mp4");
//        debug("[Death_Note][06].mkv");
//		debug("西部世界第三季.mp4");
		debug("新世纪福音战士：终.mp4");
//		debug("新世纪福音战士：终.mp4");
//		debug("[迅雷下载www.xiamp4.com]铁血孤儿[第05话].mp4");
		debug("狮子王.The.Lion.King.1994.BD1080P.X264.AAC.Mandarin&English.CHS-ENG.mp4");
		debug("Braveheart.1995.2160p.BluRay.x265.10bit.SDR.DTS-HD.MA.TrueHD.7.1.Atmos-SWTYBLZ");
//		debug("[www.domp4.com]跛豪.1991.BD1080p.国粤双语.mp4");
//		debug("/storage/emulated/0/Download/[www.domp4.com]跛豪.1991.BD1080p.国粤双语/[www.domp4.com]跛豪.1991.BD1080p.国粤双语.mp4");
//        debug("[Death_Note][32][ms32].mp4");
//        debug("Death.Note.TV.2006.DVDRip.e03.Rev2.x264.AC3-Jina");
//        debug("Death.Note.TV.2006.DVDRip.ep03.Rev2.x264.AC3-Jina");
//        debug("Death.Note.TV.2006.DVDRip.eP03.Rev2.x264.AC3-Jina");
//        debug("Death.Note.TV.2006.DVDRip.E03.Rev2.x264.AC3-Jina");
//		debug("[2017][HYSUB]ONE PUNCH MAN[10][GB_MP4][720P].mp4");
//		debug("[2017][HYSUB]ONE PUNCH MAN[10][GB_MP4][1280X720].mp4");
//		debug("2006【大兴奋!三日月岛的动物骚动!】.mp4");
//		debug("Code Geass ~Hangyaku no Lelouch R2~ 07 (BDRip 1280x720)-muxed.mp4");
//		debug("[Mobile Suit Gundam Seed Destiny HD REMASTER][46][Big5][720p][AVC_AAC][encoded by SEED].mp4");
//		debug("[www.BTxiaba.com]狂暴巨兽.Rampage.2018.1080p.WEB-DL.X264.AAC.CHS.ENG-BTxiaba&远鉴字幕组.mp4");
//		debug("【蚂蚁网www.mayi.tw】审死官CD2.mp4");
//		debug("[导火新闻线][BluRay-720P.MKV][2.67GB][国粤双语][LC-AAC.5.1].mkv");
//		debug("【远鉴字幕组&Orange字幕组】金蝉脱壳2【蓝光版特效中英双字】Escape.Plan.2.Hades.2018.1080p.BluRay.x264.DTS-HDC@CHDbits.mkv");
//		debug("[BT乐园·bt606.com]名侦探洪吉童:消失的村庄2016.HD720P.X264.AAC.韩语中字.mp4");
//
//		debug("[dmhy][Macross Delta][21][BIG5][720P_MP4][BS11].mp4");
//
//        debug("[dmhy]I lov esd[82][BIG5][720P_MP4][BS11].mp4");

//		Source.parser("Les.Magiciens.1976.VHSRip.XViD.MKO");
//		Source.parser("The.Boss.Baby.2017.HDCAM.XviD-MrGrey");
//		Source.parser("Fantastic Beasts and Where to Find Them 2016 Multi 2160p UHD BluRay HEVC HDR Atmos7.1-DDR");
   // Episodes.parser("01x02");
//       Episodes.parser("S01S02S03");
//		Episodes.parser("Death.Note.TV.2006.DVDRip.Season6.Ep03.Rev2.x264.AC3-Jina");
	}
 

	private void debug(String name){
		Log.v(TAG, "Parser :"+name);

//		VideoNameParser mParser = new VideoNameParser();
//		MovieNameInfo info = mParser.parseVideoName(name);
//		Log.v(TAG, "ParseName :"+info.toString());
//		Log.v(TAG, "********************");
		VideoNameParser mParser2=new VideoNameParser();
        MovieNameInfo info2=mParser2.parseVideoName(name);
        Log.v(TAG, "debug->"+info2.toString());
        Log.v(TAG, "********************");
	}






}
