# RefHeap, The Reference Heap!

This project is a lightweight Clojure pastebin that uses [Pygments](http://pygments.org) for syntax highlighting.

## Usage

Grab general project dependencies with lein, and grab Pygments with the provided bash script (requires [Mercurial](http://mercurial.selenic.com)).  This project also requires [MongoDB](http://www.mongodb.org).

     sh-tty0$ lein deps
     sh-tty0$ ./fetch-pyg.sh

Start the Mongo daemon, and in a separate terminal start the RefHeap server using lein.

     sh-tty0$ mongod
     sh-tty1$ lein run

The server will serve content from [http://localhost:8080](http://localhost:8080).

## Who We Are

We are [Anthony Grimes](https://github.com/Raynes) and [Alex McNamara](https://github.com/amcnamara). We like pastebins.

## License

Distributed under the Eclipse Public License, the same as Clojure; the terms of which can be found in the file epl-v10.html at the root of this disribution or at [http://opensource.org/licenses/eclipse-1.0.php](http://opensource.org/licenses/eclipse-1.0.php).

This project uses graphic assets which are part of the [PRZE](http://rrze-icon-set.berlios.de) icon set, which is licensed under Creative Commons Attribution-ShareAlike 3.0 Unported.