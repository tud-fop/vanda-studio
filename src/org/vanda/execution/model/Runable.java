package org.vanda.execution.model;

import org.vanda.execution.model.Runables.RunState;

/**
 * represents an entity or a group of entities which is executed in a bash script
 * 
 * @author kgebhardt
 *
 */
public interface Runable {

	public void doCancel();

	public void doFinish();

	public void doRun();

	public RunState getState();

	public void updateProgress(int progress);
}
