package org.vanda.studio.modules.workflows.gui;

import java.io.File;
import java.util.List;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Action;
import org.vanda.studio.model.Port;
import org.vanda.studio.model.RendererSelection;
import org.vanda.studio.model.ToolInstance;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.SimpleToolInstance;
import org.vanda.studio.modules.workflows.NestedHyperworkflow;

public class VWorkflowImpl implements VWorkflow {

	protected static final Port[] ports = new Port[0];

	ModuleInstance<VWorkflow> mod;
	File file;
	String author;
	String category;
	String date;
	String description;
	String id;
	String name;

	public VWorkflowImpl(ModuleInstance<VWorkflow> mod, File file) {
		this.mod = mod;
		this.file = file;
		// TODO: retrieve metadata
		id = toString();
		name = "test";
		description = "test";
		date = "test";
		category = "Workflows";
		author = "afischer";
	}

	@Override
	public NestedHyperworkflow load(Application app) {
		// open file as a ByteStream and construct the Term
		// TODO

		NestedHyperworkflow nhwf = null;
		if (file != null && file.exists()) {
			nhwf = NestedHyperworkflow.load(file.getPath(), app);
		}
		if (nhwf == null)
			nhwf = new NestedHyperworkflow("root");

		return nhwf;
	}

	@Override
	public void save(NestedHyperworkflow t) {
		// open file as a ByteStream and so on
		// TODO
		// broadcast that this object has been modified
		mod.getModifyObserver().notify(VWorkflowImpl.this);
	}

	@Override
	public void appendActions(List<Action> as) {
		as.add(new Action() {
			@Override
			public String getName() {
				return "Edit";
			}

			@Override
			public void invoke() {
				mod.openEditor(VWorkflowImpl.this);
			}
		});
	}

	@Override
	public ToolInstance createInstance() {
		return new SimpleToolInstance();
	}

	@Override
	public String getAuthor() {
		return author;
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public String getDate() {
		return date;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Port[] getInputPorts() {
		return ports;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Port[] getOutputPorts() {
		return ports;
	}

	@Override
	public void selectRenderer(RendererSelection rs) {
		rs.selectTermRenderer();
	}
}