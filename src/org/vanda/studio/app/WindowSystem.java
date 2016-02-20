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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.vanda.studio.core.ButtonTabComponent;
import org.vanda.studio.core.LayerLayout;
import org.vanda.studio.core.ToolFrame;
import org.vanda.studio.modules.workflows.tools.AssignmentSwitchToolFactory;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;

/**
 * Root node of the Vanda Studio Application Object Model.
 * 
 * @author buechse, rmueller, mielke
 */
public class WindowSystem {
	
	public enum Side {NORTH, EAST, SOUTH, WEST};
	
	private static int ICON_SIZE = 24;

	protected final Application app;
	protected final JFrame mainWindow;
	protected JLayeredPane mainPane;
	protected JTabbedPane contentPane;
	protected JMenuBar menuBar;
	protected JPanel iconToolBar;
	protected JMenu fileMenu;
	protected HashMap<UIMode, JRadioButtonMenuItem> modeMenuItems;
	protected HashMap<JComponent, JMenu> windowMenus;
	protected HashMap<JComponent, JPanel> iconToolBars;
	/**
	 * Maps a window to a list of its tool panes
	 * Note: the key `null` maps to tool panes that should be shown for every window
	 */
	protected HashMap<JComponent, List<JComponent>> windowTools;
	/**
	 * Maps tool window panes to the internal frames that are created for holding them
	 */
	@Deprecated
	protected HashMap<JComponent, JInternalFrame> frames;
	
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

