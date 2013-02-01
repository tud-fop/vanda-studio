package org.vanda.xml;

import java.util.HashMap;

public class CompositeFieldProcessor<Builder> implements
		FieldProcessor<Builder> {

	private final HashMap<String, SingleFieldProcessor<Builder>> map;

	public CompositeFieldProcessor(SingleFieldProcessor<Builder>[] fps) {
		map = new HashMap<String, SingleFieldProcessor<Builder>>();
		for (SingleFieldProcessor<Builder> fp : fps)
			map.put(fp.getFieldName(), fp);
	}

	@Override
	public void process(String name, String value, Builder b) {
		FieldProcessor<Builder> fp = map.get(name);
		if (fp != null)
			fp.process(name, value, b);
	}

}