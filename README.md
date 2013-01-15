# RefHeap, The Reference Heap!

This project is a lightweight Clojure pastebin that uses [Pygments](http://pygments.org) for syntax highlighting.

## Usage

Clone the repo including submodules:

``` bash
git clone --recursive git://github.com/Raynes/refheap.git
```

Grab general project dependencies with lein, and grab Pygments with the provided bash script (requires [Mercurial](http://mercurial.selenic.com)). This project also requires [MongoDB](http://www.mongodb.org).

``` bash
lein deps
./bootstrap.sh
```

Start the Mongo daemon, and then start the RefHeap server using `lein`. The server will host content from [http://localhost:3000](http://localhost:3000) (unless you have `$PORT` set).

``` bash
mongod &
lein ring server
```

## Who We Are

We are [Anthony Grimes](https://github.com/Raynes) and [Alex McNamara](https://github.com/amcnamara). We like pastebins.

## License

Distributed under the Eclipse Public License, the same as Clojure; the terms of which can be found in the file epl-v10.html at the root of this disribution or at [http://opensource.org/licenses/eclipse-1.0.php](http://opensource.org/licenses/eclipse-1.0.php).
