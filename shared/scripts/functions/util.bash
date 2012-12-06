plain2snt () {
	echo "Running: plain2snt..."
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
	g1snt="${i1new}_$(basename "$i2new").snt"
	g1vcb="${i1new}.vcb"
	g2snt="${i2new}_$(basename "$i1new").snt"
	g2vcb="${i2new}.vcb"
	$PLAIN2SNT "$i1new" "$i2new"
	o1snt="plain2snt($n1,$n2).0"
	o2snt="plain2snt($n1,$n2).1"
	o1vcb="plain2snt($n1,$n2).2"
	o2vcb="plain2snt($n1,$n2).3"
	mv "$g1snt" "$o1snt"
	mv "$g1vcb" "$o1vcb"
	mv "$g2snt" "$o2snt"
	mv "$g2vcb" "$o2vcb"
	eval $3=\"$o1snt\"
	eval $4=\"$o2snt\"
	eval $5=\"$o1vcb\"
	eval $6=\"$o2vcb\"
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
