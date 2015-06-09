# JobstTrain
# Version: 2015-06-02
# Contact: Tobias.Denkinger@tu-dresden.de
# IN english corpus :: SentenceCorpus
# IN french corpus :: SentenceCorpus
# IN time :: Integer
# OUT model :: JobstModel
JobstTrain () {
	rm -rf "$1/*"
	ln -frsv "$2" "$1/corpus.en"
	ln -frsv "$3" "$1/corpus.fr"
	(sleep "$4" && echo)  \
		| "$JOBST/translate" "$1/corpus" en fr train  \
		| grep -v '\. loop$'  # filter out tons of useless messages
	mv "$1/corpus_fr_en.bigram" "${5}_fr_en.bigram"
	mv "$1/corpus_fr_en.length" "${5}_fr_en.length"
	mv "$1/corpus_fr_en.dictionary" "${5}_fr_en.dictionary"
	mv "$1/corpus_en.words" "${5}_en.words"
	mv "$1/corpus_fr.words" "${5}_fr.words"
	touch "$5"
	# echo -e "$3\n$2\n" > "${4}.meta"
}

# JobstTranslate
# Version: 2014-02-06
# Contact: Tobias.Denkinger@tu-dresden.de
# IN model :: JobstModel
# IN french corpus :: SentenceCorpus
# OUT english corpus :: SentenceCorpus
JobstTranslate () {
	"$JOBST/translate" "$2" en fr decode < "$3" > "$4"
}
