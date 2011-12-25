# The Refuse Heap!

This is a little Clojure pastebin project that shells out to pygmentize for syntax highlighting. As such, it supports generally whatever Pygments supports. The goal of the project is to create a pastebin that (hopefully) runs on Heroku and exceeds the feature and awesomeness offering of Github's gist.

One thing this project is not going to do is use git for pastes. I've never really understood why that was such a great thing and it simply would not work on Heroku. I do, however, think that revisions is an excellent Gist feature, so that's something I'll be looking into.

The project is completely new and unusable right now. It is also totally experimental. If something cool and useful comes out of it, it is a total plus.

## Usage

```bash
lein deps
lein run
```

## License

Distributed under the Eclipse Public License, the same as Clojure.

