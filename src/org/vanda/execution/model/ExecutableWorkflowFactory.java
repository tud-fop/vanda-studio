package org.vanda.execution.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.fragment.model.SemanticAnalysis;
import org.vanda.fragment.model.SyntaxAnalysis;
import org.vanda.util.Observer;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.DataflowAnalysis;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.LiteralAdapter;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Jobs.JobEvent;

public class ExecutableWorkflowFactory {

	private static double[] workflowDimension(MutableWorkflow mwf) {
		double xmin = 0, xmax = 0, ymin = 0, ymax = 0;
		Iterator<Job> ji = mwf.getChildren().iterator();
		if (ji.hasNext()) {
			Job j = ji.next();
			xmin = j.getX();
			xmax = j.getX() + j.getWidth();
			ymin = j.getY();
			ymax = j.getY() + j.getHeight();
		}
		while (ji.hasNext()) {
			Job j = ji.next();
			if (xmin > j.getX())
				xmin = j.getX();
			if (xmax < j.getX() + j.getWidth())
				xmax = j.getX() + j.getWidth();
			if (ymin > j.getY())
				ymin = j.getY();
			if (ymax < j.getY() + j.getHeight())
				ymax = j.getY() + j.getHeight();
		}
		return new double[] { xmax - xmin, ymax - ymin };

	}

	public MutableWorkflow generateExecutableWorkflow(MutableWorkflow mwf, Database db, SyntaxAnalysis synA,
			final SemanticAnalysis semA) {
		final MutableWorkflow ewf = new MutableWorkflow(mwf.getName());

		for (int i = 0; i < db.getSize(); ++i) {
			final Map<Location, Location> translation = new HashMap<Location, Location>();
			final WeakHashMap<Location, ConnectionKey> varSources = new WeakHashMap<Location, ConnectionKey>();
			
			// TODO skip unselected Assignments
			final HashMap<String, String> dbRow = db.getRow(i);
			// TODO increase shifting values
			final double dx = 0;
			final double dy = 0;
			for (final Job j : synA.getSorted()) {
				if (j.isConnected()) {
					j.visit(new ElementVisitor() {
						@Override
						public void visitLiteral(Literal lit) {
							Job ej = new Job(new LiteralAdapter(new Literal(lit.getType(), lit.getName(), dbRow.get(lit.getKey()))), semA.getDFA().getJobId(j));
							ej.setDimensions(new double[] { j.getX() + dx, j.getY() + dy, j.getWidth(), j.getHeight() });
							ewf.addChild(ej);
							
//							ej.insert();
//							Port op = j.getOutputPorts().get(0);
//							Location valVar = new Location();
//							ej.bindings.put(op, valVar);
//							varSources.put(valVar, new ConnectionKey(ej, op));
//							translation.put(j.bindings.get(op), valVar);
							
						}

						@Override
						public void visitTool(Tool t) {
							Job ej = new Job(j.getElement(), semA.getDFA().getJobId(j));
							ej.setDimensions(new double[] { j.getX() + dx, j.getY() + dy, j.getWidth(), j.getHeight() });
							ewf.addChild(ej);

							// InPorts
//							List<Port> ports = t.getInputPorts();
//							if (ports.size() > 0) {
//								ej.bindings.put(ports.get(0), translation.get(j.bindings.get(ports.get(0))));
//							}
//							for (int i = 1; i < ports.size(); i++) {
//								ej.bindings.put(ports.get(i), translation.get(j.bindings.get(ports.get(i))));
//							}

							// OutPorts
//							for (Port op : t.getOutputPorts()) {
//								String value = toolPrefix + "." + op.getIdentifier();
//								ej.bindings.put(op, new ValuedLocation(value));
//								varSources.put(ej.bindings.get(op), new ConnectionKey(ej, op));
//								translation.put(j.bindings.get(op), (ValuedLocation) ej.bindings.get(op));
//							}

							
						}

					});
				} else {
					Job ej = new Job(j.getElement(), semA.getDFA().getJobId(j));
//					for (Port ip : ji.getInputPorts()) {
//						ej.bindings.put(ip, translation.get(ji.bindings.get(ip)));
//					}
//					for (Port op : ji.getOutputPorts()) {
//						Location variable = ji.bindings.get(op);
						
						// FIXME -> id = not necessary ?! (will not compile)
//						ej.bindings.put(op, new ValuedLocation(sb.toString()));
//						translation.put(ji.bindings.get(op), (ValuedLocation) ej.bindings.get(op));

					
						
//					}
					ewf.addChild(ej);
				}
				
			}
			
		}

		return ewf;

	}
}
