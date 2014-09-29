package org.vanda.studio.modules.previews;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.util.ExceptionMessage;

public class AlignmentsPreviewFactory implements PreviewFactory {

	public class Alignment {
		public String[] as1, as2, al;

		public Alignment(String s1, String s2, String aal) {
			as1 = s1.split(" ");
			as2 = s2.split(" ");
			al = aal.split(" ");
		}

		@Override
		public String toString() {
			String res = "";
			for (int i = 0; i < al.length - 1; i++)
				res += al[i] + " ";
			res += al[al.length - 1];
			return res;
		}
	}

	@SuppressWarnings("serial")
	public class AlignmentsPreview extends JPanel {
		private List<Alignment> als;
		private Scanner eHandle, fHandle, aHandle;
		private String file;
		private AlignmentView jAl;
		private JList jAls;
		private JButton jMore;
		private JScrollPane sAl;
		private JSplitPane split;

		public AlignmentsPreview(String file) {
			this.file = file;
			als = new ArrayList<Alignment>();
			jAls = new JList();
			jMore = new JButton(new AbstractAction("more") {

				@Override
				public void actionPerformed(ActionEvent e) {
					int idx = jAls.getSelectedIndex();
					load();
					jAls.setSelectedIndex(idx);
				}

			});

			JPanel pLeft = new JPanel(new BorderLayout());
			pLeft.add(new JScrollPane(jAls), BorderLayout.CENTER);
			pLeft.add(jMore, BorderLayout.SOUTH);

			jAl = new AlignmentView();
			split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			split.add(pLeft, 0);
			sAl = new JScrollPane(jAl);
			split.add(sAl, 1);

			jAls.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					Alignment a = (Alignment) jAls.getSelectedValue();
					jAl.setAlignment(a);
					sAl.revalidate();
				}
			});
			setLayout(new GridLayout(1, 1));
			add(split);
			split.setResizeWeight(0.5);
		}

		public void initialize() throws FileNotFoundException {
			File f = new File(file + ".meta");
			Scanner mHandle = new Scanner(f);
			File f1 = new File(app.findFile(mHandle.nextLine()));
			fHandle = new Scanner(f1);
			File f2 = new File(app.findFile(mHandle.nextLine()));
			eHandle = new Scanner(f2);
			mHandle.close();
			File f3 = new File(file);
			aHandle = new Scanner(f3);
		}

		public void load() {
			int i = 10;
			while (i > 0 && fHandle.hasNextLine() && eHandle.hasNextLine()
					&& aHandle.hasNextLine()) {
				als.add(new Alignment(fHandle.nextLine(), eHandle.nextLine(),
						aHandle.nextLine()));
				i--;
			}
			jAls.setListData(als.toArray());
			revalidate();
		}
	}

	public class AlignmentView extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7932798413164129787L;
		private static final int DYR = 3;
		private static final int FONT_SIZE = 14;
		private static final int DX = 10;
		private static final int DY = 5 * (FONT_SIZE + DYR);
		private Alignment al;
		private int width = DX;

		@Override
		public Dimension getPreferredSize() {
			Dimension d = new Dimension();
			d.height = 2 * FONT_SIZE + DY + 4 * DYR;
			d.width = width;
			return d;
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (al == null)
				return;
			Graphics2D g2d = (Graphics2D) g;

			// configure rendering
			g2d.setStroke(new BasicStroke(1.5f));
			g2d.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			FontMetrics fm = g2d.getFontMetrics();

			// parse alignments
			int[] ali = new int[al.as2.length];
			for (String a : al.al) {
				ali[Integer.parseInt(a.trim().split("-")[0])] = Integer
						.parseInt(a.trim().split("-")[1]);
			}

			// offset lines for centering
			int w1 = 0, w2 = 0;
			for (String a : al.as1)
				w1 += fm.stringWidth(a);
			w1 += (al.as1.length - 1) * DX;
			for (String a : al.as2)
				w2 += fm.stringWidth(a);
			w2 += (al.as2.length - 1) * DX;
			int x1, x2;
			if (w1 > w2) {
				x1 = DX;
				x2 = DX + (w1 - w2) / 2;
			} else {
				x1 = DX + (w2 - w1) / 2;
				x2 = DX;
			}

			// initialize data
			int[] midXs1 = new int[al.as1.length];
			int width;
			int i = 0, mid;

			// draw upper sentence
			for (String s : al.as1) {
				width = fm.stringWidth(s);
				g2d.drawString(s, x1, DYR + FONT_SIZE);

				midXs1[i] = x1 + width / 2;
				x1 += width + DX;
				i++;
			}

			// draw lower sentence and lines
			i = 0;
			for (String s : al.as2) {
				width = fm.stringWidth(s);
				mid = x2 + width / 2;

				g2d.drawString(s, x2, DY + DYR + 2 * FONT_SIZE);
				g2d.drawLine(mid, DY + FONT_SIZE, midXs1[ali[i]], 2 * DYR
						+ FONT_SIZE + fm.getDescent());

				x2 += width + DX;
				i++;
			}

			// set width for getPreferredSize()
			this.width = Math.max(x1, x2);
		}

		public void setAlignment(Alignment al) {
			this.al = al;
			repaint();
		}

	}

	private Application app;

	public AlignmentsPreviewFactory(Application app) {
		super();
		this.app = app;
	}

	@Override
	public JComponent createPreview(String value) {
		try {
			AlignmentsPreview aps = new AlignmentsPreview(value);
			aps.initialize();
			aps.load();
			return aps;
		} catch (FileNotFoundException e) {
			app.sendMessage(new ExceptionMessage(e));
			return app.getPreviewFactory(null).createPreview(value);
		}
	}

	@Override
	public JComponent createSmallPreview(String absolutePath) {
		return createPreview(absolutePath);
	}

	@Override
	public void openEditor(final String value) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Desktop.getDesktop().open(new File(value));
				} catch (IOException e) {
					app.sendMessage(new ExceptionMessage(e));
				}
			}
		});

		t.start();
	}

}
