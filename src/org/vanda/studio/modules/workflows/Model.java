package org.vanda.studio.modules.workflows;

import java.util.List;

import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.model.immutable.CompositeImmutableJob;
import org.vanda.studio.model.immutable.ImmutableWorkflow;

public final class Model<F> {

	public abstract static class SingleObjectSelection {
		public final List<Object> parent;

		public SingleObjectSelection(List<Object> parent) {
			this.parent = parent;
		}

		public MutableWorkflow<?> getParent(MutableWorkflow<?> root) {
			if (!parent.isEmpty())
				root = (MutableWorkflow<?>) ((CompositeJob<?, ?>) root
						.dereference(parent.listIterator())).getWorkflow();
			return root;
		}

		public ImmutableWorkflow<?> getParent(ImmutableWorkflow<?> root) {
			if (!parent.isEmpty())
				root = (ImmutableWorkflow<?>) ((CompositeImmutableJob<?, ?>) root
						.dereference(parent.listIterator())).getWorkflow();
			return root;
		}

		public abstract void remove(MutableWorkflow<?> root);
	}

	public static class ConnectionSelection extends SingleObjectSelection {
		private final Connection<?> cc;
		
		public ConnectionSelection(List<Object> parent, Connection<?> cc) {
			super(parent);
			this.cc = cc;
		}

		@Override
		public void remove(MutableWorkflow<?> root) {
			
		}
	}

	/*
	public static class JobSelection extends SingleObjectSelection {
		public JobSelection(List<Object> parent, Object address) {
			super(parent, address);
		}

		@Override
		public void remove(MutableWorkflow<?> root) {
			// TODO Auto-generated method stub

		}
	}

	public static class WorkflowSelection extends SingleObjectSelection {
		public WorkflowSelection(List<Object> parent) {
			super(parent, null);
		}

		@Override
		public void remove(MutableWorkflow<?> root) {
			// TODO Auto-generated method stub

		}
	}
	*/

	protected MutableWorkflow<F> hwf;
	protected ImmutableWorkflow<F> frozen;
	protected List<ImmutableWorkflow<F>> unfolded;
	protected List<Object> selection;

}
