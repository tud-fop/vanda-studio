id="egret"
varname="EGRET"
version="2013-03-08"
binpath="$id"

install_me () {
	wget http://egret-parser.googlecode.com/files/Egret.zip
	unzip Egret.zip
	cd Egret
	sed -i "2 i #include <cstdlib>" Egret/src/Tree.cpp
	sed -i "3 i #include <cstdlib>" Egret/src/utils.h
	g++ Egret/src/*.cpp -O2 -o egret
	cp egret "$1"
	mkdir -p "$2/egret_grammars"
	mv -t "$2/egret_grammars" "eng_grammar" "chn_grammar"
	cd ..
}
