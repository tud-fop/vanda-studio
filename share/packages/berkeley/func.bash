# Berkeley Tokenizer
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: corpus tools
# IN corpus :: SentenceCorpus
# OUT tokenizedCorpus :: SentenceCorpus
#
# Converts some special characters into tokens, such as ( into -LLB-
BerkeleyTokenizer () {
	java -cp "$BERKELEY_PARSER/berkeleyParser.jar:$BERKELEY_PARSER" Main < "$2" | PROGRESS "$2" > "$3"
}

# Berkeley Parser
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: parsing
# IN corpus :: SentenceCorpus
# IN grammar :: BerkeleyGrammar.sm6
# OUT trees :: PennTreeCorpus
#
# Berkeley parser using a state-split grammar. Corpus must not contain empty lines.
BerkeleyParser () {
	java -jar "$BERKELEY_PARSER/berkeleyParser.jar" -gr "$3" < "$2" | PROGRESS "$2" > "$4"
}

# Berkeley Parser n-best
# Version: 2012-05-16
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: parsing
# IN corpus :: SentenceCorpus
# IN n :: Integer
# IN grammar :: BerkeleyGrammar.sm6
# OUT trees :: PennTreeCorpus
#
# Computes n best trees for the sentences in the corpus.
bpnbest () {
	java -jar "$BERKELEY_PARSER/berkeleyParser.jar" -gr "$4" -kbest "$3" -outputFile "$5" < "$2"
}

# SM6ToText
# Version: 2013-06-27
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: parsing
# IN grammar :: BerkeleyGrammar.sm6
# OUT textGrammar :: TextualSM6
#
# Converts a Berkeley SM6 grammar to text
sm6ToText () {
	java -cp "$BERKELEY_PARSER/berkeleyParser.jar" edu/berkeley/nlp/PCFGLA/WriteGrammarToTextFile "$2" "$3"
}
