# RefHeap, The Reference Heap!

This project is a lightweight Clojure pastebin that uses [Pygments](http://pygments.org) for syntax highlighting.

## Usage

Grab general project dependencies with lein, and grab Pygments with the provided bash script. This project also requires [MongoDB](http://www.mongodb.org).

```bash
$ lein deps
$ ./bootstrap.sh
```

Get CodeMirror and mousetrap by updating git submodules (you need to have the actual refheap repo):

```bash
$ git submodule update --init`
```

Start the Mongo daemon, and in a separate terminal start the RefHeap server using lein. The server will host content from [http://localhost:3000](http://localhost:3000).

```bash
$ mongod
$ lein ring server
```

## License

Distributed under the Eclipse Public License, the same as Clojure; the terms of which can be found in the file epl-v10.html at the root of this disribution or at [http://opensource.org/licenses/eclipse-1.0.php](http://opensource.org/licenses/eclipse-1.0.php).
