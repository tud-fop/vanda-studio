#!/bin/bash

setup () {
	base
	berkeleyParser
	berkeleyTokenizer
	remEmptyLines
	giza
	moses
	ghkm
}

base () {
	echo "Creating configuration path..."
	mkdir -p ~/.vanda
	mkdir -p ~/.vanda/bin
	mkdir -p ~/.vanda/output
	mkdir -p ~/.vanda/input
	cp functions.bash ~/.vanda/.
	echo -e "#!/bin/bash\n" > ~/.vanda/vandarc
	echo "DATAPATH=$HOME/.vanda/input" >> ~/.vanda/vandarc
	echo "OUTPATH=$HOME/.vanda/output" >> ~/.vanda/vandarc
	echo -e "FUNCFILE=$HOME/.vanda/functions.bash\n" >> ~/.vanda/vandarc
	echo -e "source \"$HOME/.vanda/functions.bash\"\n" >> ~/.vanda/vandarc
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

moses () {
	echo "Installing moses..."
	git clone git://github.com/moses-smt/mosesdecoder.git
	cd mosesdecoder
	./bjam --with-giza=~/.vanda/bin/giza
	cd ..
	mv mosesdecoder ~/.vanda/bin/mosesdecoder
	echo "MOSES=$HOME/.vanda/bin/mosesdecoder" >> ~/.vanda/vandarc
	echo "Done."
}

ghkm () {
	echo "Installing GHKM..."
	echo "GHKM=$HOME/.vanda/bin/ghkm" >> ~/.vanda/vandarc
	echo "Done."
}

setup
