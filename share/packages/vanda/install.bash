id="vanda"
varname="VANDA"
version="2013-03-07"
binpath="$id"

VANDA="ssh://tdenk@tcs.inf.tu-dresden.de/~buechse/public_git/vanda -b hyperedge"

install_me () {
	git clone $VANDA "$1"
	cd "$1"
	runhaskell tools/Setup.hs configure --user
	runhaskell tools/Setup.hs build
	runhaskell tools/Setup.hs install --user
}
