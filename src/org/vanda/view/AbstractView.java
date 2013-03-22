package org.vanda.view;

import java.util.Observable;
/**
 * stores selection / highlighting information.
 * @author kgebhardt
 *
 */
public abstract class AbstractView extends Observable {
	boolean selected;
	boolean highlighted;
	
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		if (this.selected != selected)
		{
			this.selected = selected;
			setChanged();
			notifyObservers();
			clearChanged();
		}
	}
	
	public boolean isHighlighted() {
		return highlighted;
	}
	public void setHighlighted(boolean highlighted) {
		if (this.highlighted != highlighted) 
		{
			this.highlighted = highlighted;
			setChanged();
			notifyObservers();
		}

	}
	
}