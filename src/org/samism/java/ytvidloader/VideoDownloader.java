package org.samism.java.ytvidloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created with IntelliJ IDEA.
 * Author: Sameer Ismail
 * Date: 7/27/11
 * Time: 4:47 PM
 */

public class VideoDownloader {
	private final String videoUrl, savePath, videoId,
			videoTitle, uploader, videoPageSource, finalUrl,
			fileName, videoImgUrl, uploadDate, description;

	private final DownloaderGUI guiInstance;

	private static final Logger logger = LoggerFactory.getLogger(VideoDownloader.class.getSimpleName());

	public VideoDownloader(String videoUrl, String savePath, DownloaderGUI instance) {
		this.guiInstance = instance;
		this.videoUrl = videoUrl; //video page URL
		this.savePath = savePath; //local path to save video file
		this.videoId = videoUrl.substring(videoUrl.indexOf('=') + 1,
				videoUrl.indexOf('&') != -1 ? videoUrl.indexOf('&') : videoUrl.length());
		this.videoPageSource = getPageContent(videoUrl + videoId);
		this.videoTitle = obtainTitle(); //depends on variable above
		this.uploader = obtainUploader(); //depends on variable above
		this.videoImgUrl = obtainVideoImgUrl();
		this.uploadDate = obtainUploadDate();
		this.description = obtainDescription();
		this.fileName = savePath + "\\" //limit file name to first 50 chars
				+ videoTitle.substring(0, videoTitle.length() > 50 ? 50 : videoTitle.length())
				+ ".mp4"; //only supports mp4 video right now
		this.finalUrl = obtainFinalUrl(); //last piece of the puzzle
	}

	public int download() {
		int returnCode = 0;

		URL url;
		ReadableByteChannel rbc;
		FileOutputStream fos;

		try {
			url = new URL(videoUrl /* finalUrl*/);
//			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
//
//			long len = Long.parseLong(huc.getHeaderField("Content-Length"));
//
//			if (len > 0) {
			rbc = Channels.newChannel(url.openStream());
			fos = new FileOutputStream(fileName);
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
			fos.flush();
			fos.close();
//          }
		} catch (IOException e) {
			returnCode = -1;
			e.printStackTrace();
		}
		return returnCode;
	}

	private String getPageContent(String s) {
		URL u;
		HttpURLConnection conn;
		InputStream is;
		ByteArrayOutputStream output = null;

		try {
			u = new URL(s);
			conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30");
			is = conn.getInputStream();
			output = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int bytesRead; (bytesRead = is.read(buffer)) != -1; )
				output.write(buffer, 0, bytesRead);
		} catch (IOException e) {
			Object[] option = new Object[]{"OK"};
			JTabbedPane tabs = guiInstance.getTabs();
			int msg = JOptionPane.showOptionDialog(guiInstance,
					"Couldn't start download.\n" +
							"Check the URL, and/or your internet connection.",
					"Error",
					JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE,
					null,
					option,
					option[0]);
			if ((msg == JOptionPane.CLOSED_OPTION || msg == JOptionPane.OK_OPTION)
					&& tabs.getTabCount() > 0) {
				tabs.removeTabAt(tabs.getTabCount());
			}
		}

		return output.toString();
	}

	public String obtainFinalUrl() {
		int startIndex = videoPageSource.indexOf("\"url_encoded_fmt_stream_map\": \"")
				+ "\"url_encoded_fmt_stream_map\": \"".length();
		int endIndex = videoPageSource.indexOf("\"", startIndex);
		String encoded_list = videoPageSource.substring(startIndex, endIndex);
		String decoded_list = "null";

		try {
			while (!encoded_list.equals(URLDecoder.decode(encoded_list, "UTF-8"))) {
				encoded_list = URLDecoder.decode(encoded_list, "UTF-8");
			}
			encoded_list = encoded_list.replace("\\u0026", "&");
			decoded_list = encoded_list;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String firstURL = decoded_list.substring(13, decoded_list.indexOf(",itag="));
		String[] urls = decoded_list.split(",itag=([5-9]|[1-9][0-9]|10[0-2])&url=");
		for (String url : urls)
			logger.info(url);


		return "";
	}

	String obtainDescription() {
		int startIndex = videoPageSource.indexOf("<meta property=\"og:description\" content=\"")
				+ "<meta property=\"og:description\" content=\"".length();
		int endIndex = videoPageSource.indexOf("\">", startIndex);
		return videoPageSource.substring(startIndex, endIndex);
	}

	String obtainTitle() {
		int startIndex = videoPageSource.indexOf("<meta property=\"og:title\" content=\"")
				+ "<meta property=\"og:title\" content=\"".length();
		int endIndex = videoPageSource.indexOf("\">", startIndex);
		String title = videoPageSource.substring(startIndex, endIndex);

		//invalid characters for windows file names
		title = title.replace("/", "");
		title = title.replace("\\\\", "");
		title = title.replace(":", "");
		title = title.replace("*", "");
		title = title.replace("?", "");
		title = title.replace("\"", "");
		title = title.replace("<", "");
		title = title.replace(">", "");
		title = title.replace("|", "");
		return title;
	}

	String obtainUploader() {
		int startIndex = videoPageSource.indexOf("by     <a href=\"/user/") + "by     <a href=\"/user/".length();
		int endIndex = videoPageSource.indexOf("\"", startIndex);
		return videoPageSource.substring(startIndex, endIndex);
	}

	String obtainUploadDate() {
		int startIndex = videoPageSource.indexOf("on <span id=\"eow-date\" class=\"watch-video-date\" >")
				+ "on <span id=\"eow-date\" class=\"watch-video-date\" >".length();
		int endIndex = videoPageSource.indexOf("</span>", startIndex);
		return videoPageSource.substring(startIndex, endIndex);
	}

	String obtainVideoImgUrl() {
		int startIndex = videoPageSource.indexOf("<meta property=\"og:image\" content=\"")
				+ "<meta property=\"og:image\" content=\"".length();
		int endIndex = videoPageSource.indexOf("\">", startIndex);
		return videoPageSource.substring(startIndex, endIndex);
	}

	public String getDescription() {
		return description;
	}

	public String getUploadDate() {
		return uploadDate;
	}

	public String getVideoImgUrl() {
		return videoImgUrl;
	}

	public String getFileName() {
		return fileName;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public String getSavePath() {
		return savePath;
	}

	public String getVideoId() {
		return videoId;
	}

	public String getVideoTitle() {
		return videoTitle;
	}

	public String getVideoUploader() {
		return this.uploader;
	}
}
