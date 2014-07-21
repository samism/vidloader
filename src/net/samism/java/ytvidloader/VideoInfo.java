package net.samism.java.ytvidloader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.samism.java.ytvidloader.StringUtils2.logStringArray;

/**
 * Created with IntelliJ IDEA.
 * Author: Sameer Ismail
 * Date: 7/27/11
 * Time: 4:47 PM
 */

/**
 * This class is dedicated to parsing youtube data
 */

public class VideoInfo {
	private static final Logger log = LoggerFactory.getLogger(VideoInfo.class);

	private String id, title, author, videoInfo, videoPage, url, thumbUrl, description, leanLinks;

	enum Property {
		TITLE, AUTHOR, THUMBURL, DESCRIPTION, URL
	}

	public VideoInfo(String link, DownloaderGUI.Quality qual) {
		this.id = StringUtils.substringAfter(link, "=");
		this.videoInfo = getVideoInfo(id, "https://www.youtube.com/get_video_info?video_id=");
		this.videoPage = getVideoInfo(id, "https://www.youtube.com/watch?v=");
		this.leanLinks = trimGarbage(videoInfo);
		this.title = obtain(Property.TITLE);
		this.author = obtain(Property.AUTHOR);
		this.thumbUrl = obtain(Property.THUMBURL);
		this.description = obtain(Property.DESCRIPTION);
		this.url = obtain(Property.URL, qual);
	}

	/**
	 * Obtains a raw video information file based on a given video id
	 * After this is parsed, it will provide the URLs for downloading the video
	 *
	 * @param id The YouTube video's id (ubiqitously found in any video URL)
	 * @return The full, raw, URL-Encoded information returned by http://www.youtube.com/get_video_info?video_id={id}
	 */

