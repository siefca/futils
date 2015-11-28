(ns futils.core.mapply
  (:use midje.sweet)
  (:require [futils.core :refer [mapply]]))

[[{:tag "mapply-usage" :title "Usage of <code>mapply</code>"}]]
^{:refer futils.core/mapply :added "0.2"}
(fact

  (mapply assoc {} {:a 1 :b 2 :c 3})
  => {:a 1 :b 2 :c 3}

  (defn fun [& {:as args}] args)
  (mapply fun {:a 1 :b 2 :c 3})
  => {:a 1 :b 2 :c 3}

  (defn fun [a b & {:as args}] (sort (list* a b (vals args))))
  (mapply fun 10 20 {:a 1 :b 2 :c 3})
  => '(1 2 3 10 20))

[[{:tag "mapply-usage-notfun" :title "Handling invalid values by <code>mapply</code>"}]]
^{:refer futils.core/mapply :added "0.2"}
(fact
  (def notfun)

  (mapply)        => (throws clojure.lang.ArityException)
  (mapply      1) => (throws java.lang.ClassCastException)
  (mapply    nil) => (throws java.lang.NullPointerException)
  (mapply    "a") => (throws java.lang.ClassCastException)
  (mapply notfun) => (throws java.lang.IllegalStateException)
  (mapply String) => (throws java.lang.ClassCastException))
