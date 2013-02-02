package org.vanda.workflows.toolinterfaces;

import java.util.LinkedList;
import java.util.List;

import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;

public class ToolInterfaceBuilder extends RepositoryItemBuilder {

	public final LinkedList<ToolBuilder> tools;

	public ToolInterfaceBuilder() {
		tools = new LinkedList<ToolBuilder>();
	}

	public List<StaticTool> build() {
		LinkedList<StaticTool> result = new LinkedList<StaticTool>();
		StaticToolInterface ti = new StaticToolInterface(id, name, description.toString(), version, category, contact);
		for (ToolBuilder tb : tools) {
			tb.ti = ti;
			result.add(tb.build());
		}
		return result;
	}

	public static Factory<ToolInterfaceBuilder> createFactory() {
		return new Fäctory();
	}

	public static final class Fäctory implements Factory<ToolInterfaceBuilder> {
		@Override
		public ToolInterfaceBuilder create() {
			return new ToolInterfaceBuilder();
		}
	}

	@SuppressWarnings("unchecked")
	public static FieldProcessor<ToolInterfaceBuilder> createProcessor() {
		return new CompositeFieldProcessor<ToolInterfaceBuilder>(new NameProcessor(), new IdProcessor(),
				new VersionProcessor(), new CategoryProcessor(), new ContactProcessor());
	}

}
