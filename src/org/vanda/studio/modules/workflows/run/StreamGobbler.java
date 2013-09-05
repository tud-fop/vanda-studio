package org.vanda.studio.modules.workflows.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {

	private final InputStream is;

	public StreamGobbler(InputStream is) {
		this.is = is;
	}

	public void run() {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		try {
			while (br.readLine() != null);
		} catch (IOException e) {
			// ignore
		}
	}

}
