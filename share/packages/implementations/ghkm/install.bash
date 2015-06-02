id="ghkm"
varname="GHKM"
version="2015-06-02"
binpath="$id"

download () {
	rm -fr 'downloads'
	mkdir 'downloads'
	cd 'downloads'
	wget 'https://github.com/joshua-decoder/joshua/raw/master/lib/fastutil.jar'
	wget 'https://github.com/joshua-decoder/joshua/raw/master/lib/ghkm-modified.jar'
}

install_me () {
	cp 'downloads/fastutil.jar'      "$1/fastutil.jar"
	cp 'downloads/ghkm-modified.jar' "$1/ghkm.jar"
}
