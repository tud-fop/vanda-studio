package org.vanda.util;


public interface MetaRepository<T> {

	<T1 extends T> void addRepository(Repository<T1> r);
	
	Observable<T> getAddObservable();
	
	Observable<T> getModifyObservable();
	
	Repository<T> getRepository();

	Observable<T> getRemoveObservable();

	Observable<Repository<? extends T>> getRepositoryAddObservable();

	Observable<Repository<? extends T>> getRepositoryRemoveObservable();

	<T1 extends T> void removeRepository(Repository<T1> r);

}
