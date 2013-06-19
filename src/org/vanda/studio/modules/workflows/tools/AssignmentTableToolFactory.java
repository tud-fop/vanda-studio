package org.vanda.studio.modules.workflows.tools;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.JXRowHeaderTable;
import org.vanda.util.Observer;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Elements.ElementEvent;
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
			lt.update(false);
		}

	}

	private class AssignmentTable extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Database db;
		private final Map<Integer, Literal> literals;
		private boolean isTransposed = true;
		private final JXRowHeaderTable tAssignments;
		private final JPanel pRight, pLeft;
		private final JScrollPane scr;
		private Integer[] keys;

		public AssignmentTable(final WorkflowEditor wfe) {
			super(new BorderLayout());
			db = wfe.getDatabase();
			db.getObservable().addObserver(new DatabaseObserver(this));
			keys = db
					.getRow(0)
					.keySet()
					.toArray(
							new Integer[wfe.getDatabase().getRow(0).keySet()
									.size()]);
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
						l.getObservable().addObserver(
								new Observer<ElementEvent<Literal>>() {
									@Override
									public void notify(
											ElementEvent<Literal> event) {
										update(false);
									}
								});
					}
				});

			tAssignments = new JXRowHeaderTable();
			scr = new JScrollPane(tAssignments);
			update(false);
			tAssignments.setSortable(false);
			tAssignments.setEditable(false);
			tAssignments.setCellSelectionEnabled(true);
			tAssignments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ListSelectionListener lsl = new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					pRight.removeAll();
					if (tAssignments.getSelectedColumn() == -1
							|| tAssignments.getSelectedRow() == -1) {
						pRight.add(new JLabel("Select a cell."));
						return;
					}
					Integer key, run;
					if (isTransposed) {
						key = keys[tAssignments.getSelectedColumn()];
						run = tAssignments.getSelectedRow();
					} else {
						key = keys[tAssignments.getSelectedRow()];
						run = tAssignments.getSelectedColumn();
					}
					db.setCursor(run);
					if (literals.get(key) != null)
						pRight.add(eefs.literalFactories.createEditor(db, wfe
								.getWorkflowDecoration().getRoot(), literals
								.get(key)), BorderLayout.CENTER);
					else
						pRight.add(new JLabel("There exists no such literal."));
					revalidate();

				}
			};
			tAssignments.getSelectionModel().addListSelectionListener(lsl);
			tAssignments.getColumnModel().getSelectionModel()
					.addListSelectionListener(lsl);

			JButton buttonCorner = new JButton(new AbstractAction("\u2922") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					isTransposed = !isTransposed;
					update(true);
					scr.setRowHeaderView(tAssignments.getRowHeader());
				}
			});
			buttonCorner.setToolTipText("transpose");
			buttonCorner.setFont(buttonCorner.getFont().deriveFont(14.0f));

			scr.setRowHeaderView(tAssignments.getRowHeader());
			scr.setCorner(JScrollPane.UPPER_LEFT_CORNER, buttonCorner);
			pLeft.add(scr, BorderLayout.CENTER);

			JPanel pSouth = new JPanel(new GridLayout(1, 2));
			pSouth.add(new JButton(new AbstractAction("add run") {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					wfe.getDatabase().addRow();
				}

			}));
			pSouth.add(new JButton(new AbstractAction("delete run") {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					tAssignments.clearSelection();
					db.delRow();
				}

			}));
			pLeft.add(pSouth, BorderLayout.SOUTH);

			add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pLeft, pRight));
		}

		private void update(boolean hasBeenTransposed) {
			String[] columnNames = new String[db.getSize()];
			for (int i = 0; i < db.getSize(); i++)
				columnNames[i] = "Run " + i;

			String[] rowNames = new String[keys.length];
			for (int i = 0; i < keys.length; i++)
				if (literals.containsKey(keys[i]))
					rowNames[i] = literals.get(keys[i]).getName();
				else
					rowNames[i] = Integer.toHexString(keys[i]);

			String[][] data = new String[keys.length][columnNames.length];
			for (int i = 0; i < keys.length; i++)
				for (int j = 0; j < db.getSize(); j++)
					data[i][j] = db.getRow(j).get(keys[i]);

			int selectedRow = tAssignments.getSelectedRow();
			int selectedColumn = tAssignments.getSelectedColumn();
			
			if (isTransposed) {
				((DefaultTableModel) tAssignments.getModel()).setDataVector(
						transpose(data), rowNames);
				tAssignments.setRowHeaderData(columnNames);
			} else {
				((DefaultTableModel) tAssignments.getModel()).setDataVector(
						data, columnNames);
				tAssignments.setRowHeaderData(rowNames);
			}

			if (hasBeenTransposed) {
				tAssignments.getSelectionModel().setSelectionInterval(
						selectedColumn, selectedColumn);
				tAssignments.getColumnModel().getSelectionModel()
						.setSelectionInterval(selectedRow, selectedRow);
			} else {
				tAssignments.getSelectionModel().setSelectionInterval(
						selectedRow, selectedRow);
				tAssignments.getColumnModel().getSelectionModel()
						.setSelectionInterval(selectedColumn, selectedColumn);
			}

			scr.setRowHeaderView(tAssignments.getRowHeader());

			tAssignments.packAll();
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
