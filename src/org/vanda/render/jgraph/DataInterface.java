package org.vanda.render.jgraph;

public interface DataInterface {
	void createConnection(ConnectionCell connectionCell, JobCell tparval, PortCell tval);
	Graph getGraph();
	void createJob(String id, double[] d);
}
