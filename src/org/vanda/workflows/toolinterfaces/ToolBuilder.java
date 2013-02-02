package org.vanda.workflows.toolinterfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.util.TokenSource;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.ToolInterface;
import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class ToolBuilder extends RepositoryItemBuilder {
	public RendererSelector rs;
	public List<Port> inPorts;
	public List<Port> outPorts;
	public Type fragmentType;
	public String status;
	public ToolInterface ti;
	public TokenSource ts;
	public Map<String, Type> tVars;

	public ToolBuilder() {
		rs = RendererSelectors.selectors[0];
		fragmentType = null;
		status = "";
		inPorts = new ArrayList<Port>();
		outPorts = new ArrayList<Port>();
		ts = new TokenSource();
		tVars = new HashMap<String, Type>();
	}

	public StaticTool build() {
		return new StaticTool(id, name, category, version, contact, description.toString(), inPorts, outPorts, rs,
				fragmentType, status, ti);
	}

	public static Factory<ToolBuilder> createFactory() {
		return new Fäctory();
	}

	@SuppressWarnings("unchecked")
	public static FieldProcessor<ToolBuilder> createProcessor() {
		return new CompositeFieldProcessor<ToolBuilder>(new NameProcessor(), new IdProcessor(), new VersionProcessor(),
				new CategoryProcessor(), new ContactProcessor(), new FragmentTypeProcessor(), new StatusProcessor());
	}

	public static final class Fäctory implements Factory<ToolBuilder> {
		@Override
		public ToolBuilder create() {
			return new ToolBuilder();
		}
	}

	private static final class FragmentTypeProcessor implements SingleFieldProcessor<ToolBuilder> {
		@Override
		public String getFieldName() {
			return "type";
		}

		@Override
		public void process(String name, String value, ToolBuilder b) {
			b.fragmentType = Types.parseType(null, null, value);
		}
	}

	private static final class StatusProcessor implements SingleFieldProcessor<ToolBuilder> {
		@Override
		public String getFieldName() {
			return "status";
		}

		@Override
		public void process(String name, String value, ToolBuilder b) {
			b.status = value;
		}
	}

}
