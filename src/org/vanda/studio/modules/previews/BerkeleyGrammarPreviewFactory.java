package org.vanda.studio.modules.previews;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;

public class BerkeleyGrammarPreviewFactory implements PreviewFactory {

	private class InsetListCellRenderer extends DefaultListCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1809316485715057696L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel cell = (JLabel) super.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);
			cell.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
			return cell;
		}
	}

	public class BerkeleyGrammarPreview extends JList {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private DefaultListModel model;
		private Scanner fs;
		private JButton bMore;
		private static final int SIZE = 20;
		private static final String MORE = "[show more rules]";

		public BerkeleyGrammarPreview(String value, String postfix) {
			super();
			setCellRenderer(new InsetListCellRenderer());
			model = new DefaultListModel();
			setLayoutOrientation(JList.HORIZONTAL_WRAP);
			setVisibleRowCount(-1);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent evt) {
					if (evt.getClickCount() == 2
							&& model.get(locationToIndex(evt.getPoint())) == MORE)
						more();
				}
			});
			try {
				fs = new Scanner(new FileInputStream(app.findFile(value
						+ postfix)));
				more();
			} catch (FileNotFoundException e) {
				add(new JLabel(value + postfix + " does not exist."));
			}
		}

		public void more() {
			if (fs == null)
				return;
			model.removeElement(MORE);
			String[] l;
			int i = 0;
			while (i < SIZE & fs.hasNextLine()) {
				if (fs.nextLine().isEmpty())
					continue;
				String txt = "<html>";
				l = fs.nextLine().split(" ");
				for (String s : l) {
					if (s.equals("->"))
						txt += " &#10230; ";
					else if (l[l.length - 1] == s) {
						if (s.contains("E"))
							txt += "  [" + s.split("E")[0].substring(0, 5)
									+ " &middot; 10 <sup>" + s.split("E")[1]
									+ "</sup>]";
						else
							txt += "  ["
									+ (s.length() > 7 ? s.substring(0, 8) : s)
									+ "]";

					} else {
						txt += s.replace("^", "<sup>").replace("_",
								"</sup><sub>")
								+ "</sub> ";
					}
				}
				txt += "</html>";
				model.addElement(txt);
				i++;
			}
			if (fs.hasNext())
				model.addElement(MORE);
			setModel(model);
		}

		@Override
		public void finalize() {
			fs.close();
		}
	}

	private String postfix;
	private Application app;

	public BerkeleyGrammarPreviewFactory(Application app, String postfix) {
		super();
		this.app = app;
		this.postfix = postfix;
	}

	@Override
	public JComponent createPreview(String value) {
		return new JScrollPane(new BerkeleyGrammarPreview(value, postfix));
	}

	@Override
	public void openEditor(final String value) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Desktop.getDesktop().open(new File(app.findFile(value + postfix)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		t.start();
	}

	@Override
	public JComponent createSmallPreview(String value) {
		return new JScrollPane(new BerkeleyGrammarPreview(value, postfix));
	}

}
