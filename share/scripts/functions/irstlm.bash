source "$FUNCDIR/util.bash"

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
	TMP="$OUTPATH/IRSTLM_TMP"
	mkdir -p "$TMP"
	"$IRSTLM/add-start-end.sh" < "$1" > "$TMP/train.txt"
	"$IRSTLM/ngt" -i="$TMP/train.txt" -n="$2" -o="$TMP/train.www"
	"$IRSTLM/tlm" -tr="$TMP/train.www" -n="$2" -lm=wb -o="$3"
	rm -rf "$TMP"
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
	"$IRSTLM/compile-lm" "$1" "$2"
}

