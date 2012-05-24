package org.vanda.studio.modules.common;

import java.util.List;

import org.vanda.studio.app.Repository;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.types.Type;

public class LinkerUtil {

	public static Tool getConversionTool(Repository<Tool> r, Type from, Type to) {
		for (Tool t : r.getItems()) {
			if (t.getInputPorts().get(0).equals(from)
					&& t.getOutputPorts().get(0).equals(to))
				return t; // -------------------- ##########
		}
		return null;
	}

	public static boolean checkTypes(Repository<Tool> r, List<Type> from,
			List<Type> to) {
		if (from.size() != to.size())
			return false;
		int i = from.size();
		while (i > 0
				&& getConversionTool(r, from.get(i - 1), to.get(i - 1)) != null)
			i--;
		return i == 0;
	}

}
