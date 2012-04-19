/**
 * 
 */
package org.vanda.studio.app;


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
