package org.vanda.studio.modules.workflows.run;

import java.util.Date;

public class Runs {
	public static interface RunTransitions {
		void doCancel();

		void doFinish();

		void doRun();
	}

	public static class RunState {

		public void cancel() {

		}
		
		public void finish() {
			
		}
		
		public void process() {
			// perform background process for the state
		}

		public void run() {

		}

		public String getString(Date date) {
			return date.toString();
		}
	}

	public static class StateInit extends RunState {
		
		private final RunTransitions rt;
		
		public StateInit(RunTransitions rt) {
			this.rt = rt;
		}

		@Override
		public void run() {
			rt.doRun();
		}

		public String getString(Date date) {
			return "[Initial] " + date.toString();
		}
	}

	public static class StateCancelled extends RunState {
		public String getString(Date date) {
			return "[Cancelled] " + date.toString();
		}
	}

	public static class StateDone extends RunState {
		public String getString(Date date) {
			return "[Done] " + date.toString();
		}
	}

	
	


}
