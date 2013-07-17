package hmm.statesplitting;

import hmm.main.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.*;

/**
 * This class implements the State-Splitting-Algorithm
 * 
 * @author Christof Leonhardt
 */
public class StateSplitting {

	//Alpha alpha[] = new Alpha[4];
	Alpha alpha;
	double alph;
	double epsilon;
	
	/**
	 * Constructor where all parameters can be set
	 * 
	 * @param alph The amount how much the symmetry will be broke.
	 * @param epsilon The threshold when merging is performed.
	 * @param seed The seed that is used for initializing the random generator. 
	 */
	public StateSplitting(double alph, double epsilon, long seed) {
		
		if (alph <= 1 && alph > 0) {
			this.alph = alph;
		} else {
			this.alph = 0.01;
		}
		if (epsilon <= 1 && epsilon > 0) {
			this.epsilon = epsilon;
		} else {
			this.epsilon = 0.01;
		}
		initAlpha(seed);
	}

	/**
	 * Constructor where only alpha an epsilon can be set. Seed is randomized.
	 * 
	 * @param alph The amount how much the symmetry will be broke.
	 * @param epsilon The threshold when merging is performed.
	 */
	public StateSplitting(double alph, double epsilon) {
		
		if (alph <= 1 && alph > 0) {
			this.alph = alph;
		} else {
			this.alph = 0.01;
		}
		if (epsilon <= 1 && epsilon > 0) {
			this.epsilon = epsilon;
		} else {
			this.epsilon = 0.01;
		}
		initAlpha();
	}

	/**
	 * Constructor where only the seed can be set. Alpha and epsilon get the value 0.01.
	 * 
	 * @param seed The seed that is used for initializing the random generator. 
	 */
	public StateSplitting(long seed) {		
		this.alph = 0.01;
		this.epsilon = 0.01;
		initAlpha(seed);
	}

	/**
	 * Normal constructor. Alpha and epsilon get the value 0.01, the seed is randomized.
	 */
	public StateSplitting() {		
		this.alph = 0.01;
		this.epsilon = 0.01;		
		initAlpha();
	}


	/**
	 * Splits every state of a Hidden-Markov-Model.
	 * 
	 * @param hmm Unsplitted Hidden-Markov-Model. 
	 * @return Splitted Hidden-Markov-Model.
	 */
	public Hmm<ObservationInteger> split(Hmm<ObservationInteger> hmm) {
		
		int states = hmm.nbStates();
		//The number of new states will be twice the number of actual states
		int new_states = (states * 2);
		
		double[] new_pi = new double[new_states];
		double[][] new_a = new double[new_states][new_states];
		List<Opdf <ObservationInteger>> opdfs = new LinkedList<Opdf<ObservationInteger>>();
		

		double value;
		double dummy[] = new double[2];

		//Iterate through all states
		for (int i = 0; i < states; i++) {

			//Calculate probabilities from start-state to any other states 
			value = hmm.getPi(i);
			dummy[0] = alpha.SingleValue();
			new_pi[2*i]			= (value + (value * alph * dummy[0]))/2;
			new_pi[(2*i) + 1]	= (value - (value * alph * dummy[0]))/2;

			//Iterate through all states
			for (int j = 0; j < states; j++) {
				//Calculate probabilities from state to states (without special-state)
				//Create two new distributions
				dummy[0] = alpha.SingleValue();
				dummy[1] = alpha.SingleValue();
				
				//Calculate probabilities from state to state
				value = hmm.getAij(i, j);
				new_a[2*i][2*j]				= (value + (value * alph * dummy[0]))/2;
				new_a[2*i][(2*j) + 1]		= (value - (value * alph * dummy[0]))/2;

				new_a[(2*i) + 1][2*j]		= (value + (value * alph * dummy[1]))/2;
				new_a[(2*i) + 1][(2*j) + 1]	= (value + (value * alph * dummy[1]))/2;
				
			}
			
			//Assign observation-probabilities
			opdfs.add(dirt(hmm.getOpdf(i), alpha, alph));
			opdfs.add(dirt(hmm.getOpdf(i), alpha, alph));
			
		}
		
		return new Hmm<ObservationInteger>(new_pi, new_a, opdfs);
	} 

