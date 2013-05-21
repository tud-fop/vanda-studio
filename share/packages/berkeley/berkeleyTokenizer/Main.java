import edu.berkeley.nlp.io.PTBLineLexer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.*;

public class Main {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		BufferedReader inputData = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter outputData = new PrintWriter(new OutputStreamWriter(System.out));
		PTBLineLexer tokenizer = new PTBLineLexer();
		String line = "";
		while((line=inputData.readLine()) != null){
			List<String> sentence = tokenizer.tokenizeLine(line);
			boolean first = true;
			for (String word : sentence) {
				if (!first)
					outputData.write(" ");
				else
					first = false;
				outputData.write(word);
			}
			outputData.write("\n");
		}
		outputData.flush();
		outputData.close();
	}
}
