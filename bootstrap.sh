#!/bin/sh

SHA="34cc08a2c354"

mkdir -p tmp/jython-full

echo "Fetching Pygments..."
curl https://bitbucket.org/birkenfeld/pygments-main/get/$SHA.tar.gz -o pygments.tar.gz
tar -xvzf pygments.tar.gz
mv birkenfeld-pygments-main-$SHA tmp/pygments
rm pygments.tar.gz

echo "Fetching Jython..."
curl http://raynes.me/hfiles/jython_installer-2.5.2.jar -o jython_installer-2.5.2.jar

echo "Running the Jython installer..."
java -jar jython_installer-2.5.2.jar -s -d tmp/jython

# http://dev.bostone.us/2010/12/01/python-pygments-in-java-with-jython/#awp::2010/12/01/python-pygments-in-java-with-jython/
echo "Creating custom jar..."
mv tmp/jython/jython.jar tmp/jython-full
(cd tmp/jython-full && jar xvf jython.jar)
rm tmp/jython-full/jython.jar
mv tmp/jython/Lib tmp/jython-full
mv tmp/pygments/pygments tmp/jython-full/Lib
(cd tmp/jython-full && jar cvf ../../jython-full.jar .)

echo "Cleaning up..."
rm -rf tmp jython_installer-2.5.2.jar
