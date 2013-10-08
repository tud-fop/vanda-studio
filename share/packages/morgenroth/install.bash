id="morgenroth"
varname="BLEU"
version="2013-07-17"
binpath="$id"

install_me () {
	cd src
	ghc -o $1/bleu --make Main.hs
}
