# GIZA
# Version: 2013-08-16
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: alignments
# IN english corpus :: SentenceCorpus
# IN french corpus :: SentenceCorpus
# OUT alignments :: Alignments
#
# computes alignments and translation tables
GIZA () {
	rm -rf "$1/*"
	i1new="$1/corpus.en"
	i2new="$1/corpus.fr"
	if [[ ! -f "$i1new" ]]; then
		iconv -f utf-8 -t ASCII//TRANSLIT "$2" > "$i1new"
	fi
	if [[ ! -f "$i2new" ]]; then
		iconv -f utf-8 -t ASCII//TRANSLIT "$3" > "$i2new"
	fi
	"$MGIZA/mosesdecoder/scripts/training/train-model.perl" -mgiza -root-dir "$1" --corpus "$1/corpus" --e fr --f en --last-step 3 --external-bin-dir="$MGIZA/giza"
	mv "$1/model/aligned.grow-diag-final" "$4"
#	unlink "$i1new"
#	unlink "$i2new"
	echo -e "$3\n$2\n" > "${4}.meta"
}
