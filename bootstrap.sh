#!/bin/sh

PYGMENTS_SHA="2479eb54f22b"
CODEMIRROR_SHA="43379968f6a62b89e2b350acd9174971e3e50706"

echo "Fetching Pygments..."
curl https://bitbucket.org/birkenfeld/pygments-main/get/$PYGMENTS_SHA.tar.gz -o pygments.tar.gz
tar -xvzf pygments.tar.gz
mkdir -p resources/pygments
rm -rf resources/pygments/
mv birkenfeld-pygments-main-$PYGMENTS_SHA resources/pygments
rm pygments.tar.gz

echo "Fetching Codemirror..."
curl https://github.com/marijnh/CodeMirror/tarball/$CODEMIRROR_SHA -Lo codemirror.tar.gz
tar -xvzf codemirror.tar.gz
rm -rf resources/public/js/codemirror
mkdir -p resources/public/js/codemirror
mv marijnh-CodeMirror-*/* resources/public/js/codemirror
rm -rf marijnh-CodeMirror-*
rm codemirror.tar.gz