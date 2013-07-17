package hmm.evaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

//The better model has the lower perplexity

/**
 * Calculates the perplexity of a probabilistic model. Therefore a amount of probabilities
 * is needed. For example: A trained HMM parses a test corpus. After that, the calculated 
 * probabilities of the sentences will be the input-probabilities for calculating the
 * perplexity.
 * 
 * @author Christof Leonhardt
 */
public class Perplexity {

	List<Double> values;
	
	/**
	 * Normal constructor of class.
	 */
	public Perplexity() {values = new LinkedList<Double>();}
	
	/**
	 * Creates a new perplexity object with given values.
	 * @param values A list of probabilities, that will be used for calculating the 
	 * perplexity.
	 */
	public Perplexity(List<Double> values) { set(values); }
	
	/**
	 * Creates a new perplexity object by reading out a given file(-reader object).
	 * @param reader The reader object for the used file. Has to be opened before and closed
	 * after. 
	 * @throws IOException
	 */
	public Perplexity(Reader reader) throws IOException { setFromFile(reader); }
	
	/**
	 * Set calculation values from file(-reader object).
	 * @param reader The reader object for the used file. Has to be opened before and closed
	 * after. 
	 * @throws IOException
	 */
	public void setFromFile(Reader reader) throws IOException {
		values = new LinkedList<Double>();
		String line;

		//Read file from FileReader-Object
		BufferedReader br = new BufferedReader(reader);		
	    while(true) {
	      
	    	line = br.readLine();
	    	if (line == null) {break;}
	    	//Add lines to list
	    	values.add(new Double(line));

	    } 
		
	}
	
	/**
	 * Set calculation values.
	 * @param values A list of probabilities, that will be used for calculating the 
	 * perplexity.
	 */
	public void set(List<Double> values) { this.values = values; }

	/**
	 * Calculates the perplexity of given probabilities and returns it.
	 * @return Perplexity of probabilities.
	 */
	public double get() {
		
		double sum = 0;
		double uniform = 1.0 / values.size();
		
		//Calculate Cross-Entropy 
		for (Double iter : values) {
			sum += uniform * log2(iter);			
		}
		//Return Perplexity
		return Math.pow(2.0, -sum);
	}
	
	
	/**
	 * Calculates the logarithm of base 2 of a value.
	 * @param value Value of that the binary logarithm is wanted.
	 * @return Binary logarithm of given value.
	 */
	private double log2(double value) {
		return Math.log(value) / Math.log(2);
	}
	
}
