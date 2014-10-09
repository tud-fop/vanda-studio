# Berkeley parser
# Version: 2014-10-08
# Contact: Matthias.Buechse@tu-dresden.de
# Category: Language Models / Parsing
# IN grammar :: BerkeleyGrammar
# IN corpus :: SentenceCorpus
# OUT trees :: PennTreeCorpus
#
# Berkeley parser using a state-split grammar. Corpus must not contain empty lines.
BerkeleyParser () {
	java -jar "$BERKELEY_PARSER/berkeleyParser.jar" -gr "$2" < "$3" | PROGRESS "$3" > "$4"
}

# Berkeley grammar training
# Version: 2014-10-08
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models / Training
# IN corpus :: PennTreeCorpus
# OUT grammar :: BerkeleyGrammar
#
# Creates a grammer from a PennTreeCorpus.
BerkeleyTrain () {
	java -cp "$BERKELEY_PARSER/berkeleyParser.jar" edu.berkeley.nlp.PCFGLA.GrammarTrainer -path "$2" -out "$1/grammar" -treebank SINGLEFILE
	mv "$1/grammar" "$3"
}

# Berkeley n-best parser
# Version: 2014-10-08
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models / Parsing
# IN grammar :: BerkeleyGrammar
# IN n :: Integer
# IN corpus :: SentenceCorpus
# OUT trees :: PennTreeCorpus
#
# Computes n best trees for the sentences in the corpus.
bpnbest () {
	java -jar "$BERKELEY_PARSER/berkeleyParser.jar" -gr "$2" -kbest "$3" < "$4" | PROGRESSX "$4" $(expr $3 + 1) > "$5"
}

# Berkeley tokenizer
# Version: 2014-10-08
# Contact: Matthias.Buechse@tu-dresden.de
# Category: Corpus Tools
# IN corpus :: SentenceCorpus
# OUT tokenizedCorpus :: SentenceCorpus
#
# Converts some special characters into tokens, such as ( into -LLB-
BerkeleyTokenizer () {
	java -cp "$BERKELEY_PARSER/berkeleyParser.jar:$BERKELEY_PARSER" Main < "$2" | PROGRESS "$2" > "$3"
}

# Berkeley grammar to text
# Version: 2014-10-08
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Conversion
# IN grammar :: BerkeleyGrammar
# OUT textGrammar :: TextualBerkeleyGrammar
#
# Converts a Berkeley SM6 grammar to text
BerkeleyToText () {
	java -cp "$BERKELEY_PARSER/berkeleyParser.jar" edu/berkeley/nlp/PCFGLA/WriteGrammarToTextFile "$2" "$3"
}
