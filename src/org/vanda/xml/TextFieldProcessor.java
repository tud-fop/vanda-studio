package org.vanda.xml;

public class TextFieldProcessor implements FieldProcessor<StringBuilder> {

	@Override
	public void process(String name, String value, StringBuilder b) {
		b.append(value);
	}

}
