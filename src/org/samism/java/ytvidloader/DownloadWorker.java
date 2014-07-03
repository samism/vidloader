package org.samism.java.ytvidloader;

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

	private static final Logger logger = LoggerFactory.getLogger(DownloadWorker.class.getSimpleName());

	private final VideoDownloader downer;

	DownloadWorker(VideoDownloader downer) {
		this.downer = downer;
	}

	@Override
	protected Integer doInBackground() {
		return downer.download();
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

		logger.info("Video download for: \"" + downer.getVideoTitle() + "\" done with code: " + code);

		switch(code){
			case -2: logger.debug("Something wrong w/ concurrency, or dl took more than 24h");
				break;
			case -1: logger.debug("Could not extract the video file/data");
				break;
			case 0: logger.debug("Download went smoothly, code-wise.");
				break;
		}
	}
}
