package org.vanda.xml;

import java.util.HashMap;
import java.util.Map;


public final class CompositeElementHandlerFactory<Builder> implements
		ElementHandlerFactory<Builder> {

	private final Map<String, SingleElementHandlerFactory<Builder>> map;

	public CompositeElementHandlerFactory(SingleElementHandlerFactory<Builder>... sjehfs) {
		map = new HashMap<String, SingleElementHandlerFactory<Builder>>();
		if (sjehfs != null)
			addHandlers(sjehfs);
	}

	public void addHandler(SingleElementHandlerFactory<Builder> sjehf) {
		map.put(sjehf.getTag(), sjehf);
	}

	public void addHandlers(SingleElementHandlerFactory<Builder>... sjehfs) {
		for (SingleElementHandlerFactory<Builder> sjehf : sjehfs)
			map.put(sjehf.getTag(), sjehf);
	}

	@Override
	public ElementHandler create(String tag, Parser<?> p, Builder jb) {
		ElementHandlerFactory<Builder> eahf = map.get(tag);
		if (eahf != null)
			return eahf.create(tag, p, jb);
		else
			return null;
	}
}