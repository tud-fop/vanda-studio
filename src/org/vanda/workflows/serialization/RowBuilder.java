package org.vanda.workflows.serialization;

import java.util.HashMap;

import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class RowBuilder {
	
	HashMap<String, String> assignment;
	String name;

	public RowBuilder() {
		assignment = new HashMap<String, String>();
	}

	public static Factory<RowBuilder> createFactory() {
		return new Fäctory();
	}

	public static final class Fäctory implements Factory<RowBuilder> {
		@Override
		public RowBuilder create() {
			return new RowBuilder();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static FieldProcessor<RowBuilder> createProcessor() {
		return new CompositeFieldProcessor<RowBuilder>(new NameProcessor());
	} 
	
	static final class NameProcessor implements SingleFieldProcessor<RowBuilder> {

		@Override
		public void process(String name, String value, RowBuilder b) {
			b.name = value;
		}

		@Override
		public String getFieldName() {
			return "name";
		}
		
	}
	
}
