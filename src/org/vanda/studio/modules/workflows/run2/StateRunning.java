package org.vanda.studio.modules.workflows.run2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import javax.swing.SwingWorker;

import org.vanda.execution.model.ExecutableWorkflow;
import org.vanda.execution.model.Runables;
import org.vanda.execution.model.Runables.RunCancelledAll;
import org.vanda.execution.model.Runables.RunEvent;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.run2.Runs.RunState;
import org.vanda.studio.modules.workflows.run2.Runs.RunTransitions;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;
import org.vanda.util.RCChecker;
import org.vanda.util.Util;

public class StateRunning extends RunState {

	private final Observer<RunEvent> obs;
	private final String id;
	private final Application app;
	private final RunTransitions rt;

	private Worker w;

	public StateRunning(Observer<RunEvent> obs, String id, Application app, RunTransitions rt) {
		this.obs = obs;
		this.id = id;
		this.app = app;
		this.rt = rt;
	}

	@Override
	public void cancel() {
		if (!w.isDone())
			w.cancel(true);
	}

	@Override
	public void process() {
		w = new Worker();
		w.execute();
	}

	public String getString(Date date) {
		return "[Running] " + date.toString();
	}

	private class Worker extends SwingWorker<String, RunEvent> implements Observer<RunEvent> {

		private Process process;
		private StreamGobbler esg;
		private int retval = -1;

		@Override
		protected String doInBackground() {
			try {
				process = Runtime.getRuntime().exec(RCChecker.getOutPath() + "/" + id, null, null);
			} catch (Exception e) {
				app.sendMessage(new ExceptionMessage(e));
			}

			// ignore stderr, parse stdout
			InputStream stdin = process.getInputStream();
			InputStream stderr = process.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stdin);
			BufferedReader br = new BufferedReader(isr);
			LineParser lp = new OrParser();
			esg = new StreamGobbler(stderr);
			esg.start();
			try {
				String line;
				while ((line = br.readLine()) != null)
					lp.parseLine(line, this);
				retval = process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void done() {
			if (process != null) {
				process.destroy();
				process = null;
			}
			if (retval != 0) {
				obs.notify(new RunCancelledAll());
				rt.doCancel();
			} else
				rt.doFinish();
		}

		@Override
		public void notify(RunEvent event) {
			publish(event);
		}

		@Override
		protected void process(List<RunEvent> res) {
			Util.notifyAll(obs, res);
		}

	}

	private interface LineParser {
		public void parseLine(String line, Observer<RunEvent> mo);
	}

	private static class CancelledParser implements LineParser {

		@Override
		public void parseLine(String line, Observer<RunEvent> mo) {
			if (line.startsWith("Cancelled: ")) {
				String newstring = line.replaceFirst("Cancelled: ", "");
				mo.notify(new Runables.RunFinished(newstring));
			}
		}
	}

	private static class DoneParser implements LineParser {

		@Override
		public void parseLine(String line, Observer<RunEvent> mo) {
			if (line.startsWith("Done: ")) {
				String newstring = line.replaceFirst("Done: ", "");
				mo.notify(new Runables.RunFinished(newstring));
				// FIXME System.out.println("Done: " + newstring);
			}
		}
	}

	private static class OrParser implements LineParser {
		private final LineParser[] parsers = { new RunningParser(), new DoneParser(), new CancelledParser() };

		@Override
		public void parseLine(String line, Observer<RunEvent> mo) {
			for (LineParser p : parsers) {
				p.parseLine(line, mo);
			}
		}
	}

	private static class RunningParser implements LineParser {

		@Override
		public void parseLine(String line, Observer<RunEvent> mo) {
			if (line.startsWith("Running: ")) {
				String newstring = line.replace("Running: ", "");
				mo.notify(new Runables.RunStarted(newstring));
				// FIXME System.out.println("Running: " + newstring);
			}
		}
	}

}
