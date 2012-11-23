package org.vanda.studio.util.previewFactories;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.vanda.studio.app.PreviewFactory;

public class BerkeleyGrammarPreviewFactory implements PreviewFactory {

	public class BerkeleyGrammarPreview extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private GridBagConstraints gbc;
		private Scanner fs;
		private JButton bMore;
		private static final int SIZE = 20;

		public BerkeleyGrammarPreview(String value) {
			super();
			setLayout(new GridBagLayout());
			gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 1;
			gbc.weighty = 0;
			gbc.gridx = 0;
			gbc.gridy = 0;

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

			try {
				fs = new Scanner(new FileInputStream(value + ".prev"));
				more();
			} catch (FileNotFoundException e) {
				add(new JLabel("Preview does not exist."));
			}
		}

		public Component add(Component c) {
			super.add(c, gbc);
			if (gbc.gridx == 4) {
				gbc.gridx = 0;
				gbc.gridy++;
			} else {
				gbc.gridx++;
			}
			return c;
		}

		public void more() {
			if (fs == null)
				return;
			remove(bMore);
			List<String> lst = new ArrayList<String>();
			String[] l;
			int i = 0;
			while (i < SIZE & fs.hasNextLine()) {
				String txt = "<html>";
				l = fs.nextLine().split(" ");
				for (String s : l) {
					if (s.equals("->"))
						txt += " &#10230; ";
					else if (l[l.length - 1] == s) {
						if (s.split("E").length > 1)
							txt += "  [" + s.split("E")[0].substring(0, 5)
									+ " &middot; 10 <sup>" + s.split("E")[1]
									+ "</sup>]";
						else
							txt += "  [" + s.substring(0, 8) + "]";

					} else {
						txt += s.replace("^", "<sup>").replace("_",
								"</sup><sub>")
								+ "</sub> ";
					}
				}
				txt += "</html>";
				lst.add(txt);
				i++;
			}

			for (String s : lst) {
				add(new JLabel(s));
			}
			super.add(bMore, gbc);
			revalidate();
		}

		public void finalize() {
			fs.close();
		}
	}

	@Override
	public JComponent createPreview(String value) {
		return new JScrollPane(new BerkeleyGrammarPreview(value));
	}

	@Override
	public void openEditor(String value) {

	}

}
