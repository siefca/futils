(ns

    ^{:doc    "futils library, nameization."
      :author "Paweł Wilk"}

    futils.named

  (:require [futils.utils :refer :all]
            [clojure.set  :refer [intersection]]))

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

  Since version 0.7.0 it accepts multiple arity mappings expressed as pairs
  consisting of vectors of argument names and maps of default values for all
  or some of names.

  The order of names in a collection is important. Each given name will become
  a key of named argument which value will be passed to the given function on
  the same position as its position in the vector.

  If the &rest special symbol is placed in exp-args vector then the passed
  value that corresponds to its position will be a map containing all named
  arguments that weren't handled. If there are none, nil value is passed.

  The function is capable of handling multiple arities. In such case the
  declared arities (e.g. [:a :b] [:a :b :c]) will be matched against the given
  named arguments (e.g. {:a 1 :b 2}) by comparing declared argument names to
  key names. First it will try to match them without considering default
  values (if any) and in case there is no success (there is no declared arity
  that can be satisfied by the given arguments) matching is preformed again
  but with default arguments merged. From the resulting set of matching arity
  mappings the one with the least requirements is chosen (that has the lowest
  count of declared arguments).

  Function returns a function object."
  {:added "0.6"
   :tag clojure.lang.Fn}
  [^clojure.lang.Fn f & more]
  {:pre [(instance? clojure.lang.Fn f)]}
  (validate-variadity (take-nth 2 more))
  (validate-named-arities (take-nth 2 (next more)))
  (fn [& {:as args}]
    (validate-named-args args)
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

  Since version 0.7.0 it accepts multiple arity mappings expressed as pairs
  consisting of vectors of argument names and maps of default values for all
  or some of names.

  The order of names in a vector is important. Each given name will become a key
  of named argument which value will be passed to the given function on the same
  position as in the vector.

  If unquoted symbol is given in a vector or in a map, it will be transformed to
  a keyword of the same name. Use quoted symbols if you want to use symbols as
  keys of named arguments.

  The macro is capable of handling multiple arities. In such case the declared
  arities (e.g. [:a :b] [:a :b :c]) will be matched against the given named
  arguments (e.g. {:a 1 :b 2}) by comparing declared argument names to key
  names. First it will try to match them without considering default
  values (if any) and in case there is no success (there is no declared arity
  that can be satisfied by the given arguments) matching is preformed again
  but with default arguments merged. From the resulting set of matching arity
  mappings the one with the least requirements is chosen (that has the lowest
  count of declared arguments).

  If the &rest special symbol is placed in a vector then the passed value that
  corresponds to its position will be a map containing all named arguments that
  weren't handled. If there are none, nil value is passed.

  The result is a function object."
  {:added "0.6"}
  ([f exp-args] `(nameize ~f ~exp-args {}))
  ([f exp-args defaults & more]
   (let [n (->> (list* exp-args defaults more)
                (reduce (fn [acc e]
                          (if (and (vector? (first acc)) (not (map? e)))
                            (recur (cons {} acc) e)
                            (cons e acc))) ()))
         n (->> (if (map? (first n)) n (cons {} n))
                (partition-all 2)
                (reduce
                 (fn [acc [defl exp]]
                   (when-not (vector? exp)
                     (throw-arg "First element of a mapping pair must be a vector"))
                   (when-not (map? defl)
                     (throw-arg "Last element of a mapping pair must be a map"))
                   (->> acc
                        (cons (#'keywordize-syms defl))
                        (cons (cons 'list (#'keywordize-syms exp))))) ()))]
     `(nameize* ~f ~@n))))
