id="vanda"
varname="VANDA"
version="2018-02-21"
binpath="$id"

VANDA="https://github.com/tud-fop/vanda-haskell"

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
	cabal install
}
