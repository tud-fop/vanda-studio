id="vanda"
varname="VANDA"
version="2014-06-10"
binpath="$id"

VANDA="tcs.inf.tu-dresden.de/~tdenk/public_git/vanda"
VERSION="0ba1876263338c68654ff4e3611b49ca797f3d7b"

install_me () {
	if [[ ! -d "$1/.git" ]]
	then
		read -p 'Your login on tcs.inf.tu-dresden.de: '
		git clone "ssh://${REPLY}@${VANDA}" "$1"
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
	ghc -package-db dist/package.conf.inplace --make "programs/NGrams.hs"
	ghc -package-db dist/package.conf.inplace --make "programs/PennToSentenceCorpus.hs"
}
