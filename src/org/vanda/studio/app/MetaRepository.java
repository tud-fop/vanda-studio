package org.vanda.studio.app;

import org.vanda.studio.util.Observable;

public interface MetaRepository<T> {

	<T1 extends T> void addRepository(Repository<T1> r);

	Observable<Repository<? extends T>> getRepositoryAddObservable();

	Observable<Repository<? extends T>> getRepositoryRemoveObservable();

	<T1 extends T> void removeRepository(Repository<T1> r);

}
