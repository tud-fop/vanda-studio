import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
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

/**
 * The class DictView is a Swing-Component for viewing the output of the EM
 * algorithm for training a probablistic bilingual dictionary.
 * 
 */
public class DictView extends JPanel {

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
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component comp = originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (comp instanceof JLabel) {
				setFontSize((JComponent) comp);
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
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (comp instanceof JLabel) {
				setFontSize((JComponent) comp);
				((JLabel) comp).setHorizontalAlignment(JLabel.RIGHT);
				((JLabel) comp).setForeground(Color.BLACK);
				if (hasFocus)
					((JLabel) comp).setBorder(BorderFactory.createMatteBorder(2, 2, 3, 3, Color.BLUE));
				else
					((JLabel) comp).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
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
	 * A wrapper class for an immutable value of type double.
	 * 
	 * The main purpose of this class is to provide a toString function that
	 * prints the double according to a statically set precision.
	 * 
	 */
	static public class MyDouble {

		/**
		 * The current format string. Is used in the toString method.
		 */
		static private String formatString;

		/**
		 * The immutable double value that is wrapped by this class.
		 */
		private final double d;

		
		/**
		 * The initial number of positions after the decimal point.
		 */
		public static final int initPrecision;
		
		static {
			initPrecision = 4;
			formatString = "%." + initPrecision + "f";
		}

		/**
		 * The minimal number of positions after the decimal point.
		 */
		public static final int minPrecision = 0;
		/**
		 * The maximal number of positions after the decimal point.
		 */
		public static final int maxPrecision = 9;


		/**
		 * Getter method for the wrapped double value.
		 * 
		 * @return The wrapped double value.
		 */
		public double getDouble() {
			return d;
		}

		/**
		 * Constructs a wrapper object for a double value.
		 * 
		 * @param d
		 *            The double wrapped that is to be wrapped.
		 */
		public MyDouble(double d) {
			this.d = d;
		}

		/**
		 * Sets the number of positions after the decimal point that is used by
		 * the toString function.
		 * 
		 * @param precision
		 *            The number of positions after the decimal point. Must be
		 *            between minPrecision and maxPrecision.
		 */
		public static void setPrecision(int precision) {
			if (precision >= minPrecision && precision <= maxPrecision)
				formatString = "%." + precision + "f";
		}

		/**
		 * Converts the wrapped double value according to the globally set
		 * precision.
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format(formatString, d);
		}

	}

	/**
	 * The data model of the output of the EM algorithm for probabilistic
	 * bilingual dictionaries.
	 * 
	 */
	static private class DictViewModel {

		/**
		 * An auxiliary class that provides methods for reading a csv file
		 * containing the output of the EM algorithm.
		 * 
		 */
		static private class MyBufferedReader extends BufferedReader {

			/**
			 * A single character that is already read from the file but not yet
			 * processed.
			 * 
			 * Depending on the system, the end of line is either represented by
			 * a carriage return `\r', a line feed `\n', or a carriage return
			 * followed by a line feed. In order to handle either variant
			 * properly, we have to read another symbol after reading a carriage
			 * return. If the next symbol is a line feed, then we can safely
			 * proceed. If it is not a line feed, we have to buffer the read
			 * symbol and use it when processing the next line.
			 * 
			 * @see DictView.DictViewModel.MyBufferedReader#charIsBuffered
			 */
			private char bufferedChar;
			/**
			 * Is true when a character has been read form the file but not yet
			 * processed.
			 * 
			 * @see DictView.DictViewModel.MyBufferedReader#bufferedChar
			 */
			private boolean charIsBuffered = false;

			/**
			 * Is true when the end of the line has been reached.
			 */
			private boolean endOfLineReached = false;
			/**
			 * Is true when the end of the file has been reached.
			 */
			private boolean endOfFileReached = false;

			/**
			 * Getter method for endOfLineReached.
			 * 
			 * @return True if the end of the current line has been reached.
			 */
			public boolean isEndOfLineReached() {
				return endOfLineReached;
			}

			/**
			 * Getter method for endOfFileReached.
			 * 
			 * @return True if the end of the file has been reached.
			 */
			public boolean isEndOfFileReached() {
				return endOfFileReached;
			}

			/**
			 * Constructs a MyBufferedReader object for a given file reader.
			 * 
			 * @param in
			 *            The reader of the file that is to be read.
			 */
			public MyBufferedReader(Reader in) {
				super(in);
			}

			/**
			 * Reads the next char from the file.
			 * 
			 * This method maked use of the bufferedChar variable.
			 * 
			 * @return -1 if file end is reached, -2 if end of line is reached,
			 *         character otherwise
			 */
			private int getNextChar() throws IOException {
				if (charIsBuffered) {
					charIsBuffered = false;
					return bufferedChar;
				}

				int currentChar = read();

				if (currentChar == '\n')
					return -2;

				if (currentChar == '\r') {
					int nextChar = read();
					if (nextChar == -1)
						return -1;
					if (nextChar == '\n')
						return -2;
					if (nextChar < 0)
						throw new IOException("Cannot interpret symbol " + nextChar + ".");

					bufferedChar = (char) nextChar;
					charIsBuffered = true;
					return -2;
				}
				return currentChar;
			}

			/**
			 * Read the next string from the current file position. Read until
			 * separator, end of line or end of file is reached.
			 * 
			 * When this method returns, the attributes endOfLineReached and
			 * endOfFileReached are set accordingly.
			 * 
			 * @param separator
			 *            Quit reading when this character is reached.
			 * @return The read String. If there is no string (i.e. end of line,
			 *         of file, or separator is reached immediately), then
			 *         return null.
			 * @throws IOException
			 */
			public String getNextString(char separator) throws IOException {
				StringBuilder str = new StringBuilder();

				endOfLineReached = endOfFileReached = false;

				while (true) {
					int currentChar = getNextChar();
					if (currentChar == -1) {
						endOfLineReached = true;
						endOfFileReached = true;
						break;
					}
					if (currentChar == -2) {
						endOfLineReached = true;
						break;
					}
					if (currentChar == separator)
						break;

					str.append((char) currentChar);
				}

				if (str.length() == 0)
					return null;

				return str.toString();
			}

			/**
			 * Read the next double number from the current file position. Read
			 * until separator, end of line or end of file is reached.
			 * 
			 * When this method returns, the attributes endOfLineReached and
			 * endOfFileReached are set accordingly.
			 * 
			 * @param separator
			 *            Quit reading when this character is reached.
			 * @return The read double number. If there is no number (i.e. end
			 *         of line, of file, or separator is reached immediately),
			 *         then return null.
			 * @throws IOException
			 */
			public MyDouble getNextDouble(char separator) throws IOException {
				String string = getNextString(separator);
				if (string == null)
					return null;

				try {
					MyDouble number = new MyDouble(Double.parseDouble(string));
					return number;
				} catch (NumberFormatException e) {
					throw new IOException("Number expected instead of \"" + string + "\".");
				}

			}

			/**
			 * Reads the current line of the file and separates it into single
			 * String blocks.
			 * 
			 * After executing this method, the variables enfOfLineReached and
			 * endOfFileReached are set accordingly.
			 * 
			 * @param separator
			 *            Separate the line after every instance of this
			 *            character.
			 * @return The array of String blocks in this line.
			 * @throws IOException
			 *             Is thrown if the line is empty or there is no more
			 *             line in the input file.
			 */
			private String[] itemizeLineToStrings(char separator) throws IOException {
				ArrayList<String> list = new ArrayList<String>();

				while (true) {
					String currentString = getNextString(separator);

					if (currentString == null)
						throw new IOException("String expected in input file.");

					list.add(currentString);
					if (isEndOfLineReached())
						break;
				}

				String[] strings = new String[list.size()];
				return list.toArray(strings);
			}

			/**
			 * Reads the current line of the file, which consists solely of
			 * floating point constants that are separated by a fixed separator
			 * character.
			 * 
			 * After executing this method, the variables enfOfLineReached and
			 * endOfFileReached are set accordingly.
			 * 
			 * @param expectedLength
			 *            The number of floating point constants in the current
			 *            line. This must be known before parsing the current
			 *            line. If expectedLength is not the number of floating
			 *            point constants in the current line, then this method
			 *            throws an IOException.
			 * @param separator
			 *            The fixed separator character.
			 * @return The array of floating point constants in this line. If
			 *         the end of file is reached before calling
			 *         itemizeLineToDoubles, then this method returns null.
			 * @throws IOException
			 *             Is thrown if the number of constants in the current
			 *             line is not equal to expectedLength
			 */
			private MyDouble[] itemizeLineToDoubles(int expectedLength, char separator) throws IOException {
				MyDouble[] doubles = new MyDouble[expectedLength];

				for (int i = 0; i < expectedLength; i++) {
					MyDouble number = getNextDouble(separator);
					if (number == null)
						return null;

					doubles[i] = number;
					if (isEndOfLineReached() && (i != expectedLength - 1))
						throw new IOException("Line is too short (expected length: " + expectedLength + ", actual length: " + (i + 1) + ").");
				}

				if (!isEndOfLineReached())
					throw new IOException("Line is too long (expected length: " + expectedLength + ").");

				return doubles;
			}
		}

		/**
		 * The number of words in the first language.
		 */
		private int noOfEntries;
		
		/**
		 * The number of iterations of the EM algorithm (= number of rows in the resulting table).
		 */
		private int noOfIterations;

		/**
		 * The array of words in the first language.
		 */
		private String[] wordNames;

		/**
		 * The array of words of the second language.
		 */
		private String[] transWordNames;

		/**
		 * The probability distributions that the EM algorithm outputs (= entries in the table).
		 */
		private MyDouble[][] iterations;

		/**
		 * Getter method for noOfEntries.
		 * @return The number of words in the first language.
		 */
		public int getNoOfEntries() {
			return noOfEntries;
		}

		/**
		 * Getter method for noOfIterations.
		 * @return The number of iterations of the EM algorithm.
		 */
		public int getNoOfIterations() {
			return noOfIterations;
		}

		/**
		 * Getter method for wordNames.
		 * @return The array of words in the first language.
		 */
		public String[] getWordNames() {
			return wordNames;
		}

		/**
		 * Getter method for transWordNames.
		 * @return The array of words in the second language.
		 */
		public String[] getTransWordNames() {
			return transWordNames;
		}

		/**
		 * Getter method for iterations.
		 * @return The probability distributions computed by the EM algorithm.
		 */
		public MyDouble[][] getIterations() {
			return iterations;
		}

		/**
		 * Constructs a data model from an input file.
		 * @param fileName The name of the input file.
		 * @param separator The symbol that separates the tokens in the input file.
		 * @throws IOException Is thrown if the input file does not adhere to required csv file format.
		 */
		public DictViewModel(String fileName, char separator) throws IOException {
			loadFile(fileName, separator);
		}

		/**
		 * Loads a data model from an input file.
		 * @param fileName The name of the input file.
		 * @param separator The symbol that separates the tokens in the input file.
		 * @throws IOException Is thrown if the input file does not adhere to required csv file format.
		 */
		private void loadFile(String fileName, char separator) throws IOException {
			MyBufferedReader reader = new MyBufferedReader(new FileReader(fileName));

			try {
				wordNames = reader.itemizeLineToStrings(separator);
				if (reader.isEndOfFileReached())
					throw new IOException("At least two lines expected in input file.");

				transWordNames = reader.itemizeLineToStrings(separator);

				if (wordNames.length != transWordNames.length)
					throw new IOException("First and second line need to have the same number of words.");

				if (wordNames.length == 0)
					throw new IOException("File is empty.");

//				// determine number of entries
//				String firstString = firstLine[0];
//				for (noOfEntries = 1; noOfEntries < firstLine.length; noOfEntries++) {
//					if (!(firstLine[noOfEntries]).equals(firstString))
//						break;
//				}
//
//				// check whether the first line is well-formed
//				noOfTransEntries = firstLine.length / noOfEntries;
//				if (firstLine.length != noOfEntries * noOfTransEntries)
//					throw new IOException("The first line is not well-formed.");
//
//				int i = 0;
//				wordNames = new String[noOfEntries];
//				for (int j = 0; j < noOfEntries; j++) {
//					wordNames[j] = firstLine[i];
//					for (int k = 0; k < noOfTransEntries; k++) {
//						if (!(firstLine[i]).equals(wordNames[j]))
//							throw new IOException("The first line is not well-formed.");
//						i++;
//					}
//				}
//
//				// check whether second line is well-formed
//				i = 0;
//				transWordNames = new String[noOfTransEntries];
//				for (int j = 0; j < noOfEntries; j++) {
//					for (int k = 0; k < noOfTransEntries; k++) {
//						if (j == 0) {
//							transWordNames[k] = secondLine[k];
//						} else {
//							if (!(secondLine[i]).equals(transWordNames[k]))
//								throw new IOException("The second line is not well-formed.");
//						}
//						i++;
//					}
//				}
				
				noOfEntries = wordNames.length;

				// read the remaining lines
				ArrayList<MyDouble[]> linesList = new ArrayList<MyDouble[]>();

				int cnt678 = 0;
				while (!reader.isEndOfFileReached()) {
					cnt678++;
					System.out.println(cnt678);
					MyDouble[] tempLine = reader.itemizeLineToDoubles(noOfEntries, separator);
					if (tempLine == null)
						break; 
					linesList.add(tempLine);
				}

				noOfIterations = linesList.size();
				iterations = new MyDouble[noOfIterations][];

				iterations = linesList.toArray(iterations);
			} catch (IOException ex) {
				reader.close();
				throw ex;
			}

			reader.close();
		}

	}

	/**
	 * The serialization constant.
	 */
	private static final long serialVersionUID = -6905098977481942840L;

	/**
	 * The internal data model containing all data that are read from the input file.
	 */
	private DictViewModel model;

	/**
	 * The scroll pane containing the table component.
	 */
	private JScrollPane tableView;
	/**
	 * The scroll pane containing the view of the best entries of the dictionary.
	 */
	private JScrollPane bestView;
	/**
	 * The editor pane containing the text for the best entries of the dictionary.
	 */
	private JEditorPane bestText;
	/**
	 * The spinner box for the precision.
	 */
	private JSpinner spinner;
	/**
	 * The check box for beamer mode.
	 */
	private JCheckBox checkBox;
	/**
	 * The radio button for the table view.
	 */
	private JRadioButton radioButton1;
	/**
	 * The radio button for the best entries view.
	 */
	private JRadioButton radioButton2;
	/**
	 * The label containing the word "Precision" that is directly left of the spinner box.
	 */
	private JLabel precisionLabel;
	/**
	 * The label that is used to show the precise entry of a table cell.
	 */
	private JLabel cellEntryLabel;
	/**
	 * Is true if the table view is shown. Is false if the best entries view is shown.
	 */
	private boolean tableViewActive = true;

	/**
	 * The table which shows the data model.
	 */
	private JTable table;

	/**
	 * Is true if the beamer mode is enabled.
	 */
	private boolean beamerMode = false;
	/**
	 * The size of the font if the beamer mode is disabled.
	 */
	private static final float normalFontSize;
	/**
	 * The size of the font if the beamer mode is enabled.
	 */
	private static final float beamerFontSize = 25.f;
	
	
	
	static {
		JLabel label = new JLabel("");
		normalFontSize = label.getFont().getSize();
	}


	/**
	 * Constructs a DictView component. This constructor requires an input file which is shown in the DictView component.
	 * @throws IOException
	 * @param fileName The name of the input file.
	 * @param separator The symbol that separates the tokens in the input file.
	 * @param isEnglish If this is true, then all text is shown in English. If this is false, then all text is shown in German.
	 * @throws IOException Is thrown if the input file does not adhere to required csv file format.
	 */
	public DictView(String fileName, char separator, boolean isEnglish) throws IOException {
		model = new DictViewModel(fileName, separator);

		table = new JTable(new MyTableModel());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(new Object().getClass(), new MyCellRenderer());
		table.getTableHeader().setReorderingAllowed(false);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setCellSelectionEnabled(true);
		
		table.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				adjustCellEntryLabel();
			}
		});
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				adjustCellEntryLabel();
			}
		});

		JPanel radioBoxPane = new JPanel();
		radioButton1 = new JRadioButton(isEnglish ? "Full view" : "Vollansicht");
		radioButton1.setSelected(true);
		radioButton2 = new JRadioButton(isEnglish ? "Best entries" : "Beste Einträge");
		
		
		cellEntryLabel = new JLabel("");
		radioBoxPane.add(cellEntryLabel);
		Dimension dim = new Dimension(40, 0);
		radioBoxPane.add(new Box.Filler(dim, dim, dim));
		
		spinner = new JSpinner(new SpinnerNumberModel(MyDouble.initPrecision, MyDouble.minPrecision, MyDouble.maxPrecision, 1));
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int val = ((SpinnerNumberModel) spinner.getModel()).getNumber().intValue();
				setVisible(false);
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
		checkBox = new JCheckBox(isEnglish ? "Beamermode" : "Beamermodus", false);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				beamerMode = checkBox.isSelected();
				readjustFontSize();
				computeCellSizes();
				bestText.setText(constructBestString());
				cellEntryLabel.setText("");
			}
		});
		radioBoxPane.add(checkBox);

		ButtonGroup group = new ButtonGroup();
		group.add(radioButton1);
		group.add(radioButton2);

		tableView = new JScrollPane(table);
		setLayout(new BorderLayout());
		table.setFillsViewportHeight(true);
		// table.setPreferredSize(new Dimension(2000,1000));
		add(tableView, BorderLayout.CENTER);
		// scrollPane.setPreferredSize(new Dimension(1000,1000));
		add(radioBoxPane, BorderLayout.SOUTH);

		bestText = new JEditorPane();
		// setFontSize(bestText);
		bestText.setEditable(false);
		bestText.setContentType("text/html");
		bestText.setText(constructBestString());
		bestView = new JScrollPane(bestText);

		radioButton1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				if (!tableViewActive) {
					remove(bestView);
					add(tableView, BorderLayout.CENTER);
					setVisible(true);
					validate();
					tableViewActive = true;
				}
				adjustCellEntryLabel();
			}
		});

		radioButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				if (tableViewActive) { 
					remove(tableView);
					add(bestView, BorderLayout.CENTER);
					setVisible(true);
					validate();
					tableViewActive = false;
				}
				adjustCellEntryLabel();
			}
		});
		readjustFontSize();
		computeCellSizes();
	}

	/**
	 * An auxiliary function that appends a single hexadecimal digit to a string.
	 * @param str Append the digit to this StringBuilder object.
	 * @param number Must be number between 0 and 15 (inclusive). This number is converted to a hexadecimal digit.
	 */
	private static void appendHexBit(StringBuilder str, char number) {
		if (number < 10)
			str.append((char) (number + '0'));
		else
			str.append((char) (number - 10 + 'A'));
	}

	/**
	 * An auxiliary function that appends a single two digit hexadecimal number to a string.
	 * @param str Append the number to this StringBuilder object.
	 * @param number Must be a number between 0 and 255 (inclusive). This number is converted to its hexadecimal representation.
	 */
	private static void appendNumber(StringBuilder str, char number) {
		appendHexBit(str, (char) (number / 16));
		appendHexBit(str, (char) (number % 16));
	}

	/**
	 * An auxiliary function that appends a grey html color code to a string. 
	 * @param str Append the color code to this StringBuilder object.
	 * @param number Must be a number between 0 and 255 (inclusive). This number is converted to its hexadecimal representation and appended three times to the input string.
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
	 * @return The html string that is to be used for example with a JEditorPane object.
	 */
	private String constructBestString() {
		StringBuilder str = new StringBuilder("<html><");
		
		int i = 0;
		
		while (i < model.getNoOfEntries()) {
			String currentString = model.getWordNames()[i];
				
			str.append("<h3>");
			if (beamerMode)
				str.append("<font size=+2>");
			str.append(currentString);
			if (beamerMode)
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
				if (beamerMode)
					str.append(" size=+2");
				str.append(">" + it.getTransWord() + " (" + it.getProbability().toString() + ")</font><br>");
			}
			str.append("</blockquote><br>");
		}

		str.append("</html>");
		return str.toString();
	}

	/**
	 * Sets the font size of a given component depending on whether the beamer mode is enabled.
	 * @param comp Adjust the font size of this component.
	 * @return The component itself. This is provided for convenience.
	 */
	private JComponent setFontSize(JComponent comp) {
		comp.setFont(comp.getFont().deriveFont(beamerMode ? beamerFontSize : normalFontSize));
		return comp;
	}

	/**
	 * Is used for readjusting the font sizes of all components in the view.
	 */
	private void readjustFontSize() {
		setVisible(false);
		setFontSize(radioButton1);
		setFontSize(radioButton2);
		setFontSize(((JSpinner.DefaultEditor)spinner.getEditor()).getTextField());
		// setFontSize(checkBox);
		setFontSize(precisionLabel);
		setFontSize(cellEntryLabel);
		setVisible(true);
	}

	/**
	 * Is used to readjust the sizes of all cells in the table.
	 */
	private void computeCellSizes() {
		int cellWidth = 0;

		if (table.getRowCount() > 0 && table.getColumnCount() > 0) {
			Dimension dim = table.getCellRenderer(0, 0).getTableCellRendererComponent(table, table.getModel().getValueAt(0, 0), false, false, 0, 0).getPreferredSize();
			int height = (int) dim.getHeight();
			cellWidth = (int) dim.getWidth();
			table.setRowHeight(height + 4);
		}

		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setHeaderRenderer(new MyHeaderCellRenderer(table.getTableHeader().getDefaultRenderer()));
			column.setPreferredWidth(20 + Math.max(cellWidth, (int) column.getHeaderRenderer().getTableCellRendererComponent(table, table.getModel().getColumnName(i), false, false, 0, i)
					.getPreferredSize().getWidth()));
		}
	}

	/**
	 * Is used to set the text of cellEntryLabel to the precise value of the currently selected cell.
	 */
	private void adjustCellEntryLabel() {
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
}
