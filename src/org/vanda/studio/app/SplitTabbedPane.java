package org.vanda.studio.app;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

// TODO remove this nasty dependency...
import org.vanda.studio.modules.workflows.impl.DefaultWorkflowEditorImpl;

@SuppressWarnings("serial")
/**
 * A JTabbedPane where the inner components are chains of JSplitPanes.
 * The advantage of this implementation is that it provides methods for acting
 * on the "core" component instead of the outermost JSplitPane.
 * 
 * It also allows for easy insertion of splits in the lowest / nearest-to-core layer
 * and removal of components from everywhere in the layer.
 * 
 * @author mielke
 */
public class SplitTabbedPane extends JTabbedPane {

	public enum Side {NORTH, EAST, SOUTH, WEST};
	
	/**
	 * Simple struct to store all side-panes with their position and size
	 */
	public static class SideSplitPane {
		public JComponent component;

		/** the side at which the inserted pane should be */
		public Side side;
		
		/** its initial width/height in pixels */
		public int size;
		
		public SideSplitPane(JComponent c, Side sd, int sz) {
			component = c;
			side = sd;
			size = sz;
		}
	}
	
	
	/* TODO
	 * Idea: store the cores alongside the components!
	 *       This will make "lookup" trivial
	 *       ...and eliminate the disgusting hardcoded GraphComponent!
	 */

	/**
	 * Retrieves the core component in the currently selected tab
	 * @see getSelectedComponent
	 */
	public Component getSelectedCoreComponent() {
		if(getSelectedComponent() == null)
			return null;
		else
			return getCoreComponentAt(getSelectedIndex());
	}
	
	/**
	 * Selects the tab where `c` is the core component
	 * @see setSelectedComponent
	 */
	public void setSelectedCoreComponent(JComponent c) {
		setSelectedIndex(indexOfCoreComponent(c));
	}

	/**
	 * Returns the (tab) index of the component `c` 
	 * @see indexOfComponent
	 */
	public int indexOfCoreComponent(JComponent c) {
		// The easy case: the core we searched for is on the top level
		int rootIndex = indexOfComponent(c);
		if(rootIndex >= 0)
			return rootIndex;
		
		// The hard case: we have to traverse each tab until we reach the core
		for(int i = getTabCount() - 1; i >= 0; --i)
			if (getCoreComponentAt(i) == c)
				return i;
		
		// No luck at all?
		return -1;
	}

	/**
	 * Returns the innermost split that immediately contains the core component
	 * or `null` iff the root component is the core component
	 */
	private JSplitPane getInnermostSplitPane(int i) {
		Component current, left, right;
		boolean isLeftSplit, isRightSplit;
		current = getComponentAt(i);
		
		// Maybe we haven't split yet!
		if(!(current instanceof JSplitPane))
			return null;
		
		while(true) {
			left = ((JSplitPane) current).getLeftComponent();
			right = ((JSplitPane) current).getRightComponent();
			isLeftSplit = left instanceof JSplitPane;
			isRightSplit = right instanceof JSplitPane;
			
			if(isLeftSplit && !isRightSplit)
				current = left;
			else if(!isLeftSplit && isRightSplit)
				current = right;
			else if(!isLeftSplit && !isRightSplit) {
				return (JSplitPane) current;
			} else
				throw new RuntimeException("Non-chain splitting present in a SplitTabbedPane!");
		}
	}
	
	/**
	 * Gets the core component at the (tab) index `i`
	 * (using `getInnermostSplitPane`)
	 * @see getComponentAt
	 */
	private Component getCoreComponentAt(int i) {
		JSplitPane innermostSplit = getInnermostSplitPane(i);

		// Non-split case
		if(innermostSplit == null)
			return getComponentAt(i);
		
		if (innermostSplit.getLeftComponent() instanceof DefaultWorkflowEditorImpl.MyMxGraphComponent) // TODO change type visibility back to protected!!!
			return innermostSplit.getLeftComponent();
		else
			return innermostSplit.getRightComponent();
	}
	
