package org.vanda.presentationmodel;

import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.ConnectionCell;
import org.vanda.render.jgraph.PortCell;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;

public class ConnectionAdapter {
	private final ConnectionKey connectionKey;
	private final ConnectionCell visualization;
	
	public ConnectionAdapter(ConnectionKey cc, PresentationModel pm, MutableWorkflow mwf) {
		this.connectionKey = cc;
		
		// find source and target JobCells
		Job sourceJob = mwf.getConnectionSource(cc).target;
		Job targetJob = cc.target;
		Port sourcePort = mwf.getConnectionSource(cc).targetPort;
		Port targetPort = mwf.getConnectionSource(cc).targetPort;
		JobAdapter sJA = null;
		JobAdapter tJA = null;
		for (JobAdapter jA : pm.getJobs()) {
			if (jA.getJob() == sourceJob)
				sJA = jA;
			if (jA.getJob() == targetJob) 
				tJA = jA;				
		}
		assert (sJA != null && tJA != null);
		PortCell source = null;
		PortCell target = null;
		for (Cell c : sJA.getCells())
			if (c.getType() == "PortCell") 
				if (((PortCell) c).getPort() == sourcePort)
					source = (PortCell) c;
		for (Cell c : tJA.getCells())
			if (c.getType() == "PortCell")
				if (((PortCell) c).getPort() == targetPort)
					target = (PortCell) c;
		assert (source != null && target != null);

		this.visualization = new ConnectionCell(connectionKey, pm.getVisualization(), source, target);
	}
	

}
