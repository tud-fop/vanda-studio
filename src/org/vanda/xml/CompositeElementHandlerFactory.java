package org.vanda.xml;

import java.util.HashMap;
import java.util.Map;


public final class CompositeElementHandlerFactory<Builder> implements
		ElementHandlerFactory<Builder> {

	private final Map<String, SingleElementHandlerFactory<? super Builder>> map;

	public CompositeElementHandlerFactory(SingleElementHandlerFactory<? super Builder>... sjehfs) {
		map = new HashMap<String, SingleElementHandlerFactory<? super Builder>>();
		if (sjehfs != null)
			addHandlers(sjehfs);
	}

	public void addHandler(SingleElementHandlerFactory<? super Builder> sjehf) {
		map.put(sjehf.getTag(), sjehf);
	}

	public void addHandlers(SingleElementHandlerFactory<? super Builder>... sjehfs) {
		for (SingleElementHandlerFactory<? super Builder> sjehf : sjehfs)
			map.put(sjehf.getTag(), sjehf);
	}

	@Override
	public ElementHandler create(String tag, Parser<?> p, Builder jb) {
		ElementHandlerFactory<? super Builder> eahf = map.get(tag);
		if (eahf != null)
			return eahf.create(tag, p, jb);
		else
			return null;
	}
}