package org.vanda.xml;

public abstract class SimpleFieldProcessor<Builder> implements SingleFieldProcessor<Builder> {

	private final String fieldName;
	
	public SimpleFieldProcessor(String name) {
		fieldName = name;
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}
	
	@Override
	public abstract void process(String name, String value, Builder b);

}
