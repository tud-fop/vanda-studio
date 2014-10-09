#!/bin/bash

set -e

# Configuration
VANDASTUDIO="$HOME/.vanda"
BINDIR="$VANDASTUDIO/bin"
FUNCDIR="$VANDASTUDIO/functions"
IFACEDIR="$VANDASTUDIO/interfaces"
OUTDIR="$VANDASTUDIO/output"
INDIR="$VANDASTUDIO/input"
RCPATH="$VANDASTUDIO/vandarc"
PKGDB="$VANDASTUDIO/packages"

TMP="/tmp/vandapkg/$(date +%s)"

mkdir -p "$VANDASTUDIO"

if [ ! -f "$RCPATH" ]; then
		echo -e "#!/bin/bash\n"                > "$RCPATH"
		echo    "DATAPATH=$INDIR"             >> "$RCPATH"
		echo    "OUTPATH=$OUTDIR"             >> "$RCPATH"
		echo -e "FUNCDIR=$FUNCDIR\n"          >> "$RCPATH"
		echo -e "if [[ -f \"$FUNCDIR/util.bash\" ]]\nthen\n\tsource $FUNCDIR/util.bash\nfi" >> "$RCPATH"
fi

source "$RCPATH"

install_pkg () {
	cd "$1"
	source "install.bash"
	mkdir -p "$BINDIR/$binpath"
	install_me "$BINDIR/$binpath" "$INDIR"
	cd "$1"
	cp func.bash "$FUNCDIR/$id.bash"
	if [ ! -f "$PKGDB/$id" ]
	then
		echo "$varname=$BINDIR/$binpath" >> "$RCPATH"
	fi
	echo "id=\"$id\"" > "$PKGDB/$id"
	echo "varname=\"$varname\"" >> "$PKGDB/$id"
	echo "version=\"$version\"" >> "$PKGDB/$id"
	echo "binpath=\"$binpath\"" >> "$PKGDB/$id"
}

