package org.vanda.execution.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.vanda.fragment.model.SemanticAnalysis;
import org.vanda.fragment.model.SyntaxAnalysis;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.DataflowAnalysis;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.ElementAdapter;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.LiteralAdapter;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

/**
 * @author kgebhardt
 *
 */
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

	/**
	 * Creates a copy of a given job and adds it to the executable workflow
	 * 
	 * @param ewf
	 *            executable workflow
	 * @param semA
	 *            semantic analysis for setting jobIDs and creating literals
	 *            that yield values
	 * @param dx
	 *            x-shift
	 * @param dy
	 *            y-shift
	 * @param translation
	 * @param j
	 *            original Job
	 * @param element
	 *            Element -> new Literal Adapter or ToolAdapter
	 */
	private static void createJobInstance(MutableWorkflow ewf, DataflowAnalysis dfa, double dx, double dy,
			Map<Location, Location> translation, Job j, ElementAdapter element) {
		Job ej = new Job(element, dfa.getJobId(j));
		ej.setDimensions(new double[] { j.getX() + dx, j.getY() + dy, j.getWidth(), j.getHeight() });
		ewf.addChild(ej);

		// Build Variable-Translation Table
		for (Port op : j.getOutputPorts()) {
			translation.put(j.bindings.get(op), ej.bindings.get(op));
		}

		// Establish Connections
		for (Port ip : j.getInputPorts()) {
			ewf.addConnection(new ConnectionKey(ej, ip), translation.get(j.bindings.get(ip)));
		}
	}

	public static MutableWorkflow generateExecutableWorkflow(MutableWorkflow mwf, Database db,
			List<Integer> assignmentSelection, SyntaxAnalysis synA, SemanticAnalysis semA) {
		final MutableWorkflow ewf = new MutableWorkflow(mwf.getName());
		final double[] dims = workflowDimension(mwf);
		int counter = 0;
		for (int i : assignmentSelection) {
			// TODO skip unselected Assignments

			final Map<Location, Location> translation = new HashMap<Location, Location>();
			final HashMap<String, String> dbRow = db.getRow(i);
			final DataflowAnalysis dfa = semA.getDFA(synA, i);
			
			// vertical alignment of instantiated workflows
			final double dx = 0;
			final double dy = dims[1] * counter;

			for (final Job j : synA.getSorted()) {
				if (j.isConnected()) {
					j.visit(new ElementVisitor() {
						@Override
						public void visitLiteral(Literal lit) {
							createJobInstance(
									ewf,
									dfa,
									dx,
									dy,
									translation,
									j,
									new LiteralAdapter(new Literal(lit.getType(), lit.getName(),
											dbRow.get(lit.getKey()))));
						}

						@Override
						public void visitTool(Tool t) {
							createJobInstance(ewf, dfa, dx, dy, translation, j, j.getElement());
						}

					});
				} else {
					// FIXME will this case occur ?
					createJobInstance(ewf, dfa, dx, dy, translation, j, j.getElement());
				}

			}
			counter++;
		}

		return ewf;

	}
}
