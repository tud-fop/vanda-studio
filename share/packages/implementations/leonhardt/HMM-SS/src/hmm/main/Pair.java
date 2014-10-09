package hmm.main;

import java.util.LinkedList;
import java.util.List;

/**
 * Helper class that represents a pair of two values of the same type.
 * 
 * @author Christof Leonhardt
 *
 * @param <T> the two values of this pair have to be of this type.
 */
public class Pair <T> {
	
	public T fst;
	public T snd;
	
	/**
	 * Generates a new pair.
	 * @param fst The first value of the pair.
	 * @param snd The second value of the pair.
	 */
	public Pair(T fst, T snd) {
		this.fst = fst;
		this.snd = snd;
	}
	
	/**
	 * Generates an empty pair. All values are initialized with null.
	 */
	public Pair() {
		fst = null;
		snd = null;
	}
	
	/**
	 * Copy constructor.
	 * @param pair The pair that will be copied.
	 */
	public Pair(Pair<T> pair) {
		this (pair.fst, pair.snd);
	}
	
	/**
	 * Converts a pair into a list containing two elements.
	 * @return A List that contains the two elements of the pair.
	 */
	public List<T> toList() {
		List<T> result = new LinkedList<T>();
		result.add(fst);
		result.add(snd);
		return result;
	}
	
	@Override
	public String toString() {
		return "["+fst+", "+snd+"]";
	}

}
