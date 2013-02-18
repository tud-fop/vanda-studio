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
	TMP="$1"
	i1new="$TMP/corpus.en"
	i2new="$TMP/corpus.fr"
	if [[ ! -f "$i1new" ]]; then
		ln -svf "$2" "$i1new"
	fi
	if [[ ! -f "$i2new" ]]; then
		ln -svf "$3" "$i2new"
	fi
	$MOSES/scripts/training/train-model.perl -root-dir "$TMP" --corpus "$TMP/corpus" --e fr --f en --last-step 3 --external-bin-dir="$GIZA"
	mv "$TMP/model/aligned.grow-diag-final" "$4"
	unlink "$i1new"
	unlink "$i2new"
	echo -e "$3\n$2\n" > "${4}.meta"
}
