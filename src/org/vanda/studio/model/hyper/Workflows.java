package org.vanda.studio.model.hyper;

import org.vanda.studio.model.hyper.MutableWorkflow.WorkflowChildListener;
import org.vanda.studio.model.hyper.MutableWorkflow.WorkflowListener;

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

	public static class ChildInputPortAddedEvent implements MutableWorkflow.WorkflowChildEvent {
		
		private final MutableWorkflow mwf;
		private final Job j;
		private final int index;
		
		public ChildInputPortAddedEvent(MutableWorkflow mwf, Job j, int index) {
			this.mwf = mwf;
			this.j = j;
			this.index = index;
		}

		@Override
		public void doNotify(WorkflowChildListener wcl) {
			wcl.inputPortAdded(mwf, j, index);
		}
		
	}
	
	public static class ChildInputPortRemovedEvent implements MutableWorkflow.WorkflowChildEvent {
		
		private final MutableWorkflow mwf;
		private final Job j;
		private final int index;
		
		public ChildInputPortRemovedEvent(MutableWorkflow mwf, Job j, int index) {
			this.mwf = mwf;
			this.j = j;
			this.index = index;
		}

		@Override
		public void doNotify(WorkflowChildListener jl) {
			jl.inputPortRemoved(mwf, j, index);
		}
		
	}
	
	public static class ChildOutputPortAddedEvent implements MutableWorkflow.WorkflowChildEvent {
		
		private final MutableWorkflow mwf;
		private final Job j;
		private final int index;
		
		public ChildOutputPortAddedEvent(MutableWorkflow mwf, Job j, int index) {
			this.mwf = mwf;
			this.j = j;
			this.index = index;
		}

		@Override
		public void doNotify(WorkflowChildListener jl) {
			jl.outputPortAdded(mwf, j, index);
		}
		
	}
	
	public static class ChildOutputPortRemovedEvent implements MutableWorkflow.WorkflowChildEvent {
		
		private final MutableWorkflow mwf;
		private final Job j;
		private final int index;
		
		public ChildOutputPortRemovedEvent(MutableWorkflow mwf, Job j, int index) {
			this.mwf = mwf;
			this.j = j;
			this.index = index;
		}

		@Override
		public void doNotify(WorkflowChildListener jl) {
			jl.outputPortRemoved(mwf, j, index);
		}
		
	}

	public static class InputPortAddedEvent implements MutableWorkflow.WorkflowEvent {
		
		private final MutableWorkflow mwf;
		private final int index;
		
		public InputPortAddedEvent(MutableWorkflow mwf, int index) {
			this.mwf = mwf;
			this.index = index;
		}

		@Override
		public void doNotify(WorkflowListener wcl) {
			wcl.inputPortAdded(mwf, index);
		}
		
	}
	
	public static class InputPortRemovedEvent implements MutableWorkflow.WorkflowEvent {
		
		private final MutableWorkflow mwf;
		private final int index;
		
		public InputPortRemovedEvent(MutableWorkflow mwf, int index) {
			this.mwf = mwf;
			this.index = index;
		}

		@Override
		public void doNotify(WorkflowListener jl) {
			jl.inputPortRemoved(mwf, index);
		}
		
	}
	
	public static class OutputPortAddedEvent implements MutableWorkflow.WorkflowEvent {
		
		private final MutableWorkflow mwf;
		private final int index;
		
		public OutputPortAddedEvent(MutableWorkflow mwf, int index) {
			this.mwf = mwf;
			this.index = index;
		}

		@Override
		public void doNotify(WorkflowListener jl) {
			jl.outputPortAdded(mwf, index);
		}
		
	}
	
	public static class OutputPortRemovedEvent implements MutableWorkflow.WorkflowEvent {
		
		private final MutableWorkflow mwf;
		private final int index;
		
		public OutputPortRemovedEvent(MutableWorkflow mwf, int index) {
			this.mwf = mwf;
			this.index = index;
		}

		@Override
		public void doNotify(WorkflowListener jl) {
			jl.outputPortRemoved(mwf, index);
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