	/**
	 * Replaces the core component in a tab with a JSplitPane
	 * containing the core and the pane `p` we want to insert.
	 * @param i index of the tab in which to insert
	 * @param p pane definition containing side, size and the component to insert
	 */
	public void addSplitAt(int i, SideSplitPane p) {
		if(p.component instanceof JSplitPane)
			throw new RuntimeException();
		
		JSplitPane innermostSplit = getInnermostSplitPane(i);
		Component coreComponent = getCoreComponentAt(i);
		
		// Will be read only after being properly initialized, but java doesn't get that
		boolean isLeft = false;
		
		// If we already have some split, release core from it, because:
		// "Swing UIs are hierarchical and you can only add a component to the hierarchy once.
		// If you add a component to more than one container you'll get unpredictable results."
		// (http://stackoverflow.com/questions/1113799/whats-wrong-with-jsplitpanel-or-jtabbedpane)
		if(innermostSplit != null) {
			isLeft = innermostSplit.getLeftComponent() == coreComponent;
			
			if(isLeft)
				innermostSplit.setLeftComponent(null);
			else
				innermostSplit.setRightComponent(null);
		} else { // First split, but release core as well
			setComponentAt(i, null);
		}
		
		final JSplitPane newSplitPane;
		
		switch (p.side) {
			case NORTH:
				newSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
					p.component, coreComponent);
				break;
			case EAST:
				newSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
					coreComponent, p.component);
				break;
			case SOUTH:
				newSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
					coreComponent, p.component);
				break;
			case WEST:
				newSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
					p.component, coreComponent);
				break;
			default:
				newSplitPane = null; // can't happen, but java
		}
		
		// We already had a split to join into
		if(innermostSplit != null) {
			if(isLeft)
				innermostSplit.setLeftComponent(newSplitPane);
			else
				innermostSplit.setRightComponent(newSplitPane);
		} else { // This is the first split
			setComponentAt(i, newSplitPane);
		}

		// Now size it properly
		newSplitPane.setOneTouchExpandable(true);
		switch (p.side) {
			case NORTH:
				newSplitPane.setDividerLocation(p.size);
				newSplitPane.setResizeWeight(0.0);
				break;
			case EAST:
				// We could add a ComponentListener to the newSplitPane
				// and on resize use newSpliPane.getWidth(), but I prefer this
				// solution with technically wrong / guessed sizes, but less nasty overhead.
				// EDIT: actually, this is all wrong, because deeply nested panes obviously
				//       cannot grow as big as the whole SplitTabbedPane!
				//       So this is a FIXME.
				newSplitPane.setDividerLocation(this.getWidth() - 18 - p.size);
				newSplitPane.setResizeWeight(1.0);
				break;
			case SOUTH:
				// (Same thing here)
				newSplitPane.setDividerLocation(this.getHeight() - this.getTabRunCount() * 34 - p.size);
				newSplitPane.setResizeWeight(1.0);
				break;
			case WEST:
				newSplitPane.setDividerLocation(p.size);
				newSplitPane.setResizeWeight(0.0);
				break;
		}
	}

	/**
	 * Replaces the JSplitPane containing the component `c` that we want to remove
	 * and another component `c'` by just that component `c'`.
	 * @param i index of the tab in which to remove a pane
	 * @param c the component of the pane that is to be removed
	 */
	public void removeSplitAt(int i, JComponent c) {
		Component current, left, right; 
		boolean isLeftSplit, isRightSplit;
		current = getComponentAt(i);
		
		// Maybe we haven't split yet!
		if(!(current instanceof JSplitPane))
			throw new RuntimeException("Cannot remove SidePane with component "+c.toString());
		
		JSplitPane previous = null;
		boolean didIGoLeft = false; // is overwritten before first use alongside `previous`
		
		while(true) {
			left = ((JSplitPane) current).getLeftComponent();
			right = ((JSplitPane) current).getRightComponent();
			isLeftSplit = left instanceof JSplitPane;
			isRightSplit = right instanceof JSplitPane;
			
			if(isLeftSplit && !isRightSplit) {
				if(right == c) {
					if(previous == null)
						setComponentAt(i, left);
					return;
				}
				previous = (JSplitPane) current;
				current = left;
				didIGoLeft = true;
			} else if(!isLeftSplit && isRightSplit) {
				if(left == c) {
					if(previous == null)
						setComponentAt(i, right);
					return;
				}
				previous = (JSplitPane) current;
				current = right;
				didIGoLeft = false;
			} else if(!isLeftSplit && !isRightSplit) {
				// Check if its just one SplitPane
				if(previous == null)
					if(left == c) {
						setComponentAt(i, right);
						return;
					}
					else if (right == c) {
						setComponentAt(i, left);
						return;
					}
				
				// Otherwise use the predecessor
				if(didIGoLeft)
					if(left == c) {
						previous.setLeftComponent(right);
						return;
					}
					else if (right == c) {
						previous.setLeftComponent(left);
						return;
					}
				else
					if(left == c) {
						previous.setRightComponent(right);
						return;
					}
					else if (right == c) {
						previous.setRightComponent(left);
						return;
					}
				break;
			} else
				throw new RuntimeException("Non-chain splitting present in a SplitTabbedPane!");
		}

		throw new RuntimeException("Cannot remove SidePane with component "+c.toString());
	}
}