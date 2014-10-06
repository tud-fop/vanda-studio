id="giza"
varname="GIZA"
version="2013-03-18"
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
	git clone git://github.com/moses-smt/mosesdecoder.git
	pushd mosesdecoder
		./bjam "-j$(nproc)" -a symal
		pushd symal/bin/gcc-*
			cp release/debug-symbols-on/link-static/threading-multi/symal "$1/."
		popd
# install giza2bal.pl
		cp scripts/training/giza2bal.pl "$1/."
	popd
}