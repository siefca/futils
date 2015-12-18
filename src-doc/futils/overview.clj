(ns documentation.futils.overview)

[[:chapter {:title "Introduction"}]]

" `futils` is a library that adds some abstractions for managing functions in
Clojure.

Currently provided macros and functions are:

* **[`futils.args/`](#futils.args)**
  * [`argc`](#argc) – **counts arguments** a function takes (for all arities),
  * [`relax`](#relax) – transforms a function in a way that it accepts **any number of arguments**,
  * [`relax*`](#relax*) – like `relax` but it requires to explicitly describe the accepted arities;   

* **[`futils.named/`](#futils.named)**
  * [`apply`](#apply) – works like `clojure.core/apply` but on named arguments,
  * [`comp`](#comp) – creates a function that is the **composition** of the given functions,
  * [`nameize`](#nameize) – transforms a function in a way that it can take **named arguments**,
  * [`nameize*`](#nameize*) – like `nameize` but requires symbols to be quoted;   

* **[`futils.utils/`](#futils.utils)**
  * [`frepeat`](#frepeat) – returns a sequence generated by a function that uses named arguments.
"

[[:chapter {:title "Installation"}]]

"Add dependencies to `project.clj`:

`[pl.randomseed/futils `\"`{{PROJECT.version}}`\"`]`

Then (depending on which functions should be used) require it in your program:

`(require 'futils.utils)`  
`(require 'futils.args)`  
`(require 'futils.named)`  

or:

`(ns your-namespace`  
`  (:require [futils.utils :as utils])`  
`  (:require [futils.args  :as args])`  
`  (:require [futils.named :as named]))`
"

[[:chapter {:title "Usage"}]]

[[:section {:title "futils.args" :tag "futils.args"}]]

"
The `futils.args` namespace contains functions that provide positional
arguments management, like counting or transforming other functions so they
can accept variable number of arguments (with optional padding)."

[[:subsection {:title "argc" :tag "argc"}]]

[[{:tag "argc-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.args/argc f & options))

"
Determines the number of arguments that the given function takes and returns
a map containing the following keys:

* `:arities`  – a sorted sequence containing number of arguments for all arities,
* `:engine`:
  * `:clj` – if metadata were used to determine arities – DEPRECATED);
  * `:jvm` – if Java reflection methods were used to determine arities),
* `:macro`    – a flag informing whether the given object is a macro,
* `:variadic` – a flag informing whether the widest arity is variadic.

Variadic parameter is counted as one of the possible arguments (if present).

The macro flag (`:macro` key with value `true`) is only present when macro was
detected.

If the given argument cannot be used to obtain a Var bound to a functon or
a function object then it returns `nil`.
"

[[:file {:src "test/futils/args/argc.clj" :tag "argc-usage-ex"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:subsection {:title "relax" :tag "relax"}]]

[[{:tag "relax-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.args/relax f & options))

"
Returns a variadic function object that calls the given function `f`,
adjusting the number of passed arguments to the nearest matching arity. It
cuts argument list or pads it with `nil` values if necessary.

The arities will be obtained using JVM reflection calls to anonymous class
representing a function object.

To determine the number of arguments the nearest arity is picked up by matching
a number of passed arguments to number of arguments for each arity. If there is
no exact match then the next arity capable of taking all arguments is selected.

If the expected number of arguments is lower than the number of arguments
actually passed to a wrapper call then the exceeding ones will be ignored.

If the detected number of arguments that the original function expects is
higher than the number of arguments really passed then `nil` values (or other
padding values) will be passed as extra arguments.

When a variadic function is detected and its variadic arity is the closest to
a number of arguments passed then all of them will be used during a function
call (no argument will be ignored).

The `relax` takes optional named arguments:

* `:pad-fn` – a function that generates values for padding,
* `:pad-val` – a value to use for padding instead of `nil`,
* `:verbose` – a switch (defaults to `false`) that if set to true causes
               wrapper to return a map containing additional information.

See [`relax*`](#relax*) for detailed descriptions of `:pad-fn` and
`:verbose` options."

[[:file {:src "test/futils/args/relax.clj" :tag "relax-usage-ex"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:subsection {:title "relax*" :tag "relax*"}]]

[[{:tag "relax*-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.args/relax* f & options))

"
Returns a variadic function object that calls the given function, adjusting
the number of passed arguments to nearest matching arity. It cuts argument
list or pads it with nil values if necessary.

It takes 1 positional, obligatory argument, which should be a function (`f`) and
two named, keyword arguments:

* `:arities` – a sorted sequence of argument counts for all arities,
* `:variadic` – a flag informing whether the widest arity is variadic.

It also makes use of optional named arguments:

* `:pad-fn`  – a function that generates values for padding,
* `:pad-val` – a value to use for padding instead of `nil`,
* `:verbose` – a switch (defaults to `false`) that if set to `true`, causes wrapper
               to return a map containing additional information.

To determine the number of arguments the nearest arity is picked up by
matching a number of passed arguments to each number from a sequence (passed
as `:arities` keyword argument). If there is no exact match then the next
arity capable of handling all arguments is chosen.

If the expected number of arguments is lower than a number of arguments
actually passed to a wrapper call, the exceeding ones will be ignored.

If the declared number of arguments that the original function expects is
higher than the number of arguments really passed then `nil` values (or other
padding values) will be placed as extra arguments.

When a variadic function is detected and its variadic arity is the closest to
the number of passed arguments then all of them will be used."

[[:subsubsection {:title "Verbose mode" :tag "relax*-verbose-mode"}]]

"
If the `:verbose` flag is set then the result will be a map containing the
following:

* `:argc-received` – a number of arguments received by the wrapper,
* `:argc-sent`     – a number of arguments passed to a function,
* `:argc-cutted`   – a number of arguments ignored,
* `:argc-padded`   – a number of arguments padded with `nil` values,
* `:args-received` – arguments received by the wrapper,
* `:args-sent`     – arguments passed to a function,
* `:arities`       – a sorted sequence of argument counts for all arities,
* `:arity-matched` – an arity (as a number of arguments) that matched,
* `:engine`        – a method used to check arities (`:clj` or `:jvm`),
* `:result`        – a result of calling the original function,
* `:variadic`      – a flag telling that the widest arity is variadic,
* `:variadic-used` – a flag telling that a variadic arity was used,
* `:verbose`       – a verbosity flag (always `true` in this case)."

[[:subsubsection {:title "Padding function" :tag "relax*-pad-fn"}]]

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

[[:file {:src "test/futils/args/relax_st.clj" :tag "relax*-usage-ex"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:section {:title "futils.named" :tag "futils.named"}]]

"
The `futils.named` namespace contains functions transforming functions taking
positional arguments into functions that can handle named arguments."

[[:subsection {:title "apply" :tag "apply"}]]

[[{:tag "apply-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.utils/apply f args* args-map))

"
It works like `apply` but handles named arguments. Takes function `f`, an
optional list of arguments (`args*`) to be passed during a call to it and a map
(`args-map`) that will be decomposed and passed as named arguments.

Returns the result of calling f."

[[:file {:src "test/futils/named/apply.clj" :tag "apply-usage-ex"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:subsection {:title "comp" :tag "comp"}]]

[[{:tag "comp-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.named/comp function…)
  (futils.named/comp options-map function…)
  (futils.named/comp function… options-map))

"
where `function` is a function object or a map containing `:f` key associated
with a function object and optional options controlling output
transformations."

"
The `comp` function takes a set of functions that accept named arguments and
returns a function object that is the composition of those functions. The
returned function takes named arguments, applies the rightmost of functions to
the arguments, the next function (right-to-left) to the result, etc.

Each function should return a map that will be used to generate named
arguments for the next function in the execution chain. If a function does not
return a map its resulting value will be assigned to a key of newly created
map. The default name of this key will be :out unless the option :map-output
had been used (see the explanations below).

The returned value of the last called function is not transformed in any way
and there is no need for it to be a map.

Functions can be expressed as function objects or as maps. In the second
case the map must contain `:f` key with function object assigned to it and may
contain optional, controlling options which are:

* `:merge-args`: `false` (default) or `true` or a key,
* `:map-output`: `false` (default) or `true` or a key,
* `:rename-keys`   `nil` (default) or a map,
* `:post-rename`   `nil` (default) or a map.

The `:merge-args` option, when is not set to `false` nor `nil`, causes
function arguments to be merged with a returned map. If the key name is given
they all will be stored under specified key of this map. If the assigned value
is set to `true` then they will be merged. If two keys are the same the
association from arguments is overwritten by the entry being returned by a
function.

The `:map-output` causes the returned value to be stored under a specified key
of a resulting map. If the option value is set to `true` then the key name will
be `:out`.

The `:rename-keys` option causes keys of a resulting map to be renamed according
to the given map. The transformation will be performed on a returned value (if
it's a map), before any other changes (output mapping or arguments merging).

The `:post-rename` option works in the same way as `:rename-keys` but it's
performed after all other transformations are applied.

Defaults for the options described above may be given by passing a map as a
first or last argument when calling the `comp` function. Such a map should not
contain `:f` key.

The function returns a function object."

[[:file {:src "test/futils/named/comp.clj" :tag "comp-usage-ex"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:subsection {:title "nameize" :tag "nameize"}]]

[[{:tag "nameize-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.named/nameize f [names])
  (futils.named/nameize f [names] {defaults})
  (futils.named/nameize f arity-mappings…))

"
where `arity-mappings…` are pairs expressed as: `[names] {defaults}` or
just `[names]`."

"
The macro creates a wrapper that passes named arguments as positional
arguments. It takes a funtion object (`f`), a vector S-expression containing
names of expected arguments (`names` – expressed as keywords, symbols, strings
or other objects) and an optional map S-expression with default values for
named arguments (`defaults`).

Since version 0.7.0 it accepts multiple arity mappings expressed as
pairs consisting of argument name vectors and maps of default values (for
all or some of the names).

The order of names in a vector is important. Each name will become a key
of named argument which value will be passed to the given function on the same
position as in the vector.

If unquoted symbol is given in a vector or in a map, it will be transformed
into a keyword of the same name. Use quoted symbols if you want to use symbols
as keys of named arguments.

If the `&rest` special symbol is placed in a vector then the passed value that
corresponds to its position will be a map containing all named arguments that
weren't handled. If there are none, `nil` value is passed.

The macro is capable of handling multiple arities. In such case the declared
arities (e.g. `[:a :b]` `[:a :b :c]`) will be matched against the given named
arguments (e.g. `{:a 1 :b 2}`) by comparing declared argument names to key
names. Firstly it will try to match them without considering default
values (if any) and in case of no success (when there is no declared arity
that can be satisfied by the given arguments) matching is preformed again but
with default arguments merged. From the resulting collection of matching arity
mappings the one element with the least requirements is chosen (that has the
lowest count of declared arguments).

The result is a function object."

[[:file {:src "test/futils/named/nameize.clj" :tag "nameize-usage-ex"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:subsection {:title "nameize*" :tag "nameize*"}]]

[[{:tag "nameize*-synopsis" :title "Synopsis" :numbered false}]]
(comment
  (futils.named/nameize* f names)
  (futils.named/nameize* f names defaults)
  (futils.named/nameize* f arity-mappings…))

"
where `arity-mappings` is a pair expressed as: `names defaults` or just
`names`."

"
The function creates a wrapper that passes named arguments as positional
arguments. It takes a funtion object (`f`), a vector S-expression containing
names of expected arguments (`names` – expressed as keywords, symbols, strings
or other objects) and an optional map S-expression with default values for
named arguments (`defaults`).

Since version 0.7.0 it accepts multiple arity mappings expressed as
pairs consisting of argument name vectors and maps of default values (for
all or some of the names).

The order of names in a vector is important. Each name will become a key
of named argument which value will be passed to the given function on the same
position as in the vector.

If unquoted symbol is given in a vector or in a map, it will be transformed
into a keyword of the same name. Use quoted symbols if you want to use symbols
as keys of named arguments.

If the `&rest` special symbol is placed in a vector then the passed value that
corresponds to its position will be a map containing all named arguments that
weren't handled. If there are none, `nil` value is passed.

The function is capable of handling multiple arities. In such case the
declared arities (e.g. `[:a :b]` `[:a :b :c]`) will be matched against the
given named arguments (e.g. `{:a 1 :b 2}`) by comparing declared argument
names to key names. Firstly it will try to match them without considering
default values (if any) and in case of no success (when there is no declared
arity that can be satisfied by the given arguments) matching is preformed
again but with default arguments merged. From the resulting collection of
matching arity mappings the one element with the least requirements is
chosen (that has the lowest count of declared arguments).

A function object is returned."

[[:file {:src "test/futils/named/nameize_st.clj" :tag "nameize*-usage-ex"}]]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

[[:section {:title "futils.utils" :tag "futils.utils"}]]

"
The `futils.utils` namespace contains functions providing various additional
operations on functions and some helpers used by other parts of library."

[[:subsection {:title "frepeat" :tag "frepeat"}]]

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

[[:file {:src "test/futils/utils/frepeat.clj" :tag "frepeat-usage-ex"}]]

