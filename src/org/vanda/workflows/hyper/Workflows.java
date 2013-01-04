package org.vanda.workflows.hyper;

import org.vanda.workflows.hyper.MutableWorkflow.WorkflowChildListener;
import org.vanda.workflows.hyper.MutableWorkflow.WorkflowListener;

public class Workflows {
	
	public static class ChildAddedEvent implements MutableWorkflow.WorkflowChildEvent {
		private final MutableWorkflow mwf;
		private final Job j;
		
		public ChildAddedEvent(MutableWorkflow mwf, Job j) {
			this.mwf = mwf;
			this.j = j;
		}

		@Override
		public void doNotify(WorkflowChildListener wcl) {
			wcl.childAdded(mwf, j);
		}
	}
	
	public static class ChildModifiedEvent implements MutableWorkflow.WorkflowChildEvent {
		private final MutableWorkflow mwf;
		private final Job j;
		
		public ChildModifiedEvent(MutableWorkflow mwf, Job j) {
			this.mwf = mwf;
			this.j = j;
		}

		@Override
		public void doNotify(WorkflowChildListener wcl) {
			wcl.childModified(mwf, j);
		}
	}
	
	public static class ChildRemovedEvent implements MutableWorkflow.WorkflowChildEvent {
		private final MutableWorkflow mwf;
		private final Job j;
		
		public ChildRemovedEvent(MutableWorkflow mwf, Job j) {
			this.mwf = mwf;
			this.j = j;
		}

		@Override
		public void doNotify(WorkflowChildListener wcl) {
			wcl.childRemoved(mwf, j);
		}
	}
	
	public static class ConnectionAddedEvent implements MutableWorkflow.WorkflowChildEvent {
		private final MutableWorkflow mwf;
		private final Connection cc;
		
		public ConnectionAddedEvent(MutableWorkflow mwf, Connection cc) {
			this.mwf = mwf;
			this.cc = cc;
		}

		@Override
		public void doNotify(WorkflowChildListener wcl) {
			wcl.connectionAdded(mwf, cc);
		}
	}
	
	public static class ConnectionRemovedEvent implements MutableWorkflow.WorkflowChildEvent {
		private final MutableWorkflow mwf;
		private final Connection cc;
		
		public ConnectionRemovedEvent(MutableWorkflow mwf, Connection cc) {
			this.mwf = mwf;
			this.cc = cc;
		}

		@Override
		public void doNotify(WorkflowChildListener wcl) {
			wcl.connectionRemoved(mwf, cc);
		}
	}
	
	public static class PropertyChangedEvent implements MutableWorkflow.WorkflowEvent {
		
		private final MutableWorkflow mwf;
		
		public PropertyChangedEvent(MutableWorkflow mwf) {
			this.mwf = mwf;
		}

		@Override
		public void doNotify(WorkflowListener jl) {
			jl.propertyChanged(mwf);
		}
		
	}

}
