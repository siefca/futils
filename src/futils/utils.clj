(ns ^{:doc    "futils library, utility functions."
      :author "Paweł Wilk"}
    
    futils.utils)

;; Set operations.
;;
(defn ^long nearest-right
  "For the given sorted set of integer values and the given value returns the
  closest value from the set, picking up the highest one in case of no exact
  match."
  [^clojure.lang.IPersistentSet s ^long v]
  (if-let [x (first (subseq s >= v))]
    x
    (when-let [x (first (rsubseq s < v))]
      x)))

;; Coercions and ensurances.
;;

(defn ^clojure.lang.IFn ensure-fn
  "If a value of the argument f is a Var object it dereferences it first. If the
  resulting object is not a function it returns nil."
  [f]
  (when-let [fun (if (var? f) (deref f) f)]
    (when (ifn? fun) fun)))

(defn ^long not-negative
  "Ensures that a given value is positive or 0. If it's negative it returns 0."
  [^long n]
  (if (neg? n) 0 n))

;; Java interop.
;; 
(defn method-name
  "Returns the name of the given Java method."
  [^java.lang.reflect.Method m]
  (.getName m))

(defn method-argc
  "Returns the number of arguments Java method takes."
  [^java.lang.reflect.Method m]
  (alength (.getParameterTypes m)))

;; Simlifiers.
;;
(def any? (comp true? some))