install () {
# filter packages that are not existing
	declare -i i=0
	declare -i interfaces=0
	
	for pkg in "$@"; do
		if [ -f "$pkg" ]; then
			if [[ "$pkg" == *.tar.gz ]]; then
				pkgs[$i]="$pkg"
				((++i))
			elif [[ "$pkg" == *.xml ]]; then
				echo_color "[-/-] Installing \"$pkg\"..."
				cp "$pkg" "$IFACEDIR/."
				((++interfaces))
				echo_color "[-/-] Done."
			fi
		else
			echo_color "The file $pkg does not exist, skipping."
		fi
	done
	
	if [ 0 == ${#pkgs[@]} ]; then
		if [ 0 == ${interfaces} ]; then
			return 1
		else
			return 0
		fi
	fi

# configure packages
	declare -i j=1
	echo_color "Configuring."

# merging files
	mkdir -p "$TMP"
	touch "$TMP/install.bash"
	for (( i=0; i<${#pkgs[@]}; i+=1 )); do
		mkdir -p "$TMP/${pkgs[$i]}"
		tar xf "${pkgs[$i]}" -C "$TMP/${pkgs[$i]}"
		name=$(ls "$TMP/${pkgs[$i]}")
		echo "<<<< $name" >> "$TMP/install.bash"
		cat "$TMP/${pkgs[$i]}/$name/install.bash" >> "$TMP/install.bash"
		echo "" >> "$TMP/install.bash"
		echo ">>>> $name" >> "$TMP/install.bash"
	done

# configuring
	read -p "Edit install.bash [y/N]? " choice
	if [ "$choice" == "y" ]; then
		if [[ -z "$EDITOR" ]]; then
			read -p "Variable EDITOR not set. Editor command to use: " editor
			$editor "$TMP/install.bash"
		else
			$EDITOR "$TMP/install.bash"
		fi
	fi

# dissecting files
	for (( i=0; i<${#pkgs[@]}; i+=1 )); do
		name=$(ls "$TMP/${pkgs[$i]}")
		extract_subfile "$TMP/install.bash" "$name" "$TMP/${pkgs[$i]}/$name/install.bash"
	done

# install packages
	declare -i j=1
	for (( i=0; i<${#pkgs[@]}; i+=1 )); do
		name=$(ls "$TMP/${pkgs[$i]}")
		echo_color "[$j/${#pkgs[@]}] Installing \"$name\"..."
		install_pkg "$TMP/${pkgs[$i]}/$name"
		echo_color "[$j/${#pkgs[@]}] Done."
		rm -rf "$TMP/${pkgs[$i]}"
		((j+=1))
	done
	rm -rf "$TMP"
}

list () {
	for f in $(ls "$PKGDB"); do
		source "$PKGDB/$f"
		echo "$f [$version]"
	done
}

list-interfaces () {
	for f in $(ls "$IFACEDIR"); do
		echo "${f%.xml} [$(grep 'version=' "$IFACEDIR/$f" | sed 's/^.*version=\"\(.*\)\"/\1/g' | sort -r | head -n 1)]"
	done
}

remove_pkg () {
	source "$1"
	rm -rf "$BINDIR/$binpath"
	rm -f "$FUNCDIR/$id.bash"
	sed -i".bak" "/$varname/d" "$RCPATH"
	rm -f "$1"
}

remove () {
	declare -i i=1
	echo "Removing $@."
	for pkg in "$@"; do
		if [ -f "$PKGDB/$pkg" ]; then
			remove_pkg "$PKGDB/$pkg"
			echo_color "[$i/$#] Removed \"$pkg\"."
		else
			echo_color "[$i/$#] The package \"$pkg\" is not installed, skipping."
		fi
		i+=1
	done
}

remove-iface () {
	declare -i i=1
	echo "Removing $@."
	for iface in "$@"; do
		file="${IFACEDIR}/${iface}.xml"
		if [ -f "$file" ]; then
			rm "$file"
			echo_color "[$i/$#] Removed \"$iface\"."
		else
			echo_color "[$i/$#] The interface \"$iface\" is not installed, skipping."
		fi
		i+=1
	done
}

echo_color () {
	echo -e "\033[32m$1\033[m"
}

extract_subfile () {
	l1=$(sed -n "/<<<< $2/=" "$1" | head -1)
	l2=$(sed -n "/>>>> $2/=" "$1" | head -1)
	((l1+=1))
	((l2+=-1))
	sed -n $l1,$l2'p' "$1" > "$3"
}

make_package () {
	echo -n "[$2/$3] Checking folder... "
	declare -i e=0
	path="$(dirname $1)"
	name="${1##*/}"
	if [ ! -d "$1" ]; then
		echo "\"$1\" is not a directory."
		return 1
	fi
	if [ ! -f "$1/func.bash" ]; then
		echo "\"$1/func.bash\" does not exist."
		e+=1
	fi
	if [ ! -f "$1/install.bash" ]; then
		echo "\"$1/install.bash\" does not exist."
		return 1
	fi
	source "$1/install.bash"
	if [ -z "$id" ]; then
		echo "The variable \"id\" is not set."
		e+=1
	fi
	if [ -z "$varname" ]; then
		echo "The variable \"varname\" is not set."
		e+=1
	fi
	if [ -z "$version" ]; then
		echo "The variable \"version\" is not set."
		e+=1
	fi
	if [ -z "$binpath" ]; then
		echo "The variable \"binpath\" is not set."
		e+=1
	fi
	if [ "$id" != "$name" ]; then
		echo "The folder name does not match the package name."
		e+=1
	fi
	declare -F install_me > /dev/null || {
		echo "The function \"install_me\" does not exist."
		e+=1
	}
	if [[ 1 -gt $e ]]; then
		echo "Success."
		echo -n "[$2/$3] Packing archive... "
		tar czf "${name}.tar.gz" -C "${path}" "${name}"
		echo "Done."
		return 0
	else
		echo "Failed. Aborting."
		return $e
	fi
}

usage () {
	echo "usage: ./vandapkg.bash <command> [<args>]"
	echo " ./vandapkg.bash install <pkgfiles> # installs packages from a TAR.GZ-file"
	echo " ./vandapkg.bash remove <pkgnames>  # removes packages"
	echo " ./vandapkg.bash makepkg <pkgdir>   # build a package from a directory"
	echo " ./vandapkg.bash list               # shows a list of all installed packages"
	echo " ./vandapkg.bash list-interfaces    # shows a list of all installed interfaces"
}

makepkg () {
	declare -i i=1
	for f in "$@"; do
		echo_color "[$i/$#] Packing $f..."
		make_package "$f" "$i" "$#"
		((++i))
	done
}

case "$1" in
	install|remove|remove-iface|makepkg)
		if [[ -n "$2" ]]; then
			"${@:1}"
		else
			usage
		fi
	;;
	list)
		list ;;
	list-interfaces)
		list-interfaces ;;
	*)
		usage ;;
esac
