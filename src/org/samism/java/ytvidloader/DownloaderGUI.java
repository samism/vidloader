package org.samism.java.ytvidloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * Author: Sameer Ismail
 * Date: 7/28/11
 * Time: 3:36 AM
 */

public class DownloaderGUI extends JFrame {

	private static final Logger logger = LoggerFactory.getLogger(DownloaderGUI.class.getSimpleName());

	private static final ImageIcon ADD_IMAGE, REMOVE_IMAGE, WINDOW_LOGO, PAUSE_IMAGE, PLAY_IMAGE;

	private final ArrayList<VideoDownloader> downloaders = new ArrayList<VideoDownloader>();
	private final ArrayList<JProgressBar> bars = new ArrayList<JProgressBar>();
	private final ArrayList<DownloadWorker> workers = new ArrayList<DownloadWorker>();

	private String path = System.getProperty("user.dir") + "\\downloads",
			clipboardContents = null;

	private File pathFile = new File(path);

	private final JFrame me = this;
	private final JTabbedPane tabs = new JTabbedPane();
	private final JButton addTabButton, removeTabButton, pauseButton, playButton,
			browseButton = new JButton("browse"),
			dialogOkButton = new JButton("OK"),
			dialogCancelButton = new JButton("Cancel");
	private final JLabel saveToField = new JLabel(path);
	private final JTextField dialogUrlField = new JTextField();
	private final JFileChooser chooser = new JFileChooser();
	private final JDialog dialog = new JDialog(this, "Add Download");

	static {
		ADD_IMAGE = new ImageIcon(DownloaderGUI.class.getResource("add.png"));
		REMOVE_IMAGE = new ImageIcon(DownloaderGUI.class.getResource("remove.png"));
		PAUSE_IMAGE = new ImageIcon(DownloaderGUI.class.getResource("pause.jpg"));
		PLAY_IMAGE = new ImageIcon(DownloaderGUI.class.getResource("play.png"));
		WINDOW_LOGO = new ImageIcon(DownloaderGUI.class.getResource("logo.jpg"));
	}

	public DownloaderGUI() {
		if (!pathFile.exists()) {
			if (pathFile.mkdir()) {
				logger.info("downloads folder doesn't exist - created.");
			} else {
				logger.info("downloads folder doesn't exist - creation failed.");
			}
		}

		//cannot be assigned at declaration
		pauseButton = new JButton(PAUSE_IMAGE);
		playButton = new JButton(PLAY_IMAGE);
		addTabButton = new JButton(ADD_IMAGE);
		removeTabButton = new JButton(REMOVE_IMAGE);

		buildGUI();
		addListeners();

		//look and feel
		try {
			UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel");
		} catch (Exception e) { //cast up to avoid unlikely exceptions
			e.printStackTrace();
		}

		//required for laf to kick in
		SwingUtilities.updateComponentTreeUI(chooser);
		SwingUtilities.updateComponentTreeUI(dialog);
		SwingUtilities.updateComponentTreeUI(me);
	}

	private void buildGUI() {
		browseButton.setToolTipText("Select where to save your downloads");
		saveToField.setToolTipText("<html>Click to open: <br>" + path + "</html>");
		addTabButton.setToolTipText("Add a video download");
		removeTabButton.setToolTipText("Remove a video download");

		dialogUrlField.setPreferredSize(new Dimension(300, 25));
		saveToField.setPreferredSize(new Dimension(300, 25));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(pathFile);

		tabs.setPreferredSize(new Dimension(720, 450));
		tabs.setTabPlacement(JTabbedPane.LEFT);
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new FlowLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		//add shit
		Container mainPane = getContentPane();
		Container dialogPane = dialog.getContentPane();

		//download buttons panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addTabButton, BorderLayout.NORTH);
		buttonPanel.add(removeTabButton, BorderLayout.NORTH);
		buttonPanel.add(browseButton, BorderLayout.NORTH);
		buttonPanel.add(saveToField, BorderLayout.NORTH);

		//downloads tab
		mainPanel.add(buttonPanel, BorderLayout.NORTH);
		mainPanel.add(tabs, BorderLayout.CENTER);

		//main gui
		mainPane.add(mainPanel);

		//dialog frame
		dialogPanel.add(dialogUrlField);
		dialogPanel.add(dialogOkButton);
		dialogPanel.add(dialogCancelButton);
		dialogPane.add(dialogPanel);

		//dialog window properties
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setSize(new Dimension(350, 100));
		dialog.setLocationRelativeTo(me);
		dialog.setResizable(false);
		dialog.setVisible(false);

		//main window properties
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		setTitle("YouTube Downloader");
		setMinimumSize(new Dimension(750, 450));
		setIconImage(WINDOW_LOGO.getImage());
		setLocationRelativeTo(null);

	}

