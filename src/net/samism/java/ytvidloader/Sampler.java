package net.samism.java.ytvidloader;

/**
 * Created with IntelliJ IDEA.
 * User: samism
 * Date: 7/18/14
 * Time: 3:23 AM
 */

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The purpose of this class is to download the data at https://youtube.com/get_video_info?video_id=xxx
 * repeatedly in order to understand the nature of how Google randomizes the metadata surrounding their
 * raw video URLs.
 * <p/>
 * What was learned:
 * -each request to the page yielded randomized results. There is no time barrier
 * -There are exactly 80 parameters.
 */
public class Sampler {
	String test = "quality=hd720&fallback_host=tc.v3.cache1.googlevideo.com&itag=22&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&sparams=id,ip,ipbits,itag,ratebypass,requiressl,source,upn,expire&itag=22&signature=C0AF4E9A5C702CEC75480632F7B2DC6DF24B23B5.89BB16B0F0162A89146060C8EBAA644F16C2CE62&upn=JpNok5-l8dY&ip=73.36.121.23&key=yt5&expire=1405868400&sver=3&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&ratebypass=yes&mv=m&ipbits=0&mt=1405843719&mws=yes&type=video/mp4; codecs=\"avc1.64001F, mp4a.40.2\",quality=medium&fallback_host=tc.v8.cache5.googlevideo.com&itag=43&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&sparams=id,ip,ipbits,itag,ratebypass,requiressl,source,upn,expire&itag=43&signature=FA5212AD5AD30560D6FDAA02AF48B2E3E5175AC7.520C605AF70768AB4F609EECED18DB3900F29647&upn=JpNok5-l8dY&ip=73.36.121.23&key=yt5&expire=1405868400&sver=3&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&ratebypass=yes&mv=m&ipbits=0&mt=1405843719&mws=yes&type=video/webm; codecs=\"vp8.0, vorbis\",quality=medium&fallback_host=tc.v2.cache1.googlevideo.com&itag=18&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&sparams=id,ip,ipbits,itag,ratebypass,requiressl,source,upn,expire&itag=18&signature=4FE3D9A5C3ED730A75E8AB6C6FED77E8E12ACA27.96A1D0A42900DD9A76F5FC537610EEDE7BE92215&upn=JpNok5-l8dY&ip=73.36.121.23&key=yt5&expire=1405868400&sver=3&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&ratebypass=yes&mv=m&ipbits=0&mt=1405843719&mws=yes&type=video/mp4; codecs=\"avc1.42001E, mp4a.40.2\",quality=small&fallback_host=tc.v7.cache4.googlevideo.com&itag=5&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&itag=5&signature=3372A6DC2A44EE4166B7C1328780062371AD1861.7027BB18FF2A0EDBE3EDE81A766F40356400F4F5&upn=JpNok5-l8dY&ip=73.36.121.23&key=yt5&expire=1405868400&sver=3&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=id,ip,ipbits,itag,requiressl,source,upn,expire&mv=m&ipbits=0&mt=1405843719&mws=yes&type=video/x-flv,quality=small&fallback_host=tc.v23.cache7.googlevideo.com&itag=36&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&itag=36&signature=1DCE10299E781AAAE55BE38A54407E13E0415405.F2CFA66B09C8A65FF3F4DD963A7418EA801130E4&upn=JpNok5-l8dY&ip=73.36.121.23&key=yt5&expire=1405868400&sver=3&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=id,ip,ipbits,itag,requiressl,source,upn,expire&mv=m&ipbits=0&mt=1405843719&mws=yes&type=video/3gpp; codecs=\"mp4v.20.3, mp4a.40.2\",quality=small&fallback_host=tc.v21.cache3.googlevideo.com&itag=17&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&itag=17&signature=E5F174F0BC91289D2B98E4E3742D40F9A6CF0BDB.475E675EEE22D5FE39AC3605CE4250C63536932C&upn=JpNok5-l8dY&ip=73.36.121.23&key=yt5&expire=1405868400&sver=3&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=id,ip,ipbits,itag,requiressl,source,upn,expire&mv=m&ipbits=0&mt=1405843719&mws=yes&type=video/3gpp; codecs=\"mp4v.20.3, mp4a.40.2\"&allowed_ads=[0, 1, 2, 4, 8, 10]&watermark=,https://s.ytimg.com/yts/img/watermark/youtube_watermark-vflHX6b6E.png,https://s.ytimg.com/yts/img/watermark/youtube_hd_watermark-vflAzLcD6.png&host_language=en&ad_logging_flag=1&iurl=https://i1.ytimg.com/vi/xKgUwR5iMNY/hqdefault.jpg&midroll_prefetch_size=1&title=Ogus & Frank Yang Pt 1 (Vlog #270)&storyboard_spec=https://i1.ytimg.com/sb/xKgUwR5iMNY/storyboard3_L$L/$N.jpg|48#27#100#10#10#0#default#uBeanfvqpD3Qatukd6Q7sXmoirM|80#45#154#10#10#5000#M$M#1O-mN58Eq0c-s6D80bCx9OZProQ|160#90#154#5#5#5000#M$M#qWfdL616_ouBF4Q838qGwljqKHM&vq=auto&afv=True&iurlhq=https://i1.ytimg.com/vi/xKgUwR5iMNY/hqdefault.jpg&iurlsd=https://i1.ytimg.com/vi/xKgUwR5iMNY/sddefault.jpg&allow_html5_ads=1&sffb=True&uid=YvInCvFq9mYzn6YsB5zsZQ&cbr=Chrome&length_seconds=764&pltype=content&iv3_module=1&ad_video_pub_id=ca-pub-6219811747049371&hl=en_US&focEnabled=1&rmktPingThreshold=0&no_get_video_log=1&pyv_in_related_cafe_experiment_id=&ad_host_tier=3936790&excluded_ads=3=1_1,1_2,1_2_1,1_3,2_2,2_2_1,2_3&iv_load_policy=1&oid=vmx7QQ0Uxut0mm7GIXzhzw&dash=1&thumbnail_url=https://i1.ytimg.com/vi/xKgUwR5iMNY/default.jpg&ytfocEnabled=1&ptk=flexforall2 user&cos=Windows&cc_module=https://s.ytimg.com/yts/swfbin/player-vflmDyk47/subtitle_module.swf&vid=xKgUwR5iMNY&use_cipher_signature=False&instream_long=False&allow_ratings=1&mpvid=JIs3v0lNHGT0cY5x&dashmpd=https://manifest.googlevideo.com/api/manifest/dash/requiressl/yes/playback_host/r14---sn-vgqs7n7z.googlevideo.com/itag/0/signature/0829809FFCB5BDC7BDBE39F077740092F0390490.996F63216937011DD7F3120F152C3BCF5EE3D796/upn/deO0030FMKA/fexp/902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022/ip/73.36.121.23/key/yt5/expire/1405868400/sver/3/as/fmp4_audio_clear,webm_audio_clear,fmp4_sd_hd_clear,webm_sd_hd_clear,webm2_sd_hd_clear/source/youtube/ms/au/id/o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W/sparams/as,id,ip,ipbits,itag,playback_host,requiressl,source,expire/mv/m/ipbits/0/mt/1405843719/mws/yes&idpj=-2&ttsurl=https://www.youtube.com/api/timedtext?expire=1405869055&key=yttt1&hl=en_US&asr_langs=en,pt,ja,nl,fr,it,ko,de,ru,es&v=xKgUwR5iMNY&signature=0B9CABC117A065E96B99C7245C33959F7377FFDC.5327CA107087CB080E9E52E6752D7F6CD5E9DE3E&caps=asr&sparams=asr_langs,caps,v,expire&iurlmq=https://i1.ytimg.com/vi/xKgUwR5iMNY/mqdefault.jpg&avg_rating=3.60805860806&baseUrl=https://googleads.g.doubleclick.net/pagead/viewthroughconversion/962985656/&midroll_freqcap=420.0&account_playback_token=QUFFLUhqbjBBZTdwZndqX0phNHJZRHZhYXl2U3kyM3laUXxBQ3Jtc0ttNkN5bjhvRDZaLWw4a1NPSUpabGJEWVRRYUhERVFZeDBOWVRPUFAzaGJ1dDIyRW1KeVo3VXJvaUFnb0F3OGlCREdFUWtuTE5WTk1xV3E3andJaXlZUERRYUxPbHNrdFg1TVJ5b3Z4TWIxMlU1djN1bw==&view_count=31170&gut_tag=/4061/ytunknown/main&enablecsi=1&track_embed=1&loeid=914095,934804,935662,941437,946022&ad_channel_code_overlay=yt_mpvid_JIs3v0lNHGT0cY5x,yt_cid_523186,ivpypp,yt_no_ap,ytdevice_1,afv_user_id_YvInCvFq9mYzn6YsB5zsZQ,afv_user_flexforall2,ytel_embedded,ytps_default,Vertical_211,afv_overlay,invideo_overlay_480x70_cat17&ldpj=-23&tmi=1&eventid=j3nLU9LMOtPYqAOf_4GYDQ&adaptive_fmts=bitrate=4497841&clen=386932227&itag=137&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=137&lmt=1405591944507515&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=6F1B9CB4932D02104A8BB072DE0C0C7FA4ACD4F5.57F86B0CD23D55CF6E9CBB29636949018703BFA6&clen=386932227&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.362&type=video/mp4; codecs=\"avc1.640028\"&size=1920x1080&init=0-710&index=711-2578&lmt=1405591944507515,bitrate=3429391&clen=182435607&itag=248&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=248&lmt=1405830774753847&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=75D6A3280FCC7304DF3E66B635821256549E2ED5.9445C3470967072A059715FED423E57C317EC520&clen=182435607&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.329&type=video/webm; codecs=\"vp9\"&size=1920x1080&init=0-234&index=235-2965&lmt=1405830774753847,bitrate=2232027&clen=197839527&itag=136&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=136&lmt=1405591923404582&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=BD8AB8BADCF1C4EEDF24B7C80EE31E73C983B038.30EAC1EBF24C94F8A6BA0D02CF945ACC9F494DA0&clen=197839527&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.362&type=video/mp4; codecs=\"avc1.4d401f\"&size=1280x720&init=0-708&index=709-2576&lmt=1405591923404582,bitrate=1933666&clen=111256142&itag=247&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=247&lmt=1405843300745589&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=5C8F08EA51FAED00F7356398955CE13377C63A72.42AB29046A51AE5DAC43EC181FFA195105EA3DD8&clen=111256142&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.329&type=video/webm; codecs=\"vp9\"&size=1280x720&init=0-234&index=235-2957&lmt=1405843300745589,bitrate=1115163&clen=99644032&itag=135&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=135&lmt=1405591912675926&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=1351312C50397D8B1848109FEEC22993E7028DE5.A6A93BDB44027E9E8CD36FED58C54B5C7EEE7641&clen=99644032&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.362&type=video/mp4; codecs=\"avc1.4d401f\"&size=854x480&init=0-708&index=709-2576&lmt=1405591912675926,bitrate=887042&clen=56935844&itag=244&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=244&lmt=1405843275432620&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=4B4DA00FC8B44CE7FDA005AC68C44EFB6C401E3F.26617A2E05D8F3D73C40B12530F75A992DF82377&clen=56935844&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.329&type=video/webm; codecs=\"vp9\"&size=854x480&init=0-234&index=235-2935&lmt=1405843275432620,bitrate=614139&clen=52668360&itag=134&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=134&lmt=1405591900271436&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=214E51A114C5D0E97035D360A7C279F6A102F48E.4C7DBB5C7792EC68D20CD7760C7F2085855FFC88&clen=52668360&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.362&type=video/mp4; codecs=\"avc1.4d401e\"&size=640x360&init=0-708&index=709-2576&lmt=1405591900271436,bitrate=626752&clen=29860423&itag=243&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=243&lmt=1405843138997564&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=B4AD908E1F14D2F676CF283DD8488572D5A2BDDD.900CF1177BAD9C8919C179DF8A655577366F0E6F&clen=29860423&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.329&type=video/webm; codecs=\"vp9\"&size=640x360&init=0-234&index=235-2901&lmt=1405843138997564,bitrate=249347&clen=23136317&itag=133&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=133&lmt=1405591901070505&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=5391ADE2087A100B15E3467D057EF629113E7A9D.44DF6400E122EB82F58B7AE1A9E2EBEB12026964&clen=23136317&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.362&type=video/mp4; codecs=\"avc1.4d4015\"&size=426x240&init=0-672&index=673-2540&lmt=1405591901070505,bitrate=343704&clen=15796968&itag=242&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=242&lmt=1405842787718809&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=8990DB4D86F8950CB0AFBE1AA317BFCB6A963004.453E341DB5A63D7E9CBB0667199BDC48E97D999E&clen=15796968&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.329&type=video/webm; codecs=\"vp9\"&size=426x240&init=0-233&index=234-2824&lmt=1405842787718809,bitrate=114199&clen=10176409&itag=160&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=160&lmt=1405591898862019&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=5658E081D0B13A1724CD58315AF251A78FD34E21.3067A96A8CD2CE100B3E4C2E990A6AA16303EC0E&clen=10176409&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.362&type=video/mp4; codecs=\"avc1.42c00c\"&size=256x144&init=0-670&index=671-2538&lmt=1405591898862019,bitrate=129787&clen=12256628&itag=140&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=140&lmt=1405591892367520&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=DDA3FAC87DA5D65B56D3013F907BDE6621133D03.3969C02B6EA5E08E1CA68EDAEA6A5775990D7FB1&clen=12256628&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.425&type=audio/mp4; codecs=\"mp4a.40.2\"&init=0-591&index=592-1547&lmt=1405591892367520,bitrate=104857&clen=8013452&itag=171&url=https://r14---sn-vgqs7n7z.googlevideo.com/videoplayback?requiressl=yes&mws=yes&itag=171&lmt=1405837651664381&mv=m&sver=3&upn=bExJXXba5ns&ip=73.36.121.23&key=yt5&expire=1405868400&signature=B61AF9C6EC4444D74BF8C273CF5EAA7896C885BB.E839B1B65F41A373B903902E1302EC42781E29FC&clen=8013452&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&source=youtube&ms=au&id=o-ADkD0ONNJwnr2m5UAMw_k_zufb2O_QIEOm5oBd9hVE3W&sparams=clen,dur,gir,id,ip,ipbits,itag,lmt,requiressl,source,upn,expire&gir=yes&ipbits=0&mt=1405843719&dur=763.360&type=audio/webm; codecs=\"vorbis\"&init=0-4451&index=4452-5757&lmt=1405837651664381&author=flexforall2&iv_allow_in_place_switch=1&ad_host=ca-host-pub-2527939568533888&ad_eurl=http://www.youtube.com/video/xKgUwR5iMNY&afv_ad_tag=http://googleads.g.doubleclick.net/pagead/ads?ad_type=text_image_flash&client=ca-pub-6219811747049371&description_url=http://www.youtube.com/video/xKgUwR5iMNY&hl=en&host=ca-host-pub-2527939568533888&ht_id=3936790&loeid=914095,934804,935662,941437,946022&url=http://www.youtube.com/video/xKgUwR5iMNY&ytdevice=1&yt_pt=APb3F2_3DXnmsc4BYT3KMe4Qb65sQSY_qdJUxUV2TaWeD475MT_8FNzt20mwVc8niHgY-4uIPoyPQnL1629QUhXXgITrz1J0y71P1ohb9YRiePZqSZvMfStaGuXFgCvDC7Gr6n0MyYm5qzb7fF_y&channel=yt_mpvid_JIs3v0lNHGT0cY5x yt_cid_523186 ivpypp yt_no_ap ytdevice_1 afv_user_id_YvInCvFq9mYzn6YsB5zsZQ afv_user_flexforall2 ytel_embedded ytps_default Vertical_211 afv_overlay invideo_overlay_480x70_cat17&c=WEB&ptchn=YvInCvFq9mYzn6YsB5zsZQ&muted=0&video_verticals=[]&cbrver=12.0.742.122&plid=AAT-m4elQYgkwANR&iv_invideo_url=https://www.youtube.com/annotations_invideo?cap_hist=1&cta=2&video_id=xKgUwR5iMNY&as_launched_in_country=1&cosver=6.1&adsense_video_doc_id=yt_xKgUwR5iMNY&video_id=xKgUwR5iMNY&csi_page_type=embed&keywords=deadlifts,LoA,Legends,Aesthetics,Weight,Fitness,Loss,Exercise,Workout,Muscle,Body,Bodybuilding,Health,Training,Gym,Years,Personal,Muscles,Lose,Flex,Healthy,Trainer,Nutrition,Posing,Exercises,Pack,Building,Yoga,Cardio,Strong,Gain,Losing,Biggest,Six,Strength,Chest,Challenge,Program,Muscular,Pounds,Journey,Routine,Tips,P90x,Food,Olympia,Shape,Pose,Arms,heath,Burn,Obesity,Lifting,Transformation,Calories,Weights,Flexforall,Matt,Ogus,Coleman,physiques,of,greatness,hodgetwins,POG,twinmuscleworkout&cc_font=Arial Unicode MS, arial, verdana, _sans&status=ok&cc_asr=1&fexp=902408,914095,924222,927622,930008,934024,934030,934804,935662,941437,946022&yt_pt=APb3F2_3DXnmsc4BYT3KMe4Qb65sQSY_qdJUxUV2TaWeD475MT_8FNzt20mwVc8niHgY-4uIPoyPQnL1629QUhXXgITrz1J0y71P1ohb9YRiePZqSZvMfStaGuXFgCvDC7Gr6n0MyYm5qzb7fF_y&ad_device=1&cafe_experiment_id=&has_cc=True&ad_module=https://s.ytimg.com/yts/swfbin/player-vflmDyk47/ad.swf&timestamp=1405843855&iv_module=https://s.ytimg.com/yts/swfbin/player-vflmDyk47/iv_module.swf&cid=523186";
	public static void main(String[] args) {
		new Sampler();
	}

