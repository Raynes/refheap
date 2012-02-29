#!/bin/sh

rm -rf resources/pygments
curl https://bitbucket.org/birkenfeld/pygments-main/get/de3157655c32.tar.gz -o p.tar.gz
tar -xf p.tar.gz
mv birkenfeld-pygments-main-de3157655c32/ resources/pygments
rm p.tar.gz
