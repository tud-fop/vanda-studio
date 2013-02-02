package org.vanda.xml;

import java.util.HashMap;

public class CompositeFieldProcessor<Builder> implements
		FieldProcessor<Builder> {

	private final HashMap<String, SingleFieldProcessor<? super Builder>> map;

	public CompositeFieldProcessor(SingleFieldProcessor<? super Builder>... fps) {
		map = new HashMap<String, SingleFieldProcessor<? super Builder>>();
		for (SingleFieldProcessor<? super Builder> fp : fps)
			map.put(fp.getFieldName(), fp);
	}

	@Override
	public void process(String name, String value, Builder b) {
		FieldProcessor<? super Builder> fp = map.get(name);
		if (fp != null)
			fp.process(name, value, b);
	}

}