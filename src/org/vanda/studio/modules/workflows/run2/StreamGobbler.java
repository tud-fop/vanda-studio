package org.vanda.studio.modules.workflows.run2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.vanda.execution.model.Runables;
import org.vanda.execution.model.Runables.RunEvent;
import org.vanda.util.MultiplexObserver;

public class StreamGobbler extends Thread {
	private static class CancelledParser implements LineParser {

		@Override
		public void parseLine(String line, MultiplexObserver<RunEvent> mo) {
			if (line.startsWith("Cancelled:")) {
				line.replaceFirst("Cancelled:", "");
				mo.notify(new Runables.RunFinished(line));
			}
		}
	}
	private static class DoneParser implements LineParser {

		@Override
		public void parseLine(String line, MultiplexObserver<RunEvent> mo) {
			if (line.startsWith("Done:")) {
				line.replaceFirst("Done:", "");
				mo.notify(new Runables.RunFinished(line));
			}
		}
	}

	private interface LineParser {
		public void parseLine(String line, MultiplexObserver<RunEvent> mo);
	}

	private static class OrParser implements LineParser {
		private final LineParser[] parsers = { new RunningParser(),
				new DoneParser(), new CancelledParser() };

		@Override
		public void parseLine(String line, MultiplexObserver<RunEvent> mo) {
			for (LineParser p : parsers) {
				p.parseLine(line, mo);
			}
		}
	}

	private static class RunningParser implements LineParser {

		@Override
		public  void parseLine(String line, MultiplexObserver<RunEvent> mo) {
			if (line.startsWith("Running:")) {
				line.replaceFirst("Running:", "");
				mo.notify(new Runables.RunStarted(line));
			}
		}
	}

	private final InputStream is;

	private final MultiplexObserver<RunEvent> observable;

	public StreamGobbler(InputStream is, MultiplexObserver<RunEvent> mo) {
		this.is = is;
		this.observable = mo;
	}

	public void run() {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				new OrParser().parseLine(line, observable);
			}
		} catch (IOException e) {
			// ignore
		}
	}

}
