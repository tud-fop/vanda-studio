id="giza"
varname="GIZA"
version="2013-03-18"
binpath="$id"

install_me () {
	wget http://giza-pp.googlecode.com/files/giza-pp-v1.0.7.tar.gz
	tar xfv giza-pp*.tar.gz
	cd giza-pp*
	make
	mkdir -p "$1/giza"
	cp GIZA++-v2/{GIZA++,plain2snt.out,snt2cooc.out,snt2plain.out,trainGIZA++.sh} mkcls-v2/mkcls "$1/giza/."
	git clone git://github.com/moses-smt/mosesdecoder.git
	cd mosesdecoder
	./bjam -q "-j$(grep -c processor /proc/cpuinfo)" "--with-giza=$1/giza"
	cd ..
	mv -f mosesdecoder "$1/mosesdecoder"
	cd ..
}
