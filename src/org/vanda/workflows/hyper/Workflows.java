package org.vanda.workflows.hyper;


/**
 * Observer infrastructure for workflows. Using generic type <W> here to
 * circumvent circular dependency.
 * 
 * @author mbue
 */
public final class Workflows {

	public static interface WorkflowListener<W> {
		void childAdded(W mwf, Job j);
		
		void childModified(W mwf, Job j);

		void childRemoved(W mwf, Job j);

		void connectionAdded(W mwf, ConnectionKey cc);

		void connectionRemoved(W mwf, ConnectionKey cc);

		void propertyChanged(W mwf);
		
		void updated(W mwf);
	}

	public static interface WorkflowEvent<W> {
		void doNotify(WorkflowListener<W> wl);
	}

	public static class ChildAddedEvent<W> implements WorkflowEvent<W> {
		private final W mwf;
		private final Job j;
		
		public ChildAddedEvent(W mwf, Job j) {
			this.mwf = mwf;
			this.j = j;
		}

		@Override
		public void doNotify(WorkflowListener<W> wcl) {
			wcl.childAdded(mwf, j);
		}
	}
	
	public static class ChildModifiedEvent<W> implements WorkflowEvent<W> {
		private final W mwf;
		private final Job j;
		
		public ChildModifiedEvent(W mwf, Job j) {
			this.mwf = mwf;
			this.j = j;
		}

		@Override
		public void doNotify(WorkflowListener<W> wcl) {
			wcl.childModified(mwf, j);
		}
	}
	
	public static class ChildRemovedEvent<W> implements WorkflowEvent<W> {
		private final W mwf;
		private final Job j;
		
		public ChildRemovedEvent(W mwf, Job j) {
			this.mwf = mwf;
			this.j = j;
		}

		@Override
		public void doNotify(WorkflowListener<W> wcl) {
			wcl.childRemoved(mwf, j);
		}
	}
	
	public static class ConnectionAddedEvent<W> implements WorkflowEvent<W> {
		private final W mwf;
		private final ConnectionKey cc;
		
		public ConnectionAddedEvent(W mwf, ConnectionKey cc) {
			this.mwf = mwf;
			this.cc = cc;
		}

		@Override
		public void doNotify(WorkflowListener<W> wcl) {
			wcl.connectionAdded(mwf, cc);
		}
	}
	
	public static class ConnectionRemovedEvent<W> implements WorkflowEvent<W> {
		private final W mwf;
		private final ConnectionKey cc;
		
		public ConnectionRemovedEvent(W mwf, ConnectionKey cc) {
			this.mwf = mwf;
			this.cc = cc;
		}

		@Override
		public void doNotify(WorkflowListener<W> wcl) {
			wcl.connectionRemoved(mwf, cc);
		}
	}
	
	public static class PropertyChangedEvent<W> implements WorkflowEvent<W> {
		
		private final W mwf;
		
		public PropertyChangedEvent(W mwf) {
			this.mwf = mwf;
		}

		@Override
		public void doNotify(WorkflowListener<W> jl) {
			jl.propertyChanged(mwf);
		}
		
	}
	
	public static class UpdatedEvent<W> implements WorkflowEvent<W> {
		
		private final W mwf;
		
		public UpdatedEvent(W mwf) {
			this.mwf = mwf;
		}

		@Override
		public void doNotify(WorkflowListener<W> jl) {
			jl.updated(mwf);
		}
		
	}

}
