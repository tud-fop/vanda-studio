id="rparse"
varname="RPARSE"
version="2015-06-29"
binpath="$id"

download () {
	if [[ -d rparse ]]; then
		git pull
	else
		git clone "https://github.com/wmaier/rparse.git"
	fi
	pushd rparse
	ant -Djgraph.path="lib/jgrapht-jdk1.6.jar"
	popd
}

install_me () {
	mkdir -p "$1/lib"
	cp "rparse/rparse.jar" -t "$1/."
	cp "rparse/lib/jgrapht-jdk1.6.jar"  -t "$1/lib/."
}

