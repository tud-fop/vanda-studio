id="mgiza"
varname="MGIZA"
version="2013-03-18"
binpath="$id"

install_me () {
# install giza
	wget http://giza-pp.googlecode.com/files/giza-pp-v1.0.7.tar.gz
	tar xfv giza-pp*.tar.gz
	cd giza-pp*
	make
	mkdir -p "$1/giza"
	cp GIZA++-v2/{GIZA++,plain2snt.out,snt2cooc.out,snt2plain.out,trainGIZA++.sh} mkcls-v2/mkcls "$1/giza/."
	cd ..
# install mgiza
	svn checkout svn://svn.code.sf.net/p/mgizapp/code/trunk mgizapp-code
	cd mgizapp-code/mgizapp
	rm -f CMakeCache.txt
	sed -i "s/set(Boost_USE_STATIC_LIBS        ON)/set(Boost_USE_STATIC_LIBS       OFF)/g" CMakeLists.txt
	sed -i "s/FIND_PACKAGE( Boost 1.41 COMPONENTS thread)/FIND_PACKAGE(Boost COMPONENTS thread system)/g" CMakeLists.txt
	cmake .
	make mgiza
	cp bin/mgiza "$1/giza/."
	wget http://www.cs.cmu.edu/~qing/release/merge_alignment.py
	cp "merge_alignment.py" "$1/giza/."
	chmod +x "$1/giza/merge_alignment.py"
	cd ../../..
# install mosesdecoder
	git clone git://github.com/moses-smt/mosesdecoder.git
	cd mosesdecoder
	./bjam -q "-j$(grep -c processor /proc/cpuinfo)" "--with-giza=$1/giza"
	cd ..
	mv -f mosesdecoder "$1/mosesdecoder"
	cd ..
}
