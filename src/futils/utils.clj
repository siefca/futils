(ns
    
    ^{:doc    "futils library, utility functions."
      :author "Paweł Wilk"}
    
    futils.utils)

;; Set operations.
;;
(defn nearest-right
  "For the given sorted set of integer values and the given value returns the
  closest value from the set, picking up the highest one in case of no exact
  match."
  {:added "0.1"
   :tag long}
  [^clojure.lang.IPersistentSet s ^long v]
  (if-let [x (first (subseq s >= v))]
    x
    (when-let [x (first (rsubseq s < v))]
      x)))

;; Coercions and ensurances.
;;
(defn not-negative
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
  (not-negative (- x y)))

(defn require-fn
  {:added "0.1"
   :tag clojure.lang.Fn}
  [f]
  (when-let [fun (if (var? f) (deref f) f)]
    (when (and (ifn? fun) (instance? clojure.lang.Fn fun)) fun)))

(defn keywordize-syms
  "Recursively transforms all map keys from symbols to keywords."
  {:added "0.6"}
  [m]
  (let [f (fn [[k v]] (if (symbol? k) [(keyword k) v] [k v]))]
    (into {} (map f m))))

;; Java interop.
;; 
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

;; Simplifiers.
;;
(def
  ^{:added "0.1"
    :tag Boolean
    :arglists '([^clojure.lang.Fn pred ^clojure.lang.ISeq coll])}
  any?
  "Returns true if the given collection (coll) contains at least one element for
  which a value passed as the pred argument is not false and not nil."
  (comp boolean some))

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

;; Low-level args parsing.
;;
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

