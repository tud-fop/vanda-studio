plain2snt () {
	i1new="$OUTPATH/i1New"
	i2new="$OUTPATH/i2New"
	i1new=${i1new/%.txt/}
	i1new=${i1new/%.tok/}
	i2new=${i2new/%.txt/}
	i2new=${i2new/%.tok/}
	cp "$1" "$i1new"
	cp "$2" "$i2new"
	g1snt="${i1new}_$(basename "$i2new").snt"
	g1vcb="${i1new}.vcb"
	g2snt="${i2new}_$(basename "$i1new").snt"
	g2vcb="${i2new}.vcb"
	$PLAIN2SNT "$i1new" "$i2new"
	o1snt="plain2snt($n1,$n2).0"
	o2snt="plain2snt($n1,$n2).1"
	o1vcb="plain2snt($n1,$n2).2"
	o2vcb="plain2snt($n1,$n2).3"
	mv "$g1snt" "$3"
	mv "$g1vcb" "$5"
	mv "$g2snt" "$4"
	mv "$g2vcb" "$6"
}


# runs a tool
# run <n> <tool> <root> <i1> ... <in> <o1> ... <om>
# <n>     number of input arguments
# <m>     number of output arguments
# <tool>  tool to run
# <root>  directory to root temp and meta files
# <i?>    input argument
# <o?>    output argument
run () {
	args=("$@")
	echo "Running: ${args[2]}"
	mkdir -p "${args[2]}"
	
	logFile="${args[2]}/log"
	touch "$logFile"
	
	echo "Checking: ${args[2]}" >> "$logFile"
	inNew="${args[3]}"
	for (( i=4; i < $(( ${args[0]} + 3 )); i++ )); do
		if [[ "${args[i]}" -nt "$inNew" ]]; then
			inNew="${args[$i]}"
		fi
	done

	outOld="${args[$((${args[0]} + 3))]}"
	for (( i=$(( ${args[0]} + 3 )); i < $#; i++ )); do
		if [[ "${args[$i]}" -ot "$inNew" ]]; then
			outOld="${args[$i]}"
		fi
	done

	echo "$(date)" >> "$logFile"
	if [[ -f "$outOld" ]]; then
		if [[ "$inNew" -nt "$outOld" ]]; then
			echo "Running: ${args[2]}" >> "$logFile"
			"${@:2}" &>> "$logFile"
			echo "Returned: $?" >> "$logFile"
		else
			echo "Skipping: ${args[2]}" >> "$logFile"
			echo "  Reason: Up-to-date file(s) found." >> "$logFile"
		fi
	else
		echo "Running: ${args[2]}" >> "$logFile"
		"${@:2}" &>> "$logFile"
		echo "Returned: $?" >> "$logFile"
	fi

	echo "Done: ${args[2]}"
}

findFile () {
	if   [ -f "$OUTPATH/$1" ];
	then eval $2=\"$OUTPATH/$1\"
	else if   [ -f "$DATAPATH/$1" ];
         then eval $2=\"$DATAPATH/$1\"
	     else eval $2=\"$1\"
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
