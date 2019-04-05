package org.vanda.studio.modules.previews;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.awt.Component;
import java.util.Scanner;
import java.awt.Color;


import javax.swing.BorderFactory;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.AbstractAction;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.util.ExceptionMessage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TaggedPreviewFactory implements PreviewFactory {

	public class TaggedPreview extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Scanner fs;
		private JButton bMore;
		private JPanel cTables;
		private static final int SIZE = 20;
		private static final String MORE = "show more lines";

		public TaggedPreview(String absolutePath) {
			super();
			
			setLayout(new BorderLayout());
			

			bMore = new JButton(new AbstractAction(MORE) {

				@Override
				public void actionPerformed(ActionEvent e) {
					more();
				}
			});
			add(bMore, BorderLayout.PAGE_END);
			
			cTables = new JPanel();
			cTables.setLayout(new BoxLayout(cTables, BoxLayout.PAGE_AXIS));
			cTables.setBackground(Color.WHITE); 
			add(cTables, BorderLayout.CENTER);
			
			try {
				File taggedCorpus = new File(absolutePath);

				fs = new Scanner(taggedCorpus);
				more();
			} catch (FileNotFoundException e) {
				add(new JLabel("File " + absolutePath + " not found"));
			}
		}

		public void more() {
			if (fs == null)
				return;
			int i = 0;
			while (i < SIZE & fs.hasNextLine()) {
				String line = fs.nextLine();
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
				
				String[]head = new String[w.size()];
				String[][] data = new String[2][w.size()];
				Integer[] width = new Integer[w.size()];
				for (int j = 0; j < w.size(); j++) {
					head[j] = w.get(j);
					data[0][j] = w.get(j);
					data[1][j] = t.get(j);
					width[j] = Math.max(data[0][j].length(), data[1][j].length());
				}
				// build GUI
				JTable jTable = new JTable(data, head){
			        public boolean isCellEditable(int row, int column) {                
			                return false;               
			        };
			        
			        @Override
			        public Component prepareRenderer(TableCellRenderer renderer, int row,
			            int col) {
			          Component comp = super.prepareRenderer(renderer, row, col);
			          ((JLabel) comp).setHorizontalAlignment(JLabel.CENTER);
			          return comp;
			        }
			    };
				int total = 0;
				for (int j = 0; j < w.size(); j++) {
				    jTable.getColumnModel().getColumn(j).setPreferredWidth(10 + width[j]*10);
				    total += width[j];
				}
				jTable.setMaximumSize(new Dimension(total*10, 50));
				jTable.setAlignmentX(jTable.LEFT_ALIGNMENT);
				jTable.setRowSelectionAllowed(false);
				jTable.setShowHorizontalLines(false);
				jTable.setShowVerticalLines(false);
				


				cTables.add(jTable);
				cTables.add(Box.createRigidArea(new Dimension(0,5)));
				
				i++;
			}
			revalidate();
			repaint();

		}

		@Override
		public void finalize() {
			fs.close();
		}
	}
	
	
	

	private Application app;
	
	public TaggedPreviewFactory(Application app) {
		super();
		this.app = app;
	}
	
	@Override
	public JComponent createPreview(String absolutePath) {
		return new JScrollPane(new TaggedPreview(absolutePath));
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
