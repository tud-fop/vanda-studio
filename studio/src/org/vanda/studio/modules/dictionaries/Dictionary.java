package org.vanda.studio.modules.dictionaries;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * The data model of the output of the EM algorithm for probabilistic
 * bilingual dictionaries.
 *
 * @author stueber
 * 
 */
public class Dictionary {

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
		 * @see Dictionary.MyBufferedReader#charIsBuffered
		 */
		private char bufferedChar;
		/**
		 * Is true when a character has been read form the file but not yet
		 * processed.
		 * 
		 * @see DictView.Dictionary.MyBufferedReader#bufferedChar
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
	public Dictionary(String fileName, char separator) throws IOException {
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

			while (!reader.isEndOfFileReached()) {
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

