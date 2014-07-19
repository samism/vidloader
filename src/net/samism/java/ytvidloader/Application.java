package net.samism.java.ytvidloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * Author: Sameer Ismail
 * Date: 7/27/11
 * Time: 4:46 PM
 */

public class Application {
	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DownloaderGUI gui = new DownloaderGUI();
				gui.setVisible(true);
				gui.requestFocusInWindow();
			}
		});
	}
}
