# The Refuse Heap!

This is a little Clojure pastebin project that shells out to pygmentize for syntax highlighting. As such, it supports all of the languages that Pygments supports. The goal for the project is to create a pastebin that will run on Heroku and is mostly on-par with the features that gist provides. Doing other cool innovative stuff would be nice too.

The running on Heroku bit means that storing things on the file system is a no-no. That means no git-powered revisions. I've never really used gist's git stuff and I don't really see the point in pulling and forking pastes, so I don't really care about that. The one thing that *is* useful, IMO, is revisions, so that'd be nice to have as well. It just wont be powered by git.

The project is completely new and unusable right now.

## Usage

```bash
lein deps
lein run
```

## License

Distributed under the Eclipse Public License, the same as Clojure.

