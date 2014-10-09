id="giza"
varname="GIZA"
version="2014-10-06"
binpath="$id"

install_me () {
# install giza
	wget http://giza-pp.googlecode.com/files/giza-pp-v1.0.7.tar.gz
	tar xfv giza-pp*.tar.gz
	pushd giza-pp*
		make
		cp GIZA++-v2/{plain2snt.out,snt2cooc.out,GIZA++} mkcls-v2/mkcls "$1/."
	popd
# install symal
	git clone --depth 1 git://github.com/moses-smt/mosesdecoder.git
	pushd mosesdecoder
		./bjam "-j$(nproc)" -a symal
		cp symal/bin/gcc-*/release/debug-symbols-on/link-static/threading-multi/symal "$1/."
# install giza2bal.pl
		cp scripts/training/giza2bal.pl "$1/."
	popd
}
