package org.vanda.studio.util.previewFactories;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
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
		 * @param lbl Label of the leave
		 */
		public Tree(String lbl) {
			this(lbl, null);
		}

		@Override
		public String toString() {
			String result = label + "(";
			for (Tree c : children)
				result += c.toString() + ", ";
			return (result + ")").replace(", )", ")").replace("()", "");
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
		public static final int DX = 10;

		/**
		 * vertical distance between anchors of adjoining levels
		 */
		public static final int DY = 50;

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
			System.out.println(t);
			repaint();
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (tree != null)
				drawTree(g, 1, 0, tree);
		}

		/**
		 * Draws a Tree to a Graphics object
		 * @param g target Graphics object
		 * @param level level of the current subtree in the root tree 
		 * @param seedX x coordinate of the latest drawn leaves right edge
		 * @param t subtree to draw
		 * @return new right edge of latest drawn tree
		 */
		public int drawTree(Graphics g, int level, int seedX, Tree t) {
			int currentX = seedX;
			int width = (new JLabel(t.label)).getPreferredSize().width;
			if (t.children.length == 0) {
				g.drawString(t.label, currentX + DX, level * DY);
				seeds.put(t, currentX + DX + width / 2);
				currentX += DX + width;
			} else {
				for (Tree t1 : t.children) {
					currentX = drawTree(g, level + 1, currentX, t1);
				}
				int xMid = (seeds.get(t.children[0]) + seeds
						.get(t.children[t.children.length - 1])) / 2;
				g.drawString(t.label, xMid - width / 2, level * DY);
				seeds.put(t, xMid);
				for (Tree t2 : t.children) {
					g.drawLine(xMid, level * DY + 4, seeds.get(t2), level * DY
							+ DY - 13);
				}
			}
			return currentX;
		}
	}

	public class BerkeleyTreePreview extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private List<String> yields;
		private JList lTrees;
		private TreeView jTree;
		private JButton bMore;

		private Scanner scan;
		public static final int SIZE = 20;

		public BerkeleyTreePreview(String value) {
			try {
				scan = new Scanner(new FileInputStream(value));
			} catch (FileNotFoundException e1) {
				// ignore
			}
			yields = new ArrayList<String>();
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
					String s = lTrees.getSelectedValue().toString();
					jTree.setTree(s);
				}
			});
			more();
			JPanel pan = new JPanel(new BorderLayout());
			pan.add(new JScrollPane(lTrees), BorderLayout.CENTER);
			pan.add(bMore, BorderLayout.SOUTH);
			JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pan,
					new JScrollPane(jTree));
			split.setDividerLocation(0.5);
			add(split, BorderLayout.CENTER);
		}

		public void more() {
			int i = 0;
			while (i < SIZE & scan.hasNextLine()) {
				yields.add(scan.nextLine());
				i++;
			}
			if (!scan.hasNextLine())
				bMore.setEnabled(false);
			lTrees.setListData(yields.toArray());
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
