(ns futils.named.nameize
  (:use midje.sweet)
  (:require [futils.named :refer [nameize]]))

[[{:tag "nameize-usage" :title "Usage of <code>nameize</code>"}]]
^{:refer futils.named/nameize :added "0.6"}
(fact

  (def nfun (nameize #(list %1 %2) [a b]))
  (nfun :a 1 :b 2)
  => '(1 2)

  (def nfun (nameize (fn [& a] a) [a b] {:a 7 :b 8}))
  (nfun :b 2)
  => '(7 2)

  (defn fun [& more] more)

  (def nfun (nameize fun [a b c]))
  (nfun :a 1 :b 2 :c 3 :d 4)
  => '(1 2 3)

  (def nfun (nameize fun [a b c &rest]))
  (nfun :a 1 :b 2 :c 3 :d 4)
  => '(1 2 3 {:d 4})

  (def nfun (nameize fun [a &rest b c] {:a 1}))
  (nfun :b 2 :c 3 :d 4)
  => '(1 {:d 4} 2 3)

  (def nfun (nameize fun [a b c &rest]))
  (nfun :a 1 :b 2 :c 3)
  => '(1 2 3 nil))

[[{:tag "nameize-usage-notfun" :title "Handling invalid values by <code>nameize</code>"}]]
^{:refer futils.named/nameize :added "0.6"}
(fact
  (def notfun)

  (nameize notfun []) => (throws java.lang.AssertionError)

  (defn fun [a b] (list a b))
  (def nfun (nameize fun [a b]))
  (nfun :a 1)
  => (throws java.lang.IllegalArgumentException))
