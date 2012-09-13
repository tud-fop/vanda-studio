all: berkeleyParser berkeleyTokenizer remEmptyLines moses giza

base:
	mkdir -p ~/.vanda
	mkdir -p ~/.vanda/bin

berkeleyParser.jar:
	wget http://berkeleyparser.googlecode.com/files/berkeleyParser.jar

berkeleyParser: base berkeleyParser.jar
	cp berkeleyParser.jar ~/.vanda/bin

berkeleyTokenizer: base berkeleyParser.jar
	javac -cp "berkeleyParser.jar:berkeleyTokenizer" -d "berkeleyTokenizer" berkeleyTokenizer/Main.java
	mkdir -p ~/.vanda/bin/berkeleyTokenizer
	cp berkeleyTokenizer/Main.class ~/.vanda/bin/berkeleyTokenizer

remEmptyLines: base
	ghc --make -o remEmptyLines/remEmptyLines remEmptyLines/remEmptyLines.hs
	mkdir -p ~/.vanda/bin/remEmptyLines
	cp remEmptyLines/remEmptyLines ~/.vanda/bin/remEmptyLines

giza-pp-v1.0.7.tar.gz:
	wget http://giza-pp.googlecode.com/files/giza-pp-v1.0.7.tar.gz

giza: base giza-pp-v1.0.7.tar.gz
	tar xfv giza-pp*.tar.gz
	cd giza-pp*
	make
	mkdir -p ~/.vanda/bin/giza
	cp GIZA++-v2/{GIZA++,plain2snt.out,snt2cooc.out,snt2plain.out,trainGIZA++.sh} mkcls-v2/mkcls ~/.vanda/bin/giza
	cd ..

moses: base giza
	git clone git://github.com/moses-smt/mosesdecoder.git
	cd mosesdecoder
	./bjam --with-giza=~/.vanda/bin/giza
	cd ..
	cp -R mosesdecoder ~/.vanda/bin/.
