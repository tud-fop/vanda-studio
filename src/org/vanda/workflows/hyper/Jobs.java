package org.vanda.workflows.hyper;

import org.vanda.workflows.hyper.Job.JobEvent;
import org.vanda.workflows.hyper.Job.JobListener;

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

}
