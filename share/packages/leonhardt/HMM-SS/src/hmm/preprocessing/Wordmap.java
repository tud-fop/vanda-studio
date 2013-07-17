package hmm.preprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

/**
 * Represents a mapping from words to integers 
 * @author Christof Leonhardt
 */
public class Wordmap {

	private TreeMap<String, Integer> map;
	private TreeMap<Integer, Integer> count;
	private int max_count;
	
	/**
	 * Normal constructor.
	 */
	public Wordmap() {
		map = new TreeMap<String, Integer>();
		count = new TreeMap<Integer, Integer>();
		max_count = 0;
	}

	/**
	 * Creates a new Wordmap by reading it out of a file
	 * 
	 * @param reader File reader that represents a saved Wordmap.
	 * @throws IOException If one of the files could not be accessed.
	 */
	public Wordmap(Reader reader) throws IOException {
		this.map = new TreeMap<String, Integer>();
		this.count = new TreeMap<Integer, Integer>();
		this.max_count = 0;
		
		String line;
		String words[];
		
		//Read File
		BufferedReader br = new BufferedReader(reader);		
	    while(true) {
	      
	    	//Read single line of file
	    	line = br.readLine();
	    	//Check if file is empty -> leave loop
	    	if (line == null) {break;}
	      
	    	//Extract words
	    	words = line.split(" ");
			//First value of 'words' is the corresponding Integer,
	    	// third value is the word (string) itself
	    	this.map.put(words[2], new Integer(words[0]));
			//Second value is the amount how often the word appeared in corpus file 
	    	this.count.put(new Integer(words[0]), new Integer(words[1]));
			this.max_count ++;

	    } 
	}


	/**
	 * Returns the average occurrence of a single word (integer) over the whole corpus.
	 * @param index The word (integer) for that the occurrence is wanted.
	 * @return the average occurrence of the given word.
	 */
	public double occurrence (int index) {
		return (double) count.get(index) / (double) max_count;
	}
	
	/**
	 * Converts sentence to list of integers.
	 * 
	 * @param text Sentence that will be converted.
	 * @return List of integers that correspond to the Wordmap.
	 */
	public List<Integer> String2Wordmap (String text) {
		
		//Extract words out of given string
		String wordlist[] = text.split("\\s");
		List<Integer> result = new LinkedList<Integer>();
		Integer value;
		
		//Iterate through list of words (strings)
		for (int iter = 0; iter < wordlist.length; iter ++) {
			//Find corresponding integer value in map,
			// if something is found, add it to result-list
			value = map.get(wordlist[iter]);
			if (value != null) {
				result.add(value);
			}
		}
		
		return result;
		
	}
	
	/**
	 * Creates a Wordmap by reading a corpus-file.
	 * @param file Filename of corpus-file.
	 * @return True if conversion was successful.
	 * @return False Otherwise.
	 */
	public boolean create (String file){
		System.err.print("	Create Wordmap...");
	
		String[] sentence_list;
		int counter = 0;
		String line = null;
		
		int word = 0;
		int value = 0;
		
		try {
			//Read file
			BufferedReader buffer = new BufferedReader(new FileReader(file));
			
			//Iterate through lines (sentences) of file
			while ((line = buffer.readLine()) != null) {
				
				//Extract words from line
				sentence_list = line.split("\\s");
				
				if (sentence_list.length >= 2) {
					//Iterate through all words of line
					for (int iter = 0; iter < sentence_list.length; iter ++) {
						max_count ++;
						//If the selected word is not in wordmap...
						if (!map.containsKey(sentence_list[iter])) {
							//... add it and a counter and increase that counter
							map.put(sentence_list[iter], counter);
							counter ++;
						
						}
					}
					
				}
			}

			//set 'count' to 0 
			for (int iter = 0; iter < max_count; iter ++) {
				count.put(iter, 0);
			}
			//Read File
			buffer = new BufferedReader(new FileReader(file));
			
			//Iterate through lines (sentences) of file
			while ((line = buffer.readLine()) != null) {

				//Extract words from line
				sentence_list = line.split("\\s");
				
				if (sentence_list.length >= 2) {
					//Iterate through all words of line
					for (int iter = 0; iter < sentence_list.length; iter ++) {
						//Extract integer that correspond to word (String)
						word = map.get(sentence_list[iter]);
						//Increase counter of that value
						value = count.get(word) + 1;
						//And store it again
						count.put(word, value);
					}
				}
				
			}
		
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		System.err.println(" done.");
		return true;
	};
	
	/**
	 * Writes Wordmap into file. The file has to be opened and closed before and after.
	 * 
	 * @param writer File-Handle.
	 */
	public void write(Writer writer) {
		try {
	    	//Iterate through all elements of wordmap
			for (Map.Entry<String, Integer> entry : map.entrySet()) {			
				//Write every element in form like that into file:
				// [NUMBER OF WORD IN WORDMAP] [OCCURRENCE OF WORD IN CORPUS] [WORD]
				writer.write(entry.getValue()+" "+count.get(entry.getValue())+" "+entry.getKey()+"\n");			
	    	}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * Get corresponding value to given key. If value doesn't exit, null will be returned.
	 * 
	 * @param key The key for the wanted value.
	 * @return The value for the given key, or null, if key not exists.
	 */
	public int 		get(Object key)					{ return map.get(key);				}
	/**
	 * Returns size of Wordmap.
	 * @return Number of elements in Wordmap.
	 */
	public int			size()							{ return map.size();				}
}
