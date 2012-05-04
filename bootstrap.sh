#!/bin/sh

SHA="34cc08a2c354"

echo "Fetching Pygments..."
curl https://bitbucket.org/birkenfeld/pygments-main/get/$SHA.tar.gz -o pygments.tar.gz
tar -xvzf pygments.tar.gz
mv birkenfeld-pygments-main-$SHA resources/pygments
rm pygments.tar.gz

echo "Fetching submodules (codemirror)..."
git submodule init
git submodule update
