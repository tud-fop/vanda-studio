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
	cabal configure
	cabal install --force-reinstalls
	cabal exec -- ghc --make "programs/XRSToHypergraph.hs"
	cabal exec -- ghc --make "programs/XRSTranslate.hs"
	cabal exec -- ghc --make "programs/PennToSentenceCorpus.hs"
}
