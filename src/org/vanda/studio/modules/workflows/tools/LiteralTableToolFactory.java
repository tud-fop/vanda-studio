package org.vanda.studio.modules.workflows.tools;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.JXRowHeaderTable;
import org.vanda.workflows.data.Database;

public class LiteralTableToolFactory implements ToolFactory {

	public class OpenLiteralTableAction implements Action{

		private WorkflowEditor wfe;
		
		public OpenLiteralTableAction(WorkflowEditor wfe) {
			this.wfe = wfe;
		}
		
		@Override
		public String getName() {
			return "Open Literal Table...";
		}

		@Override
		public void invoke() {
			LiteralTable lt = new LiteralTable(wfe);
			JFrame f = new JFrame("Literal Table");
			f.setContentPane(lt);
			f.pack();
			f.setVisible(true);
		}
		
	}
	
	public class LiteralTable extends JPanel {

		private Database db;
		private JXRowHeaderTable tbl;

		public LiteralTable(WorkflowEditor wfe) {
			super(new GridLayout(1,1));
			db = wfe.getDatabase();
			tbl = new JXRowHeaderTable();
			update();
			tbl.packAll();
			tbl.setSortable(false);
			JScrollPane scr = new JScrollPane(tbl);
			scr.setRowHeaderView(tbl.getRowHeader());
			add(scr);
		}

		private void update() {
			Integer[] keys = db.getRow(0).keySet().toArray(new Integer[db.getRow(0).keySet().size()]);
			String[] columnNames = new String[db.getSize()];
			columnNames[0] = "";
			for (int i = 0; i < db.getSize(); i++)
				columnNames[i] = "Run " + i;
			
			String[][] data = new String[keys.length][columnNames.length];
			for (int i = 0; i < keys.length; i++)
				for (int j = 0; j < db.getSize(); j++)
					data[i][j] = db.getRow(j).get(keys[i]);
			
			tbl.setModel(new DefaultTableModel(data, columnNames));
			tbl.setRowHeaderData(keys);
		}
	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		Action a = new OpenLiteralTableAction(wfe);
		wfe.addAction(a,
				KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK));
		return a;
	}

}
