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

if [ -f "$RCPATH" ]; then
	source "$RCPATH"
fi

install_pkg () {
	cd "$1"
	source "install.bash"
	mkdir -p "$BINDIR/$binpath"
	install_me "$BINDIR/$binpath"
	cd "$1"
	echo "$varname=$BINDIR/$binpath" >> "$RCPATH"
	cp func.bash "$FUNCDIR/$id.bash"
	echo "id=\"$id\"" >> "$PKGDB/$id"
	echo "varname=\"$varname\"" >> "$PKGDB/$id"
	echo "version=\"$version\"" >> "$PKGDB/$id"
	echo "binpath=\"$binpath\"" >> "$PKGDB/$id"
}

install () {
# filter packages that are not existing
	declare -i i=0
	for pkg in "$@"; do
		if [ -f "$pkg" ]; then
			pkgs[$i]="$pkg"
			((++i))
		else
			echo_color "The file $pkg does not exist, skipping."
		fi
	done

# configure packages
	declare -i j=1
	echo_color "Configuring."

# merging files
	mkdir -p "$TMP"
	touch "$TMP/install.bash"
	for (( i=0; i<${#pkgs[@]}; i+=1 )); do
		mkdir -p "$TMP/${pkgs[$i]}"
		tar xf "${pkgs[$i]}" --strip 1 -C "$TMP/${pkgs[$i]}"
		echo "<<<< ${pkgs[$i]}" >> "$TMP/install.bash"
		cat "$TMP/${pkgs[$i]}/install.bash" >> "$TMP/install.bash"
		echo "" >> "$TMP/install.bash"
		echo ">>>> ${pkgs[$i]}" >> "$TMP/install.bash"
	done

# configuring
	read -p "Edit install.bash? [y/N]" choice
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
		extract_subfile "$TMP/install.bash" "${pkgs[$i]}" "$TMP/${pkgs[$i]}/install.bash"
	done

# install packages
	declare -i j=1
	for (( i=0; i<${#pkgs[@]}; i+=1 )); do
		echo_color "[$j/${#pkgs[@]}] Installing \"${pkgs[$i]}\"..."
		install_pkg "$TMP/${pkgs[$i]}" "${pkgs[$i]}"
		echo_color "[$j/${#pkgs[@]}] Done."
		((j+=1))
	done
	rm -r "$TMP"
}

list () {
	for f in $(ls $PKGDB); do
		source "$PKGDB/$f"
		echo "$f [$version]"
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

"${@:1}"
