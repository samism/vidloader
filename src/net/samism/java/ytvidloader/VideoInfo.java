package net.samism.java.ytvidloader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * Author: Sameer Ismail
 * Date: 7/27/11
 * Time: 4:47 PM
 */

/*
 * This class is dedicated to parsing youtube data
 *
 */

public class VideoInfo {
	private static final Logger log = LoggerFactory.getLogger(VideoInfo.class);

	private String id, title, author, videoInfo, videoPage, url, thumbUrl, description;

	enum Property {
		TITLE, AUTHOR, THUMBURL, DESCRIPTION, URL
	}

	public VideoInfo(String link, DownloaderGUI.Quality qual) {
		this.id = link.substring(link.indexOf("=") + 1);
		this.videoInfo = getVideoInfo(id, "https://www.youtube.com/get_video_info?video_id=");
		this.videoPage = getVideoInfo(id, "https://www.youtube.com/watch?v=");
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
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30");
			is = conn.getInputStream();
			output = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int bytesRead; (bytesRead = is.read(buffer)) != -1; ) output.write(buffer, 0, bytesRead);
		} catch (IOException e) {
			log.error("Can't download anything, check your internet connection");
		}

		//don't URLdecode video page. only urldecode the info page
		return url.contains("watch") ? output.toString() : decodeCompletely(output.toString());
	}


	/**
	 * A private helper method to decode a url that has been recursively encoded.
	 * I'm relatively sure that youtube urlencodes their video info about 3 times, but a universal method is nice to
	 * have.
	 *
	 * @param encoded String that is URLEncoded
	 * @return String that is completely URLDecoded
	 */
	private String decodeCompletely(String encoded) {
		String uno = encoded;
		String dos = uno;

		do {
			uno = dos;
			try {
				dos = URLDecoder.decode(uno, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		} while (!uno.equals(dos));

		return uno; //can return either one at this point
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
			case TITLE:
				int s1 = videoInfo.indexOf("&title=") + "&title=".length();
				int s2 = videoInfo.indexOf("&", s1 + 5); //chances are a legit '&' in the title is 5+ chars down in the
				//title
				return videoInfo.substring(s1, s2);
			case AUTHOR:
				int _s1 = videoInfo.indexOf("&author=") + "&author=".length();
				int _s2 = videoInfo.indexOf("&", _s1 + 5); //chances are a legit & in the title is 5+ chars
				//down in the channel name, as well
				return videoInfo.substring(_s1, _s2);
			case THUMBURL:
				int __s1 = videoInfo.indexOf("&thumbnail_url=") + "&thumbnail_url=".length();
				int __s2 = videoInfo.indexOf("&", __s1);

				return videoInfo.substring(__s1, __s2);
			case DESCRIPTION:
				int ___s1 = videoPage.indexOf("<p id=\"eow-description\" >") + "<p id=\"eow-description\" >".length();
				int ___s2 = videoPage.indexOf("</p>", ___s1);

				return videoPage.substring(___s1, ___s2);
			case URL:
				//everything should already be completely url decoded by now
				//"url_encoded_fmt_stream_map=" denotes the start of the URLs
				//trim off everything before that
				String meta = StringUtils.substringAfter(videoInfo, "url_encoded_fmt_stream_map=");

				log.info("original: " + meta);

				//delim is the unique string that will denote where the previous URL ends and the next starts
				//must be delicately obtained because there are 5 unique parameters that could be found and
				//the delim might not be the quite the same for every URL it denotes
				//need to be careful that delim doesnt occur in the body of the URL, only denotes where it starts
				String delim = meta.substring(0, meta.indexOf('=', 1) + 1);
				String[] urls = meta.split("(?=" + delim + ")"); //split according to the delim, but keep it as prefix
				log.info("delimeter: " + delim);
				printLinks(urls);

				for (int i = 0; i < 1; i++) {
					//step 1. move the stuff before the start of the url to the end of it, append &title
					log.info("url before: " + urls[i]);

					String prefix = "";
					if (!urls[i].startsWith("url=")) { //sometimes there are no prefixed stuff. url is the 1st thing
						prefix = urls[i].substring(0, urls[i].indexOf("&url="));
					}

					log.info("prefixed properties: " + prefix);

					urls[i] = urls[i].substring(prefix.length()); //discard prefixed properties
					urls[i] = urls[i].substring(urls[i].indexOf("&url=") + "&url=".length()); //remove "&url="
					urls[i] += prefix; //add them back to the end

					if (StringUtils.endsWith(urls[i], ","))
						urls[i] = StringUtils.chop(urls[i]); //get rid of trailing comma
					log.info("after: " + urls[i]);

					try {
						urls[i] += "&title=" + URLEncoder.encode(title, "UTF-8"); //make sure title of video is url safe
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					//log.info("After title: " + urls[i]);

					//step 2. remove extraneous itag (causes the entire thing to break)
					Pattern p = Pattern.compile("&itag=\\d{1,3}"); //itag code usually 2-3 integers
					Matcher m = p.matcher(urls[i]);
					if (m.find()) { //only expecting to find 2 matches
						urls[i] = urls[i].replaceFirst(m.group(), "");
					}

					log.info("After itag: " + urls[i]);
					//printLinks(urls);
				}
			default:
				return "error";
		}
	}

	private void printLinks(String[] u) {
		for (String _u : u)
			log.info("url: " + _u);
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
