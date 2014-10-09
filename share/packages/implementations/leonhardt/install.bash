id="leonhardt"
varname="STATE_SPLITTING"
version="2013-07-17"
binpath="$id"

install_me () {
	cd HMM-SS
	mkdir classes
	javac -sourcepath "src" "src/hmm/main/Main.java" -Xlint:unchecked -d "classes"
	jar -cfvm "HMM-SS.jar" "manifest"  -C classes /
	cp "HMM-SS.jar" $1/HMM-SS.jar
	chmod +x $1/HMM-SS.jar
}
