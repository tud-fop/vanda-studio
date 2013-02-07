#!/bin/bash

source "setuprc"

allPkgs="config functions berkeleyParser europarlTools remEmptyLines toParallelCorpus giza mgiza irstlm randlm xrstranslate moses ghkm emDictionary examples"

all () {
	install $allPkgs
}

config () {
	mkdir -p ~/.vanda
	mkdir -p ~/.vanda/bin
	mkdir -p ~/.vanda/output
	mkdir -p ~/.vanda/input
	echo -e "#!/bin/bash\n" > ~/.vanda/vandarc
	echo "DATAPATH=$HOME/.vanda/input" >> ~/.vanda/vandarc
	echo "OUTPATH=$HOME/.vanda/output" >> ~/.vanda/vandarc
	echo -e "FUNCDIR=$HOME/.vanda/functions\n" >> ~/.vanda/vandarc
}

functions () {
	mkdir -p ~/.vanda/functions
	mkdir -p ~/.vanda/interfaces
	cp functions/* ~/.vanda/functions/.
	cp ../interfaces/* ~/.vanda/interfaces/.
}

berkeleyParser () {
	wget http://berkeleyparser.googlecode.com/files/berkeleyParser.jar
	cp "berkeleyParser.jar" "$HOME/.vanda/bin/."
	echo "BERKELEY_PARSER=$HOME/.vanda/bin/berkeleyParser.jar" >> ~/.vanda/vandarc
	mkdir -p ~/.vanda/bin/berkeleyTokenizer
	javac -cp "berkeleyParser.jar:berkeleyTokenizer" -d "berkeleyTokenizer" berkeleyTokenizer/Main.java
	mv "berkeleyTokenizer/Main.class" ~/.vanda/bin/berkeleyTokenizer/.
	rm "berkeleyParser.jar"
	echo "BERKELEY_TOKENIZER=$HOME/.vanda/bin/berkeleyTokenizer" >> ~/.vanda/vandarc
}

europarlTools () {
	wget http://www.statmt.org/europarl/v7/tools.tgz
	tar xfv tools.tgz
	mv tools ~/.vanda/bin/europarlTools
	rm tools.tgz
	echo "EUROPARL_TOOLS=$HOME/.vanda/bin/europarlTools" >> ~/.vanda/vandarc
}

remEmptyLines () {
	mkdir -p ~/.vanda/bin/remEmptyLines
	ghc --make -o ~/.vanda/bin/remEmptyLines/remEmptyLines remEmptyLines/remEmptyLines.hs
	echo "REM_EMPTY_LINES=$HOME/.vanda/bin/remEmptyLines/remEmptyLines" >> ~/.vanda/vandarc
}

toParallelCorpus () {
	mkdir -p ~/.vanda/bin/toParallelCorpus
	ghc --make -o ~/.vanda/bin/toParallelCorpus/toParallelCorpus toParallelCorpus/toParallelCorpus.hs -main-is Main.main
	echo "TO_PARALLEL_CORPUS=$HOME/.vanda/bin/toParallelCorpus/toParallelCorpus" >> ~/.vanda/vandarc
}

giza () {
	wget http://giza-pp.googlecode.com/files/giza-pp-v1.0.7.tar.gz
	tar xfv giza-pp*.tar.gz
	cd giza-pp*
	make
	mkdir -p ~/.vanda/bin/giza
	cp GIZA++-v2/{GIZA++,plain2snt.out,snt2cooc.out,snt2plain.out,trainGIZA++.sh} mkcls-v2/mkcls ~/.vanda/bin/giza
	cd ..
	rm -rf giza-pp*
	echo "PLAIN2SNT=$HOME/.vanda/bin/giza/plain2snt.out" >> ~/.vanda/vandarc
	echo "GIZA=$HOME/.vanda/bin/giza" >> ~/.vanda/vandarc
}

mgiza () {
	wget http://ignum.dl.sourceforge.net/project/mgizapp/mgizapp-0.7.3.tgz
	tar xfv mgizapp-0.7.3.tgz
	cd mgizapp
	rm CMakeCache.txt
	sed -i "s/FIND_PACKAGE( Boost 1.41 COMPONENTS thread)/FIND_PACKAGE( Boost 1.41 COMPONENTS thread system)/g" CMakeLists.txt
	cmake .
	make mgiza
	cp bin/mgiza ~/.vanda/bin/giza
	wget http://www.cs.cmu.edu/~qing/release/merge_alignment.py -O ~/.vanda/bin/giza/merge_alignment.py
	chmod +x ~/.vanda/bin/giza/merge_alignment.py
	sed -i "s/train-model.perl -root-dir/train-model.perl -mgiza -root-dir/g" ~/.vanda/functions/giza.bash
	cd ..
	rm -rf mgizapp*
}

xrstranslate () {
	DIR=$(pwd)
	cd ~/.vanda/bin
	git clone $VANDADIR
	cd vanda
	runhaskell tools/Setup.hs configure --user
	runhaskell tools/Setup.hs build
	runhaskell tools/Setup.hs install --user
	cd "$DIR"
	echo "VANDADIR=$HOME/.vanda/bin/vanda" >> ~/.vanda/vandarc
}

irstlm () {
	svn co https://irstlm.svn.sourceforge.net/svnroot/irstlm irstlm
	cd irstlm/trunk/
	sh regenerate-makefiles.sh
	./configure --prefix="$IRSTLMDIR"
	make
	mkdir -p "$IRSTLMDIR"
	make install
	cd ../..
	echo "IRSTLM=$IRSTLMDIR/bin/" >> ~/.vanda/vandarc
}

randlm () {
	svn co https://randlm.svn.sourceforge.net/svnroot/randlm randlm
	cd randlm/trunk
	./autogen.sh
	mkdir -p "$RANDLMDIR"
	./configure --prefix="$RANDLMDIR"
	make
	make install
	cd ../..
	echo "RANDLM=$RANDLMDIR/bin/" >> ~/.vanda/vandarc
}

moses () {
	git clone git://github.com/moses-smt/mosesdecoder.git
	cd mosesdecoder
	./bjam --with-giza="$GIZADIR"
	cd ..
	mv -f mosesdecoder ~/.vanda/bin/mosesdecoder
	echo "MOSES=$HOME/.vanda/bin/mosesdecoder" >> ~/.vanda/vandarc
}

ghkm () {
	wget "http://www-nlp.stanford.edu/~mgalley/software/stanford-ghkm-latest.tar.gz"
	tar xfv "stanford-ghkm-latest.tar.gz"
	mkdir -p ~/.vanda/bin/ghkm
	cp stanford-ghkm-*/ghkm.jar ~/.vanda/bin/ghkm/.
	cp stanford-ghkm-*/lib/fastutil.jar ~/.vanda/bin/ghkm/.
	rm -rf stanford-ghkm-*
	echo "GHKM=$HOME/.vanda/bin/ghkm" >> ~/.vanda/vandarc
}

