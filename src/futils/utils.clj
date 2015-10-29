(ns
    ^{:doc    "futils library, utility functions."
      :author "Paweł Wilk"}    

    futils.utils)

;; Set operations.
;;
(defn ^long nearest-right
  "For the given sorted set of integer values and the given value returns the
  closest value from the set, picking up the highest one in case of no exact
  match."
  {:added "0.1"}
  [^clojure.lang.IPersistentSet s ^long v]
  (if-let [x (first (subseq s >= v))]
    x
    (when-let [x (first (rsubseq s < v))]
      x)))

;; Coercions and ensurances.
;;
(defn ^clojure.lang.Fn require-fn
  "If a value of the argument f is a Var object it dereferences it first. If the
  resulting object is not a function it returns nil."
  {:added "0.1"}
  [f]
  (when-let [fun (if (var? f) (deref f) f)]
    (when (and (ifn? fun) (instance? clojure.lang.Fn fun)) fun)))

(defn ^long not-negative
  "Ensures that a given value is positive or 0. If it's negative it returns 0."
  {:added "0.1"}
  [^long n]
  (if (neg? n) 0 n))

(defn ^long pos-
  "Like - operator but works for 2 numbers and never returns negative values. If
  value is negative, it will return 0."
  {:added "0.3"}
  [^long x ^long y]
  (not-negative (- x y)))

;; Java interop.
;; 
(defn method-name
  "Returns the name of the given Java method."
  {:added "0.1"}
  [^java.lang.reflect.Method m]
  (.getName m))

(defn method-argc
  "Returns the number of arguments Java method takes."
  {:added "0.1"}
  [^java.lang.reflect.Method m]
  (alength (.getParameterTypes m)))

;; Simlifiers.
;;
(def
  ^{:added "0.1"
    :arglists '([^clojure.lang.Fn pred ^clojure.lang.Sequential coll])}
  any?
  "Returns true if a collection contains at least one element for which pred is
  not false and not nil."
  (comp boolean some))

