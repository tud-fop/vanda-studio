package org.vanda.workflows.elements;

import org.vanda.types.Type;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.workflows.elements.Elements.*;

public final class Literal {

	private final Integer key;
	private String name;
	private Type type;
	private final MultiplexObserver<ElementEvent<Literal>> observable;
	
	public Literal(Type type, String name, Integer key) {
		this.type = type;
		this.name = name;
		if (key != null)
			this.key = key;
		else
			this.key = hashCode();
		observable = new MultiplexObserver<ElementEvent<Literal>>();
	}
	
	public int getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public Observable<ElementEvent<Literal>> getObservable() {
		return observable;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setName(String name) {
		if (!name.equals(this.name)) {
			this.name = name;
			observable.notify(new ValueChangeEvent<Literal>(this));
		}
	}
	
	public void setType(Type type) {
		if (!type.equals(this.type)) {
			this.type = type;
			observable.notify(new TypeChangeEvent<Literal>(this));
		}
	}
	
}
