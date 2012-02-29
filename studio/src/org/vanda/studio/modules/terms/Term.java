package org.vanda.studio.modules.terms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vanda.studio.model.Action;
import org.vanda.studio.model.RendererSelection;
import org.vanda.studio.model.VObject;
import org.vanda.studio.model.VObjectInstance;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;

public class Term {
	public static abstract class TermObject implements Cloneable {
		protected double[] dimensions = { 0, 0, 0, 0 };
		
		public TermObject() {
		}
		
		/**
		 * Append actions to a list. Do not forget to call super.
		 */
		public void appendActions(List<Action> as) {
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException {
			TermObject c = (TermObject)super.clone();
			// the following does NOT hold
			// assert(c.dimensions != dimensions); // I don't know!
			c.dimensions = new double[4];
			c.setDimensions(dimensions);
			return c;
		}
		
		/** { x, y, width, height } */
		public void getDimensions(double[] d) {
			assert(d.length == 4);
			System.arraycopy(dimensions, 0, d, 0, 4);
		}
		
		public final double getHeight() {
			return dimensions[3];
		}
		
		public abstract String[] getInputPorts();
	
		public abstract String getName();
	
		public abstract String[] getOutputPorts();

		public final double getWidth() {
			return dimensions[2];
		}
		
		public final double getX() {
			return dimensions[0];
		}
		
		public final double getY() {
			return dimensions[1];
		}
		
		public abstract void selectRenderer(RendererSelection rs);
		
		/** { x, y, width, height } */
		public void setDimensions(double[] d) {
			assert(d.length == 4);
			System.arraycopy(d, 0, dimensions, 0, 4);
		}
	}
	
	/* for serialization: implement a converter that converts to
	 * an id string (for the object) and a map (for the instance)
	 */
	public static class VOTermObject extends TermObject {
		
		VObject object;
		VObjectInstance instance;
		
		public VOTermObject(VObject o) {
			object = o;
			instance = object.createInstance();
		}
		
		@Override
		public void appendActions(List<Action> as) {
			instance.appendActions(as);
			object.appendActions(as);
			super.appendActions(as);
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException {
			HashMap<String,Object> m = new HashMap<String,Object>();
			instance.saveToMap(m);
			VOTermObject c = (VOTermObject)super.clone();
			c.instance = object.createInstance();
			c.instance.loadFromMap(m);
			return c;
		}
		
		@Override
		public String[] getInputPorts() {
			return object.getInputPorts();
		}
	
		@Override
		public String getName() {
			return object.getName();
		}
	
		@Override
		public String[] getOutputPorts() {
			return object.getOutputPorts();
		}

		@Override
		public void selectRenderer(RendererSelection rs) {
			object.selectRenderer(rs);
		}
	}
	
	public static class Connection {
		public TermObject source;
		public TermObject target;
		public int sourceIdx;
		public int targetIdx;
	}
	
	protected Set<TermObject> objects;
	protected Set<Connection> connections;
	protected MultiplexObserver<TermObject> addObservable;
	protected MultiplexObserver<TermObject> modifyObservable;
	protected MultiplexObserver<TermObject> removeObservable;
	protected MultiplexObserver<Connection> connectObservable;
	protected MultiplexObserver<Connection> disconnectObservable;
	
	public Term() {
		objects = new HashSet<TermObject>();
		connections = new HashSet<Connection>();
		addObservable = new MultiplexObserver<TermObject>();
		modifyObservable = new MultiplexObserver<TermObject>();
		removeObservable = new MultiplexObserver<TermObject>();
		connectObservable = new MultiplexObserver<Connection>();
		disconnectObservable = new MultiplexObserver<Connection>();
	}
	
	public void ensureAbsence(TermObject o) {
		if (objects.remove(o)) {
			removeObservable.notify(o);
		}
	}
	
	public void ensureConnected(Connection c) {
		if (!connections.add(c)) {
			connectObservable.notify(c);
		}
	}
	
	public void ensureDisconnected(Connection c) {
		if (!connections.remove(c)) {
			connectObservable.notify(c);
		}
	}
	
	public void ensurePresence(TermObject o) {
		if (!objects.add(o)) {
			addObservable.notify(o);
		}
	}
	
	public Observable<TermObject> getAddObservable() {
		return addObservable;
	}
	
	public Observable<Connection> getConnectObservable() {
		return connectObservable;
	}
	
	public Observable<Connection> getDisconnectObservable() {
		return disconnectObservable;
	}
	
	public Observable<TermObject> getModifyObservable() {
		return modifyObservable;
	}
	
	public Observable<TermObject> getRemoveObservable() {
		return removeObservable;
	}
	
	public void setDimensions(TermObject o, double[] d) {
		assert(objects.contains(o));
		
		if (d[0] != o.dimensions[0] || d[1] != o.dimensions[1] || d[2] != o.dimensions[2] || d[3] != o.dimensions[3]) {
			o.setDimensions(d);
			modifyObservable.notify(o);
		}
	}
	
}
