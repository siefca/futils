(ns

    ^{:doc    "futils library, nameization."
      :author "Paweł Wilk"}

    futils.named

  (:require [futils.utils :refer :all]
            [clojure.set  :refer :intersection]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Nameization support.

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

(defn- validate-variadity
  {:added "0.7"
   :tag nil}
  [^clojure.lang.ISeq arities]
  (when-not (apply distinct? (map count arities))
    (throw-arg "All declared arities should have different argument counts"))
  (when-not (apply distinct? (map sort arities))
    (throw-arg "Each declared arity should have at least one unique argument")))

(defn- validate-argmap
  "Validates sequential collection containing argument mappings by testing if
  it contains reserved (special) symbols."
  {:added "0.6"
   :tag nil}
  [^clojure.lang.IPersistentMap m]
  (when-some [e (some (partial find special-plus) (keys m))]
    (throw-arg "Key " (key e) " is reserved")))

(defn- validate-argmaps
  "Validates argument mappings expressed as a sequence of vectors (or other
  sequential collections)."
  {:added "0.7"
   :tag nil}
  [^clojure.lang.ISeq pairs]
  (let [arities (take-nth 2 pairs)]
    (doseq [m arities] (validate-argmap m))
    (validate-variadity arities)))

(defn- closest-arity
  {:added "0.7"
   :tag clojure.lang.ISeq}
  ([^clojure.lang.IPersistentMap args
    ^clojure.lang.ISeq pairs]
   (let [ari (closest-arity args pairs false)]
     (if (<= (count args) (count-first ari))
       ari
       (closest-arity args pairs true))))
  ([^clojure.lang.IPersistentMap args
    ^clojure.lang.ISeq pairs
    ^Boolean use-defaults]
   (let [base (set (keys args))
         gens (if use-defaults
                #(into (set (keys (last %))) base)
                (constantly base))]
     (->> pairs
          (partition-all 2)
          (group-by #(count (intersection (gens %) (set (first %)))))
          (apply max-key first)
          last
          (apply min-key count-first)))))

(defn nameize*
  "Creates a wrapper that passes named arguments as positional arguments. Takes
  a funtion object (f), a collection (preferably a vector) containing expected
  names of arguments (exp-args) expressed as keywords, symbols, strings or
  whatever suits you, and a map of default values for named
  arguments (defaults).

  The order of names in a collection is important. Each given name will become
  a key of named argument which value will be passed to the given function on
  the same position as its position in the vector.

  If the &rest special symbol is placed in exp-args vector then the passed
  value that corresponds to its position will be a map containing all named
  arguments that weren't handled. If there are none, nil value is passed.

  Function returns a function object."
  {:added "0.6"
   :tag clojure.lang.Fn}
  [^clojure.lang.Fn f & more]
  {:pre [(instance? clojure.lang.Fn f)]}
  (validate-argmaps more)
  (fn [& {:as args}]
    (validate-argmap args)
    (let [arit (closest-arity args more)
          expa (first arit)
          defl (last arit)
          args (into defl args)
          argp (select-keys args expa)
          argr (apply dissoc args (keys argp))
          pckr #(if-some [ent (find args %)]
                  (val ent)
                  (if (= '&rest %)
                    (not-empty argr)
                    (throw-arg "No required key present: " %)))]
      (->> arit
           (take-nth 2)
           (apply concat)
           (map pckr)
           (apply f)))))

(defn- keywordize-syms
  "Transforms all elements of linear or associative collection from symbols to
  keywords (except &rest)."
  {:added "0.6"
   :arglists '([^clojure.lang.IPersistentVector v]
               [^clojure.lang.IPersistentMap m]
               [^clojure.lang.ISeq coll])}
  [coll]
  (let [f #(if (symbol? %) (if (= '&rest %) ''&rest (keyword %)) %)]
    (if (map? coll)
      (reduce-kv #(assoc %1 (f %2) %3) (empty coll) coll)
      (map f coll))))

(defmacro nameize
  "Creates a wrapper that passes named arguments as positional arguments. Takes
  a function object (f), a vector S-expression containing names of expected
  arguments (exp-args) expressed as keywords, symbols, strings or whatever suits
  you, and an optional map S-expression of default values for named
  arguments (defaults).

  The order of names in a vector is important. Each given name will become a key
  of named argument which value will be passed to the given function on the same
  position as in the vector.

  If unquoted symbol is given in a vector or in a map, it will be transformed to
  a keyword of the same name. Use quoted symbols if you want to use symbols as
  keys of named arguments.

  If the &rest special symbol is placed in a vector then the passed value that
  corresponds to its position will be a map containing all named arguments that
  weren't handled. If there are none, nil value is passed.

  The result is a function object."
  {:added "0.6"}
  ([f exp-args] `(nameize ~f ~exp-args {}))
  ([f exp-args defaults & more]
   (let [m (partition-all 2 (list* exp-args defaults more))
         n (reduce
            (fn [acc [exp defl]]
              (when-not (vector? exp)
                (throw-arg "First argument in naming pair must be a vector"))
              (when-not (map? defl)
                (throw-arg "Last argument in naming pair must be a map"))
              (->> acc
                   (cons (#'keywordize-syms defl))
                   (cons (cons 'list (#'keywordize-syms exp)))))
            () m)]
     `(nameize* ~f ~@n))))
