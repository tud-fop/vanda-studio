package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.util.TokenSource.Token;

public final class CompositeImmutableJob extends ImmutableJob {

	private final Linker linker;
	private final ImmutableWorkflow workflow;

	public CompositeImmutableJob(Token address, Linker linker,
			ImmutableWorkflow workflow) {
		super(address);
		this.linker = linker;
		this.workflow = workflow;
	}

	@Override
	public ImmutableWorkflow dereference(ListIterator<Token> path) {
		return workflow.dereference(path);
	}

	@Override
	public List<ImmutableJob> unfold() {
		List<ImmutableWorkflow> workflows = workflow.unfold();
		List<ImmutableJob> jobs = new LinkedList<ImmutableJob>();
		for (ImmutableWorkflow w : workflows)
			jobs.add(new CompositeImmutableJob(address, linker, w));
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
		lines.append("  ");
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
		return linker.convertInputPorts(workflow.getInputPorts());
	}

	@Override
	public List<Port> getOutputPorts() {
		return linker.convertOutputPorts(workflow.getOutputPorts());
	}

	@Override
	public Type getFragmentType() {
		return linker.getFragmentType();
	}

	@Override
	public void addFragmentTypeEquation(TypeChecker tc) {
		tc.addLinkerEquation(linker.getFragmentType(),
				linker.getInnerFragmentType(), workflow.getFragmentType());
	}

	@Override
	public void typeCheck() throws Exception {
		workflow.typeCheck();
		assert (workflow.getFragmentType() != null);
	}

}
