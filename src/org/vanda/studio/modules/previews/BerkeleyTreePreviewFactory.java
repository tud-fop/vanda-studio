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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
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

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.core.DefaultPreviewFactory;
import org.vanda.studio.util.Pair;

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
			if (children.length == 0) {
				return label;
			} else {
				String result = "";
				for (Tree c : children) {
					result += c.yield() + " ";
				}
				return result.trim().replaceAll(" +", " ");
			}
		}

		@Override
		public String toString() {
			return yield();
		}
	}

	public Pair<String, Tree> parseTree(String string) {
		if (string.startsWith(" ")) {
			// remove whitespaces at the beginning
			return parseTree(string.substring(1));
		} else if (!string.startsWith("(")) {
			// generate leaves (base case)
			String xt = string.substring(string.indexOf(')'));
			String label = string.substring(0, string.indexOf(')'));
			return new Pair<String, Tree>(xt, new Tree(label));
		} else {
			// recursion case
			String[] xs = string.substring(1).replaceFirst(" ", "#").split("#");
			String label = xs[0];
			List<Tree> children = new ArrayList<Tree>();
			String xt = xs[1];
			while (!xt.startsWith(")")) {
				Pair<String, Tree> tpl = parseTree(xt);
				children.add(tpl.snd);
				xt = tpl.fst;
			}
			return new Pair<String, Tree>(xt.substring(1), new Tree(label,
					children.toArray(new Tree[0])));
		}
	}

	class DragScrollListener extends MouseAdapter implements MouseWheelListener {
		private final Cursor defCursor = Cursor
				.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		private final Cursor hndCursor = Cursor
				.getPredefinedCursor(Cursor.HAND_CURSOR);
		private final Point pp = new Point();
		private final TreeView tv;
		private final JScrollPane scr;
		
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getWheelRotation() < 0)
				tv.zoomOut();
			else if (e.getWheelRotation() > 0)
				tv.zoomIn();
			scr.revalidate();
		}
		
		public DragScrollListener(TreeView tv, JScrollPane scr){
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
			JComponent jc = (JComponent) e.getSource();
			Container c = jc.getParent();
			if (c instanceof JViewport) {
				jc.setCursor(hndCursor);
				JViewport vport = (JViewport) c;
				Point cp = SwingUtilities.convertPoint(jc, e.getPoint(), vport);
				pp.setLocation(cp);
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

		public TreeView(String berkeleyString) {
			this(parseTree(berkeleyString.substring(2,
					berkeleyString.length() - 2)).snd);
		}

		public TreeView(Tree t) {
			super();
			setBackground(Color.WHITE);
			seeds = new HashMap<Tree, Integer>();
			tree = t;
		}

		public void setTree(String s) {
			setTree(parseTree(s.substring(2, s.length() - 2)).snd);
		}

		public void setTree(Tree t) {
			tree = t;
			repaint();
		}

		public void zoomIn() {
			zoomFactor *= Math.pow(2, 0.25);
			repaint();
		}

		public void zoomOut() {
			zoomFactor *= Math.pow(2, -0.25);
			repaint();
		}

		public void zoomReset() {
			zoomFactor = 1;
			repaint();
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
			JLabel lbl = new JLabel(t.label);
			lbl.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));
			int width = lbl.getPreferredSize().width;
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
		private TreeView jTree;
		private JScrollPane sTree;
		private JButton bMore, bZoomIn, bZoomOut, bZoomN;

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
				lTrees = new JList();
				lTrees.addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						Pair<String, Tree> tpl = (Pair<String, Tree>) lTrees
								.getSelectedValue();
						jTree.setTree(tpl.snd);
						sTree.revalidate();
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
								lTrees, tpl.snd, index, isSelected, cellHasFocus);
						lbl.setText("<html><b>" + tpl.snd.yield() + "</b><br><i>"
								+ tpl.fst + "</i></html>");
						return lbl;
					}
				});

				bZoomIn = new JButton(new AbstractAction("+") {
					@Override
					public void actionPerformed(ActionEvent e) {
						jTree.zoomIn();
						sTree.revalidate();
					}
				});
				bZoomN = new JButton(new AbstractAction("O") {
					@Override
					public void actionPerformed(ActionEvent e) {
						jTree.zoomReset();
						sTree.revalidate();
					}
				});
				bZoomOut = new JButton(new AbstractAction("-") {
					@Override
					public void actionPerformed(ActionEvent e) {
						jTree.zoomOut();
						sTree.revalidate();
					}
				});

				JPanel pan = new JPanel(new BorderLayout());
				pan.add(new JScrollPane(lTrees), BorderLayout.CENTER);
				pan.add(bMore, BorderLayout.SOUTH);
				JPanel panR = new JPanel(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.fill = GridBagConstraints.BOTH;
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = 1;
				gbc.weighty = 1;
				gbc.gridheight = 2;
				sTree = new JScrollPane(jTree);
				DragScrollListener dsl = new DragScrollListener(jTree, sTree);
				jTree.addMouseMotionListener(dsl);
				jTree.addMouseListener(dsl);
				jTree.addMouseWheelListener(dsl);
				panR.add(sTree, gbc);

				gbc.fill = GridBagConstraints.NONE;
				gbc.anchor = GridBagConstraints.SOUTH;
				gbc.weightx = 0;
				gbc.weighty = 0;
				gbc.gridy = 1;
				gbc.gridx = 1;
				gbc.gridheight = 1;
				panR.add(bZoomOut, gbc);

				gbc.gridx = 2;
				panR.add(bZoomN, gbc);

				gbc.gridx = 3;
				panR.add(bZoomIn, gbc);

				JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pan,
						panR);
				add(split, BorderLayout.CENTER);

				more();
			} catch (FileNotFoundException e1) {
				add(new JLabel("File does not exist."));
			}
			
		}

		public void more() {
			int i = 0;
			String line;
			while (i < SIZE & scan.hasNextLine()) {
				line = scan.nextLine();
				trees.add(new Pair<String, Tree>(line, parseTree(line
						.substring(2, line.length() - 2)).snd));
				i++;
			}
			if (!scan.hasNextLine())
				bMore.setEnabled(false);
			lTrees.setListData(trees.toArray());
			revalidate();
		}
	}

	public JComponent createPreview(String value) {
		if ((new File(value)).exists())
			return new BerkeleyTreePreview(value);
		else
			return (new DefaultPreviewFactory(null)).createPreview(value);
	}

	public void openEditor(final String value) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Runtime.getRuntime().exec("xdg-open " + value);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		t.start();
	}

}
