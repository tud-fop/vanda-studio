package org.vanda.xml;

public class StringBuilderFactory implements Factory<StringBuilder> {

	@Override
	public StringBuilder create() {
		return new StringBuilder();
	}

}