	/**
	 * Helper method, converts Double list into double array.
	 * 
	 * @param list List of Double values.
	 * @return array of values of type double.
	 */
	public static double[] Double2double(List<Double> list) {
		
		//Create new array with size of given list
		double[] result = new double[list.size()];
		
		//Put every value from list into array (there is no standard-function for this!?)
		for (int iter = 0; iter < list.size(); iter++) {
			result[iter] = list.get(iter);
		}		
		
		return result;		
	}
	
	/**
	 * Helper method, calculates length of Opdf (due a design mistake it can not be read
	 * out of an Opdf object). 
	 * 
	 * @param opdf Opdf of which length is wanted.
	 * @return Length of given Opdf.
	 */
	public static int calcOpdfLength(Opdf<ObservationInteger> opdf) {
		int length = 0;

		try {
			//Try to get probability of observation
			do {
				opdf.probability(new ObservationInteger(length));
				//Look for the next observation
				length ++;
			} while (true);
			
			//Until we reach the end of 'opdf'
			//If we reach the end an 'IllegalArgumentException' is thrown
			// and we got the length.
		} catch (IllegalArgumentException e) {}
		
		return length;		
	}
	
	/**
	 * "Soils" Opdf with Alpha-Value.
	 * 
	 * @param opdf Opdf with to symmetric probabilities.
	 * @param alpha A distribution that will be used to break the symmetry.
	 * @param alph A factor that will be used to break the symmetry. Should be in [0, 1].
	 * @return "soiled" Opdf.
	 */
	public static Opdf<ObservationInteger> dirt (Opdf<ObservationInteger> opdf, Alpha alpha, double alph) {
		
		double probability;
		LinkedList<Double> probabilites;
		probabilites = new LinkedList<Double>();

		int length = calcOpdfLength(opdf);;
		double sum = 0;

		//Create new distribution with length of number of observations 
		alpha.calc(length);
		//Iterate through all observations
		for (int jter = 0; jter < length; jter++) {
			
			//Calculate new probability of observation
			probability = opdf.probability(new ObservationInteger(jter)) + alph * opdf.probability(new ObservationInteger(jter)) *alpha.get(jter);
			//Store probability in List
			probabilites.add(probability);

			//For normalization we need a the sum over all probabilities
			sum += probability;
		}
		//Create a new probability distribution by using probability list
		// and the calculated sum 
		for (int jter = 0; jter < length; jter++) {
			probabilites.set(jter, probabilites.get(jter) / sum);  
		}

		//Create new observations from probability distribution
		return (Opdf<ObservationInteger>) new OpdfInteger (Double2double(probabilites));
	}
	
	/**
	 * Initialize Alpha-Value with given seed.
	 *  
	 * @param seed The seed that is used for initializing the random generator. 
	 */
	private void initAlpha(long seed) {
		
		//Custom seed?
		if (seed > 0) {
			//'alpha' is an array. For every element in this array we want to create
			// a random distribution. So we must iterate through all elements of this
			// array and create a distribution
			
			//for (int iter = 0; iter < alpha.length; iter ++) {
			//	alpha[iter] = new Alpha(seed);
			//}
			alpha = new Alpha(seed);
		} else {
			initAlpha();
		}
	}

	/**
	 * Initialize Alpha-Value with given random seed.
	 */
	private void initAlpha() {
		//'alpha' is an array. For every element in this array we want to create
		// a random distribution. So we must iterate through all elements of this
		// array and create a distribution

		//for (int iter = 0; iter < alpha.length; iter ++) {
			//alpha[iter] = new Alpha();
		//}
		alpha = new Alpha();

	}
	
