id="berkeley"
varname="BERKELEY_PARSER"
version="2014-10-09"
binpath="$id"

install_me () {
	wget http://berkeleyparser.googlecode.com/files/berkeleyParser.jar
	cp "berkeleyParser.jar" "$1/."
	javac -cp "$1/berkeleyParser.jar:src" -d "src" "src/Main.java"
	mv "src/Main.class" "$1/."
}