package org.vanda.studio.modules.workflows.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.JXRowHeaderTable;
import org.vanda.util.Observer;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.DatabaseListener;
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
			return "Open assignment table...";
		}

		@Override
		public void invoke() {
			// AssignmentTable lt = new AssignmentTable(wfe);
			AssignmentTableDialog lt = new AssignmentTableDialog(wfe);
			JFrame f = new JFrame("Assignment table");
			f.setContentPane(lt);
			f.pack();
			f.setLocationRelativeTo(wfe.getApplication().getWindowSystem().getMainWindow());
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

	private static class AssignmentTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -75059113029383402L;
		protected final Database db;
		protected final SortedMap<Integer, Literal> literals;

		protected AbstractTableModel rowHeaderModel;
		protected JTable rowHeader;

		public AssignmentTableModel(WorkflowEditor wfe) {
			db = wfe.getDatabase();
			literals = new TreeMap<Integer, Literal>();
			for (Job j : wfe.getView().getWorkflow().getChildren())
				j.visit(new ElementVisitor() {
					@Override
					public void visitTool(Tool t) {
						// Do nothing
					}

					@Override
					public void visitLiteral(Literal l) {
						literals.put((Integer) literals.size(), l);
					}
				});

		}

		@Override
		public int getColumnCount() {
			return literals.size();
		}

		@Override
		public int getRowCount() {
			return db.getSize();
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {
			return db.getRow(arg0).get(literals.get(arg1).getKey());
		}

		public JTable getRowHeader() {
			return rowHeader;
		}

		@Override
		public String getColumnName(int i) {
			return literals.get(i).getName();
		}

		public void setupRowHeader(JTable table) {
			rowHeaderModel = new AbstractTableModel() {
				private static final long serialVersionUID = -7001109023530114177L;

				@Override
				public int getColumnCount() {
					return 1;
				}

				@Override
				public int getRowCount() {
					return db.getSize();
				}

				@Override
				public Object getValueAt(int arg0, int _) {
					return db.getName(arg0);
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return true;
				}

				@Override
				public void setValueAt(Object value, int rowIndex, int columnIndex) {
					if (value instanceof String) {
						if (db.getCursor() == rowIndex)
							db.setName((String) value);
					}

				}
			};
			rowHeader = new JTable(rowHeaderModel);
			JTableHeader tableHeader = rowHeader.getTableHeader();
			rowHeader.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			// rowHeader.setHorizontalAlignment(TableCellRenderer.CENTER);
			rowHeader.setForeground(tableHeader.getForeground());
			rowHeader.setOpaque(true);
			rowHeader.setFont(tableHeader.getFont());
			// rowHeader.setFixedCellHeight(rowHeader.getRowHeight());
			// rowHeader.setCellRenderer(new RowHeaderRenderer(table));
		}
	}

	private static class TransposedAssignmentTableModel extends AssignmentTableModel {

		private static final long serialVersionUID = -7089434006145996832L;

		public TransposedAssignmentTableModel(WorkflowEditor wfe) {
			super(wfe);
		}

		@Override
		public int getColumnCount() {
			return db.getSize();
		}

		@Override
		public int getRowCount() {
			return literals.size();
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {
			return db.getRow(arg1).get(literals.get(arg0).getKey());
		}

		@Override
		public JTable getRowHeader() {
			return rowHeader;
		}

		@Override
		public String getColumnName(int i) {
			return db.getName(i);
		}

		@Override
		public void setupRowHeader(JTable table) {
			this.rowHeaderModel = new AbstractTableModel() {
				private static final long serialVersionUID = -7544430252212958916L;

				@Override
				public int getColumnCount() {
					return 1;
				}

				@Override
				public int getRowCount() {
					return literals.size();
				}

				@Override
				public Object getValueAt(int arg0, int arg1) {
					return literals.get(arg0).getName();
				}
			};
			rowHeader = new JTable(rowHeaderModel);
			JTableHeader tableHeader = rowHeader.getTableHeader();
			rowHeader.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			// rowHeader.setHorizontalAlignment(TableCellRenderer.CENTER);
			rowHeader.setForeground(tableHeader.getForeground());
			rowHeader.setOpaque(true);
			rowHeader.setFont(tableHeader.getFont());
			rowHeader.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
			// rowHeader.setFixedCellHeight(table.getRowHeight());
			// rowHeader.setCellRenderer(new RowHeaderRenderer(table));
		}
	}

	private static class RowHeaderRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 3819862156147897186L;

		RowHeaderRenderer(JTable table) {
			JTableHeader tableHeader = table.getTableHeader();
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setHorizontalAlignment(CENTER);
			setForeground(tableHeader.getForeground());
			setOpaque(true);
			setFont(tableHeader.getFont());
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean fSelected,
				boolean fCellHasFocus) {
			setText((value == null) ? "" : value.toString());
			return this;
		}

	}

	private interface TransposeState {
		void selectTableModel(Transposeable t);

		void transpose(Transposeable t);
	}

	private interface Transposeable {
		void selectTransposedView();

		void selectNormalView();

		void setTransposeState(TransposeState ts);
	}

	private static class Normal implements TransposeState {

		@Override
		public void selectTableModel(Transposeable t) {
			t.selectNormalView();
		}

		@Override
		public void transpose(Transposeable t) {
			t.setTransposeState(new Transposed());
		}
	}

	private static class Transposed implements TransposeState {

		@Override
		public void selectTableModel(Transposeable t) {
			t.selectTransposedView();
		}

		@Override
		public void transpose(Transposeable t) {
			t.setTransposeState(new Normal());
		}

	}

	private class AssignmentTableDialog extends JPanel implements Transposeable, DatabaseListener<Database> {
		private class NormalSelectionListener implements ListSelectionListener {
			private final JTable table;
			private final JTable row;
			private int update = 0;

			public NormalSelectionListener(JTable table, JTable row) {
				this.table = table;
				this.row = row;
			}

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (arg0.getLastIndex() == -1 || arg0.getValueIsAdjusting()) 
					return;
				System.out.println("update " + update + " last " + arg0.getLastIndex());
				beginUpdate();
				if (arg0.getLastIndex() == table.getSelectedColumn() || arg0.getLastIndex() == table.getSelectedRow()) {
					if (update == 1) {
						System.out.println("table");
						row.clearSelection();
						if (row.getEditingRow() != -1) {
							
						}
						int run = table.getSelectedRow();
						int key = table.getSelectedColumn();
						updateSelection(run, key);
					}
				} else if (arg0.getLastIndex() == row.getSelectedRow()) {
					if (update == 1) {
						System.out.println("row");
						table.clearSelection();
						int run = arg0.getLastIndex();
						updateSelection(run, -1);
					}
				}
				endUpdate();
			}

			private void beginUpdate() {
				update++;
			}

			private void endUpdate() {
				update--;
			}
		}
		
//		private class RowSelectionListener implements ListSelectionListener {
//			private final JTable table;
//			private final JTable row;
//			public 
//		}

		private class TransposedSelectionListener implements ListSelectionListener {
			private final JTable table;

			public TransposedSelectionListener(JTable table) {
				this.table = table;
			}

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				int key = table.getSelectedRow();
				int run = table.getSelectedColumn();
				updateSelection(run, key);
			}
		}

		private static final long serialVersionUID = 4113799454513800879L;
		private final Database db;
		private final WorkflowEditor wfe;

		private final AssignmentTableModel atm;
		private final TransposedAssignmentTableModel tatm;

		private final JTable table;
		private final JScrollPane tablePane;
		private TransposeState transposeState;

		private NormalSelectionListener normalSelect;
		private TransposedSelectionListener transSelect;

		private final JPanel pLeft;
		private final JPanel pRight;

		public AssignmentTableDialog(WorkflowEditor wfe) {
			super(new BorderLayout());
			transposeState = new Normal();
			db = wfe.getDatabase();
			this.wfe = wfe;

			pLeft = new JPanel();
			GroupLayout layout = new GroupLayout(pLeft);
			pLeft.setLayout(layout);

			// create Table
			atm = new AssignmentTableModel(wfe);
			tatm = new TransposedAssignmentTableModel(wfe);
			table = new JTable(atm);
			table.setCellSelectionEnabled(true);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			atm.setupRowHeader(table);
			tatm.setupRowHeader(table);
			tablePane = new JScrollPane(table);
			tablePane.setRowHeaderView(atm.getRowHeader());

			normalSelect = new NormalSelectionListener(table, atm.getRowHeader());
			transSelect = new TransposedSelectionListener(table);

			table.getSelectionModel().addListSelectionListener(normalSelect);
			table.getColumnModel().getSelectionModel().addListSelectionListener(normalSelect);

			atm.getRowHeader().getSelectionModel().addListSelectionListener(normalSelect);

			db.getObservable().addObserver(new Observer<DatabaseEvent<Database>>() {

				@Override
				public void notify(DatabaseEvent<Database> event) {
					event.doNotify(AssignmentTableDialog.this);
				}

			});

			// transpose button
			JButton transposeButton = new JButton(new AbstractAction("\u2922") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					transposeState.transpose(AssignmentTableDialog.this);
				}
			});
			transposeButton.setToolTipText("transpose");
			transposeButton.setFont(transposeButton.getFont().deriveFont(14.0f));
			tablePane.setCorner(JScrollPane.UPPER_LEFT_CORNER, transposeButton);

			// add run button
			JButton addButton = new JButton(new AbstractAction("add run") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					db.addRow();
					db.setCursor(db.getSize() - 1);
				}

			});

			// remove run button
			JButton removeButton = new JButton(new AbstractAction("delete run") {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					db.delRow();
				}

			});

			ParallelGroup buttonVert = layout.createParallelGroup().addComponent(addButton).addComponent(removeButton);
			SequentialGroup buttonHor = layout.createSequentialGroup().addComponent(addButton)
					.addComponent(removeButton);

			// setup layout
			ParallelGroup tableHor = layout.createParallelGroup().addComponent(tablePane).addGroup(buttonHor);
			SequentialGroup tableVert = layout.createSequentialGroup().addComponent(tablePane).addGroup(buttonVert);

			layout.setHorizontalGroup(tableHor);
			layout.setVerticalGroup(tableVert);

			pRight = new JPanel(new BorderLayout());

			add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pLeft, pRight), BorderLayout.CENTER);
		}

		@Override
		public void selectTransposedView() {
			table.getSelectionModel().removeListSelectionListener(normalSelect);
			table.getColumnModel().getSelectionModel().removeListSelectionListener(normalSelect);
			table.setModel(tatm);
			tablePane.setRowHeaderView(tatm.getRowHeader());
			table.getSelectionModel().addListSelectionListener(transSelect);
			table.getColumnModel().getSelectionModel().addListSelectionListener(transSelect);
		}

		@Override
		public void selectNormalView() {
			table.getSelectionModel().removeListSelectionListener(transSelect);
			table.getColumnModel().getSelectionModel().removeListSelectionListener(transSelect);
			table.setModel(atm);
			tablePane.setRowHeaderView(atm.getRowHeader());
			table.getSelectionModel().addListSelectionListener(normalSelect);
			table.getColumnModel().getSelectionModel().addListSelectionListener(normalSelect);
		}

		@Override
		public void setTransposeState(TransposeState ts) {
			transposeState = ts;
			ts.selectTableModel(this);
		}

		public void updateAll() {
			atm.fireTableStructureChanged();
			tatm.fireTableStructureChanged();
			table.updateUI();
			tablePane.updateUI();
			updateUI();
		}

		@Override
		public void cursorChange(Database d) {
		}

		@Override
		public void dataChange(Database d, Object key) {
			updateAll();
		}

		@Override
		public void nameChange(Database d) {
			updateAll();
		}

		private void updateSelection(int run, int key) {
			System.out.println("run: " + run + " key: " + key);
			if (db.getCursor() != run && -1 < run && run < db.getSize()) {
				db.setCursor(run);
			}
			pRight.removeAll();
			if (atm.literals.get(key) != null)
				pRight.add(eefs.literalFactories.createEditor(db, wfe.getView().getWorkflow(), atm.literals.get(key)),
						BorderLayout.CENTER);
			else
				pRight.add(new JLabel("There exists no such literal."));
			revalidate();
		}

		private void beginUpdate() {

		}
	}

	private class AssignmentTable extends JPanel {
		private class EditableHeader extends JTableHeader implements CellEditorListener {

			@Override
			public void editingCanceled(ChangeEvent arg0) {
				System.out.println("Canceled" + arg0);
			}

			@Override
			public void editingStopped(ChangeEvent arg0) {
				System.out.println("Stopped" + arg0);

			}

		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Database db;
		private final Map<String, Literal> literals;
		private boolean isTransposed = true;
		private final JXRowHeaderTable tAssignments;
		private final JPanel pRight, pLeft;
		private final JScrollPane scr;
		private String[] keys;
		private int update = 0;

		public AssignmentTable(final WorkflowEditor wfe) {
			super(new BorderLayout());
			db = wfe.getDatabase();
			db.getObservable().addObserver(new DatabaseObserver(this));
			keys = db.getRow(0).keySet().toArray(new String[wfe.getDatabase().getRow(0).keySet().size()]);
			literals = new HashMap<String, Literal>();
			this.pRight = new JPanel(new BorderLayout());
			this.pLeft = new JPanel(new BorderLayout());
			for (Job j : wfe.getView().getWorkflow().getChildren())
				j.visit(new ElementVisitor() {
					@Override
					public void visitTool(Tool t) {
						// Do nothing
					}

					@Override
					public void visitLiteral(Literal l) {
						literals.put(l.getKey(), l);
						l.getObservable().addObserver(new Observer<ElementEvent<Literal>>() {
							@Override
							public void notify(ElementEvent<Literal> event) {
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
			// tAssignments.getRowHeader().setTableHeader(new EditableHeader());

			// tAssignments.getTableHeader().addPropertyChangeListener(new
			// PropertyChangeListener() {
			//
			// @Override
			// public void propertyChange(PropertyChangeEvent arg0) {
			// System.out.println(arg0);
			// }
			//
			// });

			tAssignments.getRowHeader().getModel().addTableModelListener(new TableModelListener() {

				@Override
				public void tableChanged(TableModelEvent arg0) {
					if (arg0.getType() == TableModelEvent.UPDATE) {

						System.out.println(arg0.getColumn() + " " + arg0.getFirstRow());
						if (arg0.getFirstRow() != -1) {
							beginUpdate();
							String newName = (String) tAssignments.getRowHeader().getModel()
									.getValueAt(arg0.getFirstRow(), arg0.getColumn());
							db.setName(newName);
							endUpdate();
						}

					}

				}
			});

			ListSelectionListener rowHeaderSelectionListener = new ListSelectionListener() {
				private int run;

				private void endUpdate() {
					update--;
					System.out.println(update + " " + run);
					if (update == 0) {
						if (-1 < run && run < db.getSize()) {
							db.setCursor(run);
						}
					}
				}

				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					if (isTransposed)
						return;
					// if(arg0.getValueIsAdjusting())
					// return;
					System.out.println("Selection Update" + update + " " + arg0.getFirstIndex() + " "
							+ arg0.getLastIndex());
					// beginUpdate();
					run = tAssignments.getRowHeader().getSelectedRow();
					if (run == -1) {
						// AssignmentTable.this.endUpdate();
						// return;
					}
					if (-1 < run && run < db.getSize()) {
						db.setCursor(run);
					}
					// endUpdate();
				}
			};
			tAssignments.getRowHeader().getSelectionModel().addListSelectionListener(rowHeaderSelectionListener);

			ListSelectionListener lsl = new ListSelectionListener() {
				private Integer run;
				private String key;

				private void beginUpdate() {
					update++;
				}

				private void endUpdate() {
					update--;
					if (update == 0) {
						if (0 <= run && run < db.getSize()) {
							db.setCursor(run);
						}
						if (literals.get(key) != null)
							pRight.add(
									eefs.literalFactories.createEditor(db, wfe.getView().getWorkflow(),
											literals.get(key)), BorderLayout.CENTER);
						else
							pRight.add(new JLabel("There exists no such literal."));
						revalidate();
					}
				}

				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					beginUpdate();
					pRight.removeAll();
					if (tAssignments.getSelectedColumn() == -1 || tAssignments.getSelectedRow() == -1) {
						pRight.add(new JLabel("Select a cell."));
						AssignmentTable.this.endUpdate();
						return;
					}
					if (isTransposed) {
						key = keys[tAssignments.getSelectedColumn()];
						run = tAssignments.getSelectedRow();
					} else {
						key = keys[tAssignments.getSelectedRow()];
						run = tAssignments.getSelectedColumn();
					}
					endUpdate();
				}
			};
			tAssignments.getSelectionModel().addListSelectionListener(lsl);
			tAssignments.getColumnModel().getSelectionModel().addListSelectionListener(lsl);

			JButton buttonCorner = new JButton(new AbstractAction("\u2922") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					isTransposed = !isTransposed;
					tAssignments.getRowHeader().setEditable(isTransposed);
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
					db.addRow();
					db.setCursor(db.getSize() - 1);
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
			beginUpdate();
			String[] columnNames = new String[db.getSize()];
			for (int i = 0; i < db.getSize(); i++)
				columnNames[i] = db.getName(i);

			String[] rowNames = new String[keys.length];
			for (int i = 0; i < keys.length; i++)
				if (literals.containsKey(keys[i]))
					rowNames[i] = literals.get(keys[i]).getName();
				else
					rowNames[i] = keys[i];

			String[][] data = new String[keys.length][columnNames.length];
			for (int i = 0; i < keys.length; i++)
				for (int j = 0; j < db.getSize(); j++)
					data[i][j] = db.getRow(j).get(keys[i]);

			int selectedRow = tAssignments.getSelectedRow();
			int selectedColumn = tAssignments.getSelectedColumn();

			if (isTransposed) {
				((DefaultTableModel) tAssignments.getModel()).setDataVector(transpose(data), rowNames);
				tAssignments.setRowHeaderData(columnNames);
			} else {
				((DefaultTableModel) tAssignments.getModel()).setDataVector(data, columnNames);
				tAssignments.setRowHeaderData(rowNames);
			}

			if (hasBeenTransposed) {
				tAssignments.getSelectionModel().setSelectionInterval(selectedColumn, selectedColumn);
				tAssignments.getColumnModel().getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
			} else {
				tAssignments.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
				tAssignments.getColumnModel().getSelectionModel().setSelectionInterval(selectedColumn, selectedColumn);
			}

			scr.setRowHeaderView(tAssignments.getRowHeader());

			tAssignments.packAll();
			endUpdate();
		}

		private String[][] transpose(String[][] mx) {
			String[][] result = new String[mx[0].length][mx.length];

			for (int i = 0; i < mx.length; i++)
				for (int j = 0; j < mx[0].length; j++)
					result[j][i] = mx[i][j];

			return result;
		}

		private void beginUpdate() {
			update++;
		}

		private void endUpdate() {
			update--;
		}
	}

	private ElementEditorFactories eefs;

	public AssignmentTableToolFactory(ElementEditorFactories eefs) {
		this.eefs = eefs;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		Action a = new OpenAssignmentTableAction(wfe);
		wfe.addAction(a, "application-vnd.sun.xml.calc", KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK), 4);
		return a;
	}

}
