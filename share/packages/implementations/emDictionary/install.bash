id="emDictionary"
varname="EMDICTIONARY"
version="2013-03-05"
binpath="$id"

install_me () {
	ghc --make -o "$1/toParallelCorpus" toParallelCorpus/toParallelCorpus.hs -main-is Main.main
	cd EMDictionary
	ghc --make Algorithms/EMDictionary.hs -O -fforce-recomp -main-is Algorithms.EMDictionary
	cp Algorithms/EMDictionary "$1/."
	cd ..
}