	/**
	 * Merges states of Hidden-Markov-Model, execpt of the states that are not specified in state_list.
	 * @param hmm Hidden-Markov-Model that has to be merged
	 * @param state_list List of states that will be merged.
	 * @return Merged Hidden-Markov-Model.
	 */
	public Hmm<ObservationInteger> merge(Hmm<ObservationInteger> hmm, List<Integer> state_list) {
		
		int states = hmm.nbStates();
		//If we have less states, no merging will be performed
		if (states < 2) { return hmm; } 

		//Calculate new number of states
		int new_states = states - (state_list.size() / 2);
		
		double new_pi[] = new double [new_states];
		double new_a[][] = new double [new_states][new_states];
		List<Opdf <ObservationInteger>> opdfs = new LinkedList<Opdf<ObservationInteger>>();

		//Real index for the new states
		int i_count = 0;
		int j_count = 0;
		boolean inc = false;
		boolean jnc = false;
		
		//Iterate through all (old number of) states
		for (int i = 0; i < states; i++) {

			//Calculating start-states
			//If we are not allowed to merge the given state ('i' is not in list)
			if (!state_list.contains(i)) {
				
				//Copy values
				new_pi[i_count] = hmm.getPi(i);				
				opdfs.add(hmm.getOpdf(i));
				
			} else {
				
				//Calculate new start-distribution by using the current and the next state
				// (i, i + 1)
				new_pi[i_count] = (hmm.getPi(i) + hmm.getPi(i + 1));
				//Calculate the new observation probabilities for this state
				opdfs.add(merge_opdf(hmm.getOpdf(i), hmm.getOpdf(i + 1)));
				
				//At next iteration we have to jump over the next (i + 1) state
				inc = true;
			}
			
			//Iterate through all (old number of) states
			//Real index for the new states
			j_count = 0;
			for (int j = 0; j < states; j++) {
				
				//If both states are not allowed to be merged
				if (!state_list.contains(i) && !state_list.contains(j)) {
					//copy values
					new_a[i_count][j_count] = hmm.getAij(i, j);					
				
				//If both states have to be merged
				} else if (state_list.contains(i) && state_list.contains(j)) {
					//Calculate new values
					new_a[i_count][j_count] = (hmm.getAij(i, j) + hmm.getAij(i + 1, j) + hmm.getAij(i, j + 1) + hmm.getAij(i + 1, j + 1)) / 2;

					//At next iteration we have to jump over the next (i + 1) state
					inc = true;
					//At next iteration we have to jump over the next (j + 1) state
					jnc = true;
				
				//If first state is not allowed to be merged, but the second 
				} else if (!state_list.contains(i) && state_list.contains(j)) {
					//Calculate new values
					new_a[i_count][j_count] = hmm.getAij(i, j) + hmm.getAij(i, j + 1);

					//At next iteration we have to jump over the next (j + 1) state
					jnc = true;

					//If first state has to be merged, but the second not 
				} else if (state_list.contains(i) && !state_list.contains(j)) {
					//Calculate new values
					new_a[i_count][j_count] = (hmm.getAij(i, j) + hmm.getAij(i + 1, j)) / 2;

					//At next iteration we have to jump over the next (i + 1) state
					inc = true;
				}
				//Real index for the new states
				j_count ++;
				//Increment two times if this state was merged with something
				if (jnc) {jnc = false; j++;}

			}
			//Real index for the new states
			i_count ++;
			//Increment two times if this state was merged with something
			if (inc) {inc = false; i++;}

		}
		
		//Generate new HMM and return it
		return new Hmm<ObservationInteger>(new_pi, new_a, opdfs);		
	}

	/**
	 * Merges two Opdfs.
	 * @param opdf1 First Opdf that will be merged.
	 * @param opdf2 Second Opdf that will be merged.
	 * @return Merged Opdf.
	 */
	private Opdf<ObservationInteger> merge_opdf(Opdf<ObservationInteger> opdf1, Opdf<ObservationInteger> opdf2) {

		//Get length of 'opdf'
		int length = calcOpdfLength(opdf1);
		LinkedList<Double> probabilites = new LinkedList<Double>();

		//Iterate through all observations
		for (int jter = 0; jter < length; jter++) {
			//Calculate new probability for every observation and put it into a new list
			probabilites.add( (opdf1.probability(new ObservationInteger(jter)) + opdf1.probability(new ObservationInteger(jter)))/2 );
		}
		
		//Create and return new opdf with list of probabilites
		return (Opdf<ObservationInteger>) new OpdfInteger (Double2double(probabilites));
	}
	
	
	/**
	 * Calculates likelihood of a single observation given a HMM.
	 * @param list Observation.
	 * @param hmm Hidden-Markov-Model for calculation.
	 * @return Likelihood calculated with forward-algorithm.
	 */
	public double le (List<ObservationInteger> list, Hmm<ObservationInteger> hmm) {
		return hmm.probability(list);
	}
	
