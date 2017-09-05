id="vanda"
varname="VANDA"
version="2017-08-30"
binpath="$id"

VANDA="https://gitlab.tcs.inf.tu-dresden.de/ruprecht/vanda.git"

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
	cabal install --only-dependencies --force-reinstalls
	cabal configure
	cabal build
	cabal exec -- ghc -package-db dist/package.conf.inplace --make "programs/XRSToHypergraph.hs"
	cabal exec -- ghc -package-db dist/package.conf.inplace --make "programs/XRSTranslate.hs"
	cabal exec -- ghc -package-db dist/package.conf.inplace --make "programs/PennToSentenceCorpus.hs"
}
