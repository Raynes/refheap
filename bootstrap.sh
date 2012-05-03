#!/bin/sh

SHA="34cc08a2c354"

echo "Fetching Pygments..."
curl https://bitbucket.org/birkenfeld/pygments-main/get/$SHA.zip -o pygments.zip
unzip pygments.zip
mv birkenfeld-pygments-main-$SHA resources/pygments
rm pygments.zip

echo "Fetching submodules (codemirror)..."
git submodule init
git submodule update