	/**
	 * Calculates loss of likelihood of two HMMs corresponding to the corpus.
	 * 
	 * @param corpus Corresponding corpus to the HMMs.
	 * @param hmm_merged_complete Numerator HMM (usually complete splitted).
	 * @param hmm_merged_partly Denumerator HMM (usually one pair of states is merged).
	 * @return Loss of likelihood as logarithmic probability.
	 */
	public double delta_le (List<List<ObservationInteger>> corpus, Hmm<ObservationInteger> hmm_merged_complete, Hmm<ObservationInteger> hmm_merged_partly) {
		
		double product = 0;
		double value1, value2;
		List<ObservationInteger> helper;

		//Iterate through all observation sequences of corpus
		for (Iterator<List<ObservationInteger>> iter = corpus.iterator(); iter.hasNext();) {
			helper = iter.next();

			//Calculate likelihoods of given HMMs and the observation sequence
			value1 = le (helper, hmm_merged_partly);
			value2 = le (helper, hmm_merged_complete);
			//Calculate the ratio between the likelihoods as log-probability
			product += Math.log10(value1) - Math.log10(value2);
		}
		
		return product;
	}
	
	/**
	 * Merges a Hidden-Markov-Model if it is necessary 
	 * (Merging, if epsilon > loss-of-likelihood).
	 * If epsilon is near 1 many sates are merged. Epsilon should be in [0, 1]
	 * 
	 * @param corpus Corresponding corpus to the HMM.
	 * @param hmm_to_merge HMM that will be merged.
	 * @return Partly merged HMM. 
	 */
	public Hmm<ObservationInteger> merge_if_necessary (List<List<ObservationInteger>> corpus, Hmm<ObservationInteger> hmm_to_merge) {//, Hmm<ObservationInteger> hmm_merge_complete) {
		
		Hmm<ObservationInteger> hmm_merge_partly;
		//Not merged HMM
		Hmm<ObservationInteger> hmm_merge_complete1 = hmm_to_merge;//merge(hmm_to_merge, new LinkedList<Integer>());
		
		List<Integer> merging = new LinkedList<Integer>();
		
		//Get number of states of the given HMM
		int states = hmm_to_merge.nbStates();

		double delta[] = new double[states / 2];
		double sum = 0;
		List<Pair<Integer>> pair = new ArrayList<Pair<Integer>>(states / 2);

		//Iterate through every second state of given HMM
		for (int iter = 0; iter < states; iter += 2) {

			//Create a pair of new states, with current and following state and add it to
			//a list
			pair.add(new Pair<Integer>(iter, iter + 1));
			//Merge given HMM with created pair oft states
			hmm_merge_partly = merge (hmm_to_merge, pair.get(iter / 2).toList());
			
			//Create ratio of likelihood of the two HMMs
			delta[iter / 2] = delta_le(corpus, hmm_merge_complete1, hmm_merge_partly);

			//No regression of a HMM is valid and will not used to calculate the sum
			if (delta[iter / 2] < 0 ) {
				sum += delta[iter / 2];
			}
		}		

		//Iterate through every second state of given HMM
		for (int iter = 0; iter < states; iter += 2) {
			System.err.println("			"+epsilon +" > "+(delta[iter/ 2] / sum)+ "? Result: " +(epsilon > delta[iter/ 2] / sum));
//			System.err.println("			"+epsilon +" > "+(delta[iter/ 2])+ "? Result: " +(epsilon > delta[iter/ 2]));
//			System.err.println();

			//Check if the "normalized" ratio likelihood is greater than the given
			// threshold (This isn't really a normalization because negative values are
			// not respected. But for this purpose it should be okay.) 
			//No regressions (positive values) are valid either
			if (delta[iter/ 2] >= 0 || epsilon > delta[iter / 2] / sum) {
				//Add the pair that correspond to the likelihood ratio to the 
				// "merging list"
				merging.add(pair.get(iter / 2).fst);
				merging.add(pair.get(iter / 2).snd);
			}
			
		}

		//Merge given HMM without any states that are element of the "no merging list"
		return merge(hmm_to_merge, merging);
	}
	
	

}