	@SuppressWarnings("serial")
	private static class LayoutTabbedPane extends JTabbedPane implements LayoutSelector {

		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getCenter();
		}

	}

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
		iconToolBar = new JPanel(fl);
		iconToolBar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		
		// Initialize all bookkeeping
		windowMenus = new HashMap<JComponent, JMenu>();
		windowTools = new HashMap<JComponent, List<JComponent>>();
		iconToolBars = new HashMap<JComponent, JPanel>();
		tools = new HashMap<JPanel, TreeMap<Integer, JComponent>>();
		tools.put(null, new TreeMap<Integer, JComponent>());
		actionToButton = new HashMap<JComponent, HashMap<Action, JButton>>();
		actionToButton.put(null, new HashMap<Action, JButton>());
		frames = new HashMap<JComponent, JInternalFrame>();

		// Create the central tabbed pane containing the graph...
		contentPane = new LayoutTabbedPane();
		contentPane.addChangeListener(new ChangeListener() {

			/* Switching tabs should change things other than just the contentPane itself:
			 * the menu, the iconToolBar and the tool windows!
			 */
			@Override
			public void stateChanged(ChangeEvent e) {
				// Rebuild menu
				// FIXME do not change tool windows when there is merely a new title for a content window
				Component parent = contentPane.getSelectedComponent();
				JMenu menu = windowMenus.get(parent);
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
				if (iconToolBars.get(parent) != null && parent != null)
					iconToolBar.add(iconToolBars.get(parent));
				iconToolBar.revalidate();
				iconToolBar.repaint();
				
				// Rebuild all tool windows 
				mainPane.removeAll();
				
				//mainPane.add(contentPane, JLayeredPane.DEFAULT_LAYER);
				
				/*
				List<JComponent> tcs = windowTools.get(null);
				if (tcs != null) {
					for (JComponent tc : tcs)
						mainPane.add(frames.get(tc), JLayeredPane.PALETTE_LAYER);
				}
				tcs = windowTools.get(parent);
				if (tcs != null) {
					for (JComponent tc : tcs)
						mainPane.add(frames.get(tc), JLayeredPane.PALETTE_LAYER);
				}
				*/
				
				// Build all side splits
				if(windowTools.get(parent) != null && !windowTools.get(parent).isEmpty()) {
					JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
							contentPane, windowTools.get(parent).get(0));

					splitPane.setBounds(0, 0, 600, 200);

					mainPane.add(splitPane, JLayeredPane.DEFAULT_LAYER);
				} else {
					mainPane.add(contentPane, JLayeredPane.DEFAULT_LAYER);
				}
			}

		});

		// ... and add it to the main pane
		mainPane = new JLayeredPane();
		mainPane.setLayout(new LayerLayout());
		mainPane.add(contentPane, JLayeredPane.DEFAULT_LAYER);

		
		// Add bars and panes to the window
		mainWindow.setJMenuBar(menuBar);
		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.getContentPane().add(mainPane, BorderLayout.CENTER);
		mainWindow.getContentPane().add(iconToolBar, BorderLayout.NORTH);
		
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
	 * @param associatedParent the parent to which the new panel "belongs"
	 * @param panel the panel to add
	 * @param pos the position in the toolbar
	 */
	public void addToolBarPanel(JComponent associatedParent, JComponent panel, int pos) {
		if (!iconToolBars.containsKey(associatedParent)) {
			FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
			fl.setVgap(1);
			iconToolBars.put(associatedParent, new JPanel(fl));
			iconToolBars.get(associatedParent).setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			tools.put(iconToolBars.get(associatedParent), new TreeMap<Integer, JComponent>());
			actionToButton.put(associatedParent, new HashMap<Action, JButton>());
		}
		
		// Check if we are adding a separator in front of the start or another separator
		if(panel instanceof JSeparator &&
			(tools.get(iconToolBars.get(associatedParent)).isEmpty()
				|| tools.get(iconToolBars.get(associatedParent)).lastEntry().getValue() instanceof JSeparator)
		)
			return;
		
		tools.get(iconToolBars.get(associatedParent)).put(pos, panel);
		iconToolBars.get(associatedParent).removeAll();
		for (Integer i : tools.get(iconToolBars.get(associatedParent)).navigableKeySet()) {
			iconToolBars.get(associatedParent).add(tools.get(iconToolBars.get(associatedParent)).get(i));
		}
		if (associatedParent == null)
			for (ChangeListener cl : contentPane.getChangeListeners())
				cl.stateChanged(new ChangeEvent(contentPane));
	}
	
	/**
	 * Add a new action w/ hotkey to the menu and the icon toolbar of a given window.
	 * Call this method *before* adding that window as a content window!
	 * @param associatedParent
	 * 				is `null` for universally applicable actions,
	 * 				otherwise the `MyMxGraphComponent` of the `DefaultWorkflowEditorImpl` 
	 */
	public void addAction(JComponent associatedParent, final Action a, String imageName, KeyStroke keyStroke, int pos) {
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
		
		addToolBarPanel(associatedParent, b, pos);
		actionToButton.get(associatedParent).put(a, b);
		
		// also add it to the menu!
		addAction(associatedParent, a, keyStroke, pos);
	}

	/**
	 * Add a new action w/ hotkey to the menu of a given window.
	 * Call this method *before* adding that window as a content window!
	 * @param associatedParent
	 * 				is `null` for universally applicable actions,
	 * 				otherwise the `MyMxGraphComponent` of the `DefaultWorkflowEditorImpl` 
	 */
	public void addAction(JComponent associatedParent, final Action a, KeyStroke keyStroke, int pos) {
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
		if (associatedParent != null) {
			JMenu menu = windowMenus.get(associatedParent);
			if (menu == null) {
				menu = new JMenu(associatedParent.getName());
				windowMenus.put(associatedParent, menu);
				if (contentPane.getSelectedComponent() == associatedParent)
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
		} else { // parent `null` -> FileMenu
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
	public void addSeparator(JComponent associatedParent, int pos) {
		// Add it to the menu
		JComponent key = associatedParent == null ? fileMenu : associatedParent;
		JMenu menu = windowMenus.get(associatedParent);
		if (menu == null) {
			menu = new JMenu(associatedParent.getName());
			windowMenus.put(associatedParent, menu);
			if (contentPane.getSelectedComponent() == associatedParent)
				menuBar.add(menu, 1);
			items.put(menu, new TreeMap<Integer, JMenuItem>());
		}
		items.get(menu).put(pos, null);
		
		// Also insert a spacer in the toolbar
		addToolBarPanel(associatedParent, new JSeparator(), pos);
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
			int idx = contentPane.getTabCount() - 1;
			contentPane.setTabComponentAt(idx, new ButtonTabComponent(contentPane, a));
			contentPane.setToolTipTextAt(idx, c.getName());
		}
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
		windowTools.remove(frames.get(c));
	}
	
	/**
	 * Adds a new `ToolFrame` to the `mainPane` (and the `windowTools` and `frames` structures)
	 * @param associatedParent parent with which to associate the new tool window
	 * @param c the tool window component to add
	 * @param layout the layout the new `ToolFrame` is supposed to have
	 */
	@Deprecated
	public void addToolWindow(JComponent associatedParent, JComponent c, LayoutSelector layout) {
		List<JComponent> tcs = windowTools.get(associatedParent);
		if (tcs == null) {
			tcs = new ArrayList<JComponent>();
			windowTools.put(associatedParent, tcs);
		}
		if (!tcs.contains(c)) {
			tcs.add(c);
			ToolFrame f = new ToolFrame(c, layout);
			frames.put(c, f);
			if (contentPane.getSelectedComponent() == associatedParent) {
				frames.put(c, f);
				mainPane.add(f, JLayeredPane.PALETTE_LAYER);
			}
		}
	}

	/**
	 * Removes window from the `mainPane` (and the `windowTools` structure, but not the `frames` structure)
	 */
	@Deprecated
	public void removeToolWindow(JComponent associatedParent, JComponent c) {
		List<JComponent> tcs = windowTools.get(associatedParent);
		if (tcs != null) {
			tcs.remove(c);
		}
		if (contentPane.getSelectedComponent() == associatedParent)
			mainPane.remove(frames.get(c));
	}

	/**
	 * 
	 * @param associatedParent parent with which to associate the new tool window
	 * @param c the tool window component to add
	 * @param side the side at which the inserted pane should be
	 * @param size its initial width/height in pixels
	 */
	public void addSideSplit(JComponent associatedParent, JComponent c, Side side, int size) {
		System.out.println(c.toString());
		
		if (side != Side.EAST)
			return;
		
		List<JComponent> tcs = windowTools.get(associatedParent);
		if (tcs == null) {
			tcs = new ArrayList<JComponent>();
			windowTools.put(associatedParent, tcs);
		}
		if (!tcs.contains(c)) {
			tcs.add(c);
		}
	}
	
	// TODO
	public void removeSideSplit(JComponent associatedParent, JComponent c) {
		
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
	 * TODO this is literally a no-op, delete?
	 */
	public void focusToolWindow(JComponent c) {
		try {
			// do nothing
			// toolPane.setSelectedComponent(c);
		} catch (IllegalArgumentException e) {
			// if the component is not in there, then the corresponding
			// window is not focused, and we ignore the request
		}
	}
	
	/**
	 * Selects component `c` and notifies contentPane's ChangeListeners
	 */
	public void focusContentWindow(JComponent c) {
		contentPane.setSelectedComponent(c);
		for (ChangeListener cl : contentPane.getChangeListeners())
			cl.stateChanged(new ChangeEvent(contentPane));
	}
	
	public JFrame getMainWindow() {
		return mainWindow;
	}

}