emDictionary () {
	cd EMDictionary
	ghc --make Algorithms/EMDictionary.hs -O -fforce-recomp -main-is Algorithms.EMDictionary
	mkdir -p ~/.vanda/bin/EMDictionary
	cp Algorithms/EMDictionary ~/.vanda/bin/EMDictionary
	cd ..
	echo "EMDICTIONARY=$HOME/.vanda/bin/EMDictionary/EMDictionary" >> ~/.vanda/vandarc
}

examples () {
	cp -r ../examples/* "$HOME/.vanda/input/."
}

help () {
	echo "Usage: \
./setup.bash { [ help | config | config | berkeleyParser | europarlTools \
| ghkm | examples| remEmptyLines | toParallelCorpus | giza | xrstranslate\
| irstlm | randlm | moses | emDictionary | functions ] }"
}

install () {
	echo "Installing: $@."
	local args
	args=($@)
	i=0
	echo "$date" > setup.log
	for t in $@; do
		(( i++ ))
		echo -ne "[$i/$#] Installing ${args[(($i - 1))]}."
		echo "[$i/$#] Installing ${args[(($i - 1))]}." >> setup.log
		dir=$(pwd)
		"$t" >> setup.log 2>> setup.log
		cd "$dir"
		echo " Done."
		echo "[$i/$#] Done." >> setup.log
	done
	echo "Written log: setup.log"
}

args=($@)
if [ $# = "0" ]
then
	echo "Error: Invalid syntax."
	help
else
	if [[ ${args[*]} =~ "help" ]]
	then
		help
	else if [[ ${args[*]} =~ "all" ]]
	then
		all
	else
		install "$@"
	fi
	fi
fi
