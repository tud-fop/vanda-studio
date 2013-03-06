id="mgiza"
varname="GIZA"
version="2013-03-05"
binpath="$id"

install_me () {
	wget http://ignum.dl.sourceforge.net/project/mgizapp/mgizapp-0.7.3.tgz
	tar xfv mgizapp-0.7.3.tgz
	cd mgizapp
	rm CMakeCache.txt
	sed -i "s/FIND_PACKAGE( Boost 1.41 COMPONENTS thread)/FIND_PACKAGE( Boost 1.41 COMPONENTS thread system)/g" CMakeLists.txt
	cmake .
	make mgiza
	mkdir "$1/giza"
	cp bin/mgiza "$1/giza/."
	wget http://www.cs.cmu.edu/~qing/release/merge_alignment.py
	cp "merge_alignment.py" "$1/giza"
	chmod +x "$1/giza/merge_alignment.py"
	cd ..
	git clone git://github.com/moses-smt/mosesdecoder.git
	cd mosesdecoder
	./bjam --with-giza="$1/giza"
	cd ..
	mv -f mosesdecoder "$1/mosesdecoder"
	cd ..
}