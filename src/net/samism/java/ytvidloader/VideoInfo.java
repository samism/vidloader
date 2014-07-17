package net.samism.java.ytvidloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;

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
	private String id, title, author, videoInfo, videoPage, url, thumbUrl, description;

	//private final DownloaderGUI instance;

	private static final Logger log = LoggerFactory.getLogger(VideoInfo.class);

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
		//this.instance = instance;
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
			for (int bytesRead; (bytesRead = is.read(buffer)) != -1;) output.write(buffer, 0, bytesRead);
		} catch (IOException e) {
//			Object[] option = new Object[]{"OK"};
//			JTabbedPane tabs = instance.getTabs();
//			int msg = JOptionPane.showOptionDialog(instance,
//					"Couldn't start download.\n" +
//							"Check the URL, and/or your internet connection.",
//					"Error",
//					JOptionPane.OK_OPTION,
//					JOptionPane.ERROR_MESSAGE,
//					null,
//					option,
//					option[0]);
//			if ((msg == JOptionPane.CLOSED_OPTION || msg == JOptionPane.OK_OPTION)
//					&& tabs.getTabCount() > 0) {
//				tabs.removeTabAt(tabs.getTabCount());
//			}
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

		//uno = uno.replace("\\u0026", "&");
		//System.out.println(uno.contains("\\u0026"));
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

	private String obtain(Property prop, DownloaderGUI.Quality... qual){
		switch(prop){
			case TITLE:
				int s1 = videoInfo.indexOf("&title=") + "&title=".length();
				int s2 = videoInfo.indexOf("&", s1 + 5); //chances are a legit & in the title is 5+ chars down in the
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
				int startIndex = videoInfo.indexOf("url_encoded_fmt_stream_map=");
				int endIndex = videoInfo.lastIndexOf("&fallback_host=");

				String sub = videoInfo.substring(startIndex, endIndex);
				String[] urls = sub.split(",url=");
				Arrays.toString(urls);
			default:
				return "error";
		}
	}

//	String obtainDescription() {
//		int startIndex = videoInfo.indexOf("<meta property=\"og:description\" content=\"")
//				+ "<meta property=\"og:description\" content=\"".length();
//		int endIndex = videoInfo.indexOf("\">", startIndex);
//		return videoInfo.substring(startIndex, endIndex);
//	}
//
//	String obtainTitle() {
//		int startIndex = videoInfo.indexOf("<meta property=\"og:title\" content=\"")
//				+ "<meta property=\"og:title\" content=\"".length();
//		int endIndex = videoInfo.indexOf("\">", startIndex);
//		String title = videoInfo.substring(startIndex, endIndex);
//
//		//invalid characters for windows file names
//		title = title.replace("/", "");
//		title = title.replace("\\\\", "");
//		title = title.replace(":", "");
//		title = title.replace("*", "");
//		title = title.replace("?", "");
//		title = title.replace("\"", "");
//		title = title.replace("<", "");
//		title = title.replace(">", "");
//		title = title.replace("|", "");
//		return title;
//	}
//
//	String obtainAuthor() {
//		int startIndex = videoInfo.indexOf("by     <a href=\"/user/") + "by     <a href=\"/user/".length();
//		int endIndex = videoInfo.indexOf("\"", startIndex);
//		return videoInfo.substring(startIndex, endIndex);
//	}
//
//	String obtainUploadDate() {
//		int startIndex = videoInfo.indexOf("on <span id=\"eow-date\" class=\"watch-video-date\" >")
//				+ "on <span id=\"eow-date\" class=\"watch-video-date\" >".length();
//		int endIndex = videoInfo.indexOf("</span>", startIndex);
//		return videoInfo.substring(startIndex, endIndex);
//	}
//
//	String obtainThumbUrl() {
//		int startIndex = videoInfo.indexOf("<meta property=\"og:image\" content=\"")
//				+ "<meta property=\"og:image\" content=\"".length();
//		int endIndex = videoInfo.indexOf("\">", startIndex);
//		return videoInfo.substring(startIndex, endIndex);
//	}


	// getter methods

	public String getDescription() {
		return this.description;
	}

	public String getThumbUrl() {
		return this.thumbUrl;
	}

	public String getUrl() {
		return this.url;
	}

	public String getVideoId() {
		return this.id;
	}

	public String getVideoTitle() {
		return this.title;
	}

	public String getVideoUploader() {
		return this.author;
	}
}
