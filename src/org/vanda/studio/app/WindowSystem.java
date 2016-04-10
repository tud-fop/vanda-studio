/**
 * 
 */
package org.vanda.studio.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.vanda.studio.app.SplitTabbedPane.SideSplitPane;
import org.vanda.studio.core.ButtonTabComponent;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;

/**
 * Root node of the Vanda Studio Application Object Model.
 * 
 * @author buechse, rmueller, mielke
 */
public class WindowSystem {
	
	private static int ICON_SIZE = 24;

	protected final Application app;
	protected final JFrame mainWindow;
	protected SplitTabbedPane contentPane;
	protected JMenuBar menuBar;
	protected JPanel iconToolBar;
	protected JMenu fileMenu;
	protected HashMap<UIMode, JRadioButtonMenuItem> modeMenuItems;
	protected HashMap<JComponent, JMenu> windowMenus;
	protected HashMap<JComponent, JPanel> iconToolBars;
	
	protected ButtonGroup modeGroup;

	/**
	 * stores TreeMap from indices to MenuItems for each menu
	 * the natural ordering of keys is exploited to order the menu items
	 * The JMenuItem `null` will result in a separator!
	 */
	protected HashMap<JMenu, TreeMap<Integer, JMenuItem>> items;
	/**
	 * for the toolbar
	 */
	protected HashMap<JPanel, TreeMap<Integer, JComponent>> tools;
	protected HashMap<JComponent, HashMap<Action, JButton>> actionToButton;
	private Observer<Application> shutdownObserver;
	private Observer<Application> uiModeObserver;

