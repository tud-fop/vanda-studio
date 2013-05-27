# JobstTrain
# Version: 2013-05-27
# Contact: Matthias.Buechse@tu-dresden.de
# IN english corpus :: SentenceCorpus
# IN french corpus :: SentenceCorpus
# OUT model :: JobstModel
JobstTrain () {
	rm -rf "$1/*"
	i1new="$1/corpus.en"
	i2new="$1/corpus.fr"
	if [[ ! -f "$i1new" ]]; then
		ln -svf "$2" "$i1new"
	fi
	if [[ ! -f "$i2new" ]]; then
		ln -svf "$3" "$i2new"
	fi
	(sleep 4 && echo "\n") | "$JOBST/translate" "$1/corpus" en fr train
	mv "$1/corpus_fr_en.bigram" "$4_fr_en.bigram"
	mv "$1/corpus_fr_en.length" "$4_fr_en.length"
	mv "$1/corpus_fr_en.dictionary" "$4_fr_en.dictionary"
	mv "$1/corpus_en.words" "$4_en.words"
	mv "$1/corpus_fr.words" "$4_fr.words"
	touch "$4"
	unlink "$i1new"
	unlink "$i2new"
	# echo -e "$3\n$2\n" > "${4}.meta"
}

# JobstTranslate
# IN model :: JobstModel
# IN french corpus :: SentenceCorpus
# OUT english corpus :: SentenceCorpus
JobstTranslate () {
	"$JOBST/translate" "$2" en fr decode < "$3" > "$4"
}