	private String getVideoInfo(String id, String url) {
		URL u;
		HttpURLConnection conn;
		InputStream is;
		ByteArrayOutputStream output = null;

		try {
			u = new URL(url + id);
			conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) " +
							"Chrome/12.0.742.122 Safari/534.30");
			is = conn.getInputStream();
			output = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int bytesRead; (bytesRead = is.read(buffer)) != -1; ) output.write(buffer, 0, bytesRead);
		} catch (IOException e) {
			log.error("Can't download anything, check your internet connection");
		}

		//don't URLdecode video page. only urldecode the info page
		return url.contains("watch") ?
				output.toString() :
				StringUtils2.decodeCompletely(output.toString());
	}


	/**
	 * Universal method to replace individual ones. Parses out a useful property from the
	 * raw video information.
	 *
	 * @param prop The property in question from the Property enum
	 * @param qual Only if case is Property.URL in which case the specific video link needs to be known
	 * @return Parsed property as a string
	 */

	private String obtain(Property prop, DownloaderGUI.Quality... qual) {
		switch (prop) {
			//im expecting the properties found in the html page to break periodically, as Google refactors & updates
			//their video page
			case TITLE:
				int s1 = StringUtils2.indexOfLastChar(videoInfo, "&title=");
				int s2 = videoInfo.indexOf("&", s1 + 5); //chances are a legit '&' in the title is 5+ chars down in the
				//title
				return videoInfo.substring(s1, s2);
			case AUTHOR:
				int _s1 = StringUtils2.indexOfLastChar(videoInfo, "&author=");
				int _s2 = videoInfo.indexOf("&", _s1 + 5); //chances are a legit & in the title is 5+ chars
				//down in the channel name, as well
				return videoInfo.substring(_s1, _s2);
			case THUMBURL:
				int __s1 = StringUtils2.indexOfLastChar(videoInfo, "&thumbnail_url=");
				int __s2 = videoInfo.indexOf("&", __s1);

				return videoInfo.substring(__s1, __s2);
			case DESCRIPTION:
				int ___s1 = StringUtils2.indexOfLastChar(videoPage, "<p id=\"eow-description\" >");
				int ___s2 = videoPage.indexOf("</p>", ___s1);

				return videoPage.substring(___s1, ___s2);
			case URL:
				//delim is the unique string that will denote where the previous URL ends and the next starts
				//must be delicately obtained because there are 5 unique parameters that could be found and
				//the delim might not be the quite the same for every URL it denotes
				//need to be careful that delim doesnt occur in the body of the URL, only denotes where it starts
				String delim = leanLinks.substring(0, leanLinks.indexOf('=', 1) + 1);
				delim = getDelimRegex(delim);
				String[] urls = leanLinks.split("(?=" + delim + ")"); //split according to the delim, but keep it as prefix
				log.info("delimeter: " + delim);
				log.info("Links just after splitting by delim:");

				logStringArray(urls, log);

				for (int i = 0; i < 1; i++) {
					if (StringUtils.endsWith(urls[i], ","))
						urls[i] = StringUtils.chop(urls[i]); //get rid of trailing comma

					//step 1. move the stuff before the start of the url to the end of it, append &title
					String prefix = "";
					if (!urls[i].startsWith("url=")) { //sometimes there are no prefixed stuff. url is the 1st thing
						prefix = urls[i].substring(0, urls[i].indexOf("&url="));
					}

					log.info("prefixed properties: " + prefix);

					urls[i] = urls[i].substring(prefix.length()); //discard prefixed properties
					urls[i] = StringUtils.substringAfter(urls[i], "&url="); //remove "&url="
					urls[i] += prefix; //add them back to the end

					log.info("after: " + urls[i]);

//					String fileName = title;
//					fileName = StringUtils2.normalizeForOS(fileName); //no illegal chars
//
//					try {
//						fileName = URLEncoder.encode(fileName, "UTF-8"); //urlencode
//					} catch (UnsupportedEncodingException e) {
//						e.printStackTrace();
//					}
//
//					urls[i] += "&title=" + fileName; //make sure title of video is url safe


//					//log.info("After title: " + urls[i]);
//
//					//step 2. remove extraneous itag (causes the entire thing to break)
//					Pattern p = Pattern.compile("&itag=\\d{1,3}"); //itag code usually 2-3 integers
//					Matcher m = p.matcher(urls[i]);
//					if (m.find()) { //only expecting to find 2 matches
//						urls[i] = urls[i].replaceFirst(m.group(), "");
//					}
//
//					log.info("After itag: " + urls[i]);
//					//printLinks(urls);
				}
			default:
				return "error";
		}

	}

	/**
	 * Purpose of this method is to take in the raw metadata from "get_video_info" and return a String
	 * free of any garbage metadata other than that of the URLs. Needed because URLs cannot be reliably parsed
	 * if the URLs are polluted with random other properties in between.
	 * <p/>
	 * Another function is to make sure there are no double commas or ampersands
	 *
	 * @param raw String of links+garbage
	 * @return String of only links
	 */
	private String trimGarbage(String raw) {
		//first, get rid of, and everything preceding, "url_encoded..."
		raw = StringUtils.substringAfter(videoInfo, "url_encoded_fmt_stream_map=");
		raw = raw.replaceAll("&adaptive_fmts=", "");

		log.info("raw before:" + raw);

		//all of the properties found in "get_video_info" that are not part of the URLs. Need to get rid of all of these
		//their values reside between a succeeding '=' and a '&'
		String[] garbage = new String[]{
				"muted", "cbrver", "avg_rating", "video_id", "iurlmaxres", "account_playback_token",
				"plid", "tmi", "cosver", "iurlhq", "iurlsd", "status", "watermark", "timestamp", "pltype",
				"allow_embed", "init", "sver", "mt", "author", "has_cc", "eventid", "iurl",
				"view_count", "hl", "idpj", "storyboard_spec", "no_get_video_log", "c", "video_verticals",
				"fexp", "sw", "enablecsi", "vq", "ldpj", "length_seconds", "ptk", "fmt_list", "dash",
				"csi_page_type", "use_cipher_signature", "track_embed", "token", "allow_ratings", "index",
				"loudness", "iurlmq", "thumbnail_url", "dashmpd", "cbr", "allowed_ads", "host_language",
				"ad_logging_flag", "midroll_prefetch_size", "afv", "allow_html5_ads", "sffb", "uid",
				"iv3_module", "ad_video_pub_id", "focEnabled", "rmktPingThreshold", "&pyv_in_related_cafe_experiment_id",
				"ad_host_tier", "excluded_ads", "iv_load_policy", "oid", "ytfocEnabled", "cos", "cc_module",
				"vid", "instream_long", "mpvid", "ttsurl", "key", "asr_langs", "v", "caps", "baseUrl",
				"midroll_freqcap", "gut_tag", "loeid", "ad_channel_code_overlay", "iv_allowed_in_place_switch",
				"ad_host", "ad_eurl", "afv_ad_tag", "client", "description_url", "host", "ht_id", "ytdevice",
				"yt_pt", "channel", "ptchn", "iv_invideo_url", "cta", "as_launched_in_country", "adsense_video_doc_id",
				"cc_font", "cc_asr", "ad_device", "cafe_experiment_id", "ad_module", "iv_module", "cid", "mts", "mws",
//				"sparams", "url", //normally ok but if included within garbage, become garbage
//				"adaptive_fmts", //this one often has no value &, breaks regex
				"title", "keywords" //these might have random &, breaks regex
		};

		for (String param : garbage) {
			String regex = "(?:^|)" + param + "=([^']*?&)";
			//raw = StringUtils.removePattern(raw, regex);
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(raw);

			while(m.find()){
				log.info("Regex: '" + regex + "' matched: " + m.group());
				raw = StringUtils.removePattern(raw, Pattern.quote(m.group())); //awesome
			}
		}

		//in case garbage param ends up as very LAST thing
		if(StringUtils2.containsItemFromList(raw, garbage)){
			raw = raw.substring(0, raw.lastIndexOf('&'));
		}

		log.info("raw after:" + raw);

		//double amps or commas might occur. can't have this throw off the parsing later
		raw = raw.replaceAll("&{2}", "&");
		raw = raw.replaceAll(",{2}", ",");

		return raw;
	}

	private String getDelimRegex(String delim) {
		switch (delim) {
			case "type=":
				return "type=video/[-a-zA-Z0-9]{3,}";
			case "quality=":
				return "quality=";
			case "itag=":
				return "itag=\\d{1,3}";
			case "url=":
				return "url=https://";
			case "fallback_host=":
				return "fallbackhost=tc.v";
			default:
				return null;
		}
	}

	// getter methods

	public String getDescription() {
		return this.description;
	}

	public String getThumbUrl() {
		return this.thumbUrl;
	}

	public String getVideoTitle() {
		return this.title;
	}

	public String getVideoUploader() {
		return this.author;
	}

	public String getUrl() {
		return this.url;
	}

	public String getVideoId() {
		return this.id;
	}
}
