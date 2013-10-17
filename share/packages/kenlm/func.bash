# KenLMTrain
# Version: 2013-10-17
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: language model
# IN corpus :: SentenceCorpus
# IN n-gram length :: Integer
# OUT n-grams :: ARPA
#
# Trains an n-gram model.
KenLMTrain () {
	"$KENLM/bin/lmplz" -o "$3" < "$2" > "$4"
}

# KenLMBinary
# Version: 2013-10-17
# Contact: Tobias Denkinger@tu-dresden.de
# Category: language model
# IN texual n-grams :: ARPA
# OUT binary n-grams :: BinARPA
#
# Converts ARPA to binary ARPA format.
KenLMBinary () {
	"$KENLM/bin/build_binary" "$2" "$3"
}

# KenLMScore
# Version: 2013-10-17
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: language model
# IN n-grams :: ARPA
# IN corpus :: SentenceCorpus
# OUT logarithmic scores :: Scores
#
# Scores sentences according to the language model.
KenLMScore () {
	"$KENLM/bin/query" "$2" < "$3" | grep "Total:" | sed "s/.*Total: //g" | sed "s/ OOV:.*//g" > "$4"
	echo "$3" > "${4}.meta"
}
