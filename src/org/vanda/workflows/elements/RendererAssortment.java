package org.vanda.workflows.elements;

/**
 * This approach allows having the renderer completely oblivious of the possible
 * Tool descendants, which might even be scattered over several modules.
 */

public interface RendererAssortment<R> {

	R selectAlgorithmRenderer();
	
	R selectBoxRenderer();

	R selectCorpusRenderer();

	R selectGrammarRenderer();
	
	R selectLiteralRenderer();
	
	R selectInputPortRenderer();
	
	R selectOutputPortRenderer();

	R selectOrRenderer();

	R selectSinkRenderer();

	R selectWorkflowRenderer();

}
