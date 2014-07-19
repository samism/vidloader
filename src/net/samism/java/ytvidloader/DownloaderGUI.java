package net.samism.java.ytvidloader;

import org.apache.commons.lang3.StringUtils;
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

	private static final ImageIcon ADD_IMAGE, REMOVE_IMAGE, WINDOW_LOGO;

	private final ArrayList<VideoInfo> downloaders = new ArrayList<>();
	private final ArrayList<JProgressBar> bars = new ArrayList<>();
	private final ArrayList<DownloadWorker> workers = new ArrayList<>();

	String clipboardContent = "";

	File defaultDownloadsDir = new File(System.getProperty("user.home") + "/Downloads"); //OS downloads directory
	File newDownloadsDir = new File(System.getProperty("user.dir") + "/Downloads"); //alternative to above if not found
	File workingDir = defaultDownloadsDir; //by default is the OS downloads dir. may be altered in the constructor

	enum Quality {
		//highest grabs the largest integer itag
		//default is mp4
		//lowest, etc
		HIGHEST, DEFAULT, LOWEST
	}

	private Quality qual = Quality.DEFAULT;

	private final DownloaderGUI me = this;
	private final JPanel mainPanel = new JPanel();
	private final JTabbedPane tabs = new JTabbedPane();
	private final JButton addTabButton, removeTabButton,
			browseButton = new JButton("browse"),
			dialogOkButton = new JButton("OK"),
			dialogCancelButton = new JButton("Cancel"),
			themeButton = new JButton("Change Theme");
	private final JLabel saveToField = new JLabel("Loading downloads directory...");
	private final JTextField dialogUrlField = new JTextField();
	private final JFileChooser chooser = new JFileChooser();
	private final JDialog dialog = new JDialog(this, "Add Download");

	static {
		ADD_IMAGE = new ImageIcon(DownloaderGUI.class.getResource("resources/add.png"));
		REMOVE_IMAGE = new ImageIcon(DownloaderGUI.class.getResource("resources/remove.png"));
		WINDOW_LOGO = new ImageIcon(DownloaderGUI.class.getResource("resources/big-logo.jpg"));
	}

	public DownloaderGUI() {
		//cannot be assigned at declaration
		addTabButton = new JButton(ADD_IMAGE);
		removeTabButton = new JButton(REMOVE_IMAGE);

		//check if the OS dir exists. if it doesn't, create a custom one in the current directory
		if (!defaultDownloadsDir.exists()) {
			if (newDownloadsDir.mkdir()) {
				log.info("OS downloads folder doesn't exist, a local one was created");
				workingDir = newDownloadsDir;
			} else {
				log.info("downloads folder doesn't exist - creation failed.");
			}
		}

		saveToField.setText(workingDir.getAbsolutePath());

		buildGUI();
		addListeners();
		addKeyBindings();

		updateClipboard();
		updateLAF();
	}

	private void buildGUI() {
		browseButton.setToolTipText("Select where to save your downloads");
		saveToField.setToolTipText("Click to open");
		addTabButton.setToolTipText("Add a video download");
		removeTabButton.setToolTipText("Remove a video download");

		dialogUrlField.setPreferredSize(new Dimension(300, 25));
		dialogUrlField.setToolTipText("Click to paste URL");
		saveToField.setPreferredSize(new Dimension(300, 25));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(workingDir);

		tabs.setPreferredSize(new Dimension(720, 450));
		tabs.setTabPlacement(JTabbedPane.LEFT);
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new FlowLayout());

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
		buttonPanel.add(themeButton, BorderLayout.NORTH);

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
		dialog.setFocusable(true);
		dialog.setVisible(false);

		//main window properties
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		setTitle("YouTube Downloader");
		setMinimumSize(new Dimension(750, 450));
		setIconImage(WINDOW_LOGO.getImage());
		setLocationRelativeTo(null);
		setFocusable(true);
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
					dialog.setVisible(false);
					dialogUrlField.setText("");

					VideoInfo info = new VideoInfo(link, qual);
					downloaders.add(info);
					createTab(StringUtils.abbreviate(info.getVideoTitle(), 15),
							info.getVideoTitle(),
							info.getVideoUploader(),
							info.getDescription(),
							info.getThumbUrl());

					DownloadWorker worker = new DownloadWorker(info);
					workers.add(worker);
					worker.execute();
				}
			}
		});

		dialogCancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogUrlField.setText("");
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
				updateClipboard();
				dialogUrlField.setText("");
				dialogUrlField.grabFocus();
				dialog.setVisible(true);
			}
		});

		removeTabButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				destroyTab();
			}
		});

		themeButton.addActionListener(new ActionListener() {
			String[] themeList = new String[]{
					"org.pushingpixels.substance.api.skin.SubstanceRavenLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceEmeraldDuskLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceOfficeBlue2007LookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceModerateLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceAutumnLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceChallengerDeepLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceCremeCoffeeLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceCremeLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceDustCoffeeLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceDustLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceGeminiLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceMagellanLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceMarinerLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceMistAquaLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceMistSilverLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceNebulaBrickWallLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceNebulaLookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceOfficeBlack2007LookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceOfficeSilver2007LookAndFeel",
					"org.pushingpixels.substance.api.skin.SubstanceSaharaLookAndFeel",
					"javax.swing.plaf.metal.MetalLookAndFeel"};

			int i = 0;

			public void actionPerformed(ActionEvent e) {
				try {
					UIManager.setLookAndFeel(themeList[i]);
				} catch (ClassNotFoundException | IllegalAccessException | UnsupportedLookAndFeelException
						| InstantiationException ex) {
					ex.printStackTrace();
				}
				SwingUtilities.updateComponentTreeUI(me);
				themeButton.setToolTipText(UIManager.getLookAndFeel().getName());
				i = (i == themeList.length - 1 ? 0 : i + 1);
			}
		});

		dialogUrlField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				updateClipboard();
			}
		});
	}

	public void addKeyBindings() {
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
				put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		mainPanel.getActionMap().put("enter", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addTabButton.doClick();
			}
		});

		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
				put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		dialog.getRootPane().getActionMap().put("enter", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (dialogUrlField.getText().isEmpty()) {
					dialogUrlField.setText(clipboardContent);
				} else {
					dialogOkButton.doClick();
				}
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

	private void createTab(String tabTitle, String videoTitle,
						   String videoUploader, String description, String thumbUrl) {
		JPanel newPanel = new JPanel();
		JLabel titleLabel = new JLabel("<html><u>" + videoTitle + "</u></html>");
		JLabel uploaderLabel = new JLabel("<html>by: <i>" + videoUploader + "</i></html>");
		JLabel prevImg = null;
		try {
			prevImg = new JLabel(new ImageIcon(new URL(thumbUrl)));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			log.info("unable to grab thumbnail image.");
		}

		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
		uploaderLabel.setFont(new Font("Georgia", Font.BOLD, 18));
		uploaderLabel.setHorizontalAlignment(SwingConstants.LEFT);

		JTextPane html = new JTextPane();

		html.setContentType("text/html");
		html.setText(description);
		html.setEditable(false);
		html.setBackground(Color.GRAY);
		html.setBorder(null);

		JScrollPane scrollPane = new JScrollPane(html);
		html.setCaretPosition(0);

		//keep instances of progress bars
		bars.add(new JProgressBar());

		newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
		newPanel.add(prevImg);
		newPanel.add(new JSeparator());
		newPanel.add(titleLabel);
		newPanel.add(uploaderLabel);
		newPanel.add(scrollPane);
		newPanel.add(new JSeparator());
		newPanel.add(bars.get(tabs.getTabCount()));
		tabs.addTab(tabTitle, newPanel);
		tabs.setSelectedComponent(tabs.getComponentAt(tabs.getTabCount() - 1));
	}

	private void updateClipboard() {
		try {
			clipboardContent = (String) Toolkit.getDefaultToolkit().
					getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException | IOException e1) {
			e1.printStackTrace();
		}
	}

	private void updateLAF() {
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
