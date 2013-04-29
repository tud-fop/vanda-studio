package org.vanda.studio.modules.previews;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Lexer;
import org.vanda.util.Pair;

@SuppressWarnings({ "unchecked", "serial" })
public class BerkeleyTreePreviewFactory implements PreviewFactory {

	public class Tree {
		private String label;
		private Tree[] children;

		/**
		 * Constructor for nodes
		 */
		public Tree(String lbl, Tree[] cs) {
			label = lbl;
			if (cs != null)
				children = cs;
			else
				children = new Tree[0];
		}

		/**
		 * Constructor for leaves
		 * 
		 * @param lbl
		 *            Label of the leave
		 */
		public Tree(String lbl) {
			this(lbl, null);
		}

		public int levels() {
			int max = 0;
			for (Tree t : children)
				max = Math.max(max, t.levels());
			return max + 1;
		}

		public String yield() {
			if (children.length == 0)
				return label;
			String result = "";
			for (Tree c : children) {
				result += c.yield() + " ";
			}
			return result.trim().replaceAll(" +", " ");
		}

		@Override
		public String toString() {
			return yield();
		}
	}

	public Tree parseTree(String string) {
		string = string.trim();
		if (string.equals("(())") || string.equals("(no parse)"))
			return new Tree("[no parse]");
		if (string.startsWith("( ("))
			string = string.substring(1).trim();
		Lexer lex = new Lexer("()", " ");
		Stack<String> st = lex.lex(string);
		Tree t = parseTree(st);
		if (t.label.equals("ROOT"))
			t = t.children[0];
		return t;
	}

	public Tree parseTree(Stack<String> st) {
		if (st.peek().equals("(")) {
			st.pop();
			String head = st.pop();
			List<Tree> subTrees = new ArrayList<Tree>();
			while (!st.empty() && !st.peek().equals(")")) {
				subTrees.add(parseTree(st));
			}
			if (!st.isEmpty())
				st.pop();
			return new Tree(head, subTrees.toArray(new Tree[0]));
		}
		return new Tree(st.pop());
	}

