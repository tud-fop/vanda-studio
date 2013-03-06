# IRSTLM
# Version: 2012-12-03
# Contact: Tobias.Denkinger@mailbox.tu-dresden.de
# Category: language model
# IN english corpus :: SentenceCorpus
# IN n-gram length :: Integer
# OUT n-grams :: ARPA
#
# Trains an n-gram model.
IRSTLM () {
	TMP="$1"
	"$IRSTLM/add-start-end.sh" < "$2" > "$TMP/train.txt"
	"$IRSTLM/ngt" -i="$TMP/train.txt" -n="$3" -o="$TMP/train.www"
	"$IRSTLM/tlm" -tr="$TMP/train.www" -n="$3" -lm=wb -o="$4"
}

# ARPA2Binary
# Version: 2012-12-04
# Contact: Tobias Denkinger@mailbox.tu-dresden.de
# Category: language model
# IN texual n-grams :: ARPA
# OUT binary n-grams :: BinARPA
#
# Converts ARPA to binary ARPA format.
ARPA2Binary () {
	"$IRSTLM/compile-lm" "$2" "$3"
}

