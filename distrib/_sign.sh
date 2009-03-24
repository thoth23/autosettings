#!/bin/bash

set -e # fail early

VERS="$1"
[ -n "$VERS" ] && VERS="${VERS}_"

function die() {
  echo "Error: " $*
  echo
  echo "USage: $0 <version>"
  echo "Automatically sign [A-Z].apk"
  exit 1
}

[ -z "$1" ] && die

shopt -s extglob  # extended glob pattern

function process() {
	SRC="$1"

	BASE="${SRC/.apk/}"
	
	DATE=`date +%Y%m%d`
	N=1
	while /bin/true; do
		EXT=`python -c "print chr(96+$N)"`
		DEST="${BASE}_${VERS}${DATE}${EXT}.apk"
		[ ! -e "$DEST" ] && break
		N=$((N+1))
		[ "$N" == "27" ] && die "$DEST exists, can't generate higher letter."
	done
	
	ALIAS="${USER/p*/lf}2"
	
	echo "Signing $SRC => $DEST with alias $ALIAS"
	
	jarsigner -verbose -keystore `cygpath -w ~/*-release-*.keystore` "$SRC" $ALIAS
	mv -v "$SRC" "$DEST"
}

for i in [tTfF]+([^_]).apk ; do
	[ -f "$i" ] && process "$i"
done
