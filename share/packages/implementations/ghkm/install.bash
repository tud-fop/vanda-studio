id="ghkm"
varname="GHKM"
version="2013-03-05"
binpath="$id"

download () {
	wget -O - "http://www-nlp.stanford.edu/~mgalley/software/stanford-ghkm-latest.tar.gz" | tar xz --wildcards 'stanford-ghkm-*/ghkm.jar' 'stanford-ghkm-*/lib/fastutil.jar'
}

install_me () {
	cp stanford-ghkm-*/ghkm.jar "$1/."
	cp stanford-ghkm-*/lib/fastutil.jar "$1/."
}
