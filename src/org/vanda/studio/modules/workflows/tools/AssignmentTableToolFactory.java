package org.vanda.studio.modules.workflows.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.JXRowHeaderTable;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;

public class AssignmentTableToolFactory implements ToolFactory {

	private class OpenAssignmentTableAction implements Action {

		private WorkflowEditor wfe;

		public OpenAssignmentTableAction(WorkflowEditor wfe) {
			this.wfe = wfe;
		}

		@Override
		public String getName() {
			return "Open Literal Table...";
		}

		@Override
		public void invoke() {
			AssignmentTable lt = new AssignmentTable(wfe);
			JFrame f = new JFrame("Literal Table");
			f.setContentPane(lt);
			f.pack();
			f.setVisible(true);
		}

	}

	private class DatabaseObserver implements Observer<Object> {

		private AssignmentTable lt;

		public DatabaseObserver(AssignmentTable lt) {
			this.lt = lt;
		}

		@Override
		public void notify(Object event) {
			lt.update();
		}

	}

	private class AssignmentCellEditor extends AbstractCellEditor implements
			TableCellEditor {

		private final WorkflowEditor wfe;
		private final Map<Integer, Literal> literals;
		private final Integer[] keys;
		private final boolean isTransposed;
		private final JPanel panel;
		private Integer key, run;

		public AssignmentCellEditor(WorkflowEditor wfe,
				Map<Integer, Literal> ls, Integer[] keys, boolean transposed,
				JPanel pan) {
			this.wfe = wfe;
			this.literals = ls;
			this.keys = keys;
			this.isTransposed = transposed;
			this.panel = pan;
		}

		@Override
		public Object getCellEditorValue() {
			return wfe.getDatabase().getRow(run).get(key);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			if (isTransposed) {
				key = keys[column];
				run = row;
			} else {
				key = keys[row];
				run = column;
			}
			panel.removeAll();
			if (literals.get(key) != null)
				panel.add(eefs.literalFactories.createEditor(wfe.getDatabase(),
						wfe.getWorkflowDecoration().getRoot(), literals.get(key)),
						BorderLayout.CENTER);
			else
				panel.add(new JLabel("There exists no such literal."));
			panel.revalidate();
			return new JLabel(value.toString());
		}

	}

	private class AssignmentTable extends JPanel {

		private final WorkflowEditor wfe;
		private final Map<Integer, Literal> literals;
		private boolean isTransposed = true;
		private final JXRowHeaderTable tAssignments;
		private final JPanel pRight, pLeft;
		private final JScrollPane scr;

		public AssignmentTable(final WorkflowEditor wfe) {
			super(new BorderLayout());
			this.wfe = wfe;
			wfe.getDatabase().getObservable()
					.addObserver(new DatabaseObserver(this));
			literals = new HashMap<Integer, Literal>();
			this.pRight = new JPanel(new BorderLayout());
			this.pLeft = new JPanel(new BorderLayout());
			for (Job j : wfe.getWorkflowDecoration().getRoot().getChildren())
				j.visit(new ElementVisitor() {
					@Override
					public void visitTool(Tool t) {
						// Do nothing
					}

					@Override
					public void visitLiteral(Literal l) {
						literals.put(l.getKey(), l);
					}
				});
			tAssignments = new JXRowHeaderTable();
			tAssignments.packAll();
			tAssignments.setSortable(false);
			scr = new JScrollPane(tAssignments);
			update();
			JButton buttonCorner = new JButton(new AbstractAction("\u2922") {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					isTransposed = !isTransposed;
					update();
					scr.setRowHeaderView(tAssignments.getRowHeader());
				}
			});
			buttonCorner.setToolTipText("transpose");
			buttonCorner.setFont(buttonCorner.getFont().deriveFont(14.0f));
			scr.setCorner(JScrollPane.UPPER_LEFT_CORNER, buttonCorner);
			pLeft.add(scr, BorderLayout.CENTER);
			JPanel pSouth = new JPanel(new GridLayout(1, 2));
			pSouth.add(new JButton(new AbstractAction("add run"){

				@Override
				public void actionPerformed(ActionEvent e) {
					wfe.getDatabase().addRow();
				}
				
			}));
			pSouth.add(new JButton(new AbstractAction("delete run"){

				@Override
				public void actionPerformed(ActionEvent e) {
					if (isTransposed)
						wfe.getDatabase().delRow(tAssignments.getSelectedRow());
					else
						wfe.getDatabase().delRow(tAssignments.getSelectedColumn());
				}
				
			}));
			pLeft.add(pSouth, BorderLayout.SOUTH);
			add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pLeft, pRight));
		}

		private void update() {
			Integer[] keys = wfe
					.getDatabase()
					.getRow(0)
					.keySet()
					.toArray(
							new Integer[wfe.getDatabase().getRow(0).keySet()
									.size()]);
			String[] columnNames = new String[wfe.getDatabase().getSize()];
			for (int i = 0; i < wfe.getDatabase().getSize(); i++)
				columnNames[i] = "Run " + i;

			String[] rowNames = new String[keys.length];
			for (int i = 0; i < keys.length; i++)
				if (literals.containsKey(keys[i]))
					rowNames[i] = literals.get(keys[i]).getName();
				else
					rowNames[i] = Integer.toHexString(keys[i]);

			String[][] data = new String[keys.length][columnNames.length];
			for (int i = 0; i < keys.length; i++)
				for (int j = 0; j < wfe.getDatabase().getSize(); j++)
					data[i][j] = wfe.getDatabase().getRow(j).get(keys[i]);

			tAssignments.setDefaultEditor(Object.class, new AssignmentCellEditor(wfe,
					literals, keys, isTransposed, pRight));

			if (isTransposed) {
				tAssignments.setModel(new DefaultTableModel(transpose(data), rowNames));
				tAssignments.setRowHeaderData(columnNames);
			} else {
				tAssignments.setModel(new DefaultTableModel(data, columnNames));
				tAssignments.setRowHeaderData(rowNames);
			}

			tAssignments.packAll();
			scr.setRowHeaderView(tAssignments.getRowHeader());
		}

		private String[][] transpose(String[][] mx) {
			String[][] result = new String[mx[0].length][mx.length];

			for (int i = 0; i < mx.length; i++)
				for (int j = 0; j < mx[0].length; j++)
					result[j][i] = mx[i][j];

			return result;
		}
	}

	private ElementEditorFactories eefs;

	public AssignmentTableToolFactory(ElementEditorFactories eefs) {
		this.eefs = eefs;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		Action a = new OpenAssignmentTableAction(wfe);
		wfe.addAction(a,
				KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK));
		return a;
	}

}
