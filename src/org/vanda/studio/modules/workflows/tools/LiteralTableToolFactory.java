package org.vanda.studio.modules.workflows.tools;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.JXRowHeaderTable;
import org.vanda.util.Observer;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;

public class LiteralTableToolFactory implements ToolFactory {

	private class OpenLiteralTableAction implements Action {

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

	private class DatabaseObserver implements Observer<Object> {

		private LiteralTable lt;

		public DatabaseObserver(LiteralTable lt) {
			this.lt = lt;
		}

		@Override
		public void notify(Object event) {
			lt.update();
		}

	}

	private class LiteralTable extends JPanel {

		private final Database db;
		private final Map<Integer, String> names;
		private final JXRowHeaderTable tbl;
		private boolean transposed = false;

		public LiteralTable(WorkflowEditor wfe) {
			super(new BorderLayout());
			db = wfe.getDatabase();
			db.getObservable().addObserver(new DatabaseObserver(this));
			names = new HashMap<Integer, String>();
			for (Job j : wfe.getWorkflowDecoration().getRoot().getChildren())
				j.visit(new ElementVisitor() {

					@Override
					public void visitTool(Tool t) {
						// Do nothing

					}

					@Override
					public void visitLiteral(Literal l) {
						names.put(l.getKey(), l.getName());
					}
				});
			tbl = new JXRowHeaderTable();
			update();
			tbl.packAll();
			tbl.setSortable(false);
			final JScrollPane scr = new JScrollPane(tbl);
			scr.setRowHeaderView(tbl.getRowHeader());
			JButton buttonCorner = new JButton(new AbstractAction("\u2922") {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					transposed = !transposed;
					update();
					scr.setRowHeaderView(tbl.getRowHeader());
				}
			});
			buttonCorner.setToolTipText("transpose");
			buttonCorner.setFont(buttonCorner.getFont().deriveFont(14.0f));
			scr.setCorner(JScrollPane.UPPER_LEFT_CORNER, buttonCorner);
			add(scr, BorderLayout.CENTER);
		}

		private void update() {
			Integer[] keys = db.getRow(0).keySet()
					.toArray(new Integer[db.getRow(0).keySet().size()]);
			String[] columnNames = new String[db.getSize()];
			columnNames[0] = "";
			for (int i = 0; i < db.getSize(); i++)
				columnNames[i] = "Run " + i;

			String[][] data = new String[keys.length][columnNames.length];
			for (int i = 0; i < keys.length; i++)
				for (int j = 0; j < db.getSize(); j++)
					data[i][j] = db.getRow(j).get(keys[i]);

			String[] rowNames = new String[keys.length];
			for (int i = 0; i < keys.length; i++)
				if (names.containsKey(keys[i]))
					rowNames[i] = names.get(keys[i]);
				else
					rowNames[i] = Integer.toHexString(keys[i]);

			if (transposed) {
				tbl.setModel(new DefaultTableModel(transpose(data), rowNames));
				tbl.setRowHeaderData(columnNames);
			} else {
				tbl.setModel(new DefaultTableModel(data, columnNames));
				tbl.setRowHeaderData(rowNames);
			}
			tbl.packAll();
		}

		private String[][] transpose(String[][] mx) {
			String[][] result = new String[mx[0].length][mx.length];

			for (int i = 0; i < mx.length; i++)
				for (int j = 0; j < mx[0].length; j++)
					result[j][i] = mx[i][j];

			return result;
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
