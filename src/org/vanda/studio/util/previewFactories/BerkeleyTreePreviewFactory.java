package org.vanda.studio.util.previewFactories;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.core.DefaultPreviewFactory;

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
		
		public String nestedParentheses() {
			String result = label + "(";
			for (Tree c : children)
				result += c.toString() + ", ";
			return (result + ")").replace(", )", ")").replace("()", "");
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

	public class Tuple<S, T> {
		public S s;
		public T t;

		public Tuple(S s, T t) {
			this.s = s;
			this.t = t;
		}
	}

	public Tuple<String, Tree> parseTree(String string) {
		if (string.startsWith(" ")) {
			// remove whitespaces at the beginning
			return parseTree(string.substring(1));
		} else if (!string.startsWith("(")) {
			// generate leaves (base case)
			String xt = string.substring(string.indexOf(')'));
			String label = string.substring(0, string.indexOf(')'));
			return new Tuple<String, Tree>(xt, new Tree(label));
		} else {
			// recursion case
			String[] xs = string.substring(1).replaceFirst(" ", "#").split("#");
			String label = xs[0];
			List<Tree> children = new ArrayList<Tree>();
			String xt = xs[1];
			while (!xt.startsWith(")")) {
				Tuple<String, Tree> tpl = parseTree(xt);
				children.add(tpl.t);
				xt = tpl.s;
			}
			return new Tuple<String, Tree>(xt.substring(1), new Tree(label,
					children.toArray(new Tree[0])));
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
					berkeleyString.length() - 2)).t);
		}

		public TreeView(Tree t) {
			super();
			setBackground(Color.WHITE);
			seeds = new HashMap<Tree, Integer>();
			tree = t;
		}

		public void setTree(String s) {
			setTree(parseTree(s.substring(2, s.length() - 2)).t);
		}

		public void setTree(Tree t) {
			tree = t;
			repaint();
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
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
				result.height = FONT_SIZE + (tree.levels() - 1) * (DY + FONT_SIZE) + 4;
			else
				result.height = 0;
			result.width = width + 2;
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
		private List<Tree> trees;
		private JList lTrees;
		private TreeView jTree;
		private JScrollPane sTree;
		private JButton bMore;

		private Scanner scan;
		public static final int SIZE = 20;

		public BerkeleyTreePreview(String value) {
			try {
				scan = new Scanner(new FileInputStream(value));
			} catch (FileNotFoundException e1) {
				// ignore
			}
			trees = new ArrayList<Tree>();
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
					Tree t = (Tree) lTrees.getSelectedValue();
					jTree.setTree(t);
					sTree.revalidate();
				}
			});
			JPanel pan = new JPanel(new BorderLayout());
			pan.add(new JScrollPane(lTrees), BorderLayout.CENTER);
			pan.add(bMore, BorderLayout.SOUTH);
			sTree = new JScrollPane(jTree);
			JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pan,
					sTree);
			add(split, BorderLayout.CENTER);
			
			more();
		}

		public void more() {
			int i = 0;
			String line;
			while (i < SIZE & scan.hasNextLine()) {
				line = scan.nextLine();
				trees.add(parseTree(line.substring(2,
						line.length() - 2)).t);
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

	public void openEditor(String value) {
		// TODO Auto-generated method stub

	}

}
