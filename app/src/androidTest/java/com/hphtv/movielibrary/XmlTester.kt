package com.hphtv.movielibrary

import android.util.Xml
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hphtv.movielibrary.roomdb.entity.Actor
import com.hphtv.movielibrary.roomdb.entity.Director
import com.hphtv.movielibrary.roomdb.entity.Writer
import com.hphtv.movielibrary.util.nfo.NFOEntity
import com.hphtv.movielibrary.util.nfo.factory.KodiNFOFactory
import com.hphtv.movielibrary.util.nfo.factory.NFOFactory
import com.hphtv.movielibrary.util.nfo.reader.KodiNFOReader
import com.orhanobut.logger.Logger
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

@RunWith(AndroidJUnit4::class)
class XmlTester {

    @Test
    fun parseXML() {
val inputStream = data.byteInputStream()
        val reader= KodiNFOFactory().createReader()
        reader.readFromXML(inputStream)
    }



    companion object {
        private const val data ="""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--created on 2023-08-30 11:30:02 - tinyMediaManager 4.3.13-->
<tvshow>
  <title>鬼灭之刃</title>
  <originaltitle>鬼滅の刃</originaltitle>
  <showtitle>鬼灭之刃</showtitle>
  <sorttitle/>
  <year>2019</year>
  <ratings>
    <rating default="true" max="10" name="themoviedb">
      <value>8.7</value>
      <votes>5557</votes>
    </rating>
  </ratings>
  <userrating>0.0</userrating>
  <outline/>
  <plot>电视动画《鬼灭之刃》改编自吾峠呼世晴创作的同名漫画，由 ufotable 制作。日本大正时期，那是一个吃人的恶鬼横行的世界，一名家人被鬼杀死，妹妹也变成了鬼的主人公炭治郎，在猎鬼人的指引下，成为了鬼猎人组织“鬼杀队”的一员，为了让妹妹祢豆子变回人类，为了讨伐杀害家人的恶鬼，为了斩断悲伤的连锁而展开了战斗。</plot>
  <tagline/>
  <runtime>24</runtime>
  <thumb aspect="poster">https://image.tmdb.org/t/p/original/taT33NroOl2Fn8bUGj8bwdmNw3G.jpg</thumb>
  <thumb aspect="logo">https://image.tmdb.org/t/p/original/vcyhBrJi1SH5mKMDaOBA4K73EH7.png</thumb>
  <namedseason number="0">特别篇</namedseason>
  <namedseason number="1">立志篇</namedseason>
  <namedseason number="2">无限列车篇</namedseason>
  <namedseason number="3">游郭篇</namedseason>
  <namedseason number="4">刀匠村篇</namedseason>
  <namedseason number="5">柱训练篇</namedseason>
  <thumb aspect="poster" season="0" type="season">https://image.tmdb.org/t/p/original/qr3wE5cEfXd6E7opPF9I0GsgKj8.jpg</thumb>
  <thumb aspect="poster" season="1" type="season">https://image.tmdb.org/t/p/original/gRIOC9sEqSLuLeJI3rEFud9IeAB.jpg</thumb>
  <thumb aspect="poster" season="2" type="season">https://image.tmdb.org/t/p/original/FuBnPbS4SmGiiwwrk4CMKSb7Pf.jpg</thumb>
  <thumb aspect="poster" season="3" type="season">https://image.tmdb.org/t/p/original/ton0zwuKsg7PVuZB79IaaQnnrOo.jpg</thumb>
  <thumb aspect="poster" season="4" type="season">https://image.tmdb.org/t/p/w500/7QakGpfNQOtvqt1bg1a7ZBZtof7.jpg</thumb>
  <fanart>
    <thumb>https://image.tmdb.org/t/p/original/nTvM4mhqNlHIvUkI1gVnW6XP7GG.jpg</thumb>
  </fanart>
  <mpaa>US:TV-MA</mpaa>
  <certification>US:TV-MA</certification>
  <episodeguide>{"tmdb":"85937","imdb":"tt9335498","tvdb":"348545"}</episodeguide>
  <id>348545</id>
  <imdbid>tt9335498</imdbid>
  <tmdbid>85937</tmdbid>
  <uniqueid default="false" type="tmdb">85937</uniqueid>
  <uniqueid default="false" type="imdb">tt9335498</uniqueid>
  <uniqueid default="true" type="tvdb">348545</uniqueid>
  <premiered>2019-04-06</premiered>
  <status>Continuing</status>
  <watched>false</watched>
  <playcount/>
  <genre>Animation</genre>
  <genre>Action</genre>
  <genre>Science Fiction</genre>
  <studio>Fuji TV</studio>
  <studio>Gunma TV</studio>
  <studio>Tokyo MX</studio>
  <studio>BS11</studio>
  <studio>Tokai Television Broadcasting</studio>
  <studio>Kansai TV</studio>
  <studio>Tochigi TV</studio>
  <studio>Fukui TV</studio>
  <studio>Hokkaido Cultural Broadcasting</studio>
  <studio>Iwate Menkoi Television</studio>
  <studio>Television Oita System</studio>
  <studio>Sendai Television</studio>
  <studio>SAGA TV</studio>
  <studio>Ishikawa TV</studio>
  <studio>Television Nishinippon</studio>
  <studio>OHK</studio>
  <studio>Kochi Sun Sun Broadcasting</studio>
  <studio>Television Shin Hiroshima System</studio>
  <studio>TV Shizuoka</studio>
  <studio>UMK TV Miyazaki</studio>
  <studio>NST</studio>
  <studio>NBS</studio>
  <studio>Sakuranbo TV</studio>
  <studio>TSK</studio>
  <studio>Ehime Broadcasting</studio>
  <studio>KTS</studio>
  <studio>Fukushima TV</studio>
  <studio>NIB</studio>
  <studio>AKT</studio>
  <studio>Toyama Television</studio>
  <studio>TV Kumamoto</studio>
  <studio>Okinawa Television Broadcasting</studio>
  <studio>ufotable</studio>
  <studio>Aniplex</studio>
  <studio>Shueisha</studio>
  <studio>STUDIO MAUSU</studio>
  <country>日本</country>
  <tag>sibling relationship</tag>
  <tag>swordplay</tag>
  <tag>magic</tag>
  <tag>supernatural</tag>
  <tag>undead</tag>
  <tag>coming of age</tag>
  <tag>tragedy</tag>
  <tag>based on manga</tag>
  <tag>demon</tag>
  <tag>mutilation</tag>
  <tag>dark fantasy</tag>
  <tag>anachronism</tag>
  <tag>shounen</tag>
  <tag>anime</tag>
  <tag>time skip</tag>
  <tag>taisho</tag>
  <tag>lacrimation</tag>
  <actor>
    <name>Natsuki Hanae</name>
    <role>Tanjiro Kamado (voice)</role>
    <thumb>https://image.tmdb.org/t/p/h632/A1lGrpBEdAUxZA7RoAw4Zr4ved3.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1256603</profile>
    <tmdbid>1256603</tmdbid>
  </actor>
  <actor>
    <name>Akari Kito</name>
    <role>Nezuko Kamado (voice)</role>
    <thumb>https://image.tmdb.org/t/p/h632/43SgANYtj7vpsHmz68hgPDlxC15.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1563442</profile>
    <tmdbid>1563442</tmdbid>
  </actor>
  <actor>
    <name>Kengo Kawanishi</name>
    <role>Muichiro Tokito (voice)</role>
    <thumb>https://image.tmdb.org/t/p/h632/fBzVAY4gcUvs5EfRWQMViYgp9xV.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1324472</profile>
    <tmdbid>1324472</tmdbid>
  </actor>
  <actor>
    <name>Kana Hanazawa</name>
    <role>Mitsuri Kanroji (voice)</role>
    <thumb>https://image.tmdb.org/t/p/h632/5bK9ttcRydQBWmyZp7gXDmJYOPF.jpg</thumb>
    <profile>https://www.themoviedb.org/person/119143</profile>
    <tmdbid>119143</tmdbid>
  </actor>
  <actor>
    <name>Nobuhiko Okamoto</name>
    <role>Genya Shinazugawa (voice)</role>
    <thumb>https://image.tmdb.org/t/p/h632/qyZpSYva9O9JQIZ0nVmXTf90FlL.jpg</thumb>
    <profile>https://www.themoviedb.org/person/1245094</profile>
    <tmdbid>1245094</tmdbid>
  </actor>
  <actor>
    <name>Toshio Furukawa</name>
    <role>Hantengu (voice)</role>
    <thumb>https://image.tmdb.org/t/p/h632/zZjO0NxIbEhYW3507gXVnvWeRkG.jpg</thumb>
    <profile>https://www.themoviedb.org/person/85286</profile>
    <tmdbid>85286</tmdbid>
  </actor>
  <actor>
    <name>Kohsuke Toriumi</name>
    <role>Gyokko (voice)</role>
    <thumb>https://image.tmdb.org/t/p/h632/vlRQ77taCCKh7itCZn3IWJUiYQr.jpg</thumb>
    <profile>https://www.themoviedb.org/person/122647</profile>
    <tmdbid>122647</tmdbid>
  </actor>
  <trailer>plugin://plugin.video.youtube/?action=play_video&amp;videoid=SWAMTXfqer0</trailer>
  <dateadded>2023-08-30 11:01:27</dateadded>
  <!--tinyMediaManager meta data-->
  <user_note/>
</tvshow>
        """
    }
}