package hmm.preprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.*;

/**
 * This class reads the corpus-file an stores it as a list of lists of observations. 
 * For this reason a {@link Wordmap} is generated.
 * 
 * @author Christof Leonhardt
 */
public class ObservationReader {

	private Wordmap map;
	private List<List<ObservationInteger>> list;
	private String filename;
	
	/**
	 * Normal Consturctor
	 */
	public ObservationReader() {}

	/**
	 * Creates new ObservationReader object and stores the filename. No file-reading is
	 * performed at this time. Use {@link #createObservationList()} to create a list of
	 * Observations.
	 * @param file Filename, where the observations are stored. 
	 */
	public ObservationReader(String file) {
		filename = file;
		map = new Wordmap();
		list = new LinkedList<List<ObservationInteger>>();
	}
	
	
	/**
	 * Converts a sentence to a list of observations.
	 * 
	 * @param text The sentence that has to be converted.
	 * @param map A Wordmap that is used as Dictionary.
	 * @return A List of observations that correspond to the given Wordmap.
	 */
	public static List<ObservationInteger> String2Observation (String text, Wordmap map) {
		
		List<ObservationInteger> result = new LinkedList<ObservationInteger>();
		//Convert string to list of integers by using the wordmap
		List<Integer> list = map.String2Wordmap(text);
		
		//Convert list of integers to observation sequence
		for (Iterator<Integer> iter = list.iterator(); iter.hasNext();) {
			result.add(new ObservationInteger(iter.next()));
		}
		
		return result;
	}

	
	/**
	 * Returns the Wordmap corresponding to the observations. 
	 * @return {@link Wordmap}
	 */
	public Wordmap wordmap() { return map; }
	//public void setWordMap(Wordmap wordmap) { map = wordmap; }	
	
	/**
	 * Returns list of observations stored in the ObservationReader.
	 * @return List of observations.
	 */
	public List<List<ObservationInteger>> getObservationList() {return list;}
	
	/**
	 * Reads the given corpus-file and stores all sentences as list of lists of observations. 
	 * @return True If conversion was successful.
	 * @return False Otherwise.
	 */
	public boolean createObservationList() {
		//Create a new wordmap
		if (map.create(filename)) {
			//Read file by converting every observation to list of observation sequences
			// by using the generated wordmap 
			if (create()) {
				return true;
			}
		}		
		return false;
	}

	/**
	 * Helper function for {@link #createObservationList()}.
	 * @return True If conversion was successful.
	 * @return False Otherwise.
	 */
	private boolean create() {
		System.err.print("	Create Observations...");
		
		String[] sentence_list;
		String line = null;
		
		try {
			//Read file
			BufferedReader buffer = new BufferedReader(new FileReader(filename));
			//Iterate through all lines of file
			while ((line = buffer.readLine()) != null) {
				//Extract words out of a line (sentence)
				sentence_list = line.split("\\s");
				
				if (sentence_list.length >= 2) {
					//Expand list of observation sequences 
					list.add(new LinkedList<ObservationInteger>());
					//Add observations to observation sequence
					// by matching the words of the sentence to the values in the wordmap
					for (int iter = 0; iter < sentence_list.length; iter ++) {
						list.get(list.size() - 1).add(new ObservationInteger(map.get(sentence_list[iter])));
					}
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		System.err.println(" done.");
		return true;
	}
	
}
