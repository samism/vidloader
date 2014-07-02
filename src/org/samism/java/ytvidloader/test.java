package org.samism.java.ytvidloader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Pattern;

public class test {

	private static final String uaParam = null;

	private static String inbtwn(String a, String b, String c) {
		try {
			String[] arr1 = a.split(b);
			try {
				String[] arr2 = arr1[1].split(c);
				if ((arr2[0] != null) || (!arr2[0].equals(""))) return arr2[0];
			} catch (Exception ex) {
				return arr1[1];
			}
			return arr1[1];
		} catch (Exception ignored) {
		}
		return null;
	}

	public static void main(String[] args) {
		String error = null;
		try {
			String uParam = args[0];
			String uaParam = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30";
			System.out.println("param u: " + uParam + "\r\n");
			System.out.println("param ua: " + uaParam + "\r\n");
			if ((uParam.contains("youtube.com")) || (uParam.contains("youtu.be")))
				try {
					if (uParam.contains("youtu.be/"))
						uParam = getRedirUrl(uParam);
					String pageSource = getUrlContent(uParam + "&fmt=18");
					String video_id = inbtwn(pageSource, "shortlink\" href=\"http://youtu.be/", "\"");
					if (video_id == null)
						video_id = inbtwn(pageSource, "'VIDEO_ID': \"", "\"");
					if (video_id == null) {
						System.out.println("Trying Alternate Method...");
						video_id = inbtwn(uParam, "v=", "&");
						pageSource = getUrlContent("http://www.youtube.com/get_video_info?video_id=" + video_id + "&asv=3&el=detailpage&hl=en_US");
					}

					System.out.println("Getting Title...");
					String title = inbtwn(pageSource, "'VIDEO_TITLE': '", "',");
					if (title == null)
						title = inbtwn(pageSource, "name=\"title\" content=\"", "\"");
					if (title == null)
						title = URLDecoder.decode(inbtwn(pageSource, "&title=", "&"), "UTF-8").replace("+", " ");
					try {
						title = setHTMLEntity(title);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					System.out.println("Title: " + title + "\r\n");
					System.out.println("kv_info('" + title + "', 'youtube.com', 'http://www.youtube.com/watch?v=" + video_id + "', 'http://i.ytimg.com/vi/" + video_id + "/default.jpg');");

					String fmt_url_map = inbtwn(pageSource, "&amp;fmt_url_map=", "&");
					if (fmt_url_map == null)
						fmt_url_map = inbtwn(pageSource, "&fmt_url_map=", "&");
					if (fmt_url_map != null)
						fmt_url_map = URLDecoder.decode(fmt_url_map, "UTF-8");
					if (fmt_url_map == null)
						fmt_url_map = inbtwn(pageSource, "\"fmt_url_map\": \"", "\"").replace("\\/", "/");

					System.out.println("fmt_url_map: " + fmt_url_map + "\r\n");

					String dl_3gplow = null;
					String dl_3gpmed = null;
					String dl_3gphigh = null;
					String dl_flvlow = null;
					String dl_flvmed = null;
					String dl_flvmed2 = null;
					String dl_flvhigh = null;
					String dl_mp4high = null;
					String dl_mp4hd = null;
					String dl_mp4hd2 = null;
					String dl_mp4hd3 = null;
					String dl_webmhd = null;
					String dl_webmhd2 = null;

					String[] fmt_arr = fmt_url_map.split(",");
					for (String fmt : fmt_arr) {
						String[] parts = fmt.split(Pattern.quote("|"));
						String m_qual = URLDecoder.decode(parts[0], "UTF-8");
						String m_url = URLDecoder.decode(parts[1], "UTF-8");
						if (m_qual.equals("13")) dl_3gplow = m_url;
						if (m_qual.equals("17")) dl_3gpmed = m_url;
						if (m_qual.equals("36")) dl_3gphigh = m_url;
						if (m_qual.equals("5")) dl_flvlow = m_url;
						if (m_qual.equals("34")) dl_flvmed = m_url;
						if (m_qual.equals("6")) dl_flvmed2 = m_url;
						if (m_qual.equals("35")) dl_flvhigh = m_url;
						if (m_qual.equals("18")) dl_mp4high = m_url;
						if (m_qual.equals("22")) dl_mp4hd = m_url;
						if (m_qual.equals("37")) dl_mp4hd2 = m_url;
						if (m_qual.equals("38")) dl_mp4hd3 = m_url;
						if (m_qual.equals("43")) dl_webmhd = m_url;
						if (m_qual.equals("45")) dl_webmhd2 = m_url;
						System.out.println("URL: " + m_url);
					}

					if (error == null) {
						if (dl_flvlow != null)
							System.out.println("kv_ds('dl_flvlow', 'FLV', '240p', '" + dl_flvlow + "', '" + title + "');");
						if (dl_flvmed2 != null)
							System.out.println("kv_ds('dl_flvmed2', 'FLV', '360p', '" + dl_flvmed2 + "', '" + title + "');");
						if (dl_flvmed != null)
							System.out.println("kv_ds('dl_flvmed', 'FLV', '360p', '" + dl_flvmed + "', '" + title + "');");
						if (dl_flvhigh != null)
							System.out.println("kv_ds('dl_flvhigh', 'FLV', '480p', '" + dl_flvhigh + "', '" + title + "');");
						if (dl_mp4high != null)
							System.out.println("kv_ds('dl_mp4high', 'MP4', '360p', '" + dl_mp4high + "', '" + title + "');");
						if (dl_mp4hd != null)
							System.out.println("kv_ds('dl_mp4hd', 'MP4', '720p', '" + dl_mp4hd + "', '" + title + "');");
						if (dl_mp4hd2 != null)
							System.out.println("kv_ds('dl_mp4hd2', 'MP4', '1080p', '" + dl_mp4hd2 + "', '" + title + "');");
						if (dl_mp4hd3 != null)
							System.out.println("kv_ds('dl_mp4hd3', 'MP4', '3072p (Original)', '" + dl_mp4hd3 + "', '" + title + "');");
						if (dl_webmhd != null)
							System.out.println("kv_ds('dl_webmhd', 'WebM', '480p', '" + dl_webmhd + "', '" + title + "');");
						if (dl_webmhd2 != null)
							System.out.println("kv_ds('dl_webmhd', 'WebM', '720p', '" + dl_webmhd2 + "', '" + title + "');");
						try {
							String pageMob = getUrlContent("http://m.youtube.com/watch?ajax=1&layout=mobile&tsp=1&v=" + video_id);
							String[] splmini = pageMob.split("\"related_videos\":");
							pageMob = splmini[0];
							dl_3gphigh = inbtwn(pageMob, "\"stream_url\": \"", "\"").replace("\\/", "/");
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						if (dl_3gplow != null)
							System.out.println("kv_ds('dl_3gplow', '3GP', '144p', '" + dl_3gplow + "', '" + title + "');");
						if (dl_3gpmed != null)
							System.out.println("kv_ds('dl_3gpmed', '3GP', '144p', '" + dl_3gpmed + "', '" + title + "');");
						if (dl_3gphigh != null)
							System.out.println("kv_ds('dl_3gphigh', '3GP', '240p', '" + dl_3gphigh + "', '" + title + "');");
					} else {
						System.out.println("kv_error('" + error + "');");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getUrlContent(String url) {
		try {
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			if (url.contains("layout=mobile"))
				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
			else
				conn.setRequestProperty("User-Agent", uaParam);
			InputStream is = conn.getInputStream();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int bytesRead; (bytesRead = is.read(buffer)) != -1; ) {
				output.write(buffer, 0, bytesRead);
			}
			return output.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getRedirUrl(String url) {
		String hdr;
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.addRequestProperty("User-Agent", uaParam);
			hdr = conn.getHeaderField("location");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return hdr;
	}

	private static String setHTMLEntity(String input) {
		String output;
		output = input.replace("&amp;", "")
				.replace("&lt;", "")
				.replace("&gt;", "")
				.replace("&#39;", "")
				.replace("&quot;", "")
				.replace("&", "")
				.replace("amp;", "")
				.replace("\\\"", "")
				.replace("\\'", "")
				.replace("'", "")
				.replace("'", "")
				.replace("<", "")
				.replace(">", "")
				.replace("?", "")
				.replace("/", "")
				.replace(":", "")
				.replace(";", "")
				.replace("#", "");

		return output;
	}
}