	/**
	 * @param app
	 *            Vanda Composer Application root object
	 */
	public WindowSystem(Application app) {
		this.app = app;
		shutdownObserver = new Observer<Application>() {
			@Override
			public void notify(Application a) {
				mainWindow.setVisible(false);
				mainWindow.dispose();
			}
		};
		app.getShutdownObservable().addObserver(shutdownObserver);
		mainWindow = new JFrame("Vanda Studio");
		mainWindow.setSize(800, 600);
		mainWindow.setLocation(100, 100);
		mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				WindowSystem.this.app.shutdown();
			}
		});

		// Create a simple JMenuBar
		menuBar = new JMenuBar();
		
		// Menu > Studio/File
		fileMenu = new JMenu("Studio");
		items = new HashMap<JMenu, TreeMap<Integer, JMenuItem>>();
		items.put(fileMenu, new TreeMap<Integer, JMenuItem>());
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowSystem.this.app.shutdown();
			}
		});
		// 0 -> New
		// 1 -> Open
		items.get(fileMenu).put(2, null);
		// 3 -> Edit Data Sources
		// 4 -> Fragment Profile Manager
		items.get(fileMenu).put(5, null);
		items.get(fileMenu).put(6, exitMenuItem);
		menuBar.add(fileMenu);

		// Menu > UI Mode
		final JMenu optionsMenu = new JMenu("UI Mode");
		modeGroup = new ButtonGroup();
		modeMenuItems = new HashMap<UIMode, JRadioButtonMenuItem>();
		Collection<UIMode> modes = app.getUIModes();
		for (final UIMode m : modes) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(m.getName(), app.getUIMode() == m);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					WindowSystem.this.app.setUIMode(m);
				}
			});
			optionsMenu.add(item);
			modeGroup.add(item);
			modeMenuItems.put(m, item);
		}
		uiModeObserver = new Observer<Application>() {
			@Override
			public void notify(Application a) {
				JRadioButtonMenuItem item = modeMenuItems.get(a.getUIMode());
				if (item != null)
					item.setSelected(true);
			}
		};
		app.getUIModeObservable().addObserver(uiModeObserver);
		menuBar.add(optionsMenu);
		
		// Create toolbar
		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		fl.setHgap(1);
		fl.setVgap(1);
		JPanel iconToolBarFlower = new JPanel(fl);
		iconToolBar = new JPanel();
		iconToolBar.setLayout(new BoxLayout(iconToolBar, BoxLayout.LINE_AXIS));
		iconToolBar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		iconToolBarFlower.add(iconToolBar);
		
		// Initialize all bookkeeping
		windowMenus = new HashMap<JComponent, JMenu>();
		iconToolBars = new HashMap<JComponent, JPanel>();
		tools = new HashMap<JPanel, TreeMap<Integer, JComponent>>();
		tools.put(null, new TreeMap<Integer, JComponent>());
		actionToButton = new HashMap<JComponent, HashMap<Action, JButton>>();
		actionToButton.put(null, new HashMap<Action, JButton>());

		// Create the central tabbed pane containing the graph...
		contentPane = new SplitTabbedPane();
		contentPane.addChangeListener(new ChangeListener() {
			/* Switching tabs should change things other than just the contentPane itself:
			 * the menu, the iconToolBar and the tool windows!
			 */
			@Override
			public void stateChanged(ChangeEvent e) {
				// Rebuild menu
				Component graph = contentPane.getSelectedCoreComponent();
				JMenu menu = windowMenus.get(graph);
				if (menuBar.getMenu(1) != menu) {
					if (menuBar.getMenu(1) != optionsMenu) {
						menuBar.remove(1);
					}
					if (menu != null) {
						menuBar.add(menu, 1);
					}
				}
				menuBar.revalidate();
				menuBar.repaint();

				// Rebuild icon toolbar
				iconToolBar.removeAll();
				iconToolBar.add(iconToolBars.get(null));
				if (iconToolBars.get(graph) != null && graph != null)
					iconToolBar.add(iconToolBars.get(graph));
				iconToolBar.revalidate();
				iconToolBar.repaint();
			}

		});
		
		// Add bars and panes to the window
		mainWindow.setJMenuBar(menuBar);
		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.getContentPane().add(contentPane, BorderLayout.CENTER);
		mainWindow.getContentPane().add(iconToolBarFlower, BorderLayout.NORTH);
		
		// Show window (from UI thread)
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainWindow.setVisible(true);
			}
		});
	}

	/**
	 * Adds a panel to the toolbar.
	 * Will not add a JSeparator at the start or after another JSeparator.
	 * @param associatedGraph the graph to which the new panel "belongs"
	 * @param panel the panel to add
	 * @param pos the position in the toolbar
	 */
	public void addToolBarPanel(JComponent associatedGraph, JComponent panel, int pos) {
		if (!iconToolBars.containsKey(associatedGraph)) {
			FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
			fl.setVgap(1);
			iconToolBars.put(associatedGraph, new JPanel(fl));
			iconToolBars.get(associatedGraph).setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			tools.put(iconToolBars.get(associatedGraph), new TreeMap<Integer, JComponent>());
			actionToButton.put(associatedGraph, new HashMap<Action, JButton>());
		}
		
		// Check if we are adding a separator in front of the start or another separator
		if(panel instanceof JSeparator &&
			(tools.get(iconToolBars.get(associatedGraph)).isEmpty()
				|| tools.get(iconToolBars.get(associatedGraph)).lastEntry().getValue() instanceof JSeparator)
		)
			return;
		
		tools.get(iconToolBars.get(associatedGraph)).put(pos, panel);
		iconToolBars.get(associatedGraph).removeAll();
		for (Integer i : tools.get(iconToolBars.get(associatedGraph)).navigableKeySet()) {
			iconToolBars.get(associatedGraph).add(tools.get(iconToolBars.get(associatedGraph)).get(i));
		}
		if (associatedGraph == null)
			for (ChangeListener cl : contentPane.getChangeListeners())
				cl.stateChanged(new ChangeEvent(contentPane));
	}
	
	/**
	 * Add a new action w/ hotkey to the menu and the icon toolbar of a given window.
	 * Call this method *before* adding that window as a content window!
	 * @param associatedGraph is `null` for universally applicable actions
	 */
	public void addAction(JComponent associatedGraph, final Action a, String imageName, KeyStroke keyStroke, int pos) {
		URL url = ClassLoader.getSystemClassLoader().getResource(imageName + ".png");
		JButton b = new JButton();
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				a.invoke();
			}
		});
		try {
			Image i = ImageIO.read(url);
			b.setIcon(new ImageIcon(i.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH)));
			b.setPreferredSize(new Dimension(ICON_SIZE + 2, ICON_SIZE + 2));
			b.setMargin(new Insets(0, 0, 0, 0));
			b.setToolTipText(a.getName());
		} catch (IOException e1) {
			app.sendMessage(new ExceptionMessage(e1));
			b.setText(a.getName());
		}
		
		addToolBarPanel(associatedGraph, b, pos);
		actionToButton.get(associatedGraph).put(a, b);
		
		// also add it to the menu!
		addAction(associatedGraph, a, keyStroke, pos);
	}

	/**
	 * Add a new action w/ hotkey to the menu of a given window.
	 * Call this method *before* adding that window as a content window!
	 * @param associatedGraph is `null` for universally applicable actions 
	 */
	public void addAction(JComponent associatedGraph, final Action a, KeyStroke keyStroke, int pos) {
		JMenuItem item = new JMenuItem(a.getName());
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				a.invoke();
			}
		});
		if (keyStroke != null) {
			item.setAccelerator(keyStroke);
		}
		if (associatedGraph != null) {
			JMenu menu = windowMenus.get(associatedGraph);
			if (menu == null) {
				menu = new JMenu(associatedGraph.getName());
				windowMenus.put(associatedGraph, menu);
				if (contentPane.getSelectedComponent() == associatedGraph)
					menuBar.add(menu, 1);
				items.put(menu, new TreeMap<Integer, JMenuItem>());
			}
			items.get(menu).put(pos, item);
			menu.removeAll();
			for (Integer i : items.get(menu).navigableKeySet()) {
				JMenuItem itemToAdd = items.get(menu).get(i); 
				if (itemToAdd != null)
					menu.insert(itemToAdd, i);
				else
					menu.insertSeparator(i);
			}
		} else { // graph `null` -> FileMenu
			items.get(fileMenu).put(pos, item);
			fileMenu.removeAll();
			for (Integer i : items.get(fileMenu).navigableKeySet()) {
				JMenuItem itemToAdd = items.get(fileMenu).get(i); 
				if (itemToAdd != null)
					fileMenu.insert(itemToAdd, i);
				else
					fileMenu.insertSeparator(i);
			}
		}
	}

	/**
	 * Adds a separator in the menu and the toolbar at the given position,
	 * while making sure that separators never follow each other in the toolbar
	 * or come at the start of the toolbar.
	 */
	public void addSeparator(JComponent associatedGraph, int pos) {
		// Add it to the menu
		JMenu menu = windowMenus.get(associatedGraph);
		if (menu == null) {
			menu = new JMenu(associatedGraph.getName());
			windowMenus.put(associatedGraph, menu);
			if (contentPane.getSelectedComponent() == associatedGraph)
				menuBar.add(menu, 1);
			items.put(menu, new TreeMap<Integer, JMenuItem>());
		}
		items.get(menu).put(pos, null);
		
		// Also insert a spacer in the toolbar
		addToolBarPanel(associatedGraph, new JSeparator(), pos);
	}
	
	/**
	 * Adds a new tab
	 * @param c the root component in the new tab
	 * @param a action to be performed when closing the tab, i.e. pressing the small x button
	 *          Passing `null` as an action will just remove the tab from the display!
	 */
	public void addContentWindow(Icon i, JComponent c, Action a) {
		int maxLength = 21;
		String name;
		if (c.getName().length() <= maxLength) {
			name = c.getName();
		} else {
			name = c.getName().substring(0, (maxLength - 3) / 2) + "..."
					+ c.getName().substring(c.getName().length() - (maxLength - 3) / 2 - 1);
		}
		int ix = contentPane.indexOfComponent(c);

		if (ix >= 0) {
			contentPane.setToolTipTextAt(ix, c.getName());
			contentPane.setTitleAt(ix, name);
		} else {
			contentPane.add(name, c);
			int idx = contentPane.indexOfComponent(c);
			contentPane.setTabComponentAt(idx, new ButtonTabComponent(contentPane, a));
			contentPane.setToolTipTextAt(idx, c.getName());
		}
		
		// TODO check why this is suddenly necessary
		c.setVisible(true);
	}

	/**
	 * FIXME line 2 doesn't make any sense.
	 * FIXME this method is only called from the Ctrl+W menu option (`CloseWorkflowAction`),
	 *       but not from closing the tab (since `null` is given
	 *       instead of a proper action that could call this method)
	 * What the hell.
	 */
	public void removeContentWindow(JComponent c) {
		contentPane.remove(c);
		//windowTools.remove(frames.get(c));
	}
	
	/**
	 * Adds a new tool pane to the window
	 * @param associatedGraph graph with which to associate the new tool pane
	 * @param p the tool pane to add
	 */
	public void addSideSplit(JComponent associatedGraph, SideSplitPane p) {
		contentPane.addSplitAt(contentPane.indexOfCoreComponent(associatedGraph), p);
	}

	/**
	 * Removes a tool pane from the window
	 * @param associatedGraph graph with which the pane is associated
	 * @param c the component of the pane
	 */
	public void removeSideSplit(JComponent associatedGraph, JComponent c) {
		contentPane.removeSplitAt(contentPane.indexOfCoreComponent(associatedGraph), c);
	}

	/**
	 * Traverses the menus of the main window (`null`) to find an entry w/ the given action and enables it.
	 * Uses `windowMenus` and `actionToButton`.
	 */
	public void disableAction(Action a) {
		disableAction(null, a);
	}

	/**
	 * Traverses the menus of the given window to find an entry w/ the given action and enables it.
	 * Uses `windowMenus` and `actionToButton`.
	 */
	public void disableAction(JComponent window, Action a) {
		JMenu menu = windowMenus.get(window);
		if (menu == null)
			menu = fileMenu;
		// find item that is labeled with action's name
		for (int i = 0; i < menu.getItemCount(); i++) {
			if (menu.getItem(i) != null && menu.getItem(i).getText().equals(a.getName())) {
				menu.getItem(i).setEnabled(false);
			}
		}
		if (actionToButton.get(window) != null) {
			JButton b = actionToButton.get(window).get(a);
			if (b != null)
				b.setEnabled(false);
		}
	}
	
	/**
	 * Traverses the menus of the main window (`null`) to find an entry w/ the given action and enables it.
	 * Uses `windowMenus` and `actionToButton`.
	 */
	public void enableAction(Action a) {
		enableAction(null, a);
	}
	
	/**
	 * Traverses the menus of the given window to find an entry w/ the given action and enables it.
	 * Uses `windowMenus` and `actionToButton`.
	 */
	public void enableAction(JComponent window, Action a) {
		JMenu menu = windowMenus.get(window);
		if (menu == null)
			menu = fileMenu;
		// find item that is labeled with action's name
		for (int i = 0; i < menu.getItemCount(); i++) {
			if (menu.getItem(i) != null && menu.getItem(i).getText().equals(a.getName())) {
				menu.getItem(i).setEnabled(true);
			}
		}
		if (actionToButton.get(window) != null) {
			JButton b = actionToButton.get(window).get(a);
			if (b != null)
				b.setEnabled(true);
		}
	}
	
	/**
	 * Selects component `c` and notifies contentPane's ChangeListeners
	 */
	public void focusContentWindow(JComponent c) {
		contentPane.setSelectedCoreComponent(c);
		for (ChangeListener cl : contentPane.getChangeListeners())
			cl.stateChanged(new ChangeEvent(contentPane));
	}
	
	public JFrame getMainWindow() {
		return mainWindow;
	}

}
