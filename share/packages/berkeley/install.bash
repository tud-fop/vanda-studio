id="berkeley"
varname="BERKELEY_PARSER"
version="2013-03-04"
binpath="$id"

install_me () {
	wget http://berkeleyparser.googlecode.com/files/berkeleyParser.jar
	cp "berkeleyParser.jar" "$1/."
	javac -cp "$1/berkeleyParser.jar:berkeleyTokenizer" -d "berkeleyTokenizer" berkeleyTokenizer/Main.java
	mv "berkeleyTokenizer/Main.class" "$1/."
}