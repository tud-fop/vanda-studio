package org.vanda.studio.modules.workflows;

import org.vanda.studio.app.Application;
import org.vanda.studio.modules.common.SimpleToolInstance;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class JobConverter implements Converter {

	Application app;
	
	public JobConverter(Application app) {
		this.app = app;
	}
	
	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		
		Job job = (Job) value;
		
		// save object id
		writer.startNode("object");
		writer.setValue(job.object.getId());
		writer.endNode();
		
		// save job id
		writer.startNode("id");
		writer.setValue(job.getId());
		writer.endNode();
		
		// save dimensions of job
		writer.startNode("position");
		writer.setValue(job.getX() 
				+ "," + job.getY() 
				+ "," + job.getWidth() 
				+ "," + job.getHeight());
		writer.endNode();
		
		// save serialized instance
		writer.startNode("instance");		
		XStream xs = new XStream();
		writer.setValue(xs.toXML(job.instance));
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		
		// access object id and retrieve item from global repository
		reader.moveDown();
		String objectID = reader.getValue();
		Job job = new Job(app.getGlobalRepository().getItem(objectID));
		reader.moveUp();
		
		// access job id and set it
		reader.moveDown();
		job.setId(reader.getValue());
		reader.moveUp();
		
		// access dimensions and write them back to deserialized job
		reader.moveDown();
		String[] array = reader.getValue().split(",");
		job.setDimensions(new double[] {Double.parseDouble(array[0]), 
				Double.parseDouble(array[1]), 
				Double.parseDouble(array[2]),
				Double.parseDouble(array[3])});
		reader.moveUp();
		
		// write job instance back from deserialized xml
		reader.moveDown();
		XStream xs = new XStream();
		Object instanceObj = xs.fromXML(reader.getValue());
		if (instanceObj instanceof SimpleToolInstance) {
			job.instance = (SimpleToolInstance) instanceObj;
		}
		reader.moveUp();

		return job;
	}

	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(Job.class);
	}

}
