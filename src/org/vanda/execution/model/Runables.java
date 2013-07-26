package org.vanda.execution.model;

public class Runables {
	public static class DefaultRunEventListener implements RunEventListener {
		final Runable rt;
		public DefaultRunEventListener(final Runable rt) {
			this.rt = rt;
		}
		
		@Override
		public void runCancelled(String id) {
			if (id.equals(rt.getID())) {
				rt.getState().cancel(rt);
			}
		}

		@Override
		public void runFinished(String id) {
			if (id.equals(rt.getID())) {
				rt.getState().finish(rt);
			}

		}

		@Override
		public void runStarted(String id) {
			if (id.equals(rt.getID())) {
				rt.getState().run(rt);
			}
		}

		@Override
		public void cancelledAll() {
			rt.doCancel();
		}

	}

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

	public static class RunStarted implements RunEvent {
		public String id;

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
