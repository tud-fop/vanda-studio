/**
 * 
 */
package org.vanda.studio.modules.workflows;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Tool;
import org.vanda.studio.modules.workflows.NestedHyperworkflow;

public interface VWorkflow extends Tool {

	NestedHyperworkflow load(Application app);

	void save(NestedHyperworkflow t);

}
