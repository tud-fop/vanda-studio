package hmm.statesplitting;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Helper class that creates a randomized probability distribution.
 * 
 * @author Christof Leonhardt
 */
public class Alpha {

	Random rnd;
	List<Integer> alpha_table;
	long sum;
	
	/**
	 * Creates a new object. If seed is less than 0, a seed is chosen at random.
	 * 
	 * @param seed The randomization-seed. Same seed means same random-values.
	 */
	public Alpha(long seed) {		
		if (seed < 0) {
			rnd = new Random();
		} else {
			rnd = new Random(seed);
		}
		alpha_table = new LinkedList<Integer>();
	}
	
	/**
	 * Creates a new object using a random seed.
	 */
	public Alpha() {
		rnd = new Random();
		alpha_table = new LinkedList<Integer>();
	}
	
	/**
	 * Calculates a random probability distribution with specified length.
	 * @param length The generated probability distribution will have the given length.
	 */
	public void calc(int length) {

		alpha_table = new LinkedList<Integer>();

		int value;
		sum = 0;
		//Create 'length' number of random values 
		for (int iter = 0; iter < length; iter ++) {
			//Create random value
			value = rnd.nextInt(Integer.MAX_VALUE);
			//Insert in list
			alpha_table.add(value);
			//Create sum ovar all values
			sum += value;
		}
		
	}
	
	/**
	 * Get single probability of a distribution. All probabilities will sum up to 1.
	 * 
	 * @param index Index of probability.
	 * @return Probability of given index.
	 */
	public double get(int index) {
		//Return value at position 'index' and divide through sum over all random values
		// so you get a value of the distribution
		return (double) alpha_table.get(index) / (double) sum;
	}

	/**
	 * Get array of all probabilities.
	 * 
	 * @return Double array of all probabilities.
	 */
	public double[] get() {

		double[] result = new double[alpha_table.size()];
		//Iterate through all random values that were created
		for (int i = 0; i < alpha_table.size(); i++) {
			//get every probability
			result[i] = get(i);
		}
		
		return result;
	}
	
	/**
	 * Get Single random probability. Unlike {@link get(int index)} it is only an random double value.
	 * 
	 * @return Random Value between 0 an 1.
	 */
	public double SingleValue() {
		return rnd.nextDouble();
	}
	
}
