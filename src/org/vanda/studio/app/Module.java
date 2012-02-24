/**
 * 
 */
package org.vanda.studio.app;

import java.io.File;

/**
 * 
 * 
 * @author buechse
 * 
 */
public interface Module {
	
	String getName();
	
	/**
	 * Resource acquisition is initialization
	 * don't forget to implement finalize
	 */
	Object createInstance(Application a);

}
