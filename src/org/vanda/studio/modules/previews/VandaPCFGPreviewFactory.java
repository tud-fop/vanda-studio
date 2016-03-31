package org.vanda.studio.modules.previews;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;

public class VandaPCFGPreviewFactory implements PreviewFactory {

	private class RuleRenderer implements ListCellRenderer<String> {

		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
				boolean isSelected, boolean cellHasFocus) {
			if (value.equals("..."))
				return new DefaultListCellRenderer().getListCellRendererComponent(list, value, index,
						isSelected, cellHasFocus);
			int i = value.lastIndexOf(' ');
			String[] rule = { value.substring(0, i).trim(), value.substring(i).trim() };

			JLabel lbl = (JLabel) new DefaultListCellRenderer().getListCellRendererComponent(list, "", index,
					isSelected, cellHasFocus);
			lbl.setText("<html><table style=\"width:100%\"><col align=\"left\"><col align=\"right\"><tr><td>"
					+ rule[0].replace("->", "&#10230;") + "</td><td>[" + rule[1] + "]</td></tr></table></html>");
			return lbl;
		}

	}

	private class VandaPCFGPreview extends JPanel {
		private Scanner scan;
		private DefaultListModel<String> initials, rules;

		public VandaPCFGPreview(String name) throws FileNotFoundException {
			super();
			scan = new Scanner(new FileInputStream(name));
			setLayout(new GridBagLayout());
			initials = new DefaultListModel<String>();
			rules = new DefaultListModel<String>();
			more(100);
			JList<String> lInitials = new JList<String>(initials);
			lInitials.setCellRenderer(new RuleRenderer());
			lInitials.setLayoutOrientation(JList.VERTICAL_WRAP);
			lInitials.setVisibleRowCount(-1);
			JList<String> lRules = new JList<String>(rules);
			lRules.setCellRenderer(new RuleRenderer());
			lRules.setLayoutOrientation(JList.VERTICAL_WRAP);
			lRules.setVisibleRowCount(-1);
			GridBagConstraints gbc = new GridBagConstraints();

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new Insets(1, 5, 1, 5);
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 0;
			gbc.weighty = 0;
			add(new JLabel("Initial states"), gbc);

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.weightx = 0;
			gbc.weighty = 0;
			add(new JLabel("Rules"), gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.weightx = 0;
			gbc.weighty = 1;
			gbc.fill = GridBagConstraints.BOTH;
			add(new JScrollPane(lInitials), gbc);

			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.fill = GridBagConstraints.BOTH;
			add(new JScrollPane(lRules), gbc);
		}

		private void more(int count) {
			String line;
			int i = 0;
			while (scan.hasNextLine() && i < count) {
				i++;
				line = scan.nextLine();
				if (!line.isEmpty())
					if (line.split(" +").length == 2)
						initials.addElement(line);
					else
						rules.addElement(line);
			}
			if (i == count) {
				initials.addElement("...");
				rules.addElement("...");
			}
			revalidate();
		}

	}

	@Override
	public JComponent createPreview(String value) {
		try {
			return new VandaPCFGPreview(value);
		} catch (FileNotFoundException e) {
			System.err.println("File " + value + " not found.");
			return new JLabel("File " + value + " not found.");
		}
	}

	@Override
	public JComponent createSmallPreview(String absolutePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void openEditor(String value) {
		// TODO Auto-generated method stub

	}

}
