(ns documentation.futils.overview)

[[:chapter {:title "Introduction"}]]

"
`futils` is a library that provides a set of forms that add some abstractions
for managing functions in Clojure.

Currently implemented macros and functions are:

* [`argc`](#argc) – counts arguments a function takes (for all arities),
* [`relax`](#relax) – wraps a function in a way that it accepts any number of
  arguments,
* [`args-relax`](#args-relax) – like `relax` but it requires to explicitly
  describe the accepted arities,
* [`frepeat`](#frepeat) – creates a sequence of returned values using a function
  with named parameters,
* [`mapply`](#mapply) – works like apply but for named arguments.
"

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

[[:section {:title "argc" :tag "argc"}]]

[[{:tag "argc-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.core/argc f & options))

"
Determines the number of arguments that the given function takes and returns
a map containing these keys:
  
* `:arities`  – a sorted set of argument counts for all arities,
* `:engine`:
  * `:clj` – if metadata were used to determine arities – DEPRECATED);
  * `:jvm` – if Java reflection methods were used to determine arities),
* `:macro`    – a flag informing whether the given object is a macro,
* `:variadic` – a flag informing whether the widest arity is variadic.

Variadic parameter is counted as one of the possible arguments.
  
The macro flag (`:macro`) is only present when macro was detected.
  
If the given argument cannot be used to obtain a Var bound to a functon or
a function object then it returns `nil`.
"

[[:file {:src "test/futils/core/argc.clj"}]]

[[:section {:title "relax" :tag "relax"}]]

[[{:tag "relax-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.core/relax f & options))

"
Returns a variadic function object that calls the given function `f`,
adjusting the number of passed arguments to a nearest arity. It cuts argument
list or pads it with `nil` values if necessary.

The arities will be obtained from metadata (if the given object is a symbol
bound to a `Var` or a `Var` object itself) or using JVM reflection calls to
anonymous class representing a function object (in case of function object).

To determine the number of arguments the nearest arity is picked up by matching
a number of passed arguments to number of arguments for each arity. If there is
no exact match then the next arity capable of taking all arguments is selected.

If the expected number of arguments is lower than a number of arguments
actually passed to a wrapper call, the exceeding ones will be ignored.

If the declared number of arguments that the original function expects is higher
than a number of arguments really passed then `nil` values will be placed as extra
arguments.

When a variadic function is detected and its variadic arity is the closest to
a number of arguments passed then all of them will be used during a function
call (no argument will be ignored).
  
It takes optional named arguments:
  
* `:pad-fn` – a function that generates values for padding,
* `:pad-val` – a value to use for padding instead of `nil`,  
* `:verbose` – a switch (defaults to false) that if set to true causes wrapper to
                 return a map containing additional information.
"

[[:file {:src "test/futils/core/relax.clj"}]]

[[:section {:title "frepeat" :tag "frepeat"}]]

[[{:tag "frepeat-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.core/frepeat n? f kvs?))

"
Returns a lazy sequence of results produced by the given function `f` which
should accept named arguments.

If the first argument passed to frepeat is a number (`n`) and the second is
a function (`f`) it will limit the iterations to the specified count.

If the numeric argument is missing and only a function object is given the
frepeat will produce infinite sequence of calls.

The last, optional argument should be a map (`kvs`) that initializes named
arguments that will be passed to the first and subsequent calls to `f`.

Additionally each call to `f` will pass the following keyword arguments:

* `:iteration` – a number of current iteration (starting from 1),
* `:previous` – a result of the previous call to `f`.

The first call to `f` will pass the following:

*  `:iteration` – 1,
*  `:iterations` – a total number of iterations (if nr was given).

It is possible to set the initial value of `:previous` if there is a need for
that (by passing it to `frepeat`) or shadow the value assigned to `:iterations`
after the first call (by setting it in the passed function `f`).

Values associated with `:iteration` and `:previous` keys will always change
during each call.
"
