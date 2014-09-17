id="vanda"
varname="VANDA"
version="2014-06-10"
binpath="$id"

VANDA="ssh://tdenk@tcs.inf.tu-dresden.de/~tdenk/public_git/vanda"
VERSION="de1e727644262884d4f1a8995bd845ac78b80d1f"

install_me () {
	if [[ ! -d "$1/.git" ]]
	then
		git clone $VANDA "$1"
		cd "$1"
	else
		cd "$1"
		git fetch origin master
	fi
	git checkout "$VERSION"
	runhaskell "tools/Setup.hs" configure --user
	runhaskell "tools/Setup.hs" build
	ghc -package-db dist/package.conf.inplace --make "programs/XRSToHypergraph.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/XRSTranslate.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/XRSNGrams.hs"
#	ghc -package-db dist/package.conf.inplace --make "programs/NGrams.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/PennToSentenceCorpus.hs"
}
