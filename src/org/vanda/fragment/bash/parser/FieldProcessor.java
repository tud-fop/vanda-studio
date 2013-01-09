package org.vanda.fragment.bash.parser;

public interface FieldProcessor {
	String getFieldName();

	void process(String line, Builder b);
}