package com.hphtv.movielibrary

import android.util.Xml
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hphtv.movielibrary.roomdb.entity.Actor
import com.hphtv.movielibrary.roomdb.entity.Director
import com.hphtv.movielibrary.roomdb.entity.Writer
import com.orhanobut.logger.Logger
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

@RunWith(AndroidJUnit4::class)
class XmlTester {

    @Test
    fun parseXML() {
        val ns: String? = null

    }



    companion object {
        private const val data =
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--created on 2023-08-29 09:47:10 - tinyMediaManager 4.3.13-->
<movie>
  <title>疾速追杀4</title>
  <originaltitle>John Wick: Chapter 4</originaltitle>
  <sorttitle/>
  <epbookmark/>
  <year>2023</year>
  <ratings>
    <rating default="false" max="10" name="themoviedb">
      <value>7.8</value>
      <votes>4218</votes>
    </rating>
  </ratings>
  <userrating>0.0</userrating>
  <top250>0</top250>
  <set>
    <name>疾速追杀（系列）</name>
    <overview>《疾速追杀》(英语:John Wick)是由顶峰娱乐，制作的一系列界限分明、风格明快利落的美国动作惊悚片。创作者为德瑞克·柯斯达和查德·史塔赫斯基，而David Leith也在首部电影中担任导演（未挂名）。该系列主要围绕在由基努·李维饰演的退休杀手John Wick在复出后所引发的种种事件。系列拍出了有静有动的黑色风格。联合导演David Leith和Chad Stahelski都是好莱坞资深武师武指，镜头处理有气氛有格调。基努·李维饰把落寞的独行杀手演得冷峻有型。影片让观众体验了一场惊险、刺激的旅程，随着每一颗子弹爆头一个角色，都会让观众的紧张程度上升一个等级。</overview>
  </set>
  <plot>承接上集于纽约大陆酒店中枪坠楼，大难不死的约翰·威克（基努·里维斯饰）越洋前往大阪大陆酒店，向经理兼老朋友岛津浩二（真田广之饰）求助。另一方面，高桌会的新领导人文森特·德·格拉蒙特侯爵（比尔·斯卡斯加德饰）决心斩草除根，他先对付纽约大陆酒店经理温斯顿（伊恩·麦克肖恩饰演），再逼使威克昔日战友该隐（甄子丹饰演）替他效力，誓要孤立威克。&#13; 为了找出一条真正出路、打破宿命，威克決定向格拉蒙特侯爵提出生死决斗，贏了便能从此逃离追杀、重获真正自由；但他不知道，侯爵早于决斗地点·巴黎布下天罗地网，只待他堕入死亡陷阱…</plot>
  <outline>承接上集于纽约大陆酒店中枪坠楼，大难不死的约翰·威克（基努·里维斯饰）越洋前往大阪大陆酒店，向经理兼老朋友岛津浩二（真田广之饰）求助。另一方面，高桌会的新领导人文森特·德·格拉蒙特侯爵（比尔·斯卡斯加德饰）决心斩草除根，他先对付纽约大陆酒店经理温斯顿（伊恩·麦克肖恩饰演），再逼使威克昔日战友该隐（甄子丹饰演）替他效力，誓要孤立威克。&#13; 为了找出一条真正出路、打破宿命，威克決定向格拉蒙特侯爵提出生死决斗，贏了便能从此逃离追杀、重获真正自由；但他不知道，侯爵早于决斗地点·巴黎布下天罗地网，只待他堕入死亡陷阱…</outline>
  <tagline/>
  <runtime>170</runtime>
  <thumb aspect="poster">https://image.tmdb.org/t/p/original/h34UytWk6nw91wDt2Ts3UYsRWqe.jpg</thumb>
  <fanart>
    <thumb>https://image.tmdb.org/t/p/original/i3OTGmLNOZIo4SRQLVfLjeWegB6.jpg</thumb>
  </fanart>
  <mpaa>US:R / US:Rated R</mpaa>
  <certification>US:R / US:Rated R</certification>
  <id>tt10366206</id>
  <tmdbid>603692</tmdbid>
  <uniqueid default="false" type="tmdb">603692</uniqueid>
  <uniqueid default="false" type="tmdbSet">404609</uniqueid>
  <uniqueid default="true" type="imdb">tt10366206</uniqueid>
  <country>德国</country>
  <country>美国</country>
  <status/>
  <code/>
  <premiered>2023-03-22</premiered>
  <watched>false</watched>
  <playcount>0</playcount>
  <genre>Action</genre>
  <genre>Thriller</genre>
  <genre>Crime</genre>
  <studio>Thunder Road</studio>
  <studio>87Eleven</studio>
  <studio>Summit Entertainment</studio>
  <studio>Studio Babelsberg</studio>
  <credits tmdbid="113307">Michael Finch</credits>
  <credits tmdbid="1076800">Derek Kolstad</credits>
  <credits tmdbid="2104243">Shay Hatten</credits>
  <director tmdbid="40644">Chad Stahelski</director>
  <tag>new york city</tag>
  <tag>martial arts</tag>
  <tag>hitman</tag>
  <tag>sequel</tag>
  <tag>organized crime</tag>
  <tag>osaka, japan</tag>
  <tag>aftercreditsstinger</tag>
  <tag>hunted</tag>
  <tag>professional assassin</tag>
  <tag>neo-noir</tag>
  <tag>berlin</tag>
  <actor>
    <name>Keanu Reeves</name>
    <role>John Wick</role>
    <thumb>https://image.tmdb.org/t/p/h632/4D0PpNI0kmP58hgrwGC3wCjxhnm.jpg</thumb>
    <profile>https://www.themoviedb.org/person/6384</profile>
    <tmdbid>6384</tmdbid>
  </actor>
  <actor>
    <name>Donnie Yen</name>
    <role>Caine</role>
    <thumb>https://image.tmdb.org/t/p/h632/hTlhrrZMj8hZVvD17j4KyAFWBHc.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1341</profile>
    <tmdbid>1341</tmdbid>
  </actor>
  <actor>
    <name>Bill Skarsgård</name>
    <role>Marquis</role>
    <thumb>https://image.tmdb.org/t/p/h632/cFQN6rLSSLhGx8NQI7krYWwdRpl.jpg</thumb>
    <profile>https://www.themoviedb.org/person/137905</profile>
    <tmdbid>137905</tmdbid>
  </actor>
  <actor>
    <name>Ian McShane</name>
    <role>Winston</role>
    <thumb>https://image.tmdb.org/t/p/h632/qh9RTLbnr128TZLdGuXwUH9mdBM.jpg</thumb>
    <profile>https://www.themoviedb.org/person/6972</profile>
    <tmdbid>6972</tmdbid>
  </actor>
  <actor>
    <name>Laurence Fishburne</name>
    <role>Bowery King</role>
    <thumb>https://image.tmdb.org/t/p/h632/iwx7h0AfWMm9K4sMmhru3ShSra.jpg</thumb>
    <profile>https://www.themoviedb.org/person/2975</profile>
    <tmdbid>2975</tmdbid>
  </actor>
  <actor>
    <name>Lance Reddick</name>
    <role>Charon</role>
    <thumb>https://image.tmdb.org/t/p/h632/22mVtEXZbpt0J7S0LhIhdkfRrZV.jpg</thumb>
    <profile>https://www.themoviedb.org/person/129101</profile>
    <tmdbid>129101</tmdbid>
  </actor>
  <actor>
    <name>Clancy Brown</name>
    <role>Harbinger</role>
    <thumb>https://image.tmdb.org/t/p/h632/9RgzFqbmWBLVfq9wvyDo5UW8VT1.jpg</thumb>
    <profile>https://www.themoviedb.org/person/6574</profile>
    <tmdbid>6574</tmdbid>
  </actor>
  <actor>
    <name>Hiroyuki Sanada</name>
    <role>Shimazu</role>
    <thumb>https://image.tmdb.org/t/p/h632/6XLANVi8CtFaxf1KL3LDZDiW07J.jpg</thumb>
    <profile>https://www.themoviedb.org/person/9195</profile>
    <tmdbid>9195</tmdbid>
  </actor>
  <actor>
    <name>Rina Sawayama</name>
    <role>Akira</role>
    <thumb>https://image.tmdb.org/t/p/h632/yoo4ZcHjF4G51UDBj7omUQrClYI.jpg</thumb>
    <profile>https://www.themoviedb.org/person/2337629</profile>
    <tmdbid>2337629</tmdbid>
  </actor>
  <actor>
    <name>Scott Adkins</name>
    <role>Killa</role>
    <thumb>https://image.tmdb.org/t/p/h632/9NRr2a1riIn5CWn5McZLJlk4vxR.jpg</thumb>
    <profile>https://www.themoviedb.org/person/78110</profile>
    <tmdbid>78110</tmdbid>
  </actor>
  <actor>
    <name>Aimée Kwan</name>
    <role>Mia</role>
    <thumb>https://image.tmdb.org/t/p/h632/wMxlStNh1rNdcoMa7mnJD1h15O6.jpg</thumb>
    <profile>https://www.themoviedb.org/person/3779452</profile>
    <tmdbid>3779452</tmdbid>
  </actor>
  <actor>
    <name>Marko Zaror</name>
    <role>Chidi</role>
    <thumb>https://image.tmdb.org/t/p/h632/rQ8XFdLmnfXs5x8FsttdOUrFLWa.jpg</thumb>
    <profile>https://www.themoviedb.org/person/118370</profile>
    <tmdbid>118370</tmdbid>
  </actor>
  <actor>
    <name>Natalia Tena</name>
    <role>Katia</role>
    <thumb>https://image.tmdb.org/t/p/h632/7kN9NpECZoR4NLXb4SlpHOaJx7T.jpg</thumb>
    <profile>https://www.themoviedb.org/person/3300</profile>
    <tmdbid>3300</tmdbid>
  </actor>
  <actor>
    <name>Shamier Anderson</name>
    <role>Tracker</role>
    <thumb>https://image.tmdb.org/t/p/h632/vUlPZ1owT67BOQwpIn96ZFjBxg2.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1080969</profile>
    <tmdbid>1080969</tmdbid>
  </actor>
  <actor>
    <name>George Georgiou</name>
    <role>The Elder</role>
    <thumb>https://image.tmdb.org/t/p/h632/9NoRfi7L87xsmveXpE7ryycsFQy.jpg</thumb>
    <profile>https://www.themoviedb.org/person/570010</profile>
    <tmdbid>570010</tmdbid>
  </actor>
  <actor>
    <name>Yoshinori Tashiro</name>
    <role>Sumo #1</role>
    <profile>https://www.themoviedb.org/person/2422484</profile>
    <tmdbid>2422484</tmdbid>
  </actor>
  <actor>
    <name>Hiroki Sumi</name>
    <role>Sumo #2</role>
    <profile>https://www.themoviedb.org/person/3978249</profile>
    <tmdbid>3978249</tmdbid>
  </actor>
  <actor>
    <name>Daiki Suzuki</name>
    <role>Daiki</role>
    <profile>https://www.themoviedb.org/person/1967379</profile>
    <tmdbid>1967379</tmdbid>
  </actor>
  <actor>
    <name>Julia Asuka Riedl</name>
    <role>Operator</role>
    <profile>https://www.themoviedb.org/person/3978251</profile>
    <tmdbid>3978251</tmdbid>
  </actor>
  <actor>
    <name>Milena Rendón</name>
    <role>Operator</role>
    <profile>https://www.themoviedb.org/person/3978252</profile>
    <tmdbid>3978252</tmdbid>
  </actor>
  <actor>
    <name>Ivy Quainoo</name>
    <role>Operator</role>
    <profile>https://www.themoviedb.org/person/2331772</profile>
    <tmdbid>2331772</tmdbid>
  </actor>
  <actor>
    <name>Irina Trifanov</name>
    <role>Operator</role>
    <profile>https://www.themoviedb.org/person/3978254</profile>
    <tmdbid>3978254</tmdbid>
  </actor>
  <actor>
    <name>Iryna Fedorova</name>
    <role>Babushka</role>
    <profile>https://www.themoviedb.org/person/3978255</profile>
    <tmdbid>3978255</tmdbid>
  </actor>
  <actor>
    <name>Andrej Kaminsky</name>
    <role>Priest</role>
    <profile>https://www.themoviedb.org/person/1327564</profile>
    <tmdbid>1327564</tmdbid>
  </actor>
  <actor>
    <name>Sven Marquardt</name>
    <role>Klaus</role>
    <profile>https://www.themoviedb.org/person/2219646</profile>
    <tmdbid>2219646</tmdbid>
  </actor>
  <actor>
    <name>Raicho Vasilev</name>
    <role>German Gangster</role>
    <thumb>https://image.tmdb.org/t/p/h632/fj5lqV9ayJeQg8z7KJmwW4PbHrl.jpg</thumb>
    <profile>https://www.themoviedb.org/person/22019</profile>
    <tmdbid>22019</tmdbid>
  </actor>
  <actor>
    <name>Marie Pierra Kakoma</name>
    <role>DJ</role>
    <profile>https://www.themoviedb.org/person/3978256</profile>
    <tmdbid>3978256</tmdbid>
  </actor>
  <actor>
    <name>Gina Aponte</name>
    <role>Club Host (uncredited)</role>
    <profile>https://www.themoviedb.org/person/2002674</profile>
    <tmdbid>2002674</tmdbid>
  </actor>
  <actor>
    <name>Christoph Hofmann</name>
    <role>Valet (uncredited)</role>
    <thumb>https://image.tmdb.org/t/p/h632/vnQ6eHhD3CRzpYglFZfUnlfWsAd.jpg</thumb>
    <profile>https://www.themoviedb.org/person/3248711</profile>
    <tmdbid>3248711</tmdbid>
  </actor>
  <producer tmdbid="6384">
    <name>Keanu Reeves</name>
    <thumb>https://image.tmdb.org/t/p/h632/4D0PpNI0kmP58hgrwGC3wCjxhnm.jpg</thumb>
    <profile>https://www.themoviedb.org/person/6384</profile>
  </producer>
  <producer tmdbid="10903">
    <name>Henning Molfenter</name>
    <thumb>https://image.tmdb.org/t/p/h632/ehadQnjTvjsUh3CSd8OQFdEm4iV.jpg</thumb>
    <profile>https://www.themoviedb.org/person/10903</profile>
  </producer>
  <producer tmdbid="10905">
    <name>Charlie Woebcken</name>
    <profile>https://www.themoviedb.org/person/10905</profile>
  </producer>
  <producer tmdbid="40644">
    <name>Chad Stahelski</name>
    <thumb>https://image.tmdb.org/t/p/h632/eRCryGwKDH4XqUlrdkERmeBWPo8.jpg</thumb>
    <profile>https://www.themoviedb.org/person/40644</profile>
  </producer>
  <producer tmdbid="40684">
    <name>David Leitch</name>
    <thumb>https://image.tmdb.org/t/p/h632/qykhwWkXTAteD9yvsmItXh9LxCq.jpg</thumb>
    <profile>https://www.themoviedb.org/person/40684</profile>
  </producer>
  <producer tmdbid="40513">
    <name>Michael Paseornek</name>
    <profile>https://www.themoviedb.org/person/40513</profile>
  </producer>
  <producer tmdbid="56327">
    <name>Louise Rosner-Meyer</name>
    <profile>https://www.themoviedb.org/person/56327</profile>
  </producer>
  <producer tmdbid="67759">
    <name>Basil Iwanyk</name>
    <thumb>https://image.tmdb.org/t/p/h632/7ULVJcHinmbUkjGrjseCgAztcx4.jpg</thumb>
    <profile>https://www.themoviedb.org/person/67759</profile>
  </producer>
  <producer tmdbid="1089142">
    <name>Christoph Fisser</name>
    <thumb>https://image.tmdb.org/t/p/h632/7URHtynBXlFSJph8a3ZxqPmrL2k.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1089142</profile>
  </producer>
  <producer tmdbid="1653024">
    <name>Kharmel Cochrane</name>
    <profile>https://www.themoviedb.org/person/1653024</profile>
  </producer>
  <producer tmdbid="1932093">
    <name>Erica Lee</name>
    <profile>https://www.themoviedb.org/person/1932093</profile>
  </producer>
  <trailer>plugin://plugin.video.youtube/?action=play_video&amp;videoid=IM4pGVSPf58</trailer>
  <languages>Arabic, 广州话, 廣州話, English, French, German, Japanese, Latin, Russian, Spanish</languages>
  <dateadded>2023-08-22 16:21:31</dateadded>
  <fileinfo>
    <streamdetails>
      <video>
        <codec>h264</codec>
        <aspect>1.78</aspect>
        <width>1920</width>
        <height>1080</height>
        <durationinseconds>10157</durationinseconds>
        <stereomode/>
      </video>
      <audio>
        <codec>AAC</codec>
        <language/>
        <channels>2</channels>
      </audio>
    </streamdetails>
  </fileinfo>
  <!--tinyMediaManager meta data-->
  <source>UNKNOWN</source>
  <edition>NONE</edition>
  <original_filename>阳光电影dy.ygdy8.com.疾速追杀4.2023.HD.1080P.中英双字.mkv</original_filename>
  <user_note/>
</movie>
"""
    }
}