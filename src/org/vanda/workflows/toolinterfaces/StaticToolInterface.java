package org.vanda.workflows.toolinterfaces;

import org.vanda.util.ListRepository;
import org.vanda.util.Repository;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.elements.ToolInterface;

public class StaticToolInterface implements ToolInterface {
	private final String id;
	private final String name;
	private final String description;
	private final String version;
	private final String category;
	private final String contact;
	private final ListRepository<Tool> repository;

	public StaticToolInterface(String id, String name, String description, String version, String category, String contact) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.version = version;
		this.category = category;
		this.contact = contact;
		repository = new ListRepository<Tool>();
	}
	
	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public String getContact() {
		return contact;
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
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public Repository<? extends Tool> getTools() {
		return repository;
	}
	
	public ListRepository<Tool> getRepository() {
		return repository;
	}

}
