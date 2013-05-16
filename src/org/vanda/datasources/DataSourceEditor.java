package org.vanda.datasources;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import org.vanda.util.Action;

public abstract class DataSourceEditor {
	
	private Set<Action> writeActions = new HashSet<Action>();
	
	public abstract JComponent getComponent();
	
	public abstract DataSource getDataSource();
	
	public abstract void write();
	
	public void writeChange(){
		write();
		for (Action a : writeActions)
			a.invoke();
	}
	
	public void addWriteAction(Action storeAction){
		writeActions.add(storeAction);
	}
}
