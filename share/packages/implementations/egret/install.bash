id="egret"
varname="EGRET"
version="2019-02-28"
binpath="$id"

EGRET="https://github.com/neubig/egret.git"

install_me () {
	if [[ ! -d "$1/.git" ]]
	then
		rm -rf "$1"
		git clone "${EGRET}" "$1"
		cd "$1"
	else
		cd "$1"
		git pull origin master
	fi
	sed -i "2 i #include <cstdlib>" Egret/src/Tree.cpp
	sed -i "3 i #include <cstdlib>" Egret/src/utils.h
	make
	mkdir -p "$2/egret_grammars"
	mv -n -t "$2/egret_grammars" "eng_grammar" "chn_grammar" "jpn_grammar"
	cd ..
}
