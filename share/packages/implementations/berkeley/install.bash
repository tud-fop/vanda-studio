id="berkeley"
varname="BERKELEY_PARSER"
version="2014-10-09"
binpath="$id"

download () {
	wget -nc "http://berkeleyparser.googlecode.com/files/berkeleyParser.jar"
	mkdir -p "berkeley_grammars"
	for name in eng_sm6 bul_sm5 arb_sm5 chn_sm5 fra_sm5 ger_sm5; do
		wget -P "berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/${name}.gr"
		java -cp "berkeleyParser.jar" "edu/berkeley/nlp/PCFGLA/WriteGrammarToTextFile" "berkeley_grammars/${name}.gr" "berkeley_grammars/${name}.gr.prev"
	done
}

install_me () {
	javac -cp "berkeleyParser.jar:src" -d "src" "src/Main.java"
	cp "berkeleyParser.jar" "$1/."
	cp "src/Main.class" "$1/."
	cp --recursive "berkeley_grammars" "$2/."
}

