package net.samism.java.ytvidloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created with IntelliJ IDEA.
 * Author: Sameer Ismail
 * Date: 7/30/11
 * Time: 10:15 PM
 */

public class DownloadWorker extends SwingWorker<Integer, Void> {

	private static final Logger log = LoggerFactory.getLogger(DownloadWorker.class);

	private final VideoInfo downer;

	DownloadWorker(VideoInfo downer) {
		this.downer = downer;
	}

	@Override
	protected Integer doInBackground() {
//		URL url;
//		ReadableByteChannel rbc;
//		FileOutputStream fos;
//
//		try {
//			url = new URL(/* url*/);
//			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
//
//			long len = Long.parseLong(huc.getHeaderField("Content-Length"));
//
//			if (len > 0) {
//				rbc = Channels.newChannel(url.openStream());
//				fos = new FileOutputStream(fileName);
//				fos.getChannel().transferFrom(rbc, 0, 1 << 24);
//				fos.flush();
//				fos.close();
//          }
//		} catch (IOException e) {
//			returnCode = -1;
//			e.printStackTrace();
//		}
		return 0;
	}

	@Override
	protected void done() {
		int code = -2; //allow 24 hours for a video download until timeout
		//code = 0 for success, -1 for error in downloading, -2 for exceptions in this class

		try {
			code = get(24, TimeUnit.HOURS);
		} catch (InterruptedException | TimeoutException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		log.info("Video download for: \"" + downer.getVideoTitle() + "\" done with code: " + code);

		switch(code){
			case -2: log.debug("Something wrong w/ concurrency, or dl took more than 24h");
				break;
			case -1: log.debug("Could not extract the video file/data");
				break;
			case 0: log.debug("Download went smoothly, code-wise.");
				break;
		}
	}
}
