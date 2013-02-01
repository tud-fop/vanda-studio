source "$FUNCDIR/util.bash"

# GIZA
# Version: 2012-09-12
# Contact: Matthias.Buechse@tu-dresden.de
# Category: alignments
# IN english corpus :: SentenceCorpus
# IN french corpus :: SentenceCorpus
# OUT alignments :: Alignments
#
# computes alignments and translation tables
GIZA () {
	echo "Running: GIZA..."
	TMP="$OUTPATH/GIZA_TMP"
	mkdir -p "$TMP"
	i1new="corpus.en"
	i2new="corpus.fr"
	ln -s "$1" "$TMP/$i1new"
	ln -s "$2" "$TMP/$i2new"
	$MOSES/scripts/training/train-model.perl -root-dir "$TMP" --corpus "$TMP/corpus" --e fr --f en --last-step 3 --external-bin-dir="$GIZA"
	mv "$TMP/model/aligned.grow-diag-final" "$3"
	unlink "$TMP/$i1new"
	unlink "$TMP/$i2new"
	rm -r "$TMP"
	echo "Done."
}
