id="mgiza"
varname="MGIZA"
version="2015-06-02"
binpath="$id"

download () {
	for repository in giza-pp mgiza mosesdecoder
	do
		rm -rf "${repository}"
		git clone --depth 1 "git://github.com/moses-smt/${repository}.git"
	done
	wget "http://www.cs.cmu.edu/~qing/release/merge_alignment.py"
}

install_me () {
# install giza
	pushd giza-pp*
		make
		cp GIZA++-v2/{plain2snt.out,snt2cooc.out} mkcls-v2/mkcls "$1/."
	popd
# install mgiza
	pushd mgiza/mgizapp
		rm -f CMakeCache.txt
		sed -i "s/set(Boost_USE_STATIC_LIBS        ON)/set(Boost_USE_STATIC_LIBS       OFF)/g" CMakeLists.txt
		sed -i "s/FIND_PACKAGE( Boost 1.41 COMPONENTS thread)/FIND_PACKAGE(Boost COMPONENTS thread system)/g" CMakeLists.txt
		cmake .
		make mgiza
		cp bin/mgiza "$1/."
	popd
# download merge-alignment.py
	cp "merge_alignment.py" "$1/."
	chmod +x "$1/merge_alignment.py"
# install symal
	pushd mosesdecoder
		./bjam "-j$(nproc)" -a symal
		cp symal/bin/gcc-*/release/debug-symbols-on/link-static/threading-multi/symal "$1/."
# install giza2bal.pl
		cp scripts/training/giza2bal.pl "$1/."
	popd
}
