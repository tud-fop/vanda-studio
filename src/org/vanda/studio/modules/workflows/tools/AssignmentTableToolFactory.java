package org.vanda.studio.modules.workflows.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.vanda.studio.app.Application;
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
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;
import org.vanda.workflows.hyper.Workflows.WorkflowListener;

public class AssignmentTableToolFactory implements ToolFactory {

	private class OpenAssignmentTableAction implements Action {

		private WorkflowEditor wfe;
		private JFrame f = null;

		public OpenAssignmentTableAction(WorkflowEditor wfe) {
			this.wfe = wfe;
			wfe.getApplication().getShutdownObservable().addObserver(new Observer<Application> () {

				@Override
				public void notify(Application event) {
					if (f != null) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								f.dispose();
							}
						});
					}
				}
				
			});
		}

		@Override
		public String getName() {
			return "Open assignment table...";
		}

		@Override
		public void invoke() {
			if (f != null)
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						f.toFront();
						f.repaint();						
					}
				});
			else {
				// AssignmentTable lt = new AssignmentTable(wfe);
				AssignmentTableDialog lt = new AssignmentTableDialog(wfe);
				f = new JFrame("Assignment table");
				f.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent arg0) {
						f = null;
					}
				});
				f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				f.setContentPane(lt);
				f.pack();
				f.setLocationRelativeTo(wfe.getApplication().getWindowSystem().getMainWindow());
				f.setVisible(true);
				
			}
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

	private static class AssignmentTableModel extends AbstractTableModel implements WorkflowListener<MutableWorkflow> {
		private static final long serialVersionUID = -75059113029383402L;
		protected final Database db;
		protected final SortedMap<Integer, Literal> literals;

		protected AbstractTableModel rowHeaderModel;
		protected JTable rowHeader;

		private ElementVisitor literalAddedVisitor = new ElementVisitor() {
			@Override
			public void visitTool(Tool t) {
				// Do nothing
			}

			@Override
			public void visitLiteral(Literal l) {
				literals.put((Integer) literals.size(), l);
			}
		};

		public AssignmentTableModel(WorkflowEditor wfe) {
			db = wfe.getDatabase();
			literals = new TreeMap<Integer, Literal>();
			for (Job j : wfe.getView().getWorkflow().getChildren())
				j.visit(literalAddedVisitor);
			wfe.getView().getWorkflow().getObservable().addObserver(new Observer<WorkflowEvent<MutableWorkflow>>() {

				@Override
				public void notify(WorkflowEvent<MutableWorkflow> event) {
					event.doNotify(AssignmentTableModel.this);
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
			Object value = db.getRow(arg0).get(literals.get(arg1).getKey());
			if (value == null)
				return "";
			else
				return value;
		}

		public JTable getRowHeader() {
			return rowHeader;
		}

		@Override
		public String getColumnName(int i) {
			return literals.get(i).getName();
		}

		public int getKey(JTable table) {
			return table.getSelectedColumn();
		}

		@Override
		public void fireTableStructureChanged() {
			super.fireTableStructureChanged();
			rowHeaderModel.fireTableStructureChanged();
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
						db.setName((String) value, rowIndex);
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

			JTextField txt = new JTextField();
			rowHeader.setDefaultEditor(String.class, new CustomCellEditor(txt));

			// rowHeader.setFixedCellHeight(rowHeader.getRowHeight());
			// rowHeader.setCellRenderer(new RowHeaderRenderer(table));
		}

		private class CustomCellEditor extends DefaultCellEditor {

			public CustomCellEditor(JTextField textField) {
				super(textField);
				textField.addFocusListener(new FocusListener() {

					@Override
					public void focusGained(FocusEvent arg0) {
					}

					@Override
					public void focusLost(FocusEvent arg0) {
						rowHeader.editingCanceled(new ChangeEvent(rowHeader));
					}
				});

			}
		}

		@Override
		public void childAdded(MutableWorkflow mwf, Job j) {
			j.visit(literalAddedVisitor);
			fireTableStructureChanged();
		}

		@Override
		public void childModified(MutableWorkflow mwf, Job j) {
			j.visit(new ElementVisitor() {

				@Override
				public void visitTool(Tool t) {
					// do nothing
				}

				@Override
				public void visitLiteral(Literal l) {
					for (Integer i : literals.keySet()) {
						if (literals.get(i) == l) {
							fireTableStructureChanged();
							break;
						}
					}
				}
			});
		}

		@Override
		public void childRemoved(final MutableWorkflow mwf, Job j) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					System.out.println("child removed");
					literals.clear();
					for (Job j : mwf.getChildren()) {
						j.visit(literalAddedVisitor);
					}
					fireTableStructureChanged();
				}
			});

		}

		@Override
		public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
		}

		@Override
		public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
		}

		@Override
		public void propertyChanged(MutableWorkflow mwf) {
		}

		@Override
		public void updated(MutableWorkflow mwf) {
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
		public int getKey(JTable table) {
			return table.getSelectedRow();
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
			rowHeader.setCellSelectionEnabled(false);
			rowHeader.setFocusable(false);
			// rowHeader.setFixedCellHeight(table.getRowHeight());
			// rowHeader.setCellRenderer(new RowHeaderRenderer(table));
		}
	}

	private interface TransposeState {
		void selectTableModel(Transposeable t);

		void transpose(Transposeable t);

		void selectEntry(Transposeable t, int run, int key);
	}

	private interface Transposeable {
		void selectTransposedView();

		void selectNormalView();

		void setTransposeState(TransposeState ts);

		void selectEntry(int row, int column);
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

		@Override
		public void selectEntry(Transposeable t, int run, int key) {
			t.selectEntry(run, key);
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

		@Override
		public void selectEntry(Transposeable t, int run, int key) {
			t.selectEntry(key, run);
		}

	}

	private class AssignmentTableDialog extends JPanel implements Transposeable, DatabaseListener<Database> {
		private class StopCellEditing implements Runnable {
			private final JTable table;

			public StopCellEditing(JTable table) {
				this.table = table;
			}

			@Override
			public void run() {
				if (table.isEditing())
					table.getCellEditor().stopCellEditing();
			}
		}

		private class ClearSelection implements Runnable {
			private final JTable table;

			public ClearSelection(JTable table) {
				this.table = table;
			}

			@Override
			public void run() {
				table.clearSelection();
			}
		}

		private class NormalSelectionListener implements ListSelectionListener {
			private final JTable table;
			private final JTable row;

			public NormalSelectionListener(JTable table, JTable row) {
				this.table = table;
				this.row = row;
			}

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (arg0.getLastIndex() == -1 || arg0.getValueIsAdjusting())
					return;
				System.out.println(" last " + arg0.getLastIndex());
				if (table.getSelectedColumn() == -1 || table.getSelectedRow() == -1)
					return;
				if (row.isEditing()) {
					SwingUtilities.invokeLater(new StopCellEditing(row));
				}
				SwingUtilities.invokeLater(new ClearSelection(row));
				int run = table.getSelectedRow();
				int key = table.getSelectedColumn();
				updateSelection(run, key);
			}
		}

		private class RowSelectionListener implements ListSelectionListener {
			private final JTable table;
			private final JTable row;

			public RowSelectionListener(JTable table, JTable row) {
				this.table = table;
				this.row = row;
			}

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (arg0.getLastIndex() == -1 || arg0.getValueIsAdjusting())
					return;
				System.out.println("last " + arg0.getLastIndex());
				if (row.getSelectedColumn() == -1 || row.getSelectedRow() == -1)
					return;
				SwingUtilities.invokeLater(new ClearSelection(table));
				int run = row.getSelectedRow();
				updateSelection(run, -1);
			}

		}

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
		private RowSelectionListener rowHeaderSelect;
		private TransposedSelectionListener transSelect;

		private MouseListener mouseListener;

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
			rowHeaderSelect = new RowSelectionListener(table, atm.getRowHeader());

			table.getSelectionModel().addListSelectionListener(normalSelect);
			table.getColumnModel().getSelectionModel().addListSelectionListener(normalSelect);

			atm.getRowHeader().getSelectionModel().addListSelectionListener(rowHeaderSelect);

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
					SwingUtilities.invokeLater(new StopCellEditing(atm.getRowHeader()));
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
			mouseListener = new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mousePressed(MouseEvent arg0) {
					// SwingUtilities.invokeLater(new
					// StopCellEditing(atm.getRowHeader()));
				}

				@Override
				public void mouseExited(MouseEvent arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseEntered(MouseEvent arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseClicked(MouseEvent arg0) {
					SwingUtilities.invokeLater(new StopCellEditing(atm.getRowHeader()));
				}
			};
			tablePane.getViewport().addMouseListener(mouseListener);
			tablePane.getRowHeader().addMouseListener(mouseListener);

			add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pLeft, pRight), BorderLayout.CENTER);
		}

		private void changeView(ListSelectionListener oldListener, ListSelectionListener newListener,
				AssignmentTableModel atm) {
			int row = table.getSelectedRow();
			int column = table.getSelectedColumn();
			table.getSelectionModel().removeListSelectionListener(oldListener);
			table.getColumnModel().getSelectionModel().removeListSelectionListener(oldListener);
			table.setModel(atm);
			tablePane.setRowHeaderView(atm.getRowHeader());
			table.getSelectionModel().addListSelectionListener(newListener);
			table.getColumnModel().getSelectionModel().addListSelectionListener(newListener);
			table.getSelectionModel().setSelectionInterval(column, column);
			table.getColumnModel().getSelectionModel().setSelectionInterval(row, row);

		}

		@Override
		public void selectTransposedView() {
			changeView(normalSelect, transSelect, tatm);
		}

		@Override
		public void selectNormalView() {
			changeView(transSelect, normalSelect, atm);
		}

		@Override
		public void setTransposeState(TransposeState ts) {
			transposeState = ts;
			ts.selectTableModel(this);
		}

		public void updateAll() {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					atm.fireTableStructureChanged();
					tatm.fireTableStructureChanged();
					table.updateUI();
					tablePane.updateUI();
					updateUI();
				}
			});
		}

		@Override
		public void cursorChange(Database d) {
			int key = ((AssignmentTableModel) table.getModel()).getKey(table);
			if (key != -1) {
				transposeState.selectEntry(this, d.getCursor(), key);
			} else {
				updateSelection(d.getCursor(), -1);
			}

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
			else {
				JLabel text = new JLabel("There exists no such literal.");
				text.addMouseListener(mouseListener);
				pRight.add(text);
			}
			revalidate();
		}

		@Override
		public void selectEntry(int row, int column) {
			table.getSelectionModel().setSelectionInterval(row, row);
			table.getColumnModel().getSelectionModel().setSelectionInterval(column, column);
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
