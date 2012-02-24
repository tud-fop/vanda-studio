package org.vanda.studio.model;


/**
 * This approach allows having the renderer completely oblivious of the
 * possible VObject descendants, which might even be scattered over
 * several modules.
 */

public interface RendererSelection {
	
	void selectAlgorithmRenderer();

	void selectCorpusRenderer();
	
	void selectGrammarRenderer();
	
	void selectSinkRenderer();
	
	void selectTermRenderer();

	void selectTextRenderer();
	
}
