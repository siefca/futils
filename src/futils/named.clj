(ns

    ^{:doc    "futils library, nameization."
      :author "Paweł Wilk"}

    futils.named

  (:require [futils.core  :refer :all]
            [futils.utils :refer :all]))

(futils.core/init)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Nameization.

(def
  ^{:private true
    :added "0.6"}
  special-plus
  (into {'&rest true 'nil true} special-symbols))

(defn- throw-arg
  {:added "0.7"
   :tag nil}
  [& msgs]
  (throw (IllegalArgumentException. ^String (apply str msgs))))

(defn- unicompare
  {:added "0.7"
   :tag long}
  [x y]
  (if (= (class x) (class y))
    (compare x y)
    (compare (str x) (str y))))

(defn- validate-variadity
  "Validates a sequence of collections and tests their uniqueness."
  {:added "0.7"
   :tag nil}
  [^clojure.lang.ISeq arities]
  (when-not (apply distinct? (map count arities))
    (throw-arg "All declared arities should have different argument counts"))
  (when-not (apply distinct? (map #(sort unicompare %) arities))
    (throw-arg "Each declared arity should have at least one unique argument")))

(defn- validate-args
  "Validates sequential collection containing argument names by testing if
  they are reserved (special) symbols."
  {:added "0.6"
   :tag nil}
  [^clojure.lang.ISeq coll]
  (when-some [e (some (partial find special-plus) coll)]
    (throw-arg "Key " (key e) " is reserved and cannot be used as argument name")))

(defn- validate-arities
  "Validates a sequence of arities."
  {:added "0.7"
   :tag nil}
  [^clojure.lang.ISeq colls]
  (doseq [m colls] (validate-args m)))

(def validate-named-args
  "Validates associative collection containing named arguments by testing if
  the keys contain reserved (special) symbols."
  ^{:added "0.6"
    :private true
    :tag nil
    :arglists '([^clojure.lang.IPersistentMap m])}
  (comp validate-args (partial keys)))

(def validate-named-arities
  "Validates a sequence of named arguments expressed as maps."
  ^{:added "0.7"
    :private true
    :tag nil
    :arglists '([^clojure.lang.ISeq coll])}
  (comp validate-arities (partial map keys)))

(defn- intersect-args
  "Returns a sequence of argument names that are common for the given map (as
  keys) and the given arity (as elements)."
  {:added "0.8"
   :tag clojure.lang.ISeq}
  [^clojure.lang.IPersistentMap args
   ^clojure.lang.ISeq arity]
  (filter (partial find args) arity))

(defn- arity-counter
  "Returns a function that produces a number of common arguments for the given
  pair (argument names as seq and default values as map) and named arguments
  map given while creating function. The first argument is a switch. If it's
  false or nil then defaults map is not used."
  {:added "0.8"
   :tag clojure.lang.IFn}
  [^Boolean use-defaults
   ^clojure.lang.IPersistentMap args]
  (if use-defaults
    (fn ^long [^clojure.lang.ISeq e]
      (let [arit (first e)
            defl (last e)]
        (count
         (if (nil? defl)
           (intersect-args args arit)
           (intersect-args (into defl args) arit)))))
    (fn ^long [^clojure.lang.ISeq e]
      (count (intersect-args args (first e))))))

(defn- closest-pair
  "Returns the one pair from pairs (seq of argument names, map of default
  values for those names) that is closest to the given map of named
  arguments (keys actually)."
  {:added "0.7"
   :tag clojure.lang.ISeq}
  ([^clojure.lang.IPersistentMap args
    ^clojure.lang.ISeq pairs]
   (let [fun (partial closest-pair args pairs)
         ari (fun false)]
     (if (> (count args) (count-first ari)) (fun true) ari)))
  ([^clojure.lang.IPersistentMap args
    ^clojure.lang.ISeq pairs
    ^Boolean use-defaults]
   (->> (partition-all 2 pairs)
        (group-by (arity-counter use-defaults args))
        (apply max-key first) last (apply min-key count-first))))

(defn- args-picker
  "Returns a function that for the given argument name tries to find its value
  in a predefined map, passed as an argument when creating it. Additionally it
  interpolates &rest special name by putting map with unhandled arguments or
  nil as its value. If argument name is not found in args map it throws an
  error."
  {:added "0.8"
   :tag clojure.lang.IFn}
  [^clojure.lang.IPersistentMap args
   ^clojure.lang.IPersistentMap unhandled-args]
  (fn [arg-name]
    (if-some [a (find args arg-name)]
      (val a)
      (if (= '&rest arg-name)
        (not-empty unhandled-args)
        (throw-arg "Argument is missing: " arg-name)))))

(defn nameize*
  "Creates a wrapper that passes named arguments as positional arguments. It
  takes a funtion object (f), a vector S-expression containing names of
  expected arguments (names – expressed as keywords, symbols, strings or
  other objects) and an optional map S-expression with default values for
  named arguments (defaults).

  Since version 0.7.0 it accepts multiple arity mappings expressed as
  pairs consisting of argument name vectors and maps of default values (for
  all or some of the names).

  The order of names in a vector is important. Each name will become a key
  of named argument which value will be passed to the given function on the same
  position as in the vector.

  If unquoted symbol is given in a vector or in a map, it will be transformed
  into a keyword of the same name. Use quoted symbols if you want to use symbols
  as keys of named arguments.

  If the &rest special symbol is placed in a vector then the passed value that
  corresponds to its position will be a map containing all named arguments that
  weren't handled. If there are none, nil value is passed.

  The function is capable of handling multiple arities. In such case the declared
  arities (e.g. [:a :b] [:a :b :c]) will be matched against the given named
  arguments (e.g. {:a 1 :b 2}) by comparing declared argument names to key
  names. Firstly it will try to match them without considering default
  values (if any) and in case of no success (when there is no declared arity
  that can be satisfied by the given arguments) matching is preformed again but
  with default arguments merged. From the resulting collection of matching arity
  mappings the one element with the least requirements is chosen (that has the
  lowest count of declared arguments)."
  {:added "0.6"
   :tag clojure.lang.Fn}
  [^clojure.lang.Fn f & arity-pairs]
  {:pre [(instance? clojure.lang.Fn f)]}
  (validate-variadity (take-nth 2 arity-pairs))
  (validate-named-arities (take-nth 2 (next arity-pairs)))
  (fn [& {:as args-given}]
    (validate-named-args args-given)
    (let [closest-pair  (closest-pair args-given arity-pairs)
          args-expected (first closest-pair)
          args-defaults (not-empty (last closest-pair))
          args-to-use   (if (nil? args-defaults) args-given (into args-defaults (or args-given {})))
          args-used     (select-keys args-to-use args-expected)
          args-unused   (apply dissoc args-to-use (keys args-used))]
      (->> (take-nth 2 closest-pair)
           (apply concat)
           (map (args-picker args-to-use args-unused))
           (apply f)))))

(defn- keywordize-syms
  "Transforms all elements of linear or associative collection from symbols to
  keywords (except &rest). Returns nil values if generated collections are
  empty."
  {:added "0.6"
   :arglists '([^clojure.lang.IPersistentVector v]
               [^clojure.lang.IPersistentMap m]
               [^clojure.lang.ISeq coll])}
  [coll]
  (when-not (nil? coll)
    (let [f #(if (symbol? %) (if (= '&rest %) ''&rest (keyword %)) %)]
      (not-empty
       (if (map? coll)
         (reduce-kv #(assoc %1 (f %2) %3) (empty coll) coll)
         (map f coll))))))

(defmacro nameize
  "Creates a wrapper that passes named arguments as positional arguments. It
  takes a funtion object (f), a vector S-expression containing names of
  expected arguments (names – expressed as keywords, symbols, strings or
  other objects) and an optional map S-expression with default values for
  named arguments (defaults).

  Since version 0.7.0 it accepts multiple arity mappings expressed as
  pairs consisting of argument name vectors and maps of default values (for
  all or some of the names).

  The order of names in a vector is important. Each name will become a key
  of named argument which value will be passed to the given function on the same
  position as in the vector.

  If unquoted symbol is given in a vector or in a map, it will be transformed
  into a keyword of the same name. Use quoted symbols if you want to use symbols
  as keys of named arguments.

  If the &rest special symbol is placed in a vector then the passed value that
  corresponds to its position will be a map containing all named arguments that
  weren't handled. If there are none, nil value is passed.

  The macro is capable of handling multiple arities. In such case the declared
  arities (e.g. [:a :b] [:a :b :c]) will be matched against the given named
  arguments (e.g. {:a 1 :b 2}) by comparing declared argument names to key
  names. Firstly it will try to match them without considering default
  values (if any) and in case of no success (when there is no declared arity
  that can be satisfied by the given arguments) matching is preformed again but
  with default arguments merged. From the resulting collection of matching arity
  mappings the one element with the least requirements is chosen (that has the
  lowest count of declared arguments).
  "
  {:added "0.6"}
  ([f exp-args] `(nameize ~f ~exp-args {}))
  ([f exp-args defaults & more]
   (let [n (->> (list* exp-args defaults more)
                (reduce (fn [acc e]
                          (if (and (vector? (first acc)) (not (map? e)))
                            (recur (cons nil acc) e)
                            (cons e acc))) ()))
         n (->> (if (map? (first n)) n (cons nil n))
                (partition-all 2)
                (reduce
                 (fn [acc [defl exp]]
                   (when-not (vector? exp)
                     (throw-arg "First element of a mapping pair must be a vector"))
                   (when-not (or (nil? defl) (map? defl))
                     (throw-arg "Second element of a mapping pair must be a map"))
                   (->> acc
                        (cons (#'keywordize-syms defl))
                        (cons (cons 'list (#'keywordize-syms exp))))) ()))]
     `(nameize* ~f ~@n))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Composition of functions with named arguments.

(def ^{:private true
       :added "1.2"
       :const true
       :tag clojure.lang.IPersistentMap}
  comp-defaults
  {:merge-args false
   :map-output false})

(defn- comp-map-results
  "Transforms the structure given as r into a named arguments list according
  to rules given as the opts map and optionally merging them with the args
  map."
  {:added "1.2"}
  ([r
    ^clojure.lang.IPersistentMap args
    ^clojure.lang.IPersistentMap opts]
   (let [ma (:merge-args opts)
         mo (:map-output opts)]
     (as-> r $
       (if (or mo (not (map? $))) {(if (false? mo) :out mo) $} $)
       (if ma (if (true? ma) (merge args $) (assoc $ ma args)) $)
       (mapcat identity $)))))

(defn- comp-prep-opts
  "Creates a function that prepares options for comp-map-results."
  {:added "1.2"
   :tag clojure.lang.IFn}
  [^clojure.lang.IPersistentMap defl-opts]
  (let [defl-opts (merge comp-defaults defl-opts)]
    (fn [^clojure.lang.IPersistentMap opts]
      (as-> opts $
        (if (map? $) $ {:f $})
        (merge defl-opts $)
        (update $ :map-output #(if (true? %) :out (or % false)))
        (update $ :merge-args #(or % false))))))

(defn- comp-core
  "Composes functions with named arguments represented as maps where :f entry
  is a function and other are options that control the way return values are
  structured. It also accepts functions passed directly as its first argument.
  Returns function object."
  {:added "1.2"
   :arglists '([]
               [^clojure.lang.IFn f]
               [^clojure.lang.IPersistentMap m]
               [^clojure.lang.IFn a, ^clojure.lang.IPersistentMap b]
               [^clojure.lang.IPersistentMap a, ^clojure.lang.IPersistentMap b])}
  (^clojure.lang.IFn [] identity)
  (^clojure.lang.IFn [a]
   (let [fa (if (map? a) (:f a) a)
         oa (if (map? a) (dissoc a :f) nil)]
     (fn
       ([]            (comp-map-results (fa) nil oa))
       ([k v]         (comp-map-results (fa k v) {k v} oa))
       ([k v z x]     (comp-map-results (fa k v z x) {k v z x} oa))
       ([k v z x c v] (comp-map-results (fa k v z x c v) {k v z x c v} oa))
       ([k v z x c v & args]
        (let [args (list* k v z x c v args)
              fbrt (comp-map-results (apply fa args) (apply array-map args) oa)]
          (apply fa fbrt))))))
  (^clojure.lang.IFn [a, ^clojure.lang.IPersistentMap b]
   (let [fa (if (map? a) (:f a) a)
         fb (:f b)
         ob (dissoc b :f)]
     (fn
       ([]            (apply fa (comp-map-results (fb) nil ob)))
       ([k v]         (apply fa (comp-map-results (fb k v) {k v} ob)))
       ([k v z x]     (apply fa (comp-map-results (fb k v z x) {k v z x} ob)))
       ([k v z x c v] (apply fa (comp-map-results (fb k v z x c v) {k v z x c v} ob)))
       ([k v z x c v & args]
        (let [args (list* k v z x c v args)
              fbrt (comp-map-results (apply fb args) (apply array-map args) ob)]
          (apply fa fbrt)))))))

(defn- comp-parse-args
  "Parses arguments for comp and returns a sequence of maps describing
  functions and options that control the way their return values are
  transformed and structured."
  {:added "1.2"
   :tag clojure.lang.ISeq}
  [args]
  (let [fargs (first args)
        largs  (last args)
        ff (if (fn? fargs) fargs (:f fargs))
        fl (if (fn? largs) largs (:f largs))
        [fns opts] (if ff
                     (if fl [args nil] [(butlast args) largs])
                     (if fl [(next args) fargs] [(list identity) (merge fargs largs)]))
        opts (comp-prep-opts opts)]
    (map opts fns)))

(defn comp
  {:added "1.2"}
  (^clojure.lang.IFn [] identity)
  (^clojure.lang.IFn [f] (comp-core (first (comp-parse-args [f]))))
  (^clojure.lang.IFn [f & more]
   (let [args (comp-parse-args (cons f more))]
     (if (<= (count args) 1)
       (comp-core (first args))
       (reduce comp-core args)))))

;; todo: detect how many funcs is really there and in case of just one.. (reduce calls it!)

(defn comp-explain
  {:added "1.2"}
  (^clojure.lang.IFn [] #'identity)
  (^clojure.lang.IFn [f] (first (comp-parse-args [f])))
  (^clojure.lang.IFn [f & more] (comp-parse-args (cons f more))))
