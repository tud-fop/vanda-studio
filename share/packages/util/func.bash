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