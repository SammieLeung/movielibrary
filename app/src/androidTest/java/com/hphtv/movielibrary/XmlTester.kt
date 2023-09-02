package com.hphtv.movielibrary

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hphtv.movielibrary.util.nfo.factory.KodiNFOFactory
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class XmlTester {

    @Test
    fun parseXML() {
        val inputStream = data.byteInputStream()
        val reader = KodiNFOFactory().createReader()
        reader.readFromXML(inputStream)
    }

    @Test
    fun testFile(){
        val file = findTVShowInfoFile("/storage/emulated/0/KStation/庆余年/S1E01.mp4", 2)
        println(file?.absolutePath)
    }

    fun findTVShowInfoFile(path: String, depth: Int): File? {
        if (depth < 0) {
            return null
        }
        val file = File(path)
        if (file.isDirectory) {
            val subFiles = file.listFiles()
            for (i in subFiles.indices) {
                val subFile = subFiles[i]
                if (subFile.name.equals("tvshow.nfo", ignoreCase = true)) {
                    return subFile
                }
            }
        }
        return findTVShowInfoFile(file.parentFile.path, depth - 1)
    }


    companion object {
        private const val data = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--created on 2023-09-02 14:43:50 - tinyMediaManager 4.3.13-->
<episodedetails>
  <title>逍遥梦境</title>
  <originaltitle>逍遥梦境</originaltitle>
  <showtitle>仙剑奇侠传</showtitle>
  <season>1</season>
  <episode>1</episode>
  <displayseason>-1</displayseason>
  <displayepisode>-1</displayepisode>
  <id>2283651</id>
  <uniqueid default="false" type="tmdb">399558</uniqueid>
  <uniqueid default="true" type="tvdb">2283651</uniqueid>
  <ratings/>
  <userrating>0.0</userrating>
  <plot>李逍遥梦境：与罗刹鬼婆空中激战，被打落掉回床上，被李大婶打醒。李大婶吩咐李逍遥去开店门。李逍遥刚一开门就见三个古怪的拜月教徒。三人在李家客栈（云来云去客栈）住下，其实另有目的。他们使李大婶昏迷，为救李大婶，李逍遥中计上了仙灵岛，巧遇赵灵儿，但赵灵儿和姥姥却说早在十年前就认识李逍遥，李逍遥大惑不解，姥姥让李逍遥与赵灵儿成亲。</plot>
  <runtime>46</runtime>
  <thumb/>
  <mpaa/>
  <premiered>2005-01-24</premiered>
  <aired>2005-01-24</aired>
  <watched>false</watched>
  <playcount>0</playcount>
  <studio>CTV</studio>
  <studio>唐人影视</studio>
  <director tmdbid="2749214">Lee Kwok-Lap</director>
  <actor>
    <name>Hu Ge</name>
    <role>Li Xiaoyao</role>
    <thumb>https://image.tmdb.org/t/p/h632/yMDKxcljmEFhxSGk3oeW3sl2YKu.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1106514</profile>
    <tmdbid>1106514</tmdbid>
  </actor>
  <actor>
    <name>Liu Yifei</name>
    <role>Zhao Ling'er</role>
    <thumb>https://image.tmdb.org/t/p/h632/Gj4gFStxpl6SsKnrkxrAsKBYqD.jpg</thumb>
    <profile>https://www.themoviedb.org/person/122503</profile>
    <tmdbid>122503</tmdbid>
  </actor>
  <actor>
    <name>Ady An</name>
    <role>Lin Yueru</role>
    <thumb>https://image.tmdb.org/t/p/h632/xmPVIj1Vtzltq0KNNWx0LvMlvdo.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1241442</profile>
    <tmdbid>1241442</tmdbid>
  </actor>
  <actor>
    <name>Eddie Peng</name>
    <role>Tang Yu</role>
    <thumb>https://image.tmdb.org/t/p/h632/ro73UewPtqFAO9OifEknW2kXqd7.jpg</thumb>
    <profile>https://www.themoviedb.org/person/126778</profile>
    <tmdbid>126778</tmdbid>
  </actor>
  <actor>
    <name>Esther Liu</name>
    <role>A'nu</role>
    <thumb>https://image.tmdb.org/t/p/h632/dZWLoOK2XNpwU5U8oA3JEmdjLQS.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1241444</profile>
    <tmdbid>1241444</tmdbid>
  </actor>
  <actor>
    <name>Limin Deng</name>
    <role>Shi Zhanglao</role>
    <thumb>https://image.tmdb.org/t/p/h632/BP2VOf7z6siJ3047ZyMSnWG5Oq.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1436153</profile>
    <tmdbid>1436153</tmdbid>
  </actor>
  <actor>
    <name>Jiang Xin</name>
    <role>Nüyuan / Jiang Wan'er</role>
    <thumb>https://image.tmdb.org/t/p/h632/mw1jQzfF8QgGOJFyfMZCQJxmh4f.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1241445</profile>
    <tmdbid>1241445</tmdbid>
  </actor>
  <actor>
    <name>Achel Chang</name>
    <role>Ding Xianglan</role>
    <thumb>https://image.tmdb.org/t/p/h632/m6yJfiZibIgjtZrYvlmwaMivnbh.jpg</thumb>
    <profile>https://www.themoviedb.org/person/2485043</profile>
    <tmdbid>2485043</tmdbid>
  </actor>
  <actor>
    <name>Elvis Tsui</name>
    <role>Moon Worshipping Cult's leader</role>
    <thumb>https://image.tmdb.org/t/p/h632/x110WbXYXMQjVNoPjEMoPo7qEKi.jpg</thumb>
    <profile>https://www.themoviedb.org/person/83631</profile>
    <tmdbid>83631</tmdbid>
  </actor>
  <actor>
    <name>Bryan Wong</name>
    <role>Liu Jinyuan</role>
    <thumb>https://image.tmdb.org/t/p/h632/oYaNjBXQfcJK5CClqooJPenmw4v.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1240197</profile>
    <tmdbid>1240197</tmdbid>
  </actor>
  <actor>
    <name>Tse Kwan-Ho</name>
    <role>Jiu Jian Xian Wine Sword Immortal</role>
    <thumb>https://image.tmdb.org/t/p/h632/a5y4CekUSOguVV2FOBtAVIFHMz7.jpg</thumb>
    <profile>https://www.themoviedb.org/person/931254</profile>
    <tmdbid>931254</tmdbid>
  </actor>
  <actor>
    <name>Yang Kun</name>
    <role>李大婶</role>
    <thumb>https://image.tmdb.org/t/p/h632/840dRWZEowkt5HcfDPTzyA4cWx7.jpg</thumb>
    <profile>https://www.themoviedb.org/person/2594974</profile>
    <tmdbid>2594974</tmdbid>
  </actor>
  <actor>
    <name>Wang Lei</name>
    <role>南蛮娘</role>
    <thumb>https://image.tmdb.org/t/p/h632/4pR6WRgvpBQlQqEIVuGh0KnCKc5.jpg</thumb>
    <profile>https://www.themoviedb.org/person/2443168</profile>
    <tmdbid>2443168</tmdbid>
  </actor>
  <trailer/>
  <dateadded>2023-09-02 14:42:40</dateadded>
  <epbookmark/>
  <code/>
  <fileinfo>
    <streamdetails>
      <video>
        <codec>h264</codec>
        <aspect>1.78</aspect>
        <width>3840</width>
        <height>2160</height>
        <durationinseconds>634</durationinseconds>
        <stereomode/>
      </video>
      <audio>
        <codec>MP3</codec>
        <language/>
        <channels>2</channels>
      </audio>
      <audio>
        <codec>AC3</codec>
        <language/>
        <channels>6</channels>
      </audio>
    </streamdetails>
  </fileinfo>
  <!--tinyMediaManager meta data-->
  <source>UNKNOWN</source>
  <original_filename>01.mp4</original_filename>
  <user_note/>
</episodedetails>
        """
    }
}