	class DragScrollListener extends MouseAdapter {
		private final Cursor defCursor = Cursor
				.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		private final Cursor hndCursor = Cursor
				.getPredefinedCursor(Cursor.HAND_CURSOR);
		private final Point pp = new Point();
		private final TreeView tv;
		private final JScrollPane scr;

		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getWheelRotation() < 0)
				tv.zoomIn();
			else if (e.getWheelRotation() > 0)
				tv.zoomOut();
			scr.revalidate();
		}

		public DragScrollListener(TreeView tv, JScrollPane scr) {
			super();
			this.tv = tv;
			this.scr = scr;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			final JComponent jc = (JComponent) e.getSource();
			Container c = jc.getParent();
			if (c instanceof JViewport) {
				JViewport vport = (JViewport) c;
				Point cp = SwingUtilities.convertPoint(jc, e.getPoint(), vport);
				Point vp = vport.getViewPosition();
				vp.translate(pp.x - cp.x, pp.y - cp.y);
				jc.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
				pp.setLocation(cp);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON2) {
				tv.zoomReset();
				scr.revalidate();
			} else {
				JComponent jc = (JComponent) e.getSource();
				Container c = jc.getParent();
				if (c instanceof JViewport) {
					jc.setCursor(hndCursor);
					JViewport vport = (JViewport) c;
					Point cp = SwingUtilities.convertPoint(jc, e.getPoint(),
							vport);
					pp.setLocation(cp);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			((JComponent) e.getSource()).setCursor(defCursor);
		}
	}

	public class TreeView extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * horizontal distance between edges of consecutive leaves
		 */
		public static final int DX = 20;

		public static final int FONT_SIZE = 18;

		/**
		 * vertical distance between anchors of adjoining levels
		 */
		public static final int DY = 40;

		private double zoomFactor = 1;
		private int width = 0;

		/**
		 * Tree to draw
		 */
		private Tree tree;

		/**
		 * x-coordinates of top mid anchor of all subtrees
		 */
		private Map<Tree, Integer> seeds;

		public TreeView(Tree t) {
			super();
			setBackground(Color.WHITE);
			seeds = new HashMap<Tree, Integer>();
			tree = t;
		}

		public void setTree(Tree t) {
			tree = t;
			repaint();
		}

		public TreeView zoomIn() {
			zoomFactor *= Math.pow(2, 0.25);
			repaint();
			return this;
		}

		public TreeView zoomOut() {
			zoomFactor *= Math.pow(2, -0.25);
			repaint();
			return this;
		}

		public TreeView zoomReset() {
			zoomFactor = 1;
			repaint();
			return this;
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			AffineTransform tr2 = new AffineTransform(g2.getTransform());
			tr2.scale(zoomFactor, zoomFactor);
			g2.setTransform(tr2);
			g2.setStroke(new BasicStroke(1.5f));
			g2.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			if (tree != null)
				width = drawTree(g2, 0, 2, tree);
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension result = new Dimension();
			if (tree != null)
				result.height = (int) Math.round((FONT_SIZE
						+ (tree.levels() - 1) * (DY + FONT_SIZE) + 4)
						* zoomFactor);
			else
				result.height = 0;
			result.width = (int) Math.round((width + 2) * zoomFactor);
			return result;
		}

		/**
		 * Draws a Tree to a Graphics object
		 * 
		 * @param g
		 *            target Graphics object
		 * @param level
		 *            level of the current subtree in the root tree
		 * @param seedX
		 *            x coordinate of the latest drawn leaves right edge
		 * @param t
		 *            subtree to draw
		 * @return new right edge of latest drawn tree
		 */
		public int drawTree(Graphics2D g, int level, int seedX, Tree t) {
			int currentX = seedX;
			int width = g.getFontMetrics().stringWidth(t.label);
			int xMid;
			if (t.children.length == 0) {
				g.setColor(Color.BLUE);
				g.drawString(t.label, currentX + DX, 2 + FONT_SIZE + level
						* (DY + FONT_SIZE));
				g.setColor(Color.BLACK);

				xMid = currentX + DX + width / 2;
				seeds.put(t, xMid);
				currentX += DX + width;
			} else {
				// draw the subtrees
				for (Tree t1 : t.children)
					currentX = drawTree(g, level + 1, currentX, t1);

				// determine x value of the nodes center
				xMid = (seeds.get(t.children[0]) + seeds
						.get(t.children[t.children.length - 1])) / 2;
				seeds.put(t, xMid);

				// draw the node
				g.drawString(t.label, xMid - width / 2, 2 + FONT_SIZE + level
						* (DY + FONT_SIZE));

				// draw the branches
				for (Tree t2 : t.children)
					g.drawLine(xMid, 2 + FONT_SIZE + level * (DY + FONT_SIZE)
							+ 4, seeds.get(t2), 2 + FONT_SIZE + (level + 1)
							* (DY + FONT_SIZE) - (FONT_SIZE + 4));
			}
			return currentX;
		}
	}

	public class BerkeleyTreePreview extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private List<Pair<String, Tree>> trees;
		private JList lTrees;
		private JLayeredPane jZoomPane;
		private JPanel jHUDPanel;
		private TreeView jTree;
		private JScrollPane sTree;
		private JButton bMore, bZoomIn, bZoomReset, bZoomOut;

		private Scanner scan;
		public static final int SIZE = 20;

		public BerkeleyTreePreview(String value) {
			try {
				scan = new Scanner(new FileInputStream(value));
				trees = new ArrayList<Pair<String, Tree>>();
				setLayout(new BorderLayout());
				bMore = new JButton(new AbstractAction("more") {

					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						more();
					}
				});

				jTree = new TreeView(new Tree("", null));
				sTree = new JScrollPane(jTree);
				DragScrollListener dsl = new DragScrollListener(jTree, sTree);
				jTree.addMouseMotionListener(dsl);
				jTree.addMouseListener(dsl);
				jTree.addMouseWheelListener(dsl);

				jHUDPanel = new JPanel();
				jHUDPanel.setOpaque(false);
				jHUDPanel.setLayout(new GridLayout(3, 1));
				bZoomIn = new JButton(new AbstractAction("+") {

					@Override
					public void actionPerformed(ActionEvent e) {
						jTree.zoomIn();
						sTree.revalidate();
					}
				});
				bZoomIn.setContentAreaFilled(false);
				bZoomIn.setFocusPainted(false);
				Font f = new Font(bZoomIn.getFont().getName(), Font.BOLD,
						(int) (bZoomIn.getFont().getSize() * 1.4));
				bZoomIn.setFont(f);

				bZoomReset = new JButton(new AbstractAction("O") {

					@Override
					public void actionPerformed(ActionEvent e) {
						jTree.zoomReset();
						sTree.revalidate();
					}
				});
				bZoomReset.setContentAreaFilled(false);
				bZoomReset.setFocusPainted(false);
				bZoomReset.setFont(f);

				bZoomOut = new JButton(new AbstractAction("\u2012") {

					@Override
					public void actionPerformed(ActionEvent e) {
						jTree.zoomOut();
						sTree.revalidate();
					}
				});
				bZoomOut.setContentAreaFilled(false);
				bZoomOut.setFocusPainted(false);
				bZoomOut.setFont(f);

				jHUDPanel.add(bZoomIn);
				jHUDPanel.add(bZoomReset);
				jHUDPanel.add(bZoomOut);

				lTrees = new JList();
				lTrees.addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						Pair<String, Tree> tpl = (Pair<String, Tree>) lTrees
								.getSelectedValue();
						if (tpl != null)
							jTree.setTree(tpl.snd);
						jTree.revalidate();
					}
				});
				lTrees.setCellRenderer(new ListCellRenderer() {

					@Override
					public Component getListCellRendererComponent(JList list,
							Object value, int index, boolean isSelected,
							boolean cellHasFocus) {
						Pair<String, Tree> tpl = (Pair<String, Tree>) value;
						DefaultListCellRenderer df = new DefaultListCellRenderer();
						JLabel lbl = (JLabel) df.getListCellRendererComponent(
								lTrees, tpl.snd, index, isSelected,
								cellHasFocus);
						lbl.setText("<html><b>" + tpl.snd.yield()
								+ "</b><br><i>" + tpl.fst + "</i></html>");
						return lbl;
					}
				});

				JPanel pan = new JPanel(new BorderLayout());
				pan.add(new JScrollPane(lTrees), BorderLayout.CENTER);

				jZoomPane = new JLayeredPane();
				jZoomPane.setLayout(null);
				Rectangle b = jZoomPane.getBounds();
				int pWidth = b.width;
				int pHeight = b.height;
				jZoomPane.setOpaque(false);
				jZoomPane.add(sTree, JLayeredPane.DEFAULT_LAYER);
				sTree.setBounds(0, 0, pWidth, pHeight);
				jZoomPane.add(jHUDPanel, JLayeredPane.PALETTE_LAYER);
				int pWidth1 = Math.max(
						bZoomReset.getPreferredSize().width,
						Math.max(bZoomIn.getPreferredSize().width,
								bZoomOut.getPreferredSize().width));
				int pHeight1 = bZoomIn.getPreferredSize().height
						+ bZoomReset.getPreferredSize().height
						+ bZoomOut.getPreferredSize().height;
				jHUDPanel.setBounds(pWidth - pWidth1, pHeight - pHeight1,
						pWidth1, pHeight1);
				jZoomPane.addComponentListener(new ComponentListener() {

					@Override
					public void componentShown(ComponentEvent e) {
					}

					@Override
					public void componentResized(ComponentEvent e) {
						Rectangle b = jZoomPane.getBounds();

						int pWidth = b.width;
						int pHeight = b.height;
						sTree.setBounds(0, 0, pWidth, pHeight);
						int pWidth1 = Math.max(
								bZoomReset.getPreferredSize().width, Math.max(
										bZoomIn.getPreferredSize().width,
										bZoomOut.getPreferredSize().width));
						int pHeight1 = bZoomIn.getPreferredSize().height
								+ bZoomReset.getPreferredSize().height
								+ bZoomOut.getPreferredSize().height;
						jHUDPanel.setBounds(0, 0, pWidth, pHeight);
						jHUDPanel.setBounds(pWidth - pWidth1 - 20, pHeight
								- pHeight1 - 20, pWidth1, pHeight1);
						sTree.revalidate();
					}

					@Override
					public void componentMoved(ComponentEvent e) {
					}

					@Override
					public void componentHidden(ComponentEvent e) {
					}
				});

				JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
						pan, jZoomPane);
				split.setResizeWeight(0.5);
				add(split, BorderLayout.CENTER);

				more();
			} catch (FileNotFoundException e1) {
				add(new JLabel("File does not exist."));
			}

		}

		public void more() {
			int i = 0;
			String line;
			while (i < SIZE && scan.hasNextLine()) {
				line = scan.nextLine().trim();
				if (!line.isEmpty() && line.startsWith("(")) {
					trees.add(new Pair<String, Tree>(line, parseTree(line)));
					i++;
				}
			}
			if (!scan.hasNextLine())
				bMore.setEnabled(false);
			lTrees.setListData(trees.toArray());
			revalidate();
		}
	}

	protected final Application app;

	public BerkeleyTreePreviewFactory(Application app) {
		this.app = app;
	}

	public JComponent createPreview(String value) {
		if ((new File(value)).exists())
			return new BerkeleyTreePreview(value);
		return app.getPreviewFactory(null).createPreview(value);
	}

	public void openEditor(final String value) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Runtime.getRuntime().exec("xdg-open " + value);
				} catch (IOException e) {
					app.sendMessage(new ExceptionMessage(e));
				}
			}
		});

		t.start();
	}

	@Override
	public JComponent createSmallPreview(String value) {
		Scanner scan;
		try {
			scan = new Scanner(new FileInputStream(value));
			String line = scan.nextLine();
			scan.close();
			TreeView jTree = new TreeView(parseTree(line));
			JScrollPane sTree = new JScrollPane(jTree);
			DragScrollListener dsl = new DragScrollListener(jTree, sTree);
			jTree.addMouseMotionListener(dsl);
			jTree.addMouseListener(dsl);
			jTree.addMouseWheelListener(dsl);
			sTree.setPreferredSize(new Dimension(200, 150));
			jTree.zoomOut().zoomOut();
			return sTree;
		} catch (Exception e) {
			return new JLabel("wrong format");
		}
	}
}
