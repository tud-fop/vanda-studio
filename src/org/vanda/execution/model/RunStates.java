package org.vanda.execution.model;

public class RunStates {
	public static interface RunTransitions {
		void doCancel();

		void doFinish();

		void doRun();
	}
	
	public interface RunEventListener {
		void cancelled();

		void done();
		
		void progress(int progress);

		void ready();

		void running();
	}
	
	public static interface RunEvent {
		public void doNotify(RunEventListener rsv);
	}
	
	public static class RunStateCancelled implements RunEvent {
		@Override
		public void doNotify(RunEventListener rsv) {
			rsv.cancelled();
		}
	}

	public static class RunStateDone implements RunEvent {
		@Override
		public void doNotify(RunEventListener rsv) {
			rsv.done();
		}
	}

	public static class RunStateProgress implements RunEvent {
		private final int p;
		
		public RunStateProgress(int p) {
			this.p = p;
		}
		
		@Override
		public void doNotify(RunEventListener rsv) {
			rsv.progress(p);
		}
	}

	public static class RunStateReady implements RunEvent {
		@Override
		public void doNotify(RunEventListener rsv) {
			rsv.ready();
		}
	}
	
	public static class RunStateRunning implements RunEvent {
		@Override
		public void doNotify(RunEventListener rsv) {
			rsv.running();
		}
	}

	public static class RunEventId {
		private final RunEvent event;
		
		private final String id;
		
		public RunEventId(RunEvent event, String id) {
			this.event = event;
			this.id = id;
		}
		
		public RunEvent getEvent() {
			return event;
		}
		
		public String getId() {
			return id;
		}
	}
}
