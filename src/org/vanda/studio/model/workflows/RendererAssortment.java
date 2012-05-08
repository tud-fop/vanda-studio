package org.vanda.studio.model.workflows;

/**
 * This approach allows having the renderer completely oblivious of the possible
 * Tool descendants, which might even be scattered over several modules.
 */

public interface RendererAssortment<R> {

	R selectAlgorithmRenderer();

	R selectCorpusRenderer();

	R selectGrammarRenderer();

	R selectOrRenderer();

	R selectSinkRenderer();

	R selectTermRenderer();

	R selectTextRenderer();

}
