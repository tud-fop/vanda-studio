package org.vanda.execution.model;

import org.vanda.execution.model.Runables.RunEvent;
import org.vanda.execution.model.Runables.RunState;
import org.vanda.util.MultiplexObserver;

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

	/**
	 * id used in the bash skript's status messages
	 * @return
	 */
	public String getID();

	public RunState getState();
	
	public void registerRunEventListener(
			MultiplexObserver<RunEvent> observable);
}
