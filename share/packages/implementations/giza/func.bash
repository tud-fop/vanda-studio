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
	## this script reimplements the corresponding functionality in mosesdecoder
	
	## (1) prepare data
	mkdir -p "$1"

	"$GIZA/mkcls" -c50 -n2 "-p$2" "-V$1/en.vcb.classes" opt
	"$GIZA/mkcls" -c50 -n2 "-p$3" "-V$1/fr.vcb.classes" opt

	"$GIZA/plain2snt.out" "$2" "$3"
	mv "$2.vcb" "$1/en.vcb"
	mv "$3.vcb" "$1/fr.vcb"
	mv "$2_$(basename $3).snt" "$1/en-fr-int-train.snt"
	mv "$3_$(basename $2).snt" "$1/fr-en-int-train.snt"

	
	## (2) run GIZA++
	mkdir -p "$1/giza.en-fr"
	"$GIZA/snt2cooc.out" "$1/fr.vcb" "$1/en.vcb" "$1/en-fr-int-train.snt" > "$1/giza.en-fr/en-fr.cooc"

	"$GIZA/GIZA++" -CoocurrenceFile "$1/giza.en-fr/en-fr.cooc" -c "$1/en-fr-int-train.snt" -m1 5 -m2 0 -m3 3 -m4 3 -model1dumpfrequency 1 -model4smoothfactor 0.4 -nodumps 1 -nsmooth 4 -o "$1/giza.en-fr/en-fr" -onlyaldumps 1 -p0 0.999 -s "$1/en.vcb" -t "$1/fr.vcb"

	mkdir -p "$1/giza.fr-en"
	"$GIZA/snt2cooc.out" "$1/en.vcb" "$1/fr.vcb" "$1/fr-en-int-train.snt" > "$1/giza.fr-en/fr-en.cooc"

	"$GIZA/GIZA++" -CoocurrenceFile "$1/giza.fr-en/fr-en.cooc" -c "$1/fr-en-int-train.snt" -m1 5 -m2 0 -m3 3 -m4 3 -model1dumpfrequency 1 -model4smoothfactor 0.4 -nodumps 1 -nsmooth 4 -o "$1/giza.fr-en/fr-en" -onlyaldumps 1 -p0 0.999 -s "$1/fr.vcb" -t "$1/en.vcb"

	## (3) align words
	"$GIZA/giza2bal.pl" -d "$1/giza.en-fr/en-fr.A3.final" -i "$1/giza.fr-en/fr-en.A3.final" | "$GIZA/symal" -alignment="grow" -diagonal="yes" -final="yes" -both="no" > "$4"

	echo -e "$3\n$2\n" > "${4}.meta"
}