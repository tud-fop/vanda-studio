//TODO: Pi verteilung an korpus anpasssen!

package hmm.main;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;
import be.ac.ulg.montefiore.run.jahmm.io.HmmReader;
import be.ac.ulg.montefiore.run.jahmm.io.HmmWriter;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfIntegerReader;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfIntegerWriter;
import be.ac.ulg.montefiore.run.jahmm.learn.*;
//import be.ac.ulg.montefiore.run.jahmm.toolbox.*;

import hmm.evaluation.Perplexity;
import hmm.preprocessing.ObservationReader;
import hmm.preprocessing.Wordmap;
import hmm.statesplitting.Alpha;
import hmm.statesplitting.StateSplitting;

/**
 * Main class handles command-line-options and in- and output.
 * Although the training is initialized here.
 * 
 * @author Christof Leonhardt
 */
public class Main {

	/**
	 * Main method to run the program. Here the command-line-parameters are
	 * evaluated and the training is started.
	 * @param args Command-line-parameters are stored in an array.
	 */
	public static void main(String args[]) {
//example();
//if(true) {return;}
		
		//Print given parameters
		System.err.print("Program call:");
		for (int i = 0; i < args.length; i++) {
			System.err.print(" " + args[i]);
		}
		System.err.println();
		
		//Check given parameters
		Configuration config = new Configuration();
		//If parameters are not ok, print this help-text
		if (!config.readCMD(args)) {
			System.err.println("Usage: -statesplitting -output <HMM-FILE> <WORDMAP-FILE> -input <KORPUS-FILE> [-c <TRAINING CYCLES>] [-a <ALPHA>] [-e <EPSILON>] [-s <SEED>] [-t <TOTAL-STATE-SPLIT-STEPS>] [-n <NUMBER OF STATES>]");
			System.err.println("Usage: -normal         -output <HMM-FILE> <WORDMAP-FILE> -input <KORPUS-FILE> [-c <TRAINING CYCLES>] [-a <ALPHA>] [-n <NUMBER OF STATES>] [-s <SEED>]");
			System.err.println("Usage: -parse          -input <HMM-FILE> <WORDMAP-FILE>");
			System.err.println("Usage: -perplexity");
			System.err.println();
			System.err.println("	<HMM-FILE>");
			System.err.println("		--> File that contains a Hidden-Markov-Model.");
			System.err.println("	<WORDMAP-FILE>");
			System.err.println("		--> File with the corresponding integer to word mapping for <HMM-FILE>.");
			System.err.println("	<CORPUS-FILE>");
			System.err.println("		--> File that contains a monolingual corpus for training a Hidden-Markov-Model.");
			System.err.println("	<TRAINING-CYCLES>");
			System.err.println("		--> Number of training cycles for Baum-Welch-Algorithm.");
			System.err.println("		Default: 5");
			System.err.println("	<ALPHA>");
			System.err.println("		--> Amount of soiling the probabilites at State-Splitting. Has to between 0 and 1.");
			System.err.println("		Default: 0.01");
			System.err.println("	<EPSILON>");
			System.err.println("		--> Threshold when two states will be merged.");
			System.err.println("		Default: 0.01");
			System.err.println("	<SEED>");
			System.err.println("		--> Randomization Seed.");
			System.err.println("		Default: System Random-Generator-Seed");
			System.err.println("	<TOTAL-STATE-SPLIT-STEPS>");
			System.err.println("		--> Number of times how often the model will be splitt after training.");
			System.err.println("		Default: 5");
			System.err.println("	<NUMBER OF STATES>");
			System.err.println("		--> Number of states of the Hidden-Markov-Model that will have randomized distributions.");
			System.err.println("		Default: 1");
			//return;
			System.exit(1);
		}
		
		//if parameters are ok, print read information depending on running mode 
		System.err.println("Running programm with:");
			//MODE: state-splitting
			if (config.mode() == RunningMode.StateSplitting) {
				System.err.println("	State-Splitting-Mode");
				System.err.println("	Alpha-Value: "+config.alpha());
				System.err.println("	Epsilon-Value: "+config.epsilon());
				System.err.println("	Random-Seed: "+config.random_seed());
				System.err.println("	State-Splitting-Steps: "+config.training_steps());
				System.err.println("	Number of States at begining: "+config.states());
				System.err.println("	Training-Cycles: "+config.training_cycles());
				System.err.println("	Corpus-File: "+config.input_file());
				System.err.println("	HMM-File: "+config.hmm_file());
				System.err.println("	Wordmap-File: "+config.wordmap_file());
				
				//Start state-splitting
				try {
					stateSplittingMode(config);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			
			//MODE: normal
			} else if (config.mode() == RunningMode.Normal) {
				System.err.println("	Normal-Mode");
				System.err.println("	Alpha-Value: "+config.alpha());
				System.err.println("	Number of States at begining: "+config.states());
				System.err.println("	Training-Cycles: "+config.training_cycles());
				System.err.println("	Corpus-File: "+config.input_file());
				System.err.println("	HMM-File: "+config.hmm_file());
				System.err.println("	Wordmap-File: "+config.wordmap_file());
				
				//Start normal training
				try {
					normalMode(config);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
			//MODE: perplexity
			} else if (config.mode() == RunningMode.Perplexity) {
				System.err.println("	Perplexity-Mode");
				
				//Start calculation of perplexity
				perplexityMode();
				
			//MODE: parsing
			} else {
				System.err.println("	Parsing-Mode");
				System.err.println("	HMM-File: "+config.hmm_file());
				System.err.println("	Wordmap-File: "+config.wordmap_file());
				
				//Start parsing a file
				try {
					parsingMode(config);
				} catch (FileFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
			}
	}
	
	/**
	 * Runns the program in perplexity-mode. Probabilities (double values) are read from
	 * standard-input as long as no EOF appears (good for pipelining). After receiving the
	 * EOF-signal the perplexity of the inputted values is calculated and written to the
	 * standard-output.
	 */
	private static void perplexityMode() {

		System.err.println();
		System.err.println("Please input sentences");	

		List<Double> values = new LinkedList<Double>();
		
		//Read standard input
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		
		try {
		    String line = buffer.readLine();
		 
		    //Store input in list
		    while (line != null && line.length() > 0) {
		    	values.add(new Double(line));
		    	line = buffer.readLine();
		    }

		} catch (IOException o) {
		    System.out.println(o.getMessage());
			System.exit(1);
		}
		
		//Calculate perplexity of input 
		Perplexity perplexity = new Perplexity(values);
		System.out.println(perplexity.get());
		
	}

	/**
	 * Runs the program in parsing-mode. Sentences are read from standard-input and their 
	 * probability is written to standard-output as long as no EOF appears (good for 
	 * pipelining).
	 * 
	 * @param config The configuration contains all needed parameters like in- and
	 * output files, etc. (See {@link Configuration} )
	 * @throws IOException If one of the files could not be accessed.
	 * @throws FileFormatException If the format of the given HMM-File is wrong. 
	 */
	private static void parsingMode(Configuration config) throws IOException, FileFormatException {

		System.err.println();
		System.err.println("Performing Preprocessing...");	

		//Read file that contains the Hidden-Markov-Model
		System.err.print("	Reading HMM-File...");
		FileReader reader = new FileReader(config.hmm_file());
		Hmm<ObservationInteger> hmm = HmmReader.read(reader, new OpdfIntegerReader());
		reader.close();
		System.err.println(" done");

		//Read the file that contains the corresponding wordmap
		System.err.print("	Reading Wordmap...");
		reader = new FileReader(config.wordmap_file());
		Wordmap map = new Wordmap(reader);
		reader.close();
		System.err.println(" done");
		
		System.err.println("Done.");

		System.err.println();
		System.err.println("Please input sentences");	
		
		//Read from standard input
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		try {
		    String line = buffer.readLine();
		 
		    while (line != null && line.length() > 0) {
				//Convert input into an observation-sequence 
		    	List<? extends ObservationInteger> obs = ObservationReader.String2Observation(line, map);
				//Calculate probability of sequence and print it out 
		    	if (!obs.isEmpty()) {
					System.out.println(hmm.probability(obs ));
				}
		        line = buffer.readLine();
		    }
		} catch (IOException o) {
		    System.out.println(o.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Trains a arbitary Hidden-Markov-Model. Initial probabilites are read from the given
	 * sentence corpus.
	 * 
	 * @param config The configuration contains all needed parameters like in- and
	 * output files, etc. (See {@link Configuration} )
	 * @throws IOException If one of the files could not be accessed.
	 */
	private static void normalMode(Configuration config) throws IOException {

		//Create an observation reader -> Read the corpus and create a wordmap
		ObservationReader obsR = preprocessing(config);	
		
		System.err.println("Starting Training...");	
		System.err.print("	Create HMM...");
		
		//Generate a new HMM
		Hmm<ObservationInteger> hmm = createHMM(config, obsR);		
		//Set the BW-learner
		BaumWelchLearner bwl = new BaumWelchLearner();
		//Set number of steps for training
		bwl.setNbIterations(config.training_cycles());

		System.err.println(" done");

		System.err.println("	Train "+config.training_cycles()+" times...");
		//Train HMM with given amount of steps
		hmm = bwl.learn(hmm, obsR.getObservationList());
		System.err.println("	Done");
		System.err.println("Done.");

		//Save HMM and Wordmap to a file
		saveFiles(config, obsR, hmm);
		
	}

	/**
	 * Creates a Hidden-Markov-Model by reading states and seeds, etc. out of a
	 * {@link hmm.main.Configuration}. Also a {@link hmm.preprocessing.ObservationReader}
	 * is needed.
	 * @param config The configuration contains all needed parameters like in- and
	 * output files, etc. (See {@link Configuration} )
	 * @param obsR The observation reader that contains the Observations given by the
	 * corpus and the corresponding {@link Wordmap}.
	 * @return A Hidden-Markov-Model with in the configuration specified number of states
	 * and randomized probabilites.
	 */
	private static Hmm<ObservationInteger> createHMM(Configuration config,
			ObservationReader obsR) {
		Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger> (config.states(), new OpdfIntegerFactory(obsR.wordmap().size()));
		
		//Create (not calculate!) random distributions for observations, starting states,
		// transition probabilities
		Alpha opdf_distribution = new Alpha(config.random_seed());
		Alpha pi_distribution = new Alpha(config.random_seed());
		Alpha aij_distribution = new Alpha(config.random_seed());

		
		//Create distribution of observations depending on occurrence in corpus
		double distribution[] = new double[obsR.wordmap().size()];
		for (int iter = 0; iter < obsR.wordmap().size(); iter ++) {
			distribution[iter] = obsR.wordmap().occurrence(iter);
		}
		OpdfInteger opdf = new OpdfInteger(distribution);

		//Calculate random distribution for starting-states
		pi_distribution.calc(config.states());
		//assign probabilities to each state
		for (int i = 0; i < config.states(); i++) {
			
			//assign starting probability
			hmm.setPi(i, pi_distribution.get(i));
			//assign observation (soiled) probabilities
			hmm.setOpdf(i, StateSplitting.dirt(opdf, opdf_distribution, config.alpha()));
			
			//assign transition probabilities
			aij_distribution.calc(config.states());
			for (int j = 0; j < config.states(); j++) {
				hmm.setAij(i, j, aij_distribution.get(j));
			}
			
		}
		return hmm;
	}

	/**
	 * Saves a trained HMM to a file spezified as parameter. Also the corresponding Wordmap
	 * is saved.
	 * 
	 * @param config The configuration contains all needed parameters like in- and
	 * output files, etc. (See {@link Configuration} )
	 * @param obsR The observation reader that contains the Observations given by the
	 * corpus and the corresponding {@link Wordmap}.
	 * @param hmm The trained Hidden-Markov-Model.
	 * @throws IOException If one of the files could not be accessed.
	 */
	private static void saveFiles(Configuration config, ObservationReader obsR,
			Hmm<ObservationInteger> hmm) throws IOException {
		
		System.err.println();
		System.err.println("Write Output...");
		//Open file, write HMM, close file
		FileWriter writer = new FileWriter(config.hmm_file()); 
		HmmWriter.write(writer, new OpdfIntegerWriter(), hmm);
		writer.close();
	
		//Open file, write wordmap, close file
		writer = new FileWriter(config.wordmap_file()); 
		obsR.wordmap().write(writer);
		writer.close();
		System.err.println("Done.");
		
	}

	/**
	 * Trains a Hidden-Markov-Model using State-Splitting. Initial probabilites are
	 * read from the given sentence corpus.
	 * 
	 * @param config The configuration contains all needed parameters like in- and
	 * output files, etc. (See {@link Configuration} )
	 * @throws IOException If one of the files could not be accessed.
	 */
	private static void stateSplittingMode(Configuration config) throws IOException {
		
		//Create an observation reader -> Read the corpus and create a wordmap
		ObservationReader obsR = preprocessing(config);	
	
		//Create an empty Hidden-Markov-Model
		Hmm<ObservationInteger>	hmm;
	
		System.err.println();
		System.err.println("Starting Training...");	
			//Train HMM with state-splitting
			hmm = train_ss(config, obsR);
		System.err.println("Done.");

		//Save wordmap and HMM to a file
		saveFiles(config, obsR, hmm);	
				
	}

	/**
	 * Reads the corpus and creates the corresponding {@link Wordmap}.
	 * 
	 * @param config The configuration contains all needed parameters like in- and
	 * output files, etc. (See {@link Configuration} )
	 * @return {@link ObservationReader}
	 */
	private static ObservationReader preprocessing(Configuration config) {
		System.err.println();
		System.err.println("Performing Preprocessing...");	

		//Read corpus file and create a wordmap
		ObservationReader obsR = new ObservationReader(config.input_file());
		//If any error appears, abort 
		if (!obsR.createObservationList()) {
			System.err.println("	Error creating ObservationReader. - Exit.");			
			//return;
			System.exit(1);
		}
			
		System.err.println("Done.");
		return obsR;
	}

	/**
	 * Help-Method for {@link #stateSplittingMode}
	 * 
	 * @param config The configuration contains all needed parameters like in- and
	 * output files, etc. (See {@link Configuration} )
	 * @param obsR The observation reader that contains the Observations given by the
	 * corpus and the corresponding {@link Wordmap}.
	 * @return trained {@link Hmm}
	 */
	private static Hmm<ObservationInteger> train_ss(Configuration config, ObservationReader obsR) {
		
		System.err.print("	Set State-Splitter...");
			
		//Initialize state-splitting with some parameters
		StateSplitting ss = new StateSplitting();
		ss = new StateSplitting (config.alpha(), config.epsilon(), config.random_seed());
		System.err.println(" done");
		
		
		System.err.print("	Create HMM...");
		
		//Create new HMM
		Hmm<ObservationInteger> hmm = createHMM(config, obsR);
		//Initialize BW training
		BaumWelchLearner bwl = new BaumWelchLearner();
		//Set number of training iterations
		//BaumWelchScaledLearner bwl = new BaumWelchScaledLearner();
		bwl.setNbIterations(config.training_cycles());

		System.err.println(" done");

		//Perform training with state-splitting (with given amount of state-splitting steps)
		int states;
		for (int i = 0; i < config.training_steps(); i++) {		
	
			System.err.println();
			System.err.println("	Performing State-Splitting trainig-step "+(i+1)+"...");
			
			//(1) SPLIT
			System.err.print("		Splitting...");
			hmm = ss.split(hmm);
			System.err.println(" done");
			
			//(2) TRAINING
			System.err.println("		Train "+config.training_cycles()+" times...");
			hmm = bwl.learn(hmm, obsR.getObservationList());
			System.err.println("		Done.");
			
			//(3) MERGE
			System.err.println("		Merging...");
			//safe number of states for nice output
			states = hmm.nbStates();
			//merging
			hmm = ss.merge_if_necessary(obsR.getObservationList(), hmm);
			//print difference of states of HMMs after merging
			System.err.println("		Done ("+(states-hmm.nbStates())+" states merged).");
			
			//(4) TRAINING
			System.err.println("		Train "+config.training_cycles()+" times...");
			hmm = bwl.learn(hmm, obsR.getObservationList());
			System.err.println("		Done.");

			System.err.println("	Done.");
		}
		
		return hmm;
	}

/**
 * Only for testing purpose
 */
/*
	private static void example() {
		OpdfIntegerFactory factory = new OpdfIntegerFactory(2);
		Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(2, factory);
		        
		hmm.setPi(0, 0.95);
		hmm.setPi(1, 0.05);

		hmm.setAij(0, 0, 0.3);
		hmm.setAij(0, 1, 0.7);

		hmm.setAij(0, 0, 0.3);
		hmm.setAij(1, 1, 0.7);
		
		//hmm.setOpdf(0, new OpdfInteger(new double[] { 0.95, 0.05 }));
		//hmm.setOpdf(1, new OpdfInteger(new double[] { 0.2, 0.8 }));
		hmm.setOpdf(0, new OpdfInteger(new double[] { 0.3, 0.7 }));
		hmm.setOpdf(1, new OpdfInteger(new double[] { 0.3, 0.7 }));

		BaumWelchLearner bwl = new BaumWelchLearner();
		List<? extends List<? extends ObservationInteger>> sequences = generateSequences(hmm, 200);
		Hmm<ObservationInteger> learntHmm = bwl.learn(hmm, sequences );
	
		KullbackLeiblerDistanceCalculator klc = new KullbackLeiblerDistanceCalculator();
	     
		System.out.println(learntHmm.toString());
		for (int i = 0; i < 10; i++) {
		  System.out.println(i + " " + klc.distance(learntHmm, hmm));
		  learntHmm = bwl.iterate(learntHmm, sequences);
		}
		System.out.println(learntHmm.toString());
		System.out.println(learntHmm.getOpdf(0).probability(new ObservationInteger(1))+ " "+ learntHmm.getAij(0, 0));
		System.out.println(learntHmm.getOpdf(1).probability(new ObservationInteger(1))+ " "+ learntHmm.getAij(1, 0));
		
		
		StateSplitting ss = new StateSplitting (0.01, 0.001, 10);
		System.out.println("----------------------------------------------------------");
		System.out.println(learntHmm.toString());
		learntHmm = ss.split(learntHmm);
		System.out.println(learntHmm.toString());
		List<Integer>list = new LinkedList<Integer>();
		list.add(0);
		list.add(1);
		learntHmm = ss.merge(learntHmm, list);
		System.out.println(learntHmm.toString());
		for (int i = 0; i < 10; i++) {
			  System.out.println(i + " " + klc.distance(learntHmm, hmm));
			  learntHmm = bwl.iterate(learntHmm, sequences);
			}
		System.out.println(learntHmm.toString());
		
		
		
		System.out.println("----------------------------------------------------------");
		System.out.println(learntHmm.toString());
		learntHmm = ss.split(learntHmm);
		System.out.println(learntHmm.toString());

		for (int i = 0; i < 10; i++) {
		  //System.out.println(i + " " + klc.distance(learntHmm, hmm));
		  learntHmm = bwl.iterate(learntHmm, sequences);
		}
		System.out.println(learntHmm.toString());
	}

	private static <O extends Observation> List<List<O>> generateSequences(Hmm<O> hmm, int count)
	{
	  MarkovGenerator<O> mg = new MarkovGenerator<O>(hmm);
	  List<List<O>> sequences = new ArrayList<List<O>>();

	  for (int i = 0; i < count; i++)
	    sequences.add(mg.observationSequence(100));

	  return sequences;
	}
*/

}
