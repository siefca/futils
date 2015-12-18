(ns futils.named.apply
  (:use midje.sweet)
  (:require [futils.named :as named]))

[[{:tag "apply-usage" :title "Usage of <code>apply</code>"}]]
^{:refer named/apply :added "1.2"}
(fact

  (named/apply assoc {} {:a 1 :b 2 :c 3})
  => {:a 1 :b 2 :c 3}


  (defn fun [& {:as args}] args)
  (named/apply fun {:a 1 :b 2 :c 3})
  => {:a 1 :b 2 :c 3}

  (defn fun [a b & {:as args}] (sort (list* a b (vals args))))
  (named/apply fun 10 20 {:a 1 :b 2 :c 3})
  => '(1 2 3 10 20))

[[{:tag "apply-usage-notfun" :title "Handling invalid values by <code>apply</code>"}]]
^{:refer named/apply :added "1.2"}
(fact

  (def notfun)


  (named/apply)         => (throws clojure.lang.ArityException)
  (named/apply      1)  => (throws java.lang.ClassCastException)
  (named/apply    nil)  => (throws java.lang.NullPointerException)
  (named/apply    "a")  => (throws java.lang.ClassCastException)
  (named/apply notfun)  => (throws java.lang.IllegalStateException)
  (named/apply String)  => (throws java.lang.ClassCastException))
