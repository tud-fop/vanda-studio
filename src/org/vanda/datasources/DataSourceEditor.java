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
		//System.out.println("start writing in "+this.getClass().getCanonicalName());
		for (Action a : writeActions) {
			//System.out.println("  "+a.getName());
			a.invoke();
		}
		//System.out.println("done writing");
	}
	
	public void addWriteAction(Action storeAction){
		writeActions.add(storeAction);
	}

	public boolean wasChanged() {
		return false;
	}
}
