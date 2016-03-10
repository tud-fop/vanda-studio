id="vanda"
varname="VANDA"
version="2015-07-31"
binpath="$id"

VANDA="https://gitlab.tcs.inf.tu-dresden.de/vanda/vanda.git"

install_me () {
	if [[ ! -d "$1/.git" ]]
	then
		git clone "${VANDA}" "$1"
		cd "$1"
		cabal sandbox init
	else
		cd "$1"
		git pull origin master
	fi
	cabal update
	cabal install -p --only-dependencies
	cabal configure
	cabal build
	ghc -package-db dist/package.conf.inplace --make "programs/XRSToHypergraph.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/XRSTranslate.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/PennToSentenceCorpus.hs"
}
