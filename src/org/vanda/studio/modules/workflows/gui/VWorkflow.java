/**
 * 
 */
package org.vanda.studio.modules.workflows.gui;

import org.vanda.studio.model.Tool;
import org.vanda.studio.modules.workflows.NestedHyperworkflow;

public interface VWorkflow extends Tool {

	NestedHyperworkflow load();

	void save(NestedHyperworkflow t);

}
