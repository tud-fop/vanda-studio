id="tiburon"
varname="TIBURON"
version="2013-03-14"
binpath="$id"

install_me () {
	read -p "Where is the \"tiburon.jar\" located? " jarFile
	cp "$jarFile" "$1/tiburon.jar"
}
