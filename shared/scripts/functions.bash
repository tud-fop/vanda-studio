BerkeleyTokenizer () {
	echo "Running: BerkeleyTokenizer..."
	btout="$OUTPATH/BTokenize($1)"
	eval $2=\"$btout\"
	cat "$DATAPATH/$1" | java -cp "$BERKELEY_PARSER:$BERKELEY_TOKENIZER" Main > "$btout"
	echo "Done."
}

BerkeleyParser () {
	echo "Running: BerkeleyParser..."
	bpout="$OUTPATH/BParse($2,$1)"
	eval $3=\"$bpout\"
	java -jar "$BERKELEY_PARSER" -gr "$DATAPATH/$2" -inputFile "$DATAPATH/$1" -outputFile "$bpout"
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

plain2snt () {
	echo "Running: plain2snt."
}

remEmptyLines () {
	echo "Running: remEmptyLines..."
	in1=${1//"/"/"#"}
	in2=${2//"/"/"#"}
	out1="$OUTPATH/remEmptyLines($in1,$in2).1"
	eval $3=\"$out1\"
	out2="$OUTPATH/remEmptyLines($in1,$in2).2"
	eval $4=\"$out2\"
	$REM_EMPTY_LINES "$DATAPATH/$1" "$DATAPATH/$2" "$out1" "$out2"
	echo "Done."
}
