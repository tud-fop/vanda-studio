id="vanda"
varname="VANDA"
version="2015-04-30"
binpath="$id"

VANDA="git@gitlab.tcs.inf.tu-dresden.de:vanda/vanda.git"
#VANDA="/home/sjm/documents/Uni/LCFRS/vanda/"

install_me () {
	if [[ ! -d "$1/.git" ]]
	then
		git clone "${VANDA}" "$1"
		cd "$1"
	else
		cd "$1"
		git pull origin master
	fi
	cabal install -p --only-dependencies
	runhaskell "tools/Setup.hs" configure --user
	runhaskell "tools/Setup.hs" build
	ghc -package-db dist/package.conf.inplace --make "programs/XRSToHypergraph.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/XRSTranslate.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/XRSNGrams.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/NGrams.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/PennToSentenceCorpus.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/NegraToLCFRS.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/BinarizeLCFRS.hs"
}
