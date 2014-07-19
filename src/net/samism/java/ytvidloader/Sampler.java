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

/**
 * The purpose of this class is to download the data at https://youtube.com/get_video_info?video_id=xxx
 * repeatedly in order to understand the nature of how Google randomizes the metadata surrounding their
 * raw video URLs.
 *
 * What was learned: each request to the page yielded randomized results. There is no time barrier
 */
public class Sampler {

	public static void main(String[] args) {
		new Sampler().sample("Dmyz5X9OR4U", 5); //a random video ID, x amount of times
	}

	public Sampler() {
		//meh
	}

	public void sample(String id, int count) {
		for (int i = 0; i < count; i++) {
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

			Writer writer = null;

			try {
				String out = URLDecoder.decode(output.toString(), "UTF-8"); //google URLEncodes exactly 3 times
				out = URLDecoder.decode(out, "UTF-8");
				out = URLDecoder.decode(out, "UTF-8");
				out = StringUtils.substringAfter(out, "url_encoded_fmt_stream_map="); //don't need crap before this

				String prefix = out.substring(0, out.indexOf("=")); //name the file w/ prefix
				writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream("tests/" + prefix + "-" + i + ".txt"), "UTF-8"));
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
	}
}
