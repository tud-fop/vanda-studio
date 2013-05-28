package org.vanda.render.jgraph;

public interface DataInterface {
	void createConnection(ConnectionCell connectionCell, JobCell tparval, InPortCell tval);
	void createJob(String id, double[] d);
}
