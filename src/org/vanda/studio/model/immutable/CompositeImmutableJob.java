package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.util.TokenSource.Token;

public class CompositeImmutableJob<IF, F> extends ImmutableJob<F> {

	private final Linker<IF, F> linker;

	private final ImmutableWorkflow<IF> workflow;

	public CompositeImmutableJob(Linker<IF, F> linker,
			ImmutableWorkflow<IF> workflow) {
		this.linker = linker;
		this.workflow = workflow;
	}

	@Override
	public ImmutableJob<?> dereference(ListIterator<Token> address) {
		if (address.hasNext())
			return workflow.dereference(address);
		else
			return this;
	}

	@Override
	public List<ImmutableJob<F>> unfold() {
		List<ImmutableWorkflow<IF>> workflows = workflow.unfold();
		List<ImmutableJob<F>> jobs = new LinkedList<ImmutableJob<F>>();
		for (ImmutableWorkflow<IF> w : workflows)
			jobs.add(new CompositeImmutableJob<IF, F>(linker, w));
		return jobs;
	}

	public Linker<IF, F> getLinker() {
		return linker;
	}

	public ImmutableWorkflow<IF> getWorkflow() {
		return workflow;
	}

	@Override
	public boolean isChoice() {
		return false;
	}

	@Override
	public void appendText(ArrayList<Token> inputs, ArrayList<Token> outputs,
			StringBuilder lines, StringBuilder sections) {
		workflow.appendText(sections);
		appendOutput(outputs, lines);
		lines.append(" = ");
		lines.append(workflow.toString());
		appendInput(inputs, lines);
		lines.append('\n');
	}

	@Override
	public boolean isInputPort() {
		return false;
	}

	@Override
	public boolean isOutputPort() {
		return false;
	}

	@Override
	public List<Port> getInputPorts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Port> getOutputPorts() {
		// TODO Auto-generated method stub
		return null;
	}

}
