package org.vanda.studio.modules.previews;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.Dimension;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.util.ExceptionMessage;

public class TaggedPreviewFactory implements PreviewFactory {

	private Application app;
	
	public TaggedPreviewFactory(Application app) {
		super();
		this.app = app;
	}
	
	@Override
	public JComponent createPreview(String absolutePath) {
		File taggedCorpus = new File(absolutePath);
		if (!taggedCorpus.exists())
			return app.getPreviewFactory(null).createPreview(absolutePath);
		try(Scanner scanTags = new Scanner(taggedCorpus)) {
			// read stuff from file
			List<List<String>> words= new ArrayList<List<String>>();
			List<List<String>> tags = new ArrayList<List<String>>();
			while (scanTags.hasNextLine()) {
				String line = scanTags.nextLine();
				List<String> w = new ArrayList<String>();
				List<String> t = new ArrayList<String>();
				for(String occ : line.split(" ")) {
					int pos = occ.indexOf('_');
					if(pos != -1) {
						w.add(occ.substring(0, pos));
						t.add(occ.substring(pos + 1));
					}else {
						//TODO Throw error???
					}
				}
				words.add(w);
				tags.add(t);
			}
			// write to array
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
			for(int j = 0; j < words.size(); j++) {
				String[]head = new String[words.get(j).size()];
				String[][] data = new String[2][words.get(j).size()];
				Integer[] width = new Integer[words.get(j).size()];
				for (int i = 0; i < words.get(j).size(); i++) {
					head[i] = words.get(j).get(i);
					data[0][i] = words.get(j).get(i);
					data[1][i] = tags.get(j).get(i);
					width[i] = Math.max(data[0][i].length(), data[1][i].length());
				}
				// build GUI
				JTable jTable = new JTable(data, head){
			        public boolean isCellEditable(int row, int column) {                
			                return false;               
			        };
			    };
				int total = 0;
				for (int i = 0; i < words.get(j).size(); i++) {
				    jTable.getColumnModel().getColumn(i).setPreferredWidth(10 + width[i]*10);
				    total += width[i];
				}
				jTable.setMaximumSize(new Dimension(total*10, 50));
				jTable.setAlignmentX(jTable.LEFT_ALIGNMENT);
				jTable.setRowSelectionAllowed(false);
				jTable.setShowHorizontalLines(false);

				p.add(jTable);
				p.add(Box.createRigidArea(new Dimension(0,5)));
			}

			JComponent result = new JScrollPane(p);
			return result;
		} catch (FileNotFoundException e) {
			app.sendMessage(new ExceptionMessage(e));
			return app.getPreviewFactory(null).createPreview(absolutePath);
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
