package org.vanda.dictionaries;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.vanda.dictionaries.Dictionary.MyDouble;
import org.vanda.dictionaries.DictionaryViews.DictionaryViewState;
import org.vanda.dictionaries.DictionaryViews.ViewTransition;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;

/**
 * The class DictView is a Swing Component for viewing the output of the EM
 * algorithm for training a probablistic bilingual dictionary.
 * 
 * @author stueber
 */
public class DictionaryView extends JPanel implements ViewTransition,
		org.vanda.studio.modules.previews.Previews.Preview {
	/**
	 * The class MyTableModel is the table model for the table containing the
	 * output of the EM algorithm.
	 * 
	 */
	private class MyTableModel extends AbstractTableModel {

		/**
		 * The serialization constant.
		 */
		private static final long serialVersionUID = 1372975402049754921L;

		/**
		 * Constructor for MyTableModel.
		 */
		public MyTableModel() {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return model.getNoOfEntries();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return model.getNoOfIterations();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int row, int col) {
			return model.getIterations()[row][col];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int col) {
			return model.getWordNames()[col] + " - " + model.getTransWordNames()[col];
		}

	}

	/**
	 * Background color of table entries whose values are increasing.
	 */
	private static final Color incCol = new Color(255, 150, 150);

	/**
	 * Background color of table entries whose values are decreasing.
	 */
	private static final Color decCol = new Color(150, 150, 255);

	/**
	 * Background color of table entries whose values are constant.
	 */
	private static final Color neutCol = new Color(255, 255, 255);

	/**
	 * The renderer for the header cells of the table.
	 * 
	 * This subclass of DefaultTableCellRenderer is required in order to use
	 * larger font sizes. It is a wrapper class for the original header cell
	 * renderer.
	 * 
	 */
	private class MyHeaderCellRenderer extends DefaultTableCellRenderer {

		/**
		 * The serialization constant.
		 */
		private static final long serialVersionUID = 1915324644519085191L;

		/**
		 * A reference to the wrapped original header cell renderer.
		 */
		private TableCellRenderer originalRenderer;

		/**
		 * Constructs a MyHeaderCellRenderer object that encapsulates the
		 * original header cell renderer.
		 * 
		 * @param tableCellRenderer
		 *            The original header cell renderer that is to be
		 *            encapsulated.
		 */
		public MyHeaderCellRenderer(TableCellRenderer tableCellRenderer) {
			originalRenderer = tableCellRenderer;
		}

		/**
		 * Returns the same component that the original encapsulated renderer
		 * returns. However, changes the font size accordingly.
		 * 
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
		 *      java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			Component comp = originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
					column);
			if (comp instanceof JLabel) {
				float size = isLargeContent ? beamerFontSize : normalFontSize;
				setFontSize((JComponent) comp, size);
				((JLabel) comp).setHorizontalAlignment(JLabel.CENTER);
			}
			return comp;
		}
	}

	/**
	 * The renderer for the table cells.
	 * 
	 */
	private class MyCellRenderer extends DefaultTableCellRenderer {

		/**
		 * The serialization constant.
		 */
		private static final long serialVersionUID = 1915309644519085191L;

		/**
		 * Constructs a MyCellRenderer object.
		 */
		public MyCellRenderer() {
		}

		/**
		 * Returns the same component that the original encapsulated renderer
		 * returns. However, changes the font size accordingly. Moreover, the
		 * background color of the component is changed, depending on whether
		 * the value is greater, less or equal to the value in the cell above
		 * the current one.
		 * 
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
		 *      java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (comp instanceof JLabel) {
				float size = isLargeContent ? beamerFontSize : normalFontSize;
				setFontSize((JComponent) comp, size);
				((JLabel) comp).setHorizontalAlignment(JLabel.RIGHT);
				((JLabel) comp).setForeground(Color.BLACK);
				if (hasFocus)
					((JLabel) comp).setBorder(BorderFactory.createMatteBorder(2, 2, 3, 3, Color.BLUE));
				else
					((JLabel) comp).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
				setFont(new Font("Unifont", getFont().getSize(), getFont().getSize()));
			}

			if (row == 0) {
				comp.setBackground(neutCol);
				return comp;
			}
			double current = model.getIterations()[row][column].getDouble();
			double prev = model.getIterations()[row - 1][column].getDouble();
			if (current == prev)
				comp.setBackground(neutCol);
			else if (prev < current)
				comp.setBackground(incCol);
			else
				comp.setBackground(decCol);

			return comp;
		}
	}

	/**
	 * The serialization constant.
	 */
	private static final long serialVersionUID = -6905098977481942840L;

	/**
	 * Support for beamer/tablet mode
	 */
	private boolean isLargeContent;

	/**
	 * Support for beamer/tablet mode
	 */
	private boolean isLargeUI;

	/**
	 * Support for beamer/tablet mode: defer UI update
	 */
	private int update;

	/**
	 * The internal data model containing all data that are read from the input
	 * file.
	 */
	private Dictionary model;

	/**
	 * The scroll pane containing the table component.
	 */
	private JScrollPane tableView;
	/**
	 * The scroll pane containing the view of the best entries of the
	 * dictionary.
	 */
	private JScrollPane bestView;
	/**
	 * The editor pane containing the text for the best entries of the
	 * dictionary.
	 */
	private JEditorPane bestText;
	/**
	 * The spinner box for the precision.
	 */
	private JSpinner spinner;
	/**
	 * The check box for beamer mode.
	 */
	// private JCheckBox checkBox;
	/**
	 * The radio button for the table view.
	 */
	private JRadioButton radioButton1;
	/**
	 * The radio button for the best entries view.
	 */
	private JRadioButton radioButton2;
	/**
	 * The label containing the word "Precision" that is directly left of the
	 * spinner box.
	 */
	private JLabel precisionLabel;
	/**
	 * The label that is used to show the precise entry of a table cell.
	 */
	private JLabel cellEntryLabel;

	/**
	 * The table which shows the data model.
	 */
	private JTable table;

	/**
	 * The size of the font if the beamer mode is disabled.
	 */
	private static final float normalFontSize;

	/**
	 * The size of the font if the beamer mode is enabled.
	 */
	private static final float beamerFontSize = 25.f;

	/**
	 * Indicates whether BestView or TableView is shown
	 */
	private DictionaryViewState viewState;

	/**
	 * To observe DictionaryViewState changes
	 */
	private MultiplexObserver<DictionaryViewState> observable;

	static {
		JLabel label = new JLabel("");
		normalFontSize = label.getFont().getSize();
	}

	/**
	 * Constructs a DictionaryView component.
	 * 
	 * @param a
	 *            The application.
	 * @param d
	 *            The dictionary.
	 */
	public DictionaryView(Dictionary d, DictionaryViewState viewState_) {
		boolean isEnglish = true;
		this.viewState = viewState_;
		this.observable = new MultiplexObserver<DictionaryViewState>();

		model = d; // new Dictionary(fileName, separator);

		table = new JTable(new MyTableModel());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(new Object().getClass(), new MyCellRenderer());
		table.getTableHeader().setReorderingAllowed(false);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setCellSelectionEnabled(true);
		table.getTableHeader().setFont(
				new Font("Unifont", table.getTableHeader().getFont().getStyle(), table.getTableHeader().getFont()
						.getSize()));

		table.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				adjustCellEntryLabel(true);
			}
		});

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				adjustCellEntryLabel(true);
			}
		});

		JPanel radioBoxPane = new JPanel();
		radioButton1 = new JRadioButton(isEnglish ? "Full view" : "Vollansicht");
		radioButton2 = new JRadioButton(isEnglish ? "Best entries" : "Beste Einträge");

		cellEntryLabel = new JLabel("");
		radioBoxPane.add(cellEntryLabel);
		Dimension dim = new Dimension(40, 0);
		radioBoxPane.add(new Box.Filler(dim, dim, dim));

		spinner = new JSpinner(new SpinnerNumberModel(viewState.getPrecision(), MyDouble.minPrecision,
				MyDouble.maxPrecision, 1));
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int val = ((SpinnerNumberModel) spinner.getModel()).getNumber().intValue();
				setVisible(false);
				viewState.setPrecision(val);
				observable.notify(viewState);
				MyDouble.setPrecision(val);
				computeCellSizes();
				setVisible(true);
				bestText.setText(constructBestString());
			}
		});
		precisionLabel = new JLabel(isEnglish ? "Precision" : "Genauigkeit");
		radioBoxPane.add(precisionLabel);
		radioBoxPane.add(spinner);
		dim = new Dimension(40, 0);
		radioBoxPane.add(new Box.Filler(dim, dim, dim));
		radioBoxPane.add(radioButton1);
		dim = new Dimension(10, 0);
		radioBoxPane.add(new Box.Filler(dim, dim, dim));
		radioBoxPane.add(radioButton2);
		dim = new Dimension(40, 0);
		radioBoxPane.add(new Box.Filler(dim, dim, dim));
		// checkBox = new JCheckBox(isEnglish ? "Beamermode" : "Beamermodus",
		// false);
		// checkBox.addActionListener(new ActionListener() {
		// radioBoxPane.add(checkBox);

		ButtonGroup group = new ButtonGroup();
		group.add(radioButton1);
		group.add(radioButton2);

		tableView = new JScrollPane(table);
		setLayout(new BorderLayout());
		table.setFillsViewportHeight(true);
		// table.setPreferredSize(new Dimension(2000,1000));
		// add(tableView, BorderLayout.CENTER);

		// scrollPane.setPreferredSize(new Dimension(1000,1000));
		add(radioBoxPane, BorderLayout.SOUTH);

		bestText = new JEditorPane();
		// setFontSize(bestText);
		bestText.setEditable(false);
		bestText.setContentType("text/html");
		bestView = new JScrollPane(bestText);

		viewState.selectView(this);

		radioButton1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				viewState.toTableViewState(DictionaryView.this);
			}
		});

		radioButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				viewState.toBestViewState(DictionaryView.this);
			}
		});
		readjustFontSize();
	}

	@Override
	public void setLargeContent(boolean value) {
		if (value != isLargeContent) {
			beginUpdate();
			isLargeContent = value;
			endUpdate();
		}
	}

	@Override
	public void setLargeUI(boolean value) {
		if (value != isLargeUI) {
			beginUpdate();
			isLargeUI = value;
			endUpdate();
		}
	}

	@Override
	public void beginUpdate() {
		update++;
	}

	@Override
	public void endUpdate() {
		update--;
		if (update == 0)
			adjustUI();
	}

	private void adjustUI() {
		readjustFontSize();
		computeCellSizes();
		bestText.setContentType("text/html; charset=utf-8");
		bestText.setText(constructBestString());
		cellEntryLabel.setText("");
	}

	/**
	 * An auxiliary function that appends a single hexadecimal digit to a
	 * string.
	 * 
	 * @param str
	 *            Append the digit to this StringBuilder object.
	 * @param number
	 *            Must be number between 0 and 15 (inclusive). This number is
	 *            converted to a hexadecimal digit.
	 */
	private static void appendHexBit(StringBuilder str, char number) {
		if (number < 10)
			str.append((char) (number + '0'));
		else
			str.append((char) (number - 10 + 'A'));
	}

	/**
	 * An auxiliary function that appends a single two digit hexadecimal number
	 * to a string.
	 * 
	 * @param str
	 *            Append the number to this StringBuilder object.
	 * @param number
	 *            Must be a number between 0 and 255 (inclusive). This number is
	 *            converted to its hexadecimal representation.
	 */
	private static void appendNumber(StringBuilder str, char number) {
		appendHexBit(str, (char) (number / 16));
		appendHexBit(str, (char) (number % 16));
	}

	/**
	 * An auxiliary function that appends a grey html color code to a string.
	 * 
	 * @param str
	 *            Append the color code to this StringBuilder object.
	 * @param number
	 *            Must be a number between 0 and 255 (inclusive). This number is
	 *            converted to its hexadecimal representation and appended three
	 *            times to the input string.
	 */
	private static void appendGreyColor(StringBuilder str, char number) {
		appendNumber(str, number);
		appendNumber(str, number);
		appendNumber(str, number);
	}

	private static class TranslationItem implements Comparable<TranslationItem> {

		private final String transWord;

		public String getTransWord() {
			return transWord;
		}

		public MyDouble getProbability() {
			return probability;
		}

		private final MyDouble probability;

		public TranslationItem(String transWord, MyDouble probability) {
			this.transWord = transWord;
			this.probability = probability;
		}

		@Override
		public int compareTo(TranslationItem o) {
			double thisDouble = probability.getDouble(), thatDouble = o.probability.getDouble();
			return thisDouble < thatDouble ? 1 : thisDouble == thatDouble ? 0 : -1;
		}

	}

	/**
	 * Constructs the html string that is shown in the best entries view.
	 * 
	 * @return The html string that is to be used for example with a JEditorPane
	 *         object.
	 */
	private String constructBestString() {
		String fontFamily = "Unifont";
		StringBuilder str = new StringBuilder("<html><");
		str.append("body style=\"font-family: " + fontFamily + "\"><");
		int i = 0;

		while (i < model.getNoOfEntries()) {
			String currentString = model.getWordNames()[i];

			str.append("<h3>");
			if (isLargeContent)
				str.append("<font size=+2>");
			str.append(currentString);
			if (isLargeContent)
				str.append("</font>");
			str.append("</h3><<blockquote>");

			ArrayList<TranslationItem> transList = new ArrayList<TranslationItem>();

			while (i < model.getNoOfEntries() && model.getWordNames()[i].equals(currentString)) {
				MyDouble d = model.getIterations()[model.getNoOfIterations() - 1][i];
				if (d.getDouble() > 0.1) {
					transList.add(new TranslationItem(model.getTransWordNames()[i], d));
				}
				i++;
			}

			Collections.sort(transList);
			for (TranslationItem it : transList) {
				str.append("<font color=#");
				appendGreyColor(str, (char) ((1 - it.getProbability().getDouble()) * 200));
				if (isLargeContent)
					str.append(" size=+2");
				str.append(">" + it.getTransWord() + " (" + it.getProbability().toString() + ")</font><br>");
			}
			str.append("</blockquote><br>");
		}
		str.append("</body>");
		str.append("</html>");
		return str.toString();
	}

	/**
	 * Sets the font size of a given component.
	 * 
	 * @param comp
	 *            Adjust the font size of this component.
	 * @param size
	 *            The new size.
	 * @return The component itself. This is provided for convenience.
	 */
	private static JComponent setFontSize(JComponent comp, float size) {
		comp.setFont(comp.getFont().deriveFont(size));
		return comp;
	}

	/**
	 * Is used for readjusting the font sizes of all components in the view.
	 */
	private void readjustFontSize() {
		float size = isLargeUI ? beamerFontSize : normalFontSize;
		setVisible(false);
		setFontSize(radioButton1, size);
		setFontSize(radioButton2, size);
		setFontSize(((JSpinner.DefaultEditor) spinner.getEditor()).getTextField(), size);
		// setFontSize(checkBox);
		setFontSize(precisionLabel, size);
		setFontSize(cellEntryLabel, size);
		setVisible(true);
	}

	/**
	 * Is used to readjust the sizes of all cells in the table.
	 */
	private void computeCellSizes() {
		int cellWidth = 0;

		if (table.getRowCount() > 0 && table.getColumnCount() > 0) {
			Dimension dim = table.getCellRenderer(0, 0)
					.getTableCellRendererComponent(table, table.getModel().getValueAt(0, 0), false, false, 0, 0)
					.getPreferredSize();
			int height = (int) dim.getHeight();
			cellWidth = (int) dim.getWidth();
			table.setRowHeight(height + 4);
		}

		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setHeaderRenderer(new MyHeaderCellRenderer(table.getTableHeader().getDefaultRenderer()));
			column.setPreferredWidth(20 + Math.max(cellWidth, (int) column.getHeaderRenderer()
					.getTableCellRendererComponent(table, table.getModel().getColumnName(i), false, false, 0, i)
					.getPreferredSize().getWidth()));
		}
	}

	/**
	 * Is used to set the text of cellEntryLabel to the precise value of the
	 * currently selected cell.
	 */
	private void adjustCellEntryLabel(boolean tableViewActive) {
		if (!tableViewActive) {
			cellEntryLabel.setText("");
			return;
		}
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();
		if (row >= 0 && col >= 0) {
			cellEntryLabel.setText(Double.toString(model.getIterations()[row][col].getDouble()));
		} else
			cellEntryLabel.setText("");
	}

	@Override
	public void tableViewState() {
		setVisible(false);
		remove(bestView);
		int oldPrecision = viewState.getPrecision();
		viewState = new DictionaryViews.TableViewState();
		viewState.setPrecision(oldPrecision);
		viewState.selectView(this);
		observable.notify(viewState);
	}

	@Override
	public void bestViewState() {
		setVisible(false);
		remove(tableView);
		int oldPrecision = viewState.getPrecision();
		viewState = new DictionaryViews.BestViewState();
		viewState.setPrecision(oldPrecision);
		viewState.selectView(this);
		observable.notify(viewState);
	}

	@Override
	public void selectTableView() {
		radioButton1.setSelected(true);
		add(tableView, BorderLayout.CENTER);
		setVisible(true);
		validate();
		adjustCellEntryLabel(true);
	}

	@Override
	public void selectBestView() {
		radioButton2.setSelected(true);
		adjustUI();
		add(bestView, BorderLayout.CENTER);
		setVisible(true);
		validate();
		adjustCellEntryLabel(false);
	}

	public Observable<DictionaryViewState> getObservable() {
		return observable;
	}

}
