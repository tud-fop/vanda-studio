package org.vanda.workflows.hyper;


public class Jobs {
	
	public static interface JobEvent<J> {
		void doNotify(JobListener<J> jl);
	}
	
	public static interface JobListener<J> {
		// removed: see older versions
		// void inputPortAdded(Job j, int index);
		// void inputPortRemoved(Job j, int index);
		// void outputPortAdded(Job j, int index);
		// void outputPortRemoved(Job j, int index);
		void propertyChanged(J j);		
	}
	
	public static class PropertyChangedEvent<J> implements JobEvent<J> {
		
		private final J j;
		
		public PropertyChangedEvent(J j) {
			this.j = j;
		}

		@Override
		public void doNotify(JobListener<J> jl) {
			jl.propertyChanged(j);
		}

	}

}
