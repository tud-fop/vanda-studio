source "$FUNCDIR/util.bash"

# IRSTLM
# Version: 2012-12-03
# Contact: Tobias.Denkinger@mailbox.tu-dresden.de
# Category: language model
# IN English Corpus :: Sentence Corpus
# IN n-gram length :: Integer
# OUT Alignments :: n-grams
#
# computes n-gram probabilities
IRSTLM () {
	echo "Running: IRSTLM..."
	TMP="$OUTPATH/IRSTLM_TMP"
	mkdir -p "$TMP"
	pathAndName "$1" f1 i1
	out="$OUTPATH/IRSTLM($i1,$2).0"
	"$IRSTLM/add-start-end.sh" < "$f1" > "$TMP/train.txt"
	"$IRSTLM/ngt" -i="$TMP/train.txt" -n="$2" -o="$TMP/train.www"
	"$IRSTLM/tlm" -tr="$TMP/train.www" -n="$2" -lm=wb -o="$out"
	rm -rf "$TMP"
	eval $3=\"$out\"
	echo "Done."
}