	private void addListeners() {
		//main gui components
		me.addWindowListener(new WindowListener());

		dialogUrlField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(final MouseEvent e) {
				if (clipboardContents != null && clipboardContents.startsWith("http://www.youtube.com/watch?v=")) {
					dialogUrlField.setText(clipboardContents);
				}
				logger.debug("Clipboard: " + clipboardContents);
			}

			public void mouseEntered(final MouseEvent e) {
				try {
					clipboardContents = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				} catch (UnsupportedFlavorException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				if (clipboardContents.startsWith("http://www.youtube.com/watch?v=")) {
					dialogUrlField.setToolTipText("<html>Click to paste:<br><br>" + clipboardContents + "</html>");
				}
			}
		});

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int value = chooser.showDialog(me, "Save here");
				if (value == JFileChooser.APPROVE_OPTION) {
					path = chooser.getSelectedFile().getAbsolutePath();
					saveToField.setText(path);
				}
			}
		});

		//dialog listeners
		dialogOkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String link = dialogUrlField.getText();
				if (link.startsWith("http://www.youtube.com/watch?v=")) {
					dialog.setVisible(false);

					VideoDownloader downer = new VideoDownloader(link, path, (DownloaderGUI) me);
					downloaders.add(downer);
					createTab("Video " + (tabs.getTabCount() + 1),
							downer.getVideoUrl(),
							downer.getVideoTitle(),
							downer.getVideoUploader(),
							downer.getUploadDate(),
							downer.getDescription());

					DownloadWorker worker = new DownloadWorker(downer);
					workers.add(worker);
					worker.execute();
				}
			}
		});

		dialogCancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});

		saveToField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(final MouseEvent e) {
				try {
					Desktop.getDesktop().open(new File(saveToField.getText()));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		addTabButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(true);
				dialogUrlField.setText("");
				dialogUrlField.grabFocus();
			}
		});

		removeTabButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				destroyTab();
			}
		});
	}

	private void destroyTab() {
		if (tabs.getTabCount() > 0) {
			int choice = JOptionPane.showConfirmDialog(me,
					"You will lose all progress. Are you sure?",
					"Stop download " + (tabs.getSelectedIndex() + 1),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (choice == JOptionPane.YES_OPTION && downloaders.size() > 0) {
				tabs.removeTabAt(tabs.getSelectedIndex());
				if (new File(downloaders.get(tabs.getSelectedIndex() + 1).getFileName()).delete()) {
					logger.info("download " + (tabs.getSelectedIndex() + 1)
							+ " terminated. File deleted.");
				} else {
					logger.info("download " + (tabs.getSelectedIndex() + 1)
							+ " terminated. File deletion failed.");
				}
				workers.get(tabs.getSelectedIndex() + 1).cancel(true); //stops the SwingWorker
			}
		}
	}

	private void destroyAllTabs() {
		if (tabs.getTabCount() > 0) {
			boolean didCancellations = false;

			while (tabs.getTabCount() > 0) {
				//stop all downloads by cancelling threads
				if (!didCancellations) {
					for (DownloadWorker worker : workers) {
						if (!worker.isCancelled())
							worker.cancel(true); //interrupt download
					}
					didCancellations = true;
				}

				//remove tabs
				int i = tabs.getSelectedIndex();

				tabs.removeTabAt(i);
				logger.info("trying to delete: "
						+ downloaders.get(tabs.getSelectedIndex() + 1).getVideoTitle());
				if (new File(downloaders.get(tabs.getSelectedIndex() + 1).getFileName()).delete()) {
					logger.info("download " + (tabs.getSelectedIndex() + 1)
							+ " terminated. File deleted.");
				} else {
					logger.info("download " + (tabs.getSelectedIndex() + 1)
							+ " terminated. File deletion failed.");
				}
				logger.info("removed tab " + i + " successfully");
			}
			logger.info("disposing of window");
			me.dispose();
			logger.info("program closing");
			System.exit(0);

		}
	}

	//incorporate getter/setter use of other class to get this methods info
	private void createTab(String tabTitle, String videoUrl, String videoTitle,
	                       String videoUploader, String uploadDate, String description) {
		URL imgUrl = null;

		try {
			imgUrl = new URL(downloaders.get(tabs.getSelectedIndex() + 1).getVideoImgUrl());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		JPanel newPanel = new JPanel();
		JLabel titleLabel = new JLabel("Title: " + videoTitle);
		JLabel uploaderLabel = new JLabel("Uploaded by: " + videoUploader + " on " + uploadDate);
		JLabel prevImg = new JLabel(new ImageIcon(imgUrl));
		JLabel descriptionLabel = new JLabel("<html>Description: " + description + "</html>");

		JTextField urlLabel = new JTextField("URL: " + videoUrl);

		urlLabel.setEditable(false);
		urlLabel.setBorder(null);
		urlLabel.setBackground(null);
		urlLabel.setCaretPosition(0);

		//descriptionLabel.setMaximumSize(new Dimension(800, 500));

		//keep instances of progress bars
		bars.add(new JProgressBar());

		newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
		newPanel.add(prevImg);
		newPanel.add(new JSeparator());
		newPanel.add(titleLabel);
		newPanel.add(uploaderLabel);
		newPanel.add(descriptionLabel);
		newPanel.add(urlLabel);
		newPanel.add(new JSeparator());
		newPanel.add(bars.get(tabs.getTabCount()));
		tabs.addTab(tabTitle, newPanel);
		tabs.setSelectedComponent(tabs.getComponentAt(tabs.getTabCount() - 1));
	}

	public JTabbedPane getTabs() {
		return this.tabs;
	}

	private class WindowListener extends WindowAdapter {

		final Object[] options = new Object[]{"I'm sure", "No! Keep 'em loading!"};

		public void windowClosing(WindowEvent e) {
			logger.info("Trying to exit on: " + tabs.getTabCount() + " tabs");

			if (tabs.getTabCount() > 0) {
				int user = JOptionPane.showOptionDialog(me,
						"You will lose all progress. Are you sure?",
						"Closing",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[1]);
				if (user == JOptionPane.YES_OPTION) {
					destroyAllTabs();
					logger.info("disposing of window");
					me.dispose();
					logger.info("program closing");
					System.exit(0);
				}
			} else {
				logger.info("disposing of window");
				me.dispose();
				logger.info("program closing");
				System.exit(0);
			}
		}
	}
}
