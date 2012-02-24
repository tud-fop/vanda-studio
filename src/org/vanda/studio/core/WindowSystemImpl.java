/**
 *
 */
package org.vanda.studio.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.UIMode;
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.model.Action;
import org.vanda.studio.util.Observer;

/**
 * @author buechse, rmueller
 * 
 */
public class WindowSystemImpl implements WindowSystem {
	
	protected class Window {
		String id;
		String title;
		Icon icon;
		JComponent component;
	}
	
	protected final Application app;
	protected JFrame mainWindow;
	protected JTabbedPane contentPane;
	protected JTabbedPane toolPane;
	protected JMenuBar menuBar;
	protected JMenu fileMenu;
	protected HashMap<UIMode,JRadioButtonMenuItem> modeMenuItems;
	protected ButtonGroup modeGroup;

	/**
	 * @param a
	 *            Vanda Composer Application root object
	 */
	public WindowSystemImpl(Application a) {
		app = a;
		app.getShutdownObservable().addObserver(
			new Observer<Application>() {
				@Override
				public void notify(Application a) {
					mainWindow.setVisible(false);
					mainWindow.dispose();
				}
		});
		mainWindow = new JFrame("Vanda Studio");
		mainWindow.setSize(800, 600);
		mainWindow.setLocation(100, 100);
		mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				app.shutdown();
			}
		});

		// Create a simple JMenuBar
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				app.shutdown();
			}
		});

		fileMenu.add(exitMenuItem);
		menuBar.add(fileMenu);
		
		JMenu optionsMenu = new JMenu("UI Mode");
		modeGroup = new ButtonGroup();
		modeMenuItems = new HashMap<UIMode,JRadioButtonMenuItem>();
		Collection<UIMode> modes = app.getUIModes();
		for (final UIMode m : modes) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(
				m.getName(), app.getUIMode() == m);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					app.setUIMode(m);
				}
			});
			optionsMenu.add(item);
			modeGroup.add(item);
			modeMenuItems.put(m, item);
		}
		app.getUIModeObservable().addObserver(
			new Observer<Application>() {
				@Override
				public void notify(Application a) {
					JRadioButtonMenuItem item = modeMenuItems.get(a.getUIMode());
					if (item != null)
						item.setSelected(true);
				}
			});
		menuBar.add(optionsMenu);
		
		mainWindow.setJMenuBar(menuBar);

		// Creates the library pane that contains the tabs with the palettes
		contentPane = new JTabbedPane();
		toolPane = new JTabbedPane();
		/*toolPane.add("Definitions", new JLabel("Test"));
		toolPane.add("Documentation", new JLabel("Test"));
		toolPane.add("Source Code", new JLabel("Test"));*/

		JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				contentPane, toolPane);
		inner.setOneTouchExpandable(true);
		inner.setDividerLocation(1.0);
		inner.setResizeWeight(1);
		inner.setDividerSize(6);
		inner.setBorder(null);

		// Puts everything together
		//mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.getContentPane().add(/*inner*/contentPane); // BorderLayout.CENTER
		//mainWindow.getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				/*for (ToolWindow toolWin : toolWindowManager.getToolWindows()) {
					toolWin.setAvailable(true);
					toolWin.setActive(true);
				}*/

				mainWindow.setVisible(true);
			}
		});
	}
	
	@Override
	public void addAction(final Action a) {
		JMenuItem item = new JMenuItem(a.getName());
		item.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					a.invoke();
				}
			});
		fileMenu.insert(item, 0);
	}

	/**
	 */
	@Override
	public void addContentWindow(String id, String title, Icon i, JComponent c) {
		contentPane.add(title, c);
	}

	/**
	 */
	@Override
	public void addToolWindow(String id, String title, Icon i, JComponent c) {
		toolPane.add(title, c);
	}
	
	@Override
	public void focusContentWindow(JComponent c) {
		contentPane.setSelectedComponent(c);
	}

	/**
	 */
	@Override
	public void removeContentWindow(JComponent c) {
		contentPane.remove(c);
	}

	/**
	 */
	@Override
	public void removeToolWindow(JComponent c) {
		toolPane.remove(c);
	}

}
