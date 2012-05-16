/**
 *
 */
package org.vanda.studio.core;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.UIMode;
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.Observer;

/**
 * @author buechse, rmueller
 * 
 */
public class WindowSystemImpl implements WindowSystem {

	protected final Application app;
	protected JFrame mainWindow;
	protected JTabbedPane contentPane;
	protected JTabbedPane toolPane;
	protected JMenuBar menuBar;
	protected JMenu fileMenu;
	protected HashMap<UIMode, JRadioButtonMenuItem> modeMenuItems;
	protected HashMap<JComponent, JMenu> windowMenus;
	protected HashMap<JComponent, List<JComponent>> windowTools;
	protected ButtonGroup modeGroup;

	/**
	 * @param a
	 *            Vanda Composer Application root object
	 */
	public WindowSystemImpl(Application a) {
		app = a;
		app.getShutdownObservable().addObserver(new Observer<Application>() {
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
		fileMenu = new JMenu("Studio");
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				app.shutdown();
			}
		});

		fileMenu.add(exitMenuItem);
		addSeparator();
		menuBar.add(fileMenu);

		final JMenu optionsMenu = new JMenu("UI Mode");
		modeGroup = new ButtonGroup();
		modeMenuItems = new HashMap<UIMode, JRadioButtonMenuItem>();
		Collection<UIMode> modes = app.getUIModes();
		for (final UIMode m : modes) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(m.getName(),
					app.getUIMode() == m);
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
		app.getUIModeObservable().addObserver(new Observer<Application>() {
			@Override
			public void notify(Application a) {
				JRadioButtonMenuItem item = modeMenuItems.get(a.getUIMode());
				if (item != null)
					item.setSelected(true);
			}
		});
		menuBar.add(optionsMenu);
		
		windowMenus = new HashMap<JComponent, JMenu>();
		windowTools = new HashMap<JComponent, List<JComponent>>();

		mainWindow.setJMenuBar(menuBar);

		// Creates the library pane that contains the tabs with the palettes
		contentPane = new JTabbedPane();
		toolPane = new JTabbedPane();
		/*
		 * toolPane.add("Definitions", new JLabel("Test"));
		 * toolPane.add("Documentation", new JLabel("Test"));
		 * toolPane.add("Source Code", new JLabel("Test"));
		 */

		JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				contentPane, toolPane);
		inner.setOneTouchExpandable(true);
		inner.setDividerLocation(0.7);
		inner.setResizeWeight(0.7);
		inner.setDividerSize(6);
		inner.setBorder(null);
		
		contentPane.addChangeListener(new ChangeListener () {

			@Override
			public void stateChanged(ChangeEvent e) {
				Component c = contentPane.getSelectedComponent();
				JMenu menu = windowMenus.get(c);
				if (menuBar.getMenu(1) != menu) {
					if (menuBar.getMenu(1) != optionsMenu) {
						menuBar.remove(1);
					}
					if (menu != null) {
						menuBar.add(menu, 1);
					}
				}
				mainWindow.setJMenuBar(menuBar); // XXX why is this necessary?
				int idx = toolPane.getSelectedIndex();
				toolPane.removeAll();
				List<JComponent> tcs = windowTools.get(null);
				if (tcs != null) {
					for (JComponent tc : tcs)
						toolPane.add(tc);
				}
				tcs = windowTools.get(c);
				if (tcs != null) {
					for (JComponent tc : tcs)
						toolPane.add(tc);
				}
				try {
					toolPane.setSelectedIndex(idx);
				} catch (IndexOutOfBoundsException ex) {
					// ignore
				}
			}
			
		});

		// Puts everything together
		// mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.getContentPane().add(inner); // BorderLayout.CENTER
		//mainWindow.getContentPane().add(statusBar, BorderLayout.SOUTH);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				/*
				 * for (ToolWindow toolWin : toolWindowManager.getToolWindows()) {
				 * toolWin.setAvailable(true); toolWin.setActive(true); }
				 */

				mainWindow.setVisible(true);
			}
		});
	}

	@Override
	public void addAction(JComponent c, final Action a) {
		JMenuItem item = new JMenuItem(a.getName());
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				a.invoke();
			}
		});
		if (c != null) {
			JMenu menu = windowMenus.get(c);
			if (menu == null) {
				menu = new JMenu(c.getName());
				windowMenus.put(c, menu);
			}
			menu.add(item);
		} else
			fileMenu.insert(item, 0);
	}

	/**
	 */
	@Override
	public void addContentWindow(Icon i, JComponent c, Action a) {
		contentPane.add(c);
		contentPane.setTabComponentAt(contentPane.getTabCount() - 1,
				new ButtonTabComponent(contentPane, a));
	}

	/**
	 */
	@Override
	public void addSeparator() {
		fileMenu.insertSeparator(0);
	}

	/**
	 */
	@Override
	public void addToolWindow(JComponent window, Icon i, JComponent c) {
		List<JComponent> tcs = windowTools.get(window);
		if (tcs == null) {
			tcs = new ArrayList<JComponent>();
			windowTools.put(window, tcs);
		}
		tcs.add(c);
		if (contentPane.getSelectedComponent() == window)
			toolPane.add(c);
	}

	@Override
	public void disableAction(Action a) {
		// find item that is labeled with action's name
		for (int i = 0; i < fileMenu.getItemCount(); i++) {

			if (fileMenu.getItem(i) != null
					&& fileMenu.getItem(i).getText().equals(a.getName())) {
				fileMenu.getItem(i).setEnabled(false);
			}
		}
	}

	@Override
	public void enableAction(Action a) {
		// find item that is labeled with action's name
		for (int i = 0; i < fileMenu.getItemCount(); i++) {
			if (fileMenu.getItem(i) != null
					&& fileMenu.getItem(i).getText().equals(a.getName())) {
				fileMenu.getItem(i).setEnabled(true);
			}
		}
	}

	@Override
	public void focusToolWindow(JComponent c) {
		try {
			toolPane.setSelectedComponent(c);
		} catch (IllegalArgumentException e) {
			// if the component is not in there, then the corresponding
			// window is not focused, and we ignore the request
		}
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
		windowTools.remove(c);
	}

	/**
	 */
	@Override
	public void removeToolWindow(JComponent window, JComponent c) {
		List<JComponent> tcs = windowTools.get(window);
		if (tcs != null) {
			tcs.remove(c);
		}
		if (contentPane.getSelectedComponent() == window)
			toolPane.remove(c);
	}

}
