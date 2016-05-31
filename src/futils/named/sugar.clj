(ns

    ^{:doc    "futils library, nameized built-in functions."
      :author "Paweł Wilk"}

    futils.named.sugar

  (:refer-clojure :only [fn defn if-let when cond and or])
  (:require [clojure.core :as c]
            [futils.core  :refer :all]
            [futils.named :as   named]
            [futils.utils :refer :all]))

(futils.core/init)

(defn reduce
  {:added "0.8"}
  [& {:keys [:f :coll :val] :as args}]
  (if-let [coll (c/find :coll args)]
    (c/reduce f val coll)
    (c/reduce f coll)))

(defn map
  {:added "0.8"}
  [& {:keys [:f :coll :colls] :as args}]
  (if-let [colls (c/find :colls args)]
    (c/apply c/map f coll colls)
    (c/map f coll)))

(def
  ^{:added "0.8"}
  apply
  mapply)

(defn list
  [& {:keys [:items]}]
  (c/apply c/list items))
