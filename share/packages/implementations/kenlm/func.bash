# KenLMTrain
# Version: 2014-10-08
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models / Training
# IN n :: Integer
# IN corpus :: SentenceCorpus
# OUT n-grams :: ARPA
#
# Trains an n-gram model.
KenLMTrain () {
	"$KENLM/bin/lmplz" -o "$3" < "$2" > "$4"
}

# KenLMBinary
# Version: 2014-10-08
# Contact: Tobias Denkinger@tu-dresden.de
# Category: Conversion
# IN texual n-grams :: ARPA
# OUT binary n-grams :: BinARPA
#
# Converts ARPA to binary ARPA format.
KenLMBinary () {
	"$KENLM/bin/build_binary" "$2" "$3"
}

# KenLMScore
# Version: 2014-10-08
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models / Parsing
# IN n-grams :: ARPA
# IN corpus :: SentenceCorpus
# OUT logarithmic scores :: Scores
#
# Scores sentences according to the language model.
KenLMScore () {
	"$KENLM/bin/query" "$2" < "$3" | grep "Total:" | sed "s/.*Total: //g" | sed "s/ OOV:.*//g" | PROGRESS $3 > "$4"
	echo "$3" > "${4}.meta"
}
