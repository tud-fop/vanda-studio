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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

public class EgretPreviewFactory implements PreviewFactory {

	private class InsetListCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel cell = (JLabel) super.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);
			cell.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
			return cell;
		}
	}

	public class EgretPreview extends JList {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private DefaultListModel model;
		private Scanner fs;
		private JButton bMore;
		private static final int SIZE = 20;
		private static final String MORE = "[show more rules]";

		public EgretPreview(String value, String postfix) {
			super();
			Pattern levelx = Pattern.compile("level(\\d+)");
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
				File folder = new File(app.findFile(value));
				int maxlvl = 0;
				
				for (File f : folder.listFiles()) {
					Matcher m = levelx.matcher(f.getName());
					if (m.find()) {
						 int currlvl = Integer.parseInt(m.group(1));
						 if (currlvl > maxlvl) {
							 maxlvl = currlvl;
						 }
					}
				}
				System.out.println(app.findFile(value + "/level" + maxlvl + postfix));
				fs = new Scanner(new FileInputStream(app.findFile(value + "/level" + maxlvl + postfix)));
				more();
			} catch (FileNotFoundException e) {
				add(new JLabel(value + "/level0" + postfix + " does not exist."));
			}
		}

		public void more() {
			if (fs == null)
				return;
			model.removeElement(MORE);
			String[] l;
			int i = 0;
			while (i < SIZE & fs.hasNextLine()) {
				String txt = "<html>";
				l = fs.nextLine().split(" ");
				for (String s : l) {
					if (s.equals("->"))
						txt += " &#10230; ";
					else if (s.contains("_")) {
						txt += s.replace("_",
								"<sub>")
								+ "</sub> ";
					} else txt += s;
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
		
		private String cut(String in, int n) {
			if (in.length() <= n)
				return in;
			else
				return in.substring(0, n);
		}
	}

	private String postfix;
	private Application app;

	public EgretPreviewFactory(Application app, String postfix) {
		super();
		this.app = app;
		this.postfix = postfix;
	}

	@Override
	public JComponent createPreview(String value) {
		return new JScrollPane(new EgretPreview(value, postfix));
	}

	@Override
	public void openEditor(final String value) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Desktop.getDesktop().open(new File(value));
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
		return new JScrollPane(new EgretPreview(value, postfix));
	}

}
