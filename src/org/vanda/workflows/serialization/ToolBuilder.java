package org.vanda.workflows.serialization;

import org.vanda.util.Repository;
import org.vanda.workflows.elements.Tool;
import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class ToolBuilder {
	
	public String id;
	
	public Tool build(Repository<Tool> tr) {
		return tr.getItem(id);
	}
	
	public static Factory<ToolBuilder> createFactory() {
		return new Fäctory();
	}
	
	@SuppressWarnings("unchecked")
	public static FieldProcessor<ToolBuilder> createProcessor() {
		return new CompositeFieldProcessor<ToolBuilder>(new IdProcessor());
	}
	
	public static final class Fäctory implements Factory<ToolBuilder> {
		@Override
		public ToolBuilder create() {
			return new ToolBuilder();
		}
	}

	public static final class IdProcessor implements SingleFieldProcessor<ToolBuilder> {
		@Override
		public String getFieldName() {
			return "id";
		}
		
		@Override
		public void process(String name, String value, ToolBuilder b) {
			b.id = value;
		}
	}
}
