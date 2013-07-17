package hmm.main;

/**
 * This class handles the command-line-parameters and stores all necessary information
 * like in- and output-files, training cycles, etc.
 * 
 * @author Christof Leonhardt
 */
public class Configuration {

	private RunningMode rm	= RunningMode.Undefined;
	private int cycles		= 5;
	private int steps		= 5;
	private long seed		= -1;
	private double alph	= -1;
	private double epsi	= -1;
	private String input;
	private String hmm_output;
	private String wmp_output;
	private int states		= 1;
	
	/**
	 * Normal public constructor.
	 */
	public Configuration(){}
	
	/**
	 * Parses the command-line and saves the given values.
	 * 
	 * @param args Command-line-parameters are stored in an array.
	 * @return True if command-line was OK and values were stored.
	 * @return False if command-line could not be parsed.
	 */
	public boolean readCMD(String args[]) {
		
		//Check if any arguments are given
		if (args.length == 0 ) { return false;}

		//Read running mode, or return false
		int jump = -1;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-statesplitting")) {
				rm = RunningMode.StateSplitting;
				jump = i;
			} else if (args[i].equalsIgnoreCase("-normal")) {
				rm = RunningMode.Normal;
				jump = i;
			} else if (args[i].equalsIgnoreCase("-parse")) {
				rm = RunningMode.Parsing;
				jump = i;
			} else if (args[i].equalsIgnoreCase("-perplexity")) {
				rm = RunningMode.Perplexity;
				jump = i;
			}
		}
		if (rm == RunningMode.Undefined) {
			return false;
		} else if (rm == RunningMode.Perplexity) {
			//Perplexity doesn't need any further arguments
			return true;
		}
		
		//Check arguments that correspond to the given running mode
		for (int i = 0; i < args.length; i++) {
			
			if (i == jump) { continue; }
			
			//MODE: State-Splitting
			if (rm == RunningMode.StateSplitting) {
				if (i + 2 < args.length && args[i].equalsIgnoreCase("-output")) {
					hmm_output = args[i + 1];
					wmp_output = args[i + 2];
					i += 2;
				} else if (i + 1 < args.length) {
					if(args[i].equalsIgnoreCase("-s")) {
						seed = new Long(args[i + 1]);
					} else if (args[i].equalsIgnoreCase("-a")) {
						alph = new Double(args[i + 1]);
					} else if (args[i].equalsIgnoreCase("-e")) {
						epsi = new Double(args[i + 1]);
					} else if (args[i].equalsIgnoreCase("-c")) {
						cycles = new Integer(args[i + 1]);
					} else if (args[i].equalsIgnoreCase("-t")) {
						steps = new Integer(args[i + 1]);
					} else if (args[i].equalsIgnoreCase("-n")) {
							states = new Integer(args[i + 1]);
					} else if (args[i].equalsIgnoreCase("-input")) {
						input = args[i + 1];
					}
					i++;
				} else {
					return false;
				}
				
			//MODE: Normal
			} else if (rm == RunningMode.Normal) {

				if (i + 2 < args.length && args[i].equalsIgnoreCase("-output")) {
					hmm_output = args[i + 1];
					wmp_output = args[i + 2];
					i += 2;
				} else if (i + 1 < args.length) {
					if (args[i].equalsIgnoreCase("-n")) {
						states = new Integer(args[i + 1]);
					} else if (args[i].equalsIgnoreCase("-a")) {
						alph = new Double(args[i + 1]);
					}else if (args[i].equalsIgnoreCase("-s")) {
						seed = new Integer(args[i + 1]);
					}else if (args[i].equalsIgnoreCase("-c")) {
						cycles = new Integer(args[i + 1]);
					} else if (args[i].equalsIgnoreCase("-input")) {
						input = args[i + 1];
					}
					i++;
				} else {
					return false;
				}
			
			//MODE: Parsing
			} else if (rm == RunningMode.Parsing) {
				if (i + 2 < args.length && args[i].equalsIgnoreCase("-input")) {
					hmm_output = args[i + 1];
					wmp_output = args[i + 2];
					input = "dummy";
					i += 2;
				} else {
					return false;
				}
			}
		}
		
		//Check if all strings are set.
		if (input == null || wmp_output == null || hmm_output == null || input.equals("") || wmp_output.equals("") || hmm_output.equals("")) {
			return false;
		}
		
		return true;		
	}

	/**
	 * Returns RunningMode. 
	 * @return {@link RunningMode}
	 */
	public RunningMode	mode()				{ return rm;			}

	/**
	 * Returns the number of states the Hidden-Markov-Model will have. 
	 * @return Number of states
	 */
	public int			states()			{ return states;		}

	/**
	 * Returns the number iterations in the Baum-Welch-Algorithm (See {@link be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner}). 
	 * @return Number of iterations
	 */
	public int			training_cycles()	{ return cycles;		}

	/**
	 * Returns the number of state-splits that will be performed . 
	 * @return Number of iterations
	 */
	public int			training_steps()	{ return steps;			}

	/**
	 * Returns the seed that is used for initializing the random generator. 
	 * @return -1 No seed set
	 * @return >= 0 Seed
	 */
	public long		random_seed()		{ return seed;			}

	/**
	 * Returns the threshold when merging is performed. 
	 * @return Threshold
	 */
	public double		epsilon()			{ return epsi;			}

	/**
	 * Returns the amount how much the symmetry will be broke. 
	 * @return Value between 0 and 1
	 */
	public double		alpha()				{ return alph;			}

	/**
	 * Returns the name of the corpus-file that is used 
	 * @return Name and path of File
	 */
	public String		input_file()		{ return input;			}

	/**
	 * Returns the name of the output-file, where the trained HHM will be saved. 
	 * @return Name and path of File
	 */
	public String		hmm_file()			{ return hmm_output;	}

	/**
	 * Returns the name of the output-file, where the Wordmap corresponding to
	 * the HHM will be saved. 
	 * @return Name and path of File
	 */
	public String		wordmap_file()		{ return wmp_output;	}
	
}
