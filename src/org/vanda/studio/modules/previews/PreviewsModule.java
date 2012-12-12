package org.vanda.studio.modules.previews;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.model.types.CompositeType;

public class PreviewsModule implements Module {

	@Override
	public String getName() {
		return "Preview Module";
	}

	@Override
	public Object createInstance(Application app) {
		PreviewFactory pf = new BerkeleyTreePreviewFactory(app);
		app.registerPreviewFactory(new CompositeType("Penn Tree Corpus"), pf);
		PreviewFactory pf2 = new BerkeleyGrammarPreviewFactory();
		app.registerPreviewFactory(new CompositeType("BerkeleyGrammar.sm6"), pf2);
		// PreviewFactory pf3 = new MonospacePreviewFactory();
		// a.registerPreviewFactory(new CompositeType("Dictionary"), pf3);
		// a.registerPreviewFactory(new CompositeType("Alignments"), pf3);
		app.registerPreviewFactory(null, new MonospacePreviewFactory());
		return null;
	}

}
