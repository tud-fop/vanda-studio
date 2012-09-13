BerkeleyTokenizer () {
	echo "Running: BerkeleyTokenizer..."
	btout="$OUTPATH/BTokenize($1)"
	cat "$DATAPATH/$1" | java -cp "$BERKELEY_PARSER:$BERKELEY_TOKENIZER" Main > "$btout"
	eval $2=\"$btout\"
	echo "Done."
}

BerkeleyParser () {
	echo "Running: BerkeleyParser..."
	bpout="$OUTPATH/BParse($2,$1)"
	java -jar "$BERKELEY_PARSER" -gr "$DATAPATH/$2" -inputFile "$DATAPATH/$1" -outputFile "$bpout"
	eval $3=\"$bpout\"
	echo "Done."
}

PennToInt () {
	echo "Running: PennToInt."
}

HyperGHKM () {
	echo "Running: HyperGHKM."
}

GIZA () {
	echo "Running: GIZA."
}

GIZA3 () {
	echo "Running: GIZA3..."
	TMP="$OUTPATH/GIZA3_TMP"
	mkdir -p "$TMP"
	i1orig=${1//"/"/"#"}
	i2orig=${2//"/"/"#"}
	i1new="corpus.en"
	i2new="corpus.fr"
	cp "$DATAPATH/$1" "$TMP/$i1new"
	cp "$DATAPATH/$2" "$TMP/$i2new"
	$MOSES/scripts/training/train-model.perl -root-dir "$OUTPATH/GIZA3_TMP" --corpus "$TMP/corpus" --e en --f fr --last-step 3 --external-bin-dir="$GIZA"
	out="$OUTPATH/GIZA3($i1orig,$i2orig)"
	mv "$TMP/model/aligned.grow-diag-final" "$out"
	eval $3=\"$out\"
	rm -rf "$TMP"
}

plain2snt () {
	echo "Running: plain2snt..."
#	copy input files because of write-permission
	i1new=$OUTPATH/${1//"/"/"#"}
	i2new=$OUTPATH/${2//"/"/"#"}
	i1new=${i1new/%.txt/}
	i1new=${i1new/%.tok/}
	i2new=${i2new/%.txt/}
	i2new=${i2new/%.tok/}
	cp "$DATAPATH/$1" "$i1new"
	cp "$DATAPATH/$2" "$i2new"
#	determine the filenames of generated files
	g1snt="${i1new}_$(basename "$i2new").snt"
	g1vcb="${i1new}.vcb"
	g2snt="${i2new}_$(basename "$i1new").snt"
	g2vcb="${i2new}.vcb"
	$PLAIN2SNT "$i1new" "$i2new"
#	generate new filenames for output files
	o1snt="plain2snt($(basename "$i1new"),$(basename "$i2new")).1"
	o2snt="plain2snt($(basename "$i1new"),$(basename "$i2new")).2"
	o1vcb="plain2snt($(basename "$i1new"),$(basename "$i2new")).3"
	o2vcb="plain2snt($(basename "$i1new"),$(basename "$i2new")).4"
#	rename generated files to intended names
	mv "$g1snt" "$o1snt"
	mv "$g1vcb" "$o1vcb"
	mv "$g2snt" "$o2snt"
	mv "$g2vcb" "$o2vcb"
#	tell vanda-studio the output filenames
	eval $3=\"$o1snt\"
	eval $4=\"$o2snt\"
	eval $5=\"$o1vcb\"
	eval $6=\"$o2vcb\"
	echo "Done."
}

remEmptyLines () {
	echo "Running: remEmptyLines..."
	in1=${1//"/"/"#"}
	in2=${2//"/"/"#"}
	out1="$OUTPATH/remEmptyLines($in1,$in2).1"
	out2="$OUTPATH/remEmptyLines($in1,$in2).2"
	$REM_EMPTY_LINES "$DATAPATH/$1" "$DATAPATH/$2" "$out1" "$out2"
	eval $3=\"$out1\"
	eval $4=\"$out2\"
	echo "Done."
}
