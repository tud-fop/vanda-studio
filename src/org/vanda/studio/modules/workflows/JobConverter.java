package org.vanda.studio.modules.workflows;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class JobConverter implements Converter {

	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		
		Job job = (Job) value;
		writer.startNode("job");
		
		//TODO insert XML representation here
		
		writer.endNode();
		
		System.out.println("marshalling " + value);
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		
		Job job = null;
		
		return job;
	}

	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(Job.class);
	}

}
