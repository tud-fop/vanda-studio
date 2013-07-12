package org.vanda.studio.modules.workflows.run2;

import java.util.Date;

public class Runs {
	public static interface RunTransitions {
		void doCancel();

		void doFinish();

		void doRun();
	}

	public static class RunState {

		void cancel(RunTransitions rt) {

		}

		void finish(RunTransitions rt) {

		}

		void run(RunTransitions rt) {

		}

		String getString(Date date) {
			return date.toString();
		}
	}

	public static class StateInit extends RunState {

		void run(RunTransitions rt) {
			rt.doRun();
		}

		String getString(Date date) {
			return "[Initial] " + date.toString();
		}
	}

	public static class StateCancelled extends RunState {
		String getString(Date date) {
			return "[Cancelled] " + date.toString();
		}
	}

	public static class StateDone extends RunState {
		String getString(Date date) {
			return "[Done] " + date.toString();
		}
	}

	



}
