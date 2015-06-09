# Sink
# Version: 2015-06-01
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: basics
# IN in :: t0
#
# Sink tool.
SinkTool () {
	echo "Sink: $2"
}

# runs a tool
# run <id> <n> <tool> <root> <i1> ... <in> <o1> ... <om>
# <id>    job id
# <n>     number of input arguments
# <m>     number of output arguments
# <tool>  tool to run
# <root>  directory to root temp and meta files
# <i?>    input argument
# <o?>    output argument
run () {
	args=("$@")
	echo "Running: ${args[0]}"

	tmpDir="${args[3]}"
	mkdir -p "$tmpDir"
	
	logFile="${args[3]}/log"
	touch "$logFile"
	
	echo "Checking: ${args[0]}" >> "$logFile"
	inNew="${args[4]}"
	for (( i=5; i < $(( ${args[1]} + 4 )); i++ )); do
		if [[ "${args[i]}" -nt "$inNew" ]]; then
			inNew="${args[$i]}"
		fi
	done

	outOld="${args[$((${args[1]} + 4))]}"
	for (( i=$(( ${args[1]} + 4 )); i < $#; i++ )); do
		if [[ "${args[$i]}" -ot "$inNew" ]]; then
			outOld="${args[$i]}"
		fi
	done

	echo "$(date)" >> "$logFile"
	if [[ -f "$outOld" ]]; then
		if [[ "$inNew" -nt "$outOld" ]]; then
			echo "Running: ${args[0]}" >> "$logFile"
			find "$tmpDir" ! -path "$tmpDir" ! -iname "log" -delete
			(
				set -ex -o pipefail
				"${@:3}" 3>&1 &>> "$logFile" | while read line; do echo "Progress: $1@$line"; done
			)
			retVal="$?"
		else
			echo "Skipping: ${args[0]}" >> "$logFile"
			echo "  Reason: Up-to-date file(s) found." >> "$logFile"
			retVal="0"
		fi
	else
		echo "Running: ${args[0]}" >> "$logFile"
		find "$tmpDir" ! -path "$tmpDir" ! -iname "log" -delete
		(
			set -ex -o pipefail
			"${@:3}" 3>&1 &>> "$logFile" | while read line; do echo "Progress: $1@$line"; done
		)
		retVal="$?"
	fi
	echo "Returned: $retVal" >> "$logFile"

	if [[ $retVal -eq 0 ]]
	then
		echo "Done: ${args[0]}"
	else
		echo "Cancelled: ${args[0]}"
		rm -rf "${@:$((${args[1]} + 5))}"
	fi
}

# shows the progress compared to the line number of another file
#   $1 input file to compare number of lines to
PROGRESS () {
	pv -s "$(wc -l < "$1")" -l -n 2>&3
}

# shows the progress compared to the line number of another file
# where the newly generated file has a multiple of lines of the
# other file
#   $1 input file to compare number of lines to
#   $2 number of lines per each input line
PROGRESSX () {
	lines="$(wc -l < $1)"
	lines=$(expr $lines \* $2)
	pv -s "$lines" -l -n 2>&3
}
