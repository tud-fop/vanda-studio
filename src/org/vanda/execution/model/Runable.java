package org.vanda.execution.model;

import org.vanda.execution.model.Runables.RunState;

/**
 * represents a thing or a group of things which is executed in a bash script
 * 
 * @author kgebhardt
 *
 */
public interface Runable {

	public void doCancel();

	public void doFinish();

	public void doRun();

//	/**
//	 * id used in the bash skript's status messages
//	 * @return
//	 */
//	public String getID();

	public RunState getState();
}
