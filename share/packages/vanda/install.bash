id="vanda"
varname="VANDA"
version="2013-10-07"
binpath="$id"

VANDA="ssh://tdenk@tcs.inf.tu-dresden.de/~buechse/public_git/vanda -b hyperedge"
VERSION="06d3f99841c65db85135d2bfd93449c527ce6437"

install_me () {
	if [[ ! -d "$1" ]]
	then
		git clone $VANDA "$1"
		cd "$1"
	else
		cd "$1"
		git fetch origin hyperedge
	fi
	git checkout "$VERSION"
	runhaskell "tools/Setup.hs" configure --user
	runhaskell "tools/Setup.hs" build
	ghc -package-db dist/package.conf.inplace --make "programs/XRSToHypergraph.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/XRSTranslate.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/XRSNGrams.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/NGrams.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/PennToSentenceCorpus.hs"
}
