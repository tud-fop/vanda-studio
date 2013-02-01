package org.vanda.xml;

public interface FieldProcessor<Builder> {
	void process(String name, String value, Builder b);
}