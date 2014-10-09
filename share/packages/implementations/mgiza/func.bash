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
	## this script reimplements the corresponding functionality in mosesdecoder
	
	## (1) prepare data
	mkdir -p "$1"

	"$MGIZA/mkcls" -c50 -n2 "-p$2" "-V$1/en.vcb.classes" opt
	"$MGIZA/mkcls" -c50 -n2 "-p$3" "-V$1/fr.vcb.classes" opt

	"$MGIZA/plain2snt.out" "$2" "$3"
	mv "$2.vcb" "$1/en.vcb"
	mv "$3.vcb" "$1/fr.vcb"
	mv "$2_$(basename $3).snt" "$1/en-fr-int-train.snt"
	mv "$3_$(basename $2).snt" "$1/fr-en-int-train.snt"

	
	## (2) run GIZA++
	mkdir -p "$1/giza.en-fr"
	"$MGIZA/snt2cooc.out" "$1/fr.vcb" "$1/en.vcb" "$1/en-fr-int-train.snt" > "$1/giza.en-fr/en-fr.cooc"

	"$MGIZA/mgiza" -CoocurrenceFile "$1/giza.en-fr/en-fr.cooc" -c "$1/en-fr-int-train.snt" -m1 5 -m2 0 -m3 3 -m4 3 -model1dumpfrequency 1 -model4smoothfactor 0.4 -ncpus "${NPROC}" -nodumps 1 -nsmooth 4 -o "$1/giza.en-fr/en-fr" -onlyaldumps 1 -p0 0.999 -s "$1/en.vcb" -t "$1/fr.vcb"

	"$MGIZA/merge_alignment.py" $1/giza.en-fr/en-fr.A3.final.part* > "$1/giza.en-fr/en-fr.A3.final"

	mkdir -p "$1/giza.fr-en"
	"$MGIZA/snt2cooc.out" "$1/en.vcb" "$1/fr.vcb" "$1/fr-en-int-train.snt" > "$1/giza.fr-en/fr-en.cooc"

	"$MGIZA/mgiza" -CoocurrenceFile "$1/giza.fr-en/fr-en.cooc" -c "$1/fr-en-int-train.snt" -m1 5 -m2 0 -m3 3 -m4 3 -model1dumpfrequency 1 -model4smoothfactor 0.4 -ncpus "${NPROC}" -nodumps 1 -nsmooth 4 -o "$1/giza.fr-en/fr-en" -onlyaldumps 1 -p0 0.999 -s "$1/fr.vcb" -t "$1/en.vcb"

	"$MGIZA/merge_alignment.py" $1/giza.fr-en/fr-en.A3.final.part* > "$1/giza.fr-en/fr-en.A3.final"

	## (3) align words
	"$MGIZA/giza2bal.pl" -d "$1/giza.en-fr/en-fr.A3.final" -i "$1/giza.fr-en/fr-en.A3.final" | "$MGIZA/symal" -alignment="grow" -diagonal="yes" -final="yes" -both="no" > "$4"

	echo -e "$3\n$2\n" > "${4}.meta"
}
