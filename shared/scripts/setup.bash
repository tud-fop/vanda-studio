#!/bin/bash

GIZADIR="$HOME/.vanda/bin/giza"
IRSTLMDIR="$HOME/.vanda/bin/irstlm"

setup () {
	base
	berkeleyParser
	berkeleyTokenizer
	remEmptyLines
	toParallelCorpus
	giza
	irstlm
	moses
	ghkm
	emDictionary
	emDictionaryShow
	examples
}

base () {
	echo "Creating configuration path..."
	mkdir -p ~/.vanda
	mkdir -p ~/.vanda/bin
	mkdir -p ~/.vanda/output
	mkdir -p ~/.vanda/input
	cp -r functions ~/.vanda/.
	echo -e "#!/bin/bash\n" > ~/.vanda/vandarc
	echo "DATAPATH=$HOME/.vanda/input" >> ~/.vanda/vandarc
	echo "OUTPATH=$HOME/.vanda/output" >> ~/.vanda/vandarc
	echo -e "FUNCDIR=$HOME/.vanda/functions\n" >> ~/.vanda/vandarc
	echo -e "for f in \"\$FUNCDIR/\"; do\n\tsource \$f;\ndone\n" >> ~/.vanda/vandarc
	echo "Done."
}

berkeleyParser () {
	echo "Installing berkeleyParser..."
	wget http://berkeleyparser.googlecode.com/files/berkeleyParser.jar
	cp "berkeleyParser.jar" "$HOME/.vanda/bin/."
	echo "BERKELEY_PARSER=$HOME/.vanda/bin/berkeleyParser.jar" >> ~/.vanda/vandarc
	echo "Done."
}

berkeleyTokenizer () {
	echo "Installing berkeleyTokenizer..."
	mkdir -p ~/.vanda/bin/berkeleyTokenizer
	javac -cp "berkeleyParser.jar:berkeleyTokenizer" -d "berkeleyTokenizer" berkeleyTokenizer/Main.java
	mv "berkeleyTokenizer/Main.class" ~/.vanda/bin/berkeleyTokenizer/.
	rm "berkeleyParser.jar"
	echo "BERKELEY_TOKENIZER=$HOME/.vanda/bin/berkeleyTokenizer" >> ~/.vanda/vandarc
	echo "Done."
}

remEmptyLines () {
	echo "Installing remEmptyLines..."
	mkdir -p ~/.vanda/bin/remEmptyLines
	ghc --make -o ~/.vanda/bin/remEmptyLines/remEmptyLines remEmptyLines/remEmptyLines.hs
	echo "REM_EMPTY_LINES=$HOME/.vanda/bin/remEmptyLines/remEmptyLines" >> ~/.vanda/vandarc
	echo "Done."
}

toParallelCorpus () {
	echo "Installing toParallelCorpus..."
	mkdir -p ~/.vanda/bin/toParallelCorpus
	ghc --make -o ~/.vanda/bin/toParallelCorpus toParallelCorpus/toParallelCorpus.hs -main-is Main.main
	echo "TO_PARALLEL_CORPUS=$HOME/.vanda/bin/toParallelCorpus/toParallelCorpus" >> ~/.vanda/vandarc
	echo "Done."
}

giza () {
	echo "Installing GIZA..."
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
	echo "Done."
}

irstlm () {
	echo "Installing IRSTLM..."
	svn co https://irstlm.svn.sourceforge.net/svnroot/irstlm irstlm
	cd irstlm/trunk/
	sh regenerate-makefiles.sh
	./configure --prefix="$IRSTLMDIR"
	make
	mkdir "$IRSTLMDIR"
	make install
	cd ../..
	echo "IRSTLM=$HOME/.vanda/bin/irstlm/bin/" >> ~/.vanda/vandarc
	echo "Done."
}

moses () {
	echo "Installing moses..."
	git clone git://github.com/moses-smt/mosesdecoder.git
	cd mosesdecoder
	./bjam --with-giza="$GIZADIR"
	cd ..
	mv -f mosesdecoder ~/.vanda/bin/mosesdecoder
	echo "MOSES=$HOME/.vanda/bin/mosesdecoder" >> ~/.vanda/vandarc
	echo "Done."
}

ghkm () {
	echo "Installing GHKM..."
	wget "http://www-nlp.stanford.edu/~mgalley/software/stanford-ghkm-latest.tar.gz"
	tar xfv "stanford-ghkm-latest.tar.gz"
	mkdir -p ~/.vanda/bin/ghkm
	cp stanford-ghkm-*/ghkm.jar ~/.vanda/bin/ghkm/.
	cp stanford-ghkm-*/lib/fastutil.jar ~/.vanda/bin/ghkm/.
	rm -rf stanford-ghkm-*
	echo "GHKM=$HOME/.vanda/bin/ghkm" >> ~/.vanda/vandarc
	echo "Done."
}

emDictionary () {
	echo "Installing EMDictionary..."
	cd EMDictionary
	ghc --make Algorithms/EMDictionary.hs -O -fforce-recomp -main-is Algorithms.EMDictionary
	mkdir -p ~/.vanda/bin/EMDictionary
	cp Algorithms/EMDictionary ~/.vanda/bin/EMDictionary
	cd ..
	echo "EMDICTIONARY=$HOME/.vanda/bin/EMDictionary/EMDictionary" >> ~/.vanda/vandarc
	echo "Done."
}

emDictionaryShow () {
	echo "Installing EMDictionaryShowSteps..."
	cd EMDictionaryShowSteps
	javac DictViewTest.java
	mkdir -p ~/.vanda/bin/EMDictionaryShowSteps
	cp *.class ~/.vanda/bin/EMDictionaryShowSteps
	cd ..
	echo "EMDICTIONARYSHOW=$HOME/.vanda/bin/EMDictionaryShowSteps" >> ~/.vanda/vandarc
	echo "Done."
}

examples () {
	echo "Copying examples..."
	cp -R "../examples/*" "$HOME/.vanda/input/."
	echo "Done."
}

setup
