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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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
import org.vanda.studio.modules.workflows.impl.DefaultWorkflowEditorImpl;
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
	protected SplitTabbedPane contentPane;
	protected JMenuBar menuBar;
	protected JPanel iconToolBar;
	protected JMenu fileMenu;
	protected HashMap<UIMode, JRadioButtonMenuItem> modeMenuItems;
	protected HashMap<JComponent, JMenu> windowMenus;
	protected HashMap<JComponent, JPanel> iconToolBars;
	
	// Note: We still need the damn bookkeeping for rebuilding everything
	// from scratch in the case of a sidesplit removal!
	/**
	 * Maps a window to a list of its tool pane components
	 * Note: the key `null` maps to tool panes that should be shown for every window
	 */
	protected HashMap<JComponent, List<JComponent>> windowTools;
	/**
	 * Maps tool window panes to the (positioned and sized) sidesplits that are created for holding them
	 */
	protected HashMap<JComponent, SideSplitPane> frames;
	
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
	 * Simple struct to store all side-panes with their position and size
	 */
	public static class SideSplitPane {
		public JComponent component;

		/** the side at which the inserted pane should be */
		public Side side;
		
		/** its initial width/height in pixels */
		public int size;
		
		public SideSplitPane(JComponent c, Side sd, int sz) {
			component = c;
			side = sd;
			size = sz;
		}
	}
	
	private static class SplitTabbedPane extends JTabbedPane {
		/* TODO
		 * Idea: store the cores alongside the components!
		 *       This will make "lookup" trivial
		 *       ...and eliminate the disgusting hardcoded GraphComponent!
		 */
		
		public void setSelectedCoreComponent(JComponent c) {
			setSelectedIndex(indexOfCoreComponent(c));
		}

		public int indexOfCoreComponent(JComponent c) {
			// The easy case: the core we searched for is on the top level
			int rootIndex = indexOfComponent(c);
			if(rootIndex >= 0)
				return rootIndex;
			
			// The hard case: we have to traverse each tab until we reach the core
			for(int i = getTabCount() - 1; i >= 0; --i)
				if (getCoreComponentAt(i) == c)
					return i;
			
			// No luck at all?
			return -1;
		}

		/**
		 * @return `null` iff the root component is the core component
		 */
		private JSplitPane getInnermostSplitPane(int i) {
			Component current, left, right;
			boolean isLeftSplit, isRightSplit;
			current = getComponentAt(i);
			
			// Maybe we haven't split yet!
			if(!(current instanceof JSplitPane))
				return null;
			
			while(true) {
				left = ((JSplitPane) current).getLeftComponent();
				right = ((JSplitPane) current).getRightComponent();
				isLeftSplit = left instanceof JSplitPane;
				isRightSplit = right instanceof JSplitPane;
				
				if(isLeftSplit && !isRightSplit)
					current = left;
				else if(!isLeftSplit && isRightSplit)
					current = right;
				else if(!isLeftSplit && !isRightSplit) {
					return (JSplitPane) current;
				} else
					throw new RuntimeException("Non-chain splitting present in a SplitTabbedPane!");
			}
		}
		
		private Component getCoreComponentAt(int i) {
			JSplitPane innermostSplit = getInnermostSplitPane(i);

			// Non-split case
			if(innermostSplit == null)
				return getComponentAt(i);
			
			if (innermostSplit.getLeftComponent() instanceof DefaultWorkflowEditorImpl.MyMxGraphComponent) // TODO change type visibility back to protected!!!
				return innermostSplit.getLeftComponent();
			else
				return innermostSplit.getRightComponent();
		}
		
		public Component getSelectedCoreComponent() {
			if(getSelectedComponent() == null)
				return null;
			else
				return getCoreComponentAt(getSelectedIndex());
		}
		
		public void addSplitAt(int i, SideSplitPane p) {
			JSplitPane innermostSplit = getInnermostSplitPane(i);
			Component coreComponent = getCoreComponentAt(i);
			
			// Will be read only after being properly initialized, but java doesn't get that
			boolean isLeft = false;
			
			// If we already have some split, release core from it, because:
			// "Swing UIs are hierarchical and you can only add a component to the hierarchy once.
			// If you add a component to more than one container you'll get unpredictable results."
			// (http://stackoverflow.com/questions/1113799/whats-wrong-with-jsplitpanel-or-jtabbedpane)
			if(innermostSplit != null) {
				isLeft = innermostSplit.getLeftComponent() == coreComponent;
				
				if(isLeft)
					innermostSplit.setLeftComponent(null);
				else
					innermostSplit.setRightComponent(null);
			} else { // First split, but release core as well
				setComponentAt(i, null);
			}
			
			final JSplitPane newSplitPane;
			
			switch (p.side) {
				case NORTH:
					newSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
						p.component, coreComponent);
					break;
				case EAST:
					newSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
						coreComponent, p.component);
					break;
				case SOUTH:
					newSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
						coreComponent, p.component);
					break;
				case WEST:
					newSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
						p.component, coreComponent);
					break;
				default:
					newSplitPane = null; // can't happen, but java
			}
			
			newSplitPane.setOneTouchExpandable(true);
			
			// We already had a split to join into
			if(innermostSplit != null) {
				if(isLeft)
					innermostSplit.setLeftComponent(newSplitPane);
				else
					innermostSplit.setRightComponent(newSplitPane);
			} else { // This is the first split
				setComponentAt(i, newSplitPane);
			}

			// Now size it properly
			switch (p.side) {
				case NORTH:
					newSplitPane.setDividerLocation(p.size);
					break;
				case EAST:
					// We could add a ComponentListener to the newSplitPane
					// and on resize use newSpliPane.getWidth(), but I prefer this
					// solution with technically wrong / guessed sizes, but less nasty overhead.
					newSplitPane.setDividerLocation(this.getWidth() - 18 - p.size);
					break;
				case SOUTH:
					// (Same thing here)
					newSplitPane.setDividerLocation(this.getHeight() - this.getTabRunCount() * 34 - p.size);
					break;
				case WEST:
					newSplitPane.setDividerLocation(p.size);
					break;
			}
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
		frames = new HashMap<JComponent, SideSplitPane>();

		// Create the central tabbed pane containing the graph...
		contentPane = new SplitTabbedPane();
		contentPane.addChangeListener(new ChangeListener() {
			/* Switching tabs should change things other than just the contentPane itself:
			 * the menu, the iconToolBar and the tool windows!
			 */
			@Override
			public void stateChanged(ChangeEvent e) {
				// Rebuild menu
				Component parent = contentPane.getSelectedCoreComponent();
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
			}

		});
		
		// Add bars and panes to the window
		mainWindow.setJMenuBar(menuBar);
		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.getContentPane().add(contentPane, BorderLayout.CENTER);
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
	 * Adds a new tool pane to the window (or more exactly to the internal data structures)
	 * @param associatedParent parent with which to associate the new tool window
	 * @param p the tool pane to add
	 */
	public void addSideSplit(JComponent associatedParent, SideSplitPane p) {
		List<JComponent> sspcs = windowTools.get(associatedParent);
		if (sspcs == null) {
			sspcs = new ArrayList<JComponent>();
			windowTools.put(associatedParent, sspcs);
		}
		if (!sspcs.contains(p.component)) {
			sspcs.add(p.component);
			frames.put(p.component, p);
		}
		
		contentPane.addSplitAt(contentPane.indexOfCoreComponent(associatedParent), p);
	}
	
	public void removeSideSplit(JComponent associatedParent, JComponent c) {
		List<JComponent> sspcs = windowTools.get(associatedParent);
		if (sspcs != null) {
			sspcs.remove(c);
		}
		
		// TODO
		//rebuildSideSplits(associatedParent);
	}

	// TODO deprecate this and all that bookkeeping shit!
	private void rebuildSideSplits(JComponent graph) {
		// Swing UIs are hierarchical and you can only add a component to the hierarchy once.
		// If you add a component to more than one container you'll get unpredictable results.
		// (http://stackoverflow.com/questions/1113799/whats-wrong-with-jsplitpanel-or-jtabbedpane)
		
		JComponent newRoot = graph;
		int oldIndex = contentPane.indexOfCoreComponent(graph);
		contentPane.setComponentAt(oldIndex, null);
		
		if(windowTools.get(graph) != null)
		for(JComponent toolComponent : windowTools.get(graph)) {
			SideSplitPane tool = frames.get(toolComponent);
			
			//toolPane.setMaximumSize(new Dimension(400, mainPane.getHeight()));
			//contentPane.setMinimumSize(new Dimension(500,0));
			
			JSplitPane splitPane = null;
			
			//System.err.println(">>>>>>>>> " + tool.side.toString());
			
			switch (tool.side) {
				case NORTH:
					splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
						tool.component, newRoot);
					break;
				case EAST:
					splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
						newRoot, tool.component);
					break;
				case SOUTH:
					splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
						newRoot, tool.component);
					break;
				case WEST:
					splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
						tool.component, newRoot);
					break;
			}
			
			splitPane.setOneTouchExpandable(true);
			
			newRoot = splitPane;
		}

		contentPane.setComponentAt(oldIndex, newRoot);
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
