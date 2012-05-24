package org.vanda.studio.model.hyper;

import org.vanda.studio.model.hyper.Job.JobEvent;
import org.vanda.studio.model.hyper.Job.JobListener;

public class Jobs {
	
	public static class PropertyChangedEvent implements JobEvent {
		
		private final Job j;
		
		public PropertyChangedEvent(Job j) {
			this.j = j;
		}

		@Override
		public void doNotify(JobListener jl) {
			jl.propertyChanged(j);
		}

	}

	public static class InputPortAddedEvent implements Job.JobEvent {
		
		private final Job j;
		private final int index;
		
		public InputPortAddedEvent(Job j, int index) {
			this.j = j;
			this.index = index;
		}

		@Override
		public void doNotify(JobListener jl) {
			jl.inputPortAdded(j, index);
		}
		
	}
	
	public static class InputPortRemovedEvent implements Job.JobEvent {
		
		private final Job j;
		private final int index;
		
		public InputPortRemovedEvent(Job j, int index) {
			this.j = j;
			this.index = index;
		}

		@Override
		public void doNotify(JobListener jl) {
			jl.inputPortRemoved(j, index);
		}
		
	}
	
	public static class OutputPortAddedEvent implements Job.JobEvent {
		
		private final Job j;
		private final int index;
		
		public OutputPortAddedEvent(Job j, int index) {
			this.j = j;
			this.index = index;
		}

		@Override
		public void doNotify(JobListener jl) {
			jl.outputPortAdded(j, index);
		}
		
	}
	
	public static class OutputPortRemovedEvent implements Job.JobEvent {
		
		private final Job j;
		private final int index;
		
		public OutputPortRemovedEvent(Job j, int index) {
			this.j = j;
			this.index = index;
		}

		@Override
		public void doNotify(JobListener jl) {
			jl.outputPortRemoved(j, index);
		}
		
	}

}
