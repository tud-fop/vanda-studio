package org.vanda.execution.model;

public class Runables {
	public static class RunCancelled implements RunEvent {
		public String id;

		public RunCancelled(String id) {
			this.id = id;
		}

		@Override
		public void doNotify(RunEventListener rl) {
			rl.runCancelled(id);
		}

		@Override
		public String getId() {
			return id;
		}
	}

	public static interface RunEvent {
		public void doNotify(RunEventListener rl);
		
		public String getId();
	}

	public static interface RunEventListener {
		public void progressUpdate(String id, int progress);
		
		public void runCancelled(String id);

		public void runFinished(String id);

		public void runStarted(String id);
		
		public void cancelledAll();
	}

	public static class RunFinished implements RunEvent {
		public String id;

		public RunFinished(String id) {
			this.id = id;
		}

		@Override
		public void doNotify(RunEventListener rl) {
			rl.runFinished(id);
		}

		@Override
		public String getId() {
			return id;
		}
	}
	
	public static class RunCancelledAll implements RunEvent {

		@Override
		public void doNotify(RunEventListener rl) {
			rl.cancelledAll();
		}

		@Override
		public String getId() {
			return null;
		}
		
	}
	
	public static class RunProgress implements RunEvent {
		private final String id;
		private final int progress;
		
		public RunProgress(String id, int progress) {
			this.id = id;
			this.progress = progress;
		}
		
		@Override
		public void doNotify(RunEventListener rl) {
			rl.progressUpdate(id, progress);			
		}

		@Override
		public String getId() {
			return id;
		}
		
	}

	public static class RunStarted implements RunEvent {
		private final String id;

		public RunStarted(String id) {
			this.id = id;
		}

		@Override
		public void doNotify(RunEventListener rl) {
			rl.runStarted(id);
		}

		@Override
		public String getId() {
			return id;
		}
	}
	
	public static interface RunState {
		public void cancel(Runable rt);

		public void finish(Runable rt);

		public boolean isDone();

		public boolean isErroneous();

		public boolean isReady();

		public boolean isRunning();
		
		public void progress(Runable rt, int progress);

		public void run(Runable rt);

		public void visit(RunStateVisitor runStateVisitor);
	}
	
	public interface RunStateVisitor {

		void cancelled();

		void done();

		void ready();

		void running();
		
	}

}
