id="ghkm"
varname="GHKM"
version="2013-03-05"
binpath="$id"

install_me () {
	wget "http://www-nlp.stanford.edu/~mgalley/software/stanford-ghkm-latest.tar.gz"
	tar xfv "stanford-ghkm-latest.tar.gz"
	cp stanford-ghkm-*/ghkm.jar "$1/."
	cp stanford-ghkm-*/lib/fastutil.jar "$1/."
}
