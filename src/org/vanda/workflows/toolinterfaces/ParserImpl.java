package org.vanda.workflows.toolinterfaces;

import java.io.File;
import java.io.FileNotFoundException;

import org.vanda.util.Observer;
import org.vanda.workflows.elements.ToolInterface;

public class ParserImpl implements Parser {
	
	private ToolBuilder tb;
	private ToolInterfaceBuilder tib;
	private final Observer<ToolInterface> o;

	public ParserImpl(Observer<ToolInterface> o) {
		this.o = o;
		tb = new ToolBuilder();
		tib = new ToolInterfaceBuilder();
	}
	
	public void init(File file) throws FileNotFoundException {
		tb.reset();
		tib.reset();
	}
	
	public void done() {
		
	}
	
	public void process() {
		o.notify(null);
	}

	@Override
	public ToolBuilder getToolBuilder() {
		return tb;
	}

	@Override
	public ToolInterfaceBuilder getToolInterfaceBuilder() {
		return tib;
	}

	@Override
	public void buildTool() {
		tib.tools.add(tb);
		tb = new ToolBuilder();
	}

	@Override
	public void buildToolInterface() {
		ToolInterface ti = tib.build();
		o.notify(ti);
		tib.reset();
		tb.reset();
	}
	
}
