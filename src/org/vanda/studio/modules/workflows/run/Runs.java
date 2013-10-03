package org.vanda.studio.modules.workflows.run;

import java.util.Date;

import org.vanda.execution.model.RunStates.*;

public class Runs {
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

		public void visit(RunEventListener rsv) {
			
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
		
		@Override
		public void visit(RunEventListener rsv) {
			rsv.ready();
		}

		public String getString(Date date) {
			return "[Initial] " + date.toString();
		}
	}

	public static class StateCancelled extends RunState {
		@Override
		public void visit(RunEventListener rsv) {
			rsv.cancelled();
		}

		public String getString(Date date) {
			return "[Cancelled] " + date.toString();
		}
	}

	public static class StateDone extends RunState {
		@Override
		public void visit(RunEventListener rsv) {
			rsv.done();
		}

		public String getString(Date date) {
			return "[Done] " + date.toString();
		}
	}

	
	


}
