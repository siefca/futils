(ns

    ^{:doc    "futils library, utility functions."
      :author "Paweł Wilk"}

    futils.utils)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Simplifiers.

(def
  ^{:added "0.1"
    :tag Boolean
    :arglists '([^clojure.lang.Fn pred ^clojure.lang.ISeq coll])}
  any?
  "Returns true if the given collection (coll) contains at least one element for
  which a value passed as the pred argument is not false and not nil."
  (comp boolean some))

(def
  ^{:added "0.1"
    :tag Boolean
    :arglists '([^clojure.lang.Symbol c])}
  not-ampersand?
  (partial not= '&))

(def
  ^{:added "0.7"
    :tag long
    :arglists '([^clojure.lang.ISeq coll])}
  count-first
  "Counts elements of a first collection in a given collection."
  (comp count (partial first)))

(defmacro
  ^{:added "0.6"}
  if->
  [val pred & body]
  (let [v `~val
        p (if (sequential? pred) pred (list pred))]
    (list 'if (cons (first p) (cons v (rest p))) (cons 'do body) v)))

(defmacro
  ^{:added "0.6"}
  if-not->
  [val pred & body]
  (let [v `~val
        p (if (sequential? pred) pred (list pred))]
    (list 'if (cons (first p) (cons v (rest p))) v (cons 'do body))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Set operations.

(defn nearest-right
  "For the given sorted set of integer values and the given value returns the
  closest value from the set, picking up the highest one in case of no exact
  match."
  {:added "0.1"
   :tag long}
  [^clojure.lang.IPersistentSet s ^long v]
  (if-let [x (first (subseq s >= v))]
    x
    (when-let [x (first (rsubseq s < v))] x)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Coercions and ensurances.

(defn non-negative
  "Ensures that a given value is positive or 0. If it's negative it returns 0."
  {:added "0.1"
   :tag long}
  [^long n]
  (if (neg? n) 0 n))

(defn pos-
  "Like - operator but works for 2 numbers and never returns negative values. If
  value is negative, it will return 0."
  {:added "0.3"
   :tag long}
  [^long x ^long y]
  (non-negative (- x y)))

(defn require-fn
  {:added "0.1"
   :tag clojure.lang.Fn}
  [f]
  (when-let [fun (if (var? f) (deref f) f)]
    (when (and (ifn? fun) (instance? clojure.lang.Fn fun)) fun)))

(def
  ^{:added "0.6"
    :tag clojure.lang.IPersistentMap
    :const true}
  special-symbols
  (. clojure.lang.Compiler specials))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Java interop.

(defn method-name
  "Returns the name of the given Java method."
  {:added "0.1"
   :tag String}
  [^java.lang.reflect.Method m]
  (.getName m))

(defn method-argc
  "Returns the number of arguments Java method takes."
  {:added "0.1"
   :tag long}
  [^java.lang.reflect.Method m]
  (alength (.getParameterTypes m)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Low-level args parsing.

(defn parse-opts-defn
  {:added "0.6"
   :tag clojure.lang.IPersistentVector}
  [^clojure.lang.ISeq options]
  (let
      [[m o] [{} options]
       [m o] (if (string? (first o)) [(assoc m :doc (first o)) (next o)] [m o])
       [m o] (if (map?    (first o)) [(conj  m (first o)) (next o)] [m o])
       o  (if (vector? (first o)) (list o) o)
       [m o] (if (map?    (last  o)) [(conj  m (last o)) (butlast o)] [m o])]
    [m o]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Applying named arguments.

(defn mapply
  "Like apply but works on named arguments. Takes function f and a list of
  arguments to be passed, were the last argument should be a map that will be
  decomposed and passed as named arguments.

  Returns the result of calling f."
  {:added "0.2"}
  [^clojure.lang.Fn f & args]
  (apply f (concat (butlast args) (mapcat identity (last args)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sequences generation with named arguments.

(defn- frepeat-core
  {:added "0.2"
   :tag clojure.lang.ISeq}
  [^long nr
   ^clojure.lang.Fn f
   ^clojure.lang.IPersistentMap params]
  (lazy-seq
   (let [par (assoc params :iteration nr)
         res (mapply f par)]
     (cons res
           (frepeat-core (inc nr) f (assoc par :previous res))))))

(defn frepeat
  "Returns a lazy sequence of results produced by the given function f
  which should accept named arguments.

  If the first argument passed to frepeat is a number and the second is
  a function it will limit the iterations to the specified count. If the numeric
  argument is missing and only a function object is given the frepeat will
  produce infinite sequence of calls.

  The last, optional argument should be a map that initializes named arguments
  that will be passed to the first and subsequent calls to f.

  Additionally each call to f will pass the following keyword arguments:

  :iteration     – a number of current iteration (starting from 1),
  :previous      – a result of the previous call to f.

  The first call to f will pass the following:

  :iteration     – 1,
  :iterations    – a total number of iterations (if nr was given).

  It is possible to set the initial value of :previous if there is a need for
  that (by passing it to frepeat) or shadow the :iterations after the first call
  (by setting it in the passed function f).

  Values associated with :iteration and :previous keys will always change during
  each call."
  {:added "0.2"
   :tag clojure.lang.ISeq
   :arglists '([^clojure.lang.Fn f]
               [^clojure.lang.Fn f ^clojure.lang.IPersistentMap kvs]
               [^long n ^clojure.lang.Fn f]
               [^long n ^clojure.lang.Fn f ^clojure.lang.IPersistentMap kvs])}
  ([^clojure.lang.Fn f]
   (frepeat-core 1 f nil))
  ([n-f f-m]
   (if (number? n-f)
     (frepeat (long n-f) f-m nil)
     (frepeat-core 1 n-f f-m)))
  ([^long n
    ^clojure.lang.Fn f
    ^clojure.lang.IPersistentMap kvs]
   (take n (frepeat-core 1 f (assoc kvs :iterations n)))))
