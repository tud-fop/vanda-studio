/**
 * 
 */
package org.vanda.studio.modules.workflows.gui;

import org.vanda.studio.model.VObject;
import org.vanda.studio.modules.workflows.NestedHyperworkflow;

public interface VWorkflow extends VObject {

	NestedHyperworkflow load();

	void save(NestedHyperworkflow t);

}
