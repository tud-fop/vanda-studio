source "$FUNCDIR/util.bash"

# GIZA
# Version: 2012-09-12
# Contact: Matthias.Buechse@tu-dresden.de
# Category: alignments
# IN English Corpus :: Sentence Corpus
# IN French Corpus :: Sentence Corpus
# OUT Alignments :: Alignments
#
# computes alignments and translation tables
GIZA () {
	echo "Running: GIZA..."
	TMP="$OUTPATH/GIZA_TMP"
	mkdir -p "$TMP"
	i1new="corpus.en"
	i2new="corpus.fr"
	pathAndName "$1" f1 i1orig
	pathAndName "$2" f2 i2orig
	cp "$f1" "$TMP/$i1new"
	cp "$f2" "$TMP/$i2new"
	$MOSES/scripts/training/train-model.perl -root-dir "$TMP" --corpus "$TMP/corpus" --e en --f fr --last-step 3 --external-bin-dir="$GIZA"
	out="$OUTPATH/GIZA($i1orig,$i2orig).0"
	mv "$TMP/model/aligned.grow-diag-final" "$out"
	eval $3=\"$out\"
	echo "Done."
}
