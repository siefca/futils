(ns documentation.futils.overview)

[[:chapter {:title "Introduction"}]]

"`futils` is a library that provides a set of forms that add some abstractions
for managing functions in Clojure.

Currently implemented macros and functions are:

* `argc` – counts arguments a function takes (for all arities),
* `frelax` – wraps a function in a way that it accepts any number of arguments,
* `args-relax` – like `frelax` but it requires to explicitly describe the accepted arities,
* `frepeat` – creates a sequence of results returned by the given function (which uses named arguments),
* `mapply` – works like apply but for named arguments."

[[:chapter {:title "Installation"}]]

"Add to `project.clj` dependencies: 

`[pl.randomseed/futils `\"`{{PROJECT.version}}`\"`]`

Then require it in your program:

`(require 'futils.core :as futils)`

or:

`(ns your-namespace`
`  (:require [futils.core :as futils]))`

"

[[:chapter {:title "Usage"}]]

[[:section {:title "`argc`"}]]

[[:file {:src "src-doc/futils/fn-argc.clj"}]]
