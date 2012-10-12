BerkeleyTokenizer () {
	echo "Running: BerkeleyTokenizer..."
	pathAndName "$1" f1 n1
	btout="$OUTPATH/BTokenize($n1)"
	cat "$f1" | java -cp "$BERKELEY_PARSER:$BERKELEY_TOKENIZER" Main > "$btout"
	eval $2=\"$btout\"
	echo "Done."
}

BerkeleyParser () {
	echo "Running: BerkeleyParser..."
	pathAndName "$1" f1 n1
	pathAndName "$2" f2 n2
	bpout="$OUTPATH/BParse($n2,$n1)"
	java -jar "$BERKELEY_PARSER" -gr "$f2" -inputFile "$f1" -outputFile "$bpout"
	eval $3=\"$bpout\"
	echo "Done."
}

PennToInt () {
	echo "Running: PennToInt."
}

HyperGHKM () {
	echo "Running: HyperGHKM."
	pathAndName "$1" align nAlign
	pathAndName "$2" ecorpus nEcorpus
	pathAndName "$3" fcorpus nFcorpus
	target="$OUTPATH/HyperGHKM($nAlign,$nEcorpus,$nFcorpus)"
	java -Xmx1g -Xms1g -cp "$GHKM/ghkm.jar:$GHKM/fastutil.jar" -XX:+UseCompressedOops edu.stanford.nlp.mt.syntax.ghkm.RuleExtractor -fCorpus "$fcorpus" -eParsedCorpus "$ecorpus" -align "$align" -joshuaFormat false > "$target"
	eval $4=\"$target\"
}

GIZA () {
	echo "Running: GIZA."
}

GIZA3 () {
	echo "Running: GIZA3..."
	TMP="$OUTPATH/GIZA3_TMP"
	mkdir -p "$TMP"
	i1new="corpus.en"
	i2new="corpus.fr"
	pathAndName "$1" f1 i1orig
	pathAndName "$2" f2 i2orig
	cp "$f1" "$TMP/$i1new"
	cp "$f2" "$TMP/$i2new"
	$MOSES/scripts/training/train-model.perl -root-dir "$OUTPATH/GIZA3_TMP" --corpus "$TMP/corpus" --e en --f fr --last-step 3 --external-bin-dir="$GIZA"
	out="$OUTPATH/GIZA3($i1orig,$i2orig)"
	mv "$TMP/model/aligned.grow-diag-final" "$out"
	eval $3=\"$out\"
	rm -rf "$TMP"
}

plain2snt () {
	echo "Running: plain2snt..."
#	copy input files because of write-permission
	pathAndName "$1" f1 n1
	pathAndName "$2" f2 n2
	i1new=$OUTPATH/$n1
	i2new=$OUTPATH/$n2
	i1new=${i1new/%.txt/}
	i1new=${i1new/%.tok/}
	i2new=${i2new/%.txt/}
	i2new=${i2new/%.tok/}
	cp "$f1" "$i1new"
	cp "$f2" "$i2new"
#	determine the filenames of generated files
	g1snt="${i1new}_$(basename "$i2new").snt"
	g1vcb="${i1new}.vcb"
	g2snt="${i2new}_$(basename "$i1new").snt"
	g2vcb="${i2new}.vcb"
	$PLAIN2SNT "$i1new" "$i2new"
#	generate new filenames for output files
	o1snt="plain2snt($n1,$n2).1"
	o2snt="plain2snt($n1,$n2).2"
	o1vcb="plain2snt($n1,$n2).3"
	o2vcb="plain2snt($n1,$n2).4"
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
	pathAndName "$1" f1 in1
	pathAndName "$2" f2 in2
	out1="$OUTPATH/remEmptyLines($in1,$in2).1"
	out2="$OUTPATH/remEmptyLines($in1,$in2).2"
	$REM_EMPTY_LINES "$f1" "$f2" "$out1" "$out2"
	eval $3=\"$out1\"
	eval $4=\"$out2\"
	echo "Done."
}

EMDictionary () {
	echo "Running: EMDictionary..."
	pathAndName "$1" f1 in1
	out1="$OUTPATH/EMDictionary($in1,$2).1"
	out2="$OUTPATH/EMDictionary($in1,$2).2"
	$EMDICTIONARY best "$2" "$f1" > "$out1"
	$EMDICTIONARY csv "$2" "$f1" > "$out2"
	eval $3=\"$out1\"
	eval $4=\"$out2\"
	echo "Done."
}

EMDictionaryShowSteps () {
	echo "Running: EMDictionaryShowSteps..."
	pathAndName "$1" f1 in1
	java -cp "$EMDICTIONARYSHOW" DictViewTest "$f1" &
	echo "Done."
}

findFile () {
	if   [ -f "$1" ];
	then eval $2=\"$1\"
	else if   [ -f "$OUTPATH/$1" ];
         then eval $2=\"$OUTPATH/$1\"
	     else eval $2=\"$DATAPATH/$1\"
	     fi
	fi
}

getName () {
	name=${1#"$DATAPATH/"}
	name=${name#"$OUTPATH/"}
	name=${name#"$DATAPATH/"}
	name=${name//"/"/"#"}
	eval $2=\"$name\"
}

pathAndName () {
	findFile "$1" path
	getName "$path" name
	eval $2=\"$path\"
	eval $3=\"$name\"
}
