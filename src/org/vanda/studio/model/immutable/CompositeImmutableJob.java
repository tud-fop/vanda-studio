package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.util.TokenSource.Token;

public class CompositeImmutableJob extends ImmutableJob {

	private final Linker linker;

	private final ImmutableWorkflow workflow;

	public CompositeImmutableJob(Linker linker,
			ImmutableWorkflow workflow) {
		this.linker = linker;
		this.workflow = workflow;
	}

	@Override
	public ImmutableWorkflow dereference(ListIterator<Token> path) {
		if (path.hasNext())
			return workflow.dereference(path);
		else
			return null;
	}

	@Override
	public List<ImmutableJob> unfold() {
		List<ImmutableWorkflow> workflows = workflow.unfold();
		List<ImmutableJob> jobs = new LinkedList<ImmutableJob>();
		for (ImmutableWorkflow w : workflows)
			jobs.add(new CompositeImmutableJob(linker, w));
		return jobs;
	}

	public Linker getLinker() {
		return linker;
	}

	public ImmutableWorkflow getWorkflow() {
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
		lines.append(linker.getName());
		lines.append('[');
		lines.append(workflow.getName());
		lines.append(']');
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
