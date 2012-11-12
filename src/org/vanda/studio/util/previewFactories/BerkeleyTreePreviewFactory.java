package org.vanda.studio.util.previewFactories;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.core.DefaultPreviewFactory;

public class BerkeleyTreePreviewFactory implements PreviewFactory {

	private static String TMP = System.getProperty("java.io.tmpdir");
	private static String TMP_TEX = TMP + "/berkeleyTree.tex";
	private static String TMP_PNG = TMP + "/berkeleyTree.png";

	public class BerkeleyTree {

		protected String texString;
		protected String berkeleyString;

		public BerkeleyTree(String berkeleyString) {
			texString = "\\Tree "
					+ berkeleyString.replaceAll("\\( ", "")
							.replaceAll(" \\)", "").replaceAll("\\(", "[.")
							.replaceAll("\\)", " ]").replaceAll("\\$", "'");
			this.berkeleyString = berkeleyString;
		}

		public String getYield() {
			return berkeleyString;
		}

		public String getTexString() {
			return texString;
		}

		public String getFullTex() {
			String result = "\\documentclass[convert={density=120,outext=.png}]{standalone}\n"
					+ "\\usepackage{qtree}\n"
					+ "\\begin{document}\n"
					+ getTexString() + "\n" + "\\end{document}";

			return result;
		}
	}

	@SuppressWarnings("serial")
	public class BerkeleyTreePreview extends JPanel {

		private Map<String, BerkeleyTree> trees;
		private List<String> yields;

		private JLabel jTree;
		private JList lTrees;

		private JButton bMore;

		private Scanner scan;

		public static final int SIZE = 20;

		public BerkeleyTreePreview(String value) {
			try {
				scan = new Scanner(new FileInputStream(value));
			} catch (FileNotFoundException e1) {
				// ignore
			}
			trees = new HashMap<String, BerkeleyTreePreviewFactory.BerkeleyTree>();
			yields = new ArrayList<String>();
			setLayout(new BorderLayout());
			bMore = new JButton(new AbstractAction("more") {

				@Override
				public void actionPerformed(ActionEvent e) {
					more();

				}
			});
			jTree = new JLabel();
			lTrees = new JList();
			lTrees.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					BerkeleyTree t = (BerkeleyTree) trees.get(lTrees
							.getSelectedValue());
					ImageIcon ic = generatePicture(t.getFullTex());
					ic.getImage().flush();
					jTree.setIcon(ic);
					jTree.repaint();
					// BerkeleyTreePreview.this.getLayout().layoutContainer(
					// BerkeleyTreePreview.this);
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
			String l;
			BerkeleyTree b;
			int i = 0;
			while (i < SIZE & scan.hasNextLine()) {
				l = scan.nextLine();
				b = new BerkeleyTree(l);
				trees.put(b.getYield(), b);
				yields.add(b.getYield());
				i++;
			}
			if (!scan.hasNextLine())
				bMore.setEnabled(false);
			lTrees.setListData(yields.toArray());
			revalidate();
		}

		public ImageIcon generatePicture(String tex) {
			BufferedWriter out;
			try {
				out = new BufferedWriter(new FileWriter(TMP_TEX));
				out.write(tex);
				out.close();
				Process p = Runtime.getRuntime().exec(
						"pdflatex -shell-escape " + TMP_TEX, null,
						new File(TMP));
				InputStreamReader isr = new InputStreamReader(
						p.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				while (br.readLine() != null)
					;
				ImageIcon img = new ImageIcon(TMP_PNG);
				return img;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}

	@Override
	public JComponent createPreview(String value) {
		if ((new File(value)).exists())
			return new BerkeleyTreePreview(value);
		else
			return (new DefaultPreviewFactory(null)).createPreview(value);
	}

	@Override
	public void openEditor(String value) {
		// TODO Auto-generated method stub

	}

}
