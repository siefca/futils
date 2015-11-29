(ns documentation.futils.overview)

[[:chapter {:title "Introduction"}]]

"
`futils` is a library that provides a set of forms that add some abstractions
for managing functions in Clojure.

Currently provided macros and functions are:

* [`futils.args/argc`](#argc) – counts arguments a function takes (for all arities),
* [`futils.args/relax`](#relax) – wraps a function in a way that it accepts any number of
  arguments,
* [`futils.args/relax*`](#relax*) – like `relax` but it requires to explicitly
  describe the accepted arities,
* [`futils.named/nameize`](#nameize) – transforms a function so it accepts named arguments,
* [`futils.named/nameize*`](#nameize*) – like `nameize` but requires symbols to be quoted,
* [`futils.utils/frepeat`](#frepeat) – creates a sequence of returned values (uses named arguments),
* [`futils.utlis/mapply`](#mapply) – works like apply but with named arguments.
"

[[:chapter {:title "Installation"}]]

"Add to `project.clj` dependencies:

`[pl.randomseed/futils `\"`{{PROJECT.version}}`\"`]`

Then require it in your program:

`(require 'futils.utils :as futils)`  
`(require 'futils.args  :as fargs)`  
`(require 'futils.named :as fnamed)`  

(depending on which functions should be used) or:

`(ns your-namespace`  
`  (:require [futils.utils :as futils])`  
`  (:require [futils.args  :as fargs])`  
`  (:require [futils.named :as fnamed]))`
"

[[:chapter {:title "Usage"}]]


[[:section {:title "argc" :tag "argc"}]]

[[{:tag "argc-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.args/argc f & options))

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

[[:subsection {:title "Usage examples" :tag "argc-usage-ex"}]]
[[:file {:src "test/futils/args/argc.clj"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:section {:title "relax" :tag "relax"}]]

[[{:tag "relax-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.args/relax f & options))

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
* `:verbose` – a switch (defaults to `false`) that if set to true causes
               wrapper to return a map containing additional information.

See [`relax*`](#relax*) for detailed descriptions of `:pad-fn` and
`:verbose` options.
"

[[:subsection {:title "Usage examples" :tag "relax-usage-ex"}]]
[[:file {:src "test/futils/args/relax.clj"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:section {:title "relax*" :tag "relax*"}]]

[[{:tag "relax*-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.args/relax* f & options))

"
Returns a variadic function object that calls the given function, adjusting
the number of passed arguments to a nearest arity. It cuts argument list or pads
it with nil values if necessary.

It takes 1 positional, obligatory argument, which should be a function (`f`) and
two named, keyword arguments:

* `:arities` – a sorted set of argument counts for all arities,
* `:variadic` – a flag informing whether the widest arity is variadic.

It also makes use of optional named arguments:

* `:pad-fn`  – a function that generates values for padding,
* `:pad-val` – a value to use for padding instead of `nil`,
* `:verbose` – a switch (defaults to `false`) that if set to `true`, causes wrapper
               to return a map containing additional information.

To determine the number of arguments the nearest arity is picked up by matching
a number of passed arguments to each number from a set (passed as `:arities`
keyword argument). If there is no exact match then the next arity capable of
handling all arguments is selected.

If the expected number of arguments is lower than a number of arguments
actually passed to a wrapper call, the exceeding ones will be ignored.

If the declared number of arguments that the original function expects is higher
than a number of arguments really passed then nil values will be placed as extra
arguments.

When a variadic function is detected and its variadic arity is the closest to
a number of arguments passed then all of them will be used to call a function."

[[:subsection {:title "Verbose mode" :tag "relax*-verbose-mode"}]]

"
If the `:verbose` flag is set the result will be a map containing the following:

* `:argc-received` – a number of arguments received by the wrapper,
* `:argc-sent`     – a number of arguments passed to a function,
* `:argc-cutted`   – a number of arguments ignored,
* `:argc-padded`   – a number of arguments padded with `nil` values,
* `:args-received` – arguments received by the wrapper,
* `:args-sent`     – arguments passed to a function,
* `:arities`       – a sorted set of argument counts for all arities,
* `:arity-matched` – an arity (as a number of arguments) that matched,
* `:engine`        – a method used to check arities (`:clj` or `:jvm`),
* `:result`        – a result of calling the original function,
* `:variadic`      – a flag telling that the widest arity is variadic,
* `:variadic-used` – a flag telling that a variadic arity was used,
* `:verbose`       – a verbosity flag (always `true` in this case)."

[[:subsection {:title "Padding function" :tag "relax*-pad-fn"}]]

"
If a padding function is given (with `:pad-fn`) it should take keyword
arguments. During each call the following keys will be set:

* `:argc-received` – a number of arguments received by the wrapper,
* `:arity-matched` – an arity (as a number of arguments) that matched,
* `:iteration`     – a number of current iteration (starting from 1),
* `:iterations`    – a total number of iterations,
* `:previous`      – a value of previously calculated argument (the result
  of a previous call or a value of the last positional argument when padding
  function is called for the first time).

Values associated with `:iteration` and `:previous` keys will change during each
call, the rest will remain constant.

If there is no last argument processed at a time when `f` is called for the first
time (because no arguments were passed), the :previous key is not added to
a passed map. That allows to use a default value in a binding map of `f` or to
make easy checks if there would be some previous value (`nil` is ambiguous)."

[[:subsection {:title "Usage examples" :tag "relax*-usage-ex"}]]
[[:file {:src "test/futils/args/relax_st.clj"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:section {:title "nameize" :tag "nameize"}]]

[[{:tag "nameize-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.named/nameize f [names])
  (futils.named/nameize f [names] {defaults})
  (futils.named/nameize f arity-mappings…))

"where `arity-mappings` is a pair expressed as: `[names] {defaults}`."

"
Creates a wrapper that passes named arguments as positional arguments. Takes
a funtion object (`f`), a vector S-expression containing names of expected
arguments (`names`) expressed as keywords, symbols, strings or whatever suits
you, and an optional map S-expression of default values for named
arguments (`defaults`).

Since version 0.7.0 it accepts multiple arity mappings expressed as
pairs consisting of vectors of argument names and maps of default values for
all or some of names.

The order of names in a vector is important. Each given name will become a key
of named argument which value will be passed to the given function on the same
position as in the vector.

If unquoted symbol is given in a vector or in a map, it will be transformed to
a keyword of the same name. Use quoted symbols if you want to use symbols as
keys of named arguments.

If the `&rest` special symbol is placed in a vector then the passed value that
corresponds to its position will be a map containing all named arguments that
weren't handled. If there are none, `nil` value is passed.

The macro is capable of handling multiple arities. In such case the declared
arity will be matched against the given named arguments by comparing its keys
with keys in all declared mappings. First it will try to match them without
considering default values (if any) and in case there is no success (there is
no declared arity that can be satisfied by the given arguments) matching is
preformed again with default arguments merged. From the resulting set of
matching arity mappings the picked one is that with the least
requirements (that has the lowest count of declared arguments).

The result is a function object."

[[:subsection {:title "Usage examples" :tag "nameize-usage-ex"}]]
[[:file {:src "test/futils/named/nameize.clj"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:section {:title "nameize*" :tag "nameize*"}]]

[[{:tag "nameize*-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.named/nameize* f names)
  (futils.named/nameize* f names defaults)
  (futils.named/nameize* f arity-mappings…))

"where `arity-mappings` is a pair expressed as: `names defaults`."

"
Creates a wrapper that passes named arguments as positional arguments. Takes
a funtion object (`f`), a collection (preferably a vector) containing expected
names of arguments (`names`) expressed as keywords, symbols, strings or whatever
suits you, and a map of default values for named arguments (`defaults`).

Since version 0.7.0 it accepts multiple arity mappings expressed as
pairs consisting of vectors of argument names and maps of default values for
all or some of names.

The order of names in a vector is important. Each given name will become a key of
named argument which value will be passed to the given function on the same
position as in the vector.

If unquoted symbol is given in a vector or in a map, it will be transformed to
a keyword of the same name. Use quoted symbols if you want to use symbols as
keys of named arguments.

If the `&rest` special symbol is placed in `exp-args` vector then the passed
value that corresponds to its position will be a map containing all named
arguments that weren't handled. If there are none, nil value is passed.

The function is capable of handling multiple arities. In such case the
declared arity will be matched against the given named arguments by comparing
its keys with keys in all declared mappings. First it will try to match them
without considering default values (if any) and in case there is no
success (there is no declared arity that can be satisfied by the given
arguments) matching is preformed again with default arguments merged. From the
resulting set of matching arity mappings the picked one is that with the least
requirements (that has the lowest count of declared arguments).

A function object is returned."

[[:subsection {:title "Usage examples" :tag "nameize*-usage-ex"}]]
[[:file {:src "test/futils/named/nameize_st.clj"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:section {:title "frepeat" :tag "frepeat"}]]

[[{:tag "frepeat-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.utils/frepeat n? f kvs?))

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
*  `:iterations` – a total number of iterations (if `n` was given).

It is possible to set the initial value of `:previous` if there is a need for
that (by passing it to `frepeat`) or shadow the value assigned to `:iterations`
after the first call (by setting it in the passed function `f`).

Values associated with `:iteration` and `:previous` keys will always change
during each call.
"

[[:subsection {:title "Usage examples" :tag "frepeat-usage-ex"}]]
[[:file {:src "test/futils/utils/frepeat.clj"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:section {:title "mapply" :tag "mapply"}]]

[[{:tag "mapply-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.utils/mapply f args* args-map))

"
It works like `apply` but handles named arguments. Takes function `f`, an
optional list of arguments (`args*`) to be passed during a call to it and a map
(`args-map`) that will be decomposed and passed as named arguments.

Returns the result of calling f."

[[:subsection {:title "Usage examples" :tag "mapply-usage-ex"}]]
[[:file {:src "test/futils/utils/mapply.clj"}]]

