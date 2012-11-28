source "$FUNCDIR/util.bash"

# Berkeley Tokenizer
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: corpus tools
# IN Corpus :: Sentence Corpus
# OUT Tokenized Corpus :: Sentence Corpus
#
# Converts some special characters into tokens, such as ( into -LLB-
BerkeleyTokenizer () {
	echo "Running: BerkeleyTokenizer..."
	pathAndName "$1" f1 n1
	btout="$OUTPATH/BerkeleyTokenizer($n1).0"
	cat "$f1" | java -cp "$BERKELEY_PARSER:$BERKELEY_TOKENIZER" Main > "$btout"
	eval $2=\"$btout\"
	echo "Done."
}

# Berkeley Parser
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: parsing
# IN Corpus :: Sentence Corpus
# IN Grammar :: BerkeleyGrammar.sm6
# OUT Tree Corpus :: Penn Tree Corpus
#
# Berkeley parser using a state-split grammar. Corpus must not contain empty lines.
BerkeleyParser () {
	echo "Running: BerkeleyParser..."
	pathAndName "$1" f1 n1
	pathAndName "$2" f2 n2
	bpout="$OUTPATH/BerkeleyParser($n1,$n2).0"
	java -jar "$BERKELEY_PARSER" -gr "$f2" -inputFile "$f1" -outputFile "$bpout"
	eval $3=\"$bpout\"
	echo "Done."
}
