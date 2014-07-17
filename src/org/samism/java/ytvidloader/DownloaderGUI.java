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

	private static final Logger log = LoggerFactory.getLogger(DownloaderGUI.class);

	private static final ImageIcon ADD_IMAGE, REMOVE_IMAGE, WINDOW_LOGO, PAUSE_IMAGE, PLAY_IMAGE;

	private final ArrayList<VideoInfo> downloaders = new ArrayList<>();
	private final ArrayList<JProgressBar> bars = new ArrayList<>();
	private final ArrayList<DownloadWorker> workers = new ArrayList<>();

	String clipboardContent = "";
	
	File defaultDownloadsDir = new File(System.getProperty("user.home") + "/Downloads"); //OS created downloads directory
	File newDownloadsDir = new File(System.getProperty("user.dir") + "/Downloads"); //alternative to above if not found
	File workingDir = defaultDownloadsDir; //by default is the OS downloads dir. may be altered in the constructor

	enum Quality {
		//highest grabs the largest integer itag
		//default is mp4
		//lowest, etc
		HIGHEST, DEFAULT, LOWEST
	}

	private Quality qual = Quality.DEFAULT;

	private final JFrame me = this;
	private final JTabbedPane tabs = new JTabbedPane();
	private final JButton addTabButton, removeTabButton, pauseButton, playButton,
			browseButton = new JButton("browse"),
			dialogOkButton = new JButton("OK"),
			dialogCancelButton = new JButton("Cancel");
	private final JLabel saveToField = new JLabel("Loading downloads directory...");
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
		//check if OS download dir exists. if it doesn't, create a custom one in the current directory
		if (!defaultDownloadsDir.exists()) {
			if (newDownloadsDir.mkdir()) {
				log.info("OS downloads folder doesn't exist, a local one was created");
				workingDir = newDownloadsDir;
			} else {
				log.info("downloads folder doesn't exist - creation failed.");
			}
		}

		saveToField.setText(workingDir.getAbsolutePath());

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
		saveToField.setToolTipText("Click to open");
		addTabButton.setToolTipText("Add a video download");
		removeTabButton.setToolTipText("Remove a video download");

		dialogUrlField.setPreferredSize(new Dimension(300, 25));
		saveToField.setPreferredSize(new Dimension(300, 25));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(workingDir);

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
				if (!clipboardContent.isEmpty() && clipboardContent.startsWith("https://www.youtube.com/watch?v=")) {
					dialogUrlField.setText(clipboardContent);
				}
				log.debug("Clipboard: " + clipboardContent);
			}

			public void mouseEntered(final MouseEvent e) {
				try {
					clipboardContent = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				} catch (UnsupportedFlavorException | IOException e1) {
					e1.printStackTrace();
				}

				dialogUrlField.setToolTipText("Click to paste URL");
			}
		});

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int value = chooser.showDialog(me, "Save here");
				if (value == JFileChooser.APPROVE_OPTION) {
					workingDir = chooser.getSelectedFile();
					saveToField.setText(workingDir.getPath());
					log.info("workingDir set to: " + workingDir.getPath());
				}
			}
		});

		//dialog listeners
		dialogOkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String link = dialogUrlField.getText();
				if (link.startsWith("https://www.youtube.com/watch?v=")) {
					String id = link.substring(link.indexOf("=") + 1);
					dialog.setVisible(false);

					VideoInfo downer = new VideoInfo(id, qual/*, (DownloaderGUI) me*/);
					downloaders.add(downer);
					createTab("Video " + (tabs.getTabCount() + 1),
							downer.getUrl(),
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
				if (new File(downloaders.get(tabs.getSelectedIndex() + 1).getVideoTitle() + ".mp4").delete()) {
					log.info("download " + (tabs.getSelectedIndex() + 1)
							+ " terminated. File deleted.");
				} else {
					log.info("download " + (tabs.getSelectedIndex() + 1)
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
				log.info("trying to delete: "
						+ downloaders.get(tabs.getSelectedIndex() + 1).getVideoTitle());
				if (new File(downloaders.get(tabs.getSelectedIndex() + 1).getVideoTitle() + ".mp4").delete()) {
					log.info("download " + (tabs.getSelectedIndex() + 1)
							+ " terminated. File deleted.");
				} else {
					log.info("download " + (tabs.getSelectedIndex() + 1)
							+ " terminated. File deletion failed.");
				}
				log.info("removed tab " + i + " successfully");
			}
			log.info("disposing of window");
			me.dispose();
			log.info("program closing");
			System.exit(0);
		}
	}

	//incorporate getter/setter use of other class to get this methods info
	private void createTab(String tabTitle, String videoUrl, String videoTitle,
	                       String videoUploader, String uploadDate, String description) {
		URL imgUrl = null;

//		try {
//			imgUrl = new URL(downloaders.get(tabs.getSelectedIndex() + 1).getThumbUrl());
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}

		JPanel newPanel = new JPanel();
		JLabel titleLabel = new JLabel("Title: " + videoTitle);
		JLabel uploaderLabel = new JLabel("Uploaded by: " + videoUploader/* + " on " + uploadDate*/);
		JLabel prevImg = new JLabel(new ImageIcon("http://t3.gstatic.com/images?q=tbn:ANd9GcSbNO8DD1Q3hePzh2gpX0syP0md8D6qGEoINhE0vnQY5sGJdGxpSg"));
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
			log.info("Trying to exit on: " + tabs.getTabCount() + " tabs");

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
					log.info("disposing of window");
					me.dispose();
					log.info("program closing");
					System.exit(0);
				}
			} else {
				log.info("disposing of window");
				me.dispose();
				log.info("program closing");
				System.exit(0);
			}
		}
	}
}
