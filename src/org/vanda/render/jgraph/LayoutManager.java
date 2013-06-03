package org.vanda.render.jgraph;

public interface LayoutManager {
	public void setUpLayout(Graph graph, Cell container);
	
	static final LayoutSelector INPORT = new LayoutSelector() {
		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getInport();
		}
	};
	
	static final LayoutSelector OUTPORT = new LayoutSelector() {
		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getOutport();
		}
	};

	static final LayoutSelector LOCATION = new LayoutSelector() {
		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getLocation();
		}
	};

	static final LayoutSelector JOBCELL = new LayoutSelector () {

		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getJob();
		}
		
	};

	static final LayoutSelector CONNECTION = new LayoutSelector () {

		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getConnection();
		}
		
	};

	static final LayoutSelector WORKFLOW = new LayoutSelector () {

		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getWorkflow();
		}
		
	};

}
