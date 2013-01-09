package org.vanda.fragment.bash.parser;

/**
 * ParserState is responsible for processing a line of input. The line is
 * processed twice: once in lookAhead and once in handleLine. The idea is
 * that lookAhead can decide to change the state before the real processing
 * is done.
 * 
 * @author mbue
 *
 */
public interface ParserState {
	/**
	 * Returns true iff builder should try to build now
	 * 
	 * @param line
	 * @return
	 */
	boolean handleLine(String line);

	void lookAhead(String line);
}