	public Sampler() {
		HashSet<String> big_list = new HashSet<>();

		big_list.addAll(getAllParams(test));

//		for(int i = 0; i < 50; i++){
//			HashSet<String> params = getAllParams(sample("OlGgLCBU4Ws", false));
//			//System.out.println("Added " + params.size() + " new params this iteration");
//			big_list.addAll(params);
//		}

		//StringUtils2.printStringArray(big_list.toArray(new String[big_list.size()]), "");
		StringUtils2.printAsStringArray(big_list.toArray(new String[big_list.size()]));

		System.out.println("length: " + big_list.size() + " params total.");
	}

	public HashSet<String> getAllParams(String c) {
		HashSet<String> params = new HashSet<>(); //prevents duplicate elements

		Pattern p = Pattern.compile("(&|,)[\\w\\d_,]+=");
		Matcher m = p.matcher(c);

		while (m.find()) {
			String match = m.group();
			match = match.substring(1, match.length() - 1); //StringUtils.substringBetween(match, "&", "=");

			params.add(match);
		}

		return params;
	}

	/**
	 * @param id
	 * @param writeToFile
	 * @return If writeToFile is true, returns null, else returns the content
	 */

	public String sample(String id, boolean writeToFile) {
		String out = null;

		URL u;
		HttpURLConnection conn;
		InputStream is;
		ByteArrayOutputStream output = null;

		try {
			u = new URL("https://youtube.com/get_video_info?video_id=" + id);
			conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30");
			is = conn.getInputStream();
			output = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int bytesRead; (bytesRead = is.read(buffer)) != -1; ) output.write(buffer, 0, bytesRead);
		} catch (IOException e) {
			e.getStackTrace();
		}


		out = StringUtils2.decodeCompletely(output.toString());
		out = StringUtils.substringAfter(out, "url_encoded_fmt_stream_map="); //don't need crap before this


		if (writeToFile) {
			Writer writer = null;

			try {
				String prefix = out.substring(0, out.indexOf("=")); //name the file w/ prefix
				writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream("tests/" + prefix + "-" + ".txt"), "UTF-8"));
				writer.write(out);
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					writer.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		return out;
	}
}
