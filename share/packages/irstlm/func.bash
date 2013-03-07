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
	"$IRSTLM/bin/add-start-end.sh" < "$2" > "$1/train.txt"
	"$IRSTLM/bin/ngt" -i="$1/train.txt" -n="$3" -o="$1/train.www"
	"$IRSTLM/bin/tlm" -tr="$1/train.www" -n="$3" -lm=wb -o="$4"
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
	"$IRSTLM/bin/compile-lm" "$2" "$3"
}

