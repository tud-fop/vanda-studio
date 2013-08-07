/**
 *
 */
package org.vanda.studio.core;

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
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.LayoutAssortment;
import org.vanda.studio.app.LayoutSelector;
import org.vanda.studio.app.UIMode;
import org.vanda.studio.app.WindowSystem;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;

/**
 * @author buechse, rmueller
 * 
 */
public class WindowSystemImpl implements WindowSystem {

	private static int ICON_SIZE = 24;
	
	protected final Application app;
	protected final JFrame mainWindow;
	protected JLayeredPane mainPane;
	protected JTabbedPane contentPane;
	// protected JTabbedPane toolPane;
	protected JMenuBar menuBar;
	protected JPanel iconToolBar;
	protected JMenu fileMenu;
	protected HashMap<UIMode, JRadioButtonMenuItem> modeMenuItems;
	protected HashMap<JComponent, JMenu> windowMenus;
	protected HashMap<JComponent, JPanel> iconToolBars;
	protected HashMap<JComponent, List<JComponent>> windowTools;
	protected ButtonGroup modeGroup;
	protected HashMap<JComponent, JInternalFrame> frames;

	@SuppressWarnings("serial")
	private static class LayoutTabbedPane extends JTabbedPane implements
			LayoutSelector {

		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getCenter();
		}

	}

	/**
	 * @param a
	 *            Vanda Composer Application root object
	 */
	public WindowSystemImpl(Application app) {
		this.app = app;
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
		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				WindowSystemImpl.this.app.shutdown();
			}
		});

		// Create a simple JMenuBar
		menuBar = new JMenuBar();
		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		fl.setHgap(1);
		fl.setVgap(1);
		iconToolBar = new JPanel(fl);
		iconToolBar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		fileMenu = new JMenu("Studio");
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowSystemImpl.this.app.shutdown();
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
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(m.getName(), app.getUIMode() == m);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					WindowSystemImpl.this.app.setUIMode(m);
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
		iconToolBars = new HashMap<JComponent, JPanel>();
		frames = new HashMap<JComponent, JInternalFrame>();

		mainWindow.setJMenuBar(menuBar);

		// Creates the library pane that contains the tabs with the palettes
		contentPane = new LayoutTabbedPane();
		// toolPane = new JTabbedPane();

		// toolPanel = new JPanel(new BorderLayout());
		// toolPanel.add(new JPanel(), BorderLayout.CENTER);
		// toolPanel.add(toolPane, BorderLayout.SOUTH);

		/*
		 * JPanel pp = new JPanel(); pp.setOpaque(false);
		 * 
		 * JSplitPane inner2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pp,
		 * toolPane); inner2.setOpaque(false);
		 * inner2.setOneTouchExpandable(true); inner2.setDividerLocation(0.7);
		 * inner2.setResizeWeight(0.7); inner2.setDividerSize(6);
		 * inner2.setBorder(null);
		 */

		mainPane = new JLayeredPane();
		mainPane.setLayout(new LayerLayout());
		// contentPane.setBounds(0, 0, 500, 500);
		// inner2.setBounds(0, 300, 500, 500);
		mainPane.add(contentPane, JLayeredPane.DEFAULT_LAYER);
		// inner.add(inner2, new Integer(1));
		// mainPane.setBorder(BorderFactory.createTitledBorder(
		// "Move the Mouse to Move Duke"));

		contentPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// FIXME do not change tool windows when there is merely a
				// new title for a content window
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
				if (iconToolBars.get(c) != null) {
					iconToolBar.removeAll();
					iconToolBar.add(iconToolBars.get(null));
					if (c != null)
						iconToolBar.add(iconToolBars.get(c));
				}
				menuBar.revalidate();
				menuBar.repaint();
				mainPane.removeAll();
				mainPane.add(contentPane, JLayeredPane.DEFAULT_LAYER);
				List<JComponent> tcs = windowTools.get(null);
				if (tcs != null) {
					for (JComponent tc : tcs)
						mainPane.add(frames.get(tc), JLayeredPane.PALETTE_LAYER);
				}
				tcs = windowTools.get(c);
				if (tcs != null) {
					for (JComponent tc : tcs)
						mainPane.add(frames.get(tc), JLayeredPane.PALETTE_LAYER);
				}
			}

		});

		// Puts everything together
		mainWindow.getContentPane().add(mainPane, BorderLayout.CENTER);
		mainWindow.getContentPane().add(iconToolBar, BorderLayout.NORTH);
		// mainWindow.getContentPane().add(statusBar, BorderLayout.SOUTH);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				/*
				 * for (ToolWindow toolWin : toolWindowManager.getToolWindows())
				 * { toolWin.setAvailable(true); toolWin.setActive(true); }
				 */

				mainWindow.setVisible(true);
			}
		});
	}

	@Override
	public void addAction(JComponent c, final Action a, String imageName,
			KeyStroke keyStroke) {
		URL url = ClassLoader.getSystemClassLoader().getResource(
				imageName + ".png");
		JButton b = new JButton();
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				a.invoke();
			}
		});
		try {
			Image i = ImageIO.read(url);
			b.setIcon(new ImageIcon(i.getScaledInstance(ICON_SIZE, ICON_SIZE,
					Image.SCALE_SMOOTH)));
			b.setPreferredSize(new Dimension(ICON_SIZE + 2, ICON_SIZE + 2));
			b.setMargin(new Insets(0, 0, 0, 0));
			b.setToolTipText(a.getName());
		} catch (IOException e1) {
			app.sendMessage(new ExceptionMessage(e1));
			b.setText(a.getName());
		}
		if (!iconToolBars.containsKey(c)) {
			FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
			fl.setVgap(1);
			iconToolBars.put(c, new JPanel(fl));
			iconToolBars.get(c).setComponentOrientation(
					ComponentOrientation.LEFT_TO_RIGHT);
		}
		iconToolBars.get(c).add(b);
		if (c == null)
			for (ChangeListener cl : contentPane.getChangeListeners())
				cl.stateChanged(new ChangeEvent(contentPane));
		addAction(c, a, keyStroke);
	}

	@Override
	public void addAction(JComponent c, final Action a, KeyStroke keyStroke) {
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
		if (c != null) {
			JMenu menu = windowMenus.get(c);
			if (menu == null) {
				menu = new JMenu(c.getName());
				windowMenus.put(c, menu);
				if (contentPane.getSelectedComponent() == c)
					menuBar.add(menu, 1);
			}
			menu.add(item);
		} else
			fileMenu.insert(item, 0);
	}

	/**
	 */
	@Override
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
	 */
	@Override
	public void addSeparator() {
		fileMenu.insertSeparator(0);
	}

	/**
	 */
	@Override
	public void addToolWindow(JComponent window, Icon i, JComponent c,
			LayoutSelector layout) {
		List<JComponent> tcs = windowTools.get(window);
		if (tcs == null) {
			tcs = new ArrayList<JComponent>();
			windowTools.put(window, tcs);
		}
		if (!tcs.contains(c)) {
			tcs.add(c);
			ToolFrame f = new ToolFrame(c, layout);
			frames.put(c, f);
			if (contentPane.getSelectedComponent() == window) {
				frames.put(c, f);
				mainPane.add(f, JLayeredPane.PALETTE_LAYER);
			}
		}
	}

	@Override
	public void disableAction(Action a) {
		disableAction(null, a);
	}

	@Override
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
	}

	@Override
	public void enableAction(Action a) {
		enableAction(null, a);
	}

	@Override
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
	}

	@Override
	public void focusToolWindow(JComponent c) {
		try {
			// do nothing
			// toolPane.setSelectedComponent(c);
		} catch (IllegalArgumentException e) {
			// if the component is not in there, then the corresponding
			// window is not focused, and we ignore the request
		}
	}

	@Override
	public void focusContentWindow(JComponent c) {
		contentPane.setSelectedComponent(c);
		for (ChangeListener cl : contentPane.getChangeListeners())
			cl.stateChanged(new ChangeEvent(contentPane));
	}
	
	@Override
	public JFrame getMainWindow() {
		return mainWindow;
	}

	/**
	 */
	@Override
	public void removeContentWindow(JComponent c) {
		contentPane.remove(c);
		windowTools.remove(frames.get(c));
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
			mainPane.remove(frames.get(c));
	}

}
