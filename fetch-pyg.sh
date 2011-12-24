#!/bin/sh

wget http://pypi.python.org/packages/source/P/Pygments/Pygments-1.4.tar.gz#md5=d77ac8c93a7fb27545f2522abe9cc462
tar -xvf Pygments-1.4.tar.gz
rm Pygments-1.4.tar.gz
mv Pygments-1.4 resources/pygments
