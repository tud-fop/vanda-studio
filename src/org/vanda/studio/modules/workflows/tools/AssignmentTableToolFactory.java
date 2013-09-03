package org.vanda.studio.modules.workflows.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import org.vanda.datasources.Element;
import org.vanda.datasources.RootDataSource;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.Observer;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.DatabaseListener;
import org.vanda.workflows.elements.ElementVisitor;
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
		private final Observer<Application> shutdownObserver;
		protected Observer<DatabaseEvent<Database>> databaseObserver;

		public OpenAssignmentTableAction(WorkflowEditor wfe) {
			this.wfe = wfe;
			shutdownObserver = new Observer<Application>() {

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
			};
			wfe.getApplication().getShutdownObservable().addObserver(shutdownObserver);
		}

		@Override
		public String getName() {
			return "Open assignment table...";
		}

		@Override
		public void invoke() {
			// Create a new editor only if none is opened,
			// otherwise bring existing one to the front
			if (f != null)
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						f.toFront();
						f.repaint();
					}
				});
			else {
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

	/**
	 * The TableModel yields the connection to the Database. It listens for
	 * WorkflowEvents, to update itself in case of creation / removal of new
	 * Literals.
	 * 
	 * @author kgebhardt
	 * 
	 */
	private static class AssignmentTableModel extends AbstractTableModel implements WorkflowListener<MutableWorkflow> {
		/**
		 * This table is the row header of the main table
		 * 
		 * @author kgebhardt
		 * 
		 */
		protected class RowHeaderTable extends JTable {
			private static final long serialVersionUID = -6693774875669907410L;

			public RowHeaderTable(AbstractTableModel rowHeaderModel) {
				super(rowHeaderModel);
				setBorder(UIManager.getBorder("TableHeader.cellBorder"));
				setForeground(getTableHeader().getForeground());
				setBackground(getTableHeader().getBackground());
				setOpaque(true);
				setFont(getTableHeader().getFont());
				setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			}

			@Override
			public Dimension getPreferredScrollableViewportSize() {
				Dimension d = super.getPreferredScrollableViewportSize();
				d.width = super.getColumnModel().getColumn(0).getPreferredWidth();
				return d;
			}

		}

		private static final long serialVersionUID = -75059113029383402L;
		protected final Database db;
		private final Observer<WorkflowEvent<MutableWorkflow>> workflowObserver;
		public final SortedMap<Integer, Literal> literals;

		protected AbstractTableModel rowHeaderModel;
		protected JTable rowHeader;
		private final RootDataSource rds;

		private ElementVisitor literalAddedVisitor = new ElementVisitor() {
			@Override
			public void visitTool(Tool t) {
				// do nothing
			}

			@Override
			public void visitLiteral(Literal l) {
				literals.put((Integer) literals.size(), l);
			}
		};

		public AssignmentTableModel(WorkflowEditor wfe) {
			db = wfe.getDatabase();
			rds = wfe.getApplication().getRootDataSource();
			literals = new TreeMap<Integer, Literal>();
			for (Job j : wfe.getView().getWorkflow().getChildren())
				j.visit(literalAddedVisitor);
			workflowObserver = new Observer<WorkflowEvent<MutableWorkflow>>() {

				@Override
				public void notify(WorkflowEvent<MutableWorkflow> event) {
					event.doNotify(AssignmentTableModel.this);
				}

			};
			wfe.getView().getWorkflow().getObservable().addObserver(workflowObserver);
			setupRowHeader();
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

		public void setupRowHeader() {
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
			rowHeader = new RowHeaderTable(rowHeaderModel);
			rowHeader.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			rowHeader.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
			JTextField txt = new JTextField();
			rowHeader.setDefaultEditor(String.class, new CustomCellEditor(txt));

		}

		private class CustomCellEditor extends DefaultCellEditor {
			private static final long serialVersionUID = 4727103518497915680L;

			public CustomCellEditor(JTextField textField) {
				super(textField);
				textField.addFocusListener(new FocusAdapter() {
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

		public boolean isErroneous(int arg0, int arg1) {
			Literal lit = literals.get(arg1);
			String val = db.getRow(arg0).get(lit.getKey());
			if (val == null)
				return true;
			return !rds.getType(Element.fromString(val)).equals(lit.getType());
		}
		
		public boolean hasAValue(int arg0, int arg1) {
			Literal lit = literals.get(arg1);
			String val = db.getRow(arg0).get(lit.getKey());
			if (val == null)
				return false;
			int i = val.indexOf(':');
			if (i == -1 || i == val.length() - 1)
				return false;
			return true;
		}
	}

	/**
	 * Transposed version of the AssignmentTableModel, i.e. diagonally mirrored
	 * 
	 * @author kgebhardt
	 * 
	 */
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
			return super.getValueAt(arg1, arg0);
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
		public boolean isErroneous(int arg0, int arg1) {
			return super.isErroneous(arg1, arg0);
		}
		
		@Override
		public boolean hasAValue(int arg0, int arg1) {
			return super.hasAValue(arg1, arg0);
		}

		@Override
		public void setupRowHeader() {
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
			rowHeader = new RowHeaderTable(rowHeaderModel);
			rowHeader.setCellSelectionEnabled(false);
			rowHeader.setFocusable(false);
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

		/**
		 * SelectionListener for Normal i.e. non-mirrored Table-Mode
		 * 
		 * @author kgebhardt
		 * 
		 */
		private class NormalSelectionListener implements ListSelectionListener {
			private final JTable table;
			private final JTable rowHeader;

			public NormalSelectionListener(JTable table, JTable row) {
				this.table = table;
				this.rowHeader = row;
			}

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (arg0.getLastIndex() == -1 || arg0.getValueIsAdjusting())
					return;
				if (table.getSelectedColumn() == -1 || table.getSelectedRow() == -1)
					return;
				if (rowHeader.isEditing()) {
					SwingUtilities.invokeLater(new StopCellEditing(rowHeader));
				}
				SwingUtilities.invokeLater(new ClearSelection(rowHeader));
				int run = table.getSelectedRow();
				int key = table.getSelectedColumn();
				updateSelection(run, key);
			}
		}

		/**
		 * SelectionListener for RowHeader in non-mirrored Table-Mode.
		 * 
		 * @author kgebhardt
		 * 
		 */
		private class NormalRowHeaderSelectionListener implements ListSelectionListener {
			private final JTable mainTable;
			private final JTable rowHeader;

			public NormalRowHeaderSelectionListener(JTable table, JTable row) {
				this.mainTable = table;
				this.rowHeader = row;
			}

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (arg0.getLastIndex() == -1 || arg0.getValueIsAdjusting())
					return;
				if (rowHeader.getSelectedColumn() == -1 || rowHeader.getSelectedRow() == -1)
					return;
				SwingUtilities.invokeLater(new ClearSelection(mainTable));
				int run = rowHeader.getSelectedRow();
				updateSelection(run, -1);
			}

		}

		/**
		 * SelectionListener for TransposedTableMode There is no RowHeader
		 * Listener, since it is not selectable.
		 * 
		 * @author kgebhardt
		 * 
		 */
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
		private final Observer<DatabaseEvent<Database>> dbObserver;
		private final WorkflowEditor wfe;

		private final AssignmentTableModel atm;
		private final TransposedAssignmentTableModel tatm;

		private final JTable table;
		private final JScrollPane tablePane;
		private TransposeState transposeState;

		private NormalSelectionListener normalSelList;
		private NormalRowHeaderSelectionListener rowHeadSelList;
		private TransposedSelectionListener transSelList;

		private MouseListener rowHeaderEditingStopMouseListener;

		private final JPanel buttonAndTabelPanel;
		private final JPanel literalEditorPanel;

		public AssignmentTableDialog(WorkflowEditor wfe) {
			super(new BorderLayout());
			transposeState = new Normal();
			db = wfe.getDatabase();
			this.wfe = wfe;

			buttonAndTabelPanel = new JPanel();
			GroupLayout layout = new GroupLayout(buttonAndTabelPanel);
			buttonAndTabelPanel.setLayout(layout);

			// create Table in normal mode
			atm = new AssignmentTableModel(wfe);
			tatm = new TransposedAssignmentTableModel(wfe);
			table = new JTable(atm);
			table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
				private static final long serialVersionUID = -6915713192476941622L;

				@Override
				public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4,
						int arg5) {
					JLabel comp = (JLabel) super.getTableCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5);
					if (((AssignmentTableModel) table.getModel()).isErroneous(arg4, arg5)) {
						comp.setBackground(Color.red);
						comp.setToolTipText("Literal and Datasource types do not match.");
					}
					else if (!((AssignmentTableModel) table.getModel()).hasAValue(arg4, arg5)) {
						comp.setBackground(Color.yellow);
						comp.setToolTipText("No value is selected.");
					}
					else {
						comp.setBackground(table.getBackground());
						comp.setToolTipText(null);
					}
						
					return comp;					
				}
			});

			table.setCellSelectionEnabled(true);
			table.setTableHeader(new JTableHeader(table.getColumnModel()) {
				private static final long serialVersionUID = -3125419306047857928L;

				@Override
				public boolean getReorderingAllowed() {
					return false;
				}

				@Override
				public Dimension getMinimumSize() {
					Dimension d = super.getMinimumSize();
					d.height = JTableHeader.HEIGHT;
					return d;
				}
			});
			table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tablePane = new JScrollPane(table);
			tablePane.setRowHeaderView(atm.getRowHeader());
			tablePane.setColumnHeader(new JViewport() {
				private static final long serialVersionUID = -1142664914807579190L;

				@Override
				public Dimension getPreferredSize() {
					Dimension d = super.getPreferredSize();
					d.height = new JLabel("123").getPreferredSize().height + 4;
					return d;
				}
			});

			// create SelectionListeners
			normalSelList = new NormalSelectionListener(table, atm.getRowHeader());
			rowHeadSelList = new NormalRowHeaderSelectionListener(table, atm.getRowHeader());
			transSelList = new TransposedSelectionListener(table);

			// add normal Listeners to the table
			table.getSelectionModel().addListSelectionListener(normalSelList);
			table.getColumnModel().getSelectionModel().addListSelectionListener(normalSelList);
			atm.getRowHeader().getSelectionModel().addListSelectionListener(rowHeadSelList);

			// register DatabaseObserver
			dbObserver = new Observer<DatabaseEvent<Database>>() {

				@Override
				public void notify(DatabaseEvent<Database> event) {
					event.doNotify(AssignmentTableDialog.this);
				}

			};
			db.getObservable().addObserver(dbObserver);

			// add transpose button
			JButton transposeButton = new JButton(new AbstractAction("\u2922") {
				private static final long serialVersionUID = 7260156402014653557L;

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
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					db.addRow();
					db.setCursor(db.getSize() - 1);
				}

			});

			// remove run button
			JButton removeButton = new JButton(new AbstractAction("delete run") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					db.delRow();
				}

			});

			// button layout
			ParallelGroup buttonVert = layout.createParallelGroup().addComponent(addButton).addComponent(removeButton);
			SequentialGroup buttonHor = layout.createSequentialGroup().addComponent(addButton)
					.addComponent(removeButton);

			// create mouse listener to detect row header focus lost while
			// editing
			rowHeaderEditingStopMouseListener = new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					SwingUtilities.invokeLater(new StopCellEditing(atm.getRowHeader()));
				}
			};
			tablePane.getViewport().addMouseListener(rowHeaderEditingStopMouseListener);
			tablePane.getRowHeader().addMouseListener(rowHeaderEditingStopMouseListener);

			// main layout
			ParallelGroup tableHor = layout.createParallelGroup().addComponent(tablePane).addGroup(buttonHor);
			SequentialGroup tableVert = layout.createSequentialGroup().addComponent(tablePane).addGroup(buttonVert);

			layout.setHorizontalGroup(tableHor);
			layout.setVerticalGroup(tableVert);
			literalEditorPanel = new JPanel(new BorderLayout());

			add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonAndTabelPanel, literalEditorPanel),
					BorderLayout.CENTER);
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
			changeView(normalSelList, transSelList, tatm);
		}

		@Override
		public void selectNormalView() {
			changeView(transSelList, normalSelList, atm);
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
					atm.fireTableDataChanged();
					atm.fireTableStructureChanged();
					tatm.fireTableDataChanged();
					tatm.fireTableStructureChanged();
					table.repaint();
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
			// FIXME System.out.println("run: " + run + " key: " + key);
			if (db.getCursor() != run && -1 < run && run < db.getSize()) {
				db.setCursor(run);
			}
			literalEditorPanel.removeAll();
			if (key == -1) {
				JLabel text = new JLabel("Please select a literal.");
				text.addMouseListener(rowHeaderEditingStopMouseListener);
				literalEditorPanel.add(text);
			} else if (atm.literals.get(key) != null)
				literalEditorPanel.add(
						eefs.literalFactories.createEditor(db, wfe.getView().getWorkflow(), atm.literals.get(key)),
						BorderLayout.CENTER);
			else {
				JLabel text = new JLabel("There exists no such literal.");
				text.addMouseListener(rowHeaderEditingStopMouseListener);
				literalEditorPanel.add(text);
			}
			revalidate();
		}

		@Override
		public void selectEntry(int row, int column) {
			table.getSelectionModel().setSelectionInterval(row, row);
			table.getColumnModel().getSelectionModel().setSelectionInterval(column, column);
		}
	}

	private ElementEditorFactories eefs;

	public AssignmentTableToolFactory(ElementEditorFactories eefs) {
		this.eefs = eefs;
	}

	@Override
	public Object instantiate(final WorkflowEditor wfe) {
		final OpenAssignmentTableAction a = new OpenAssignmentTableAction(wfe);
		wfe.addAction(a, "application-vnd.sun.xml.calc", KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK), 4);

		// disables assignment table if database is empty
		final DatabaseListener<Database> listener = new DatabaseListener<Database>() {
			private boolean active = true;

			@Override
			public void cursorChange(Database d) {
			}

			@Override
			public void dataChange(Database d, Object key) {
				if (active && d.getSize() == 0) {
					wfe.disableAction(a);
					active = false;
				} else if (!active && d.getSize() > 0) {
					wfe.enableAction(a);
					active = true;
				}
			}

			@Override
			public void nameChange(Database d) {
			}
		};
		a.databaseObserver = new Observer<DatabaseEvent<Database>>() {

			@Override
			public void notify(DatabaseEvent<Database> event) {
				event.doNotify(listener);
			}

		};
		wfe.getDatabase().getObservable().addObserver(a.databaseObserver);

		// initialize button
		listener.dataChange(wfe.getDatabase(), null);

		return a;
	}
}
