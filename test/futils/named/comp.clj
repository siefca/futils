(ns futils.named.comp
  (:use midje.sweet)
  (:require [futils.named :as named]))

[[{:tag "comp-usage" :title "Basic usage of <code>comp</code>"}]]
^{:refer futils.named/comp :added "1.2"}
(fact

  (defn f1 [& {:as args}] (assoc args :f1 1))
  (defn f2 [& {:as args}] (assoc args :f2 2))
  (defn f3 [& {:as args}] (assoc args :f3 3))
  (def nfun (named/comp f1 f2 f3))
  
  (nfun)                           => {:f1 1 :f2 2 :f3 3}
  (nfun :a 1)                      => {:f1 1 :f2 2 :f3 3 :a 1}
  (nfun :a 1 :b 2)                 => {:f1 1 :f2 2 :f3 3 :a 1 :b 2}
  (nfun :a 1 :b 2 :c 3)            => {:f1 1 :f2 2 :f3 3 :a 1 :b 2 :c 3}
  (nfun :a 1 :b 2 :c 3 :d 4)       => {:f1 1 :f2 2 :f3 3 :a 1 :b 2 :c 3 :d 4}
  
  (def nfun (named/comp f1))
  
  (nfun)                           => (just [:f1 1]                     :in-any-order)
  (nfun :a 1)                      => (just [:f1 1 :a 1]                :in-any-order)
  (nfun :a 1 :b 2)                 => (just [:f1 1 :a 1 :b 2]           :in-any-order)
  (nfun :a 1 :b 2 :c 3)            => (just [:f1 1 :a 1 :b 2 :c 3]      :in-any-order)
  (nfun :a 1 :b 2 :c 3 :d 4)       => (just [:f1 1 :a 1 :b 2 :c 3 :d 4] :in-any-order)
  
  (def nfun (named/comp))
  
  (nfun)                           => nil
  (nfun :a 1)                      => (just [:a 1]                :in-any-order)
  (nfun :a 1 :b 2)                 => (just [:a 1 :b 2]           :in-any-order)
  (nfun :a 1 :b 2 :c 3)            => (just [:a 1 :b 2 :c 3]      :in-any-order)
  (nfun :a 1 :b 2 :c 3 :d 4)       => (just [:a 1 :b 2 :c 3 :d 4] :in-any-order))

[[{:tag "comp-usage-args" :title "Merging arguments with <code>comp</code>"}]]
^{:refer futils.named/comp :added "1.2"}
(fact

  (defn f1 [& {:as args}] (assoc args :f1 1))
  (defn f2 [& {:as args}] (assoc args :f2 2))
  (defn f3 [& {:as args}] (assoc args :f3 3))
  
  (def nfun (named/comp f1 {:f f2 :merge-args :in} f3))
  
  (nfun)       => {:f1 1 :f2 2 :f3 3 :in {:f3 3}}
  (nfun :a 1)  => {:f1 1 :f2 2 :f3 3 :a 1 :in {:a 1 :f3 3}}
  
  ;; Now the f3 function is not passing args on its own.
  ;; Instead it emits a simple value.
  (defn f3 [& {:as args}] 123456)
  
  ;; Normally it will ignore args.
  ((named/comp f1 f2 f3) :a 1)
  => {:f1 1 :f2 2 :out 123456}
  
  ;; With :merge-args set to some value it will merge args
  ;; with the resulting map under the given key.
  ((named/comp f1 f2 {:f f3 :merge-args :f3-in}) :a 1)
  => {:f1 1 :f2 2 :f3-in {:a 1} :out 123456}
  
  ;; With :merge-args set to true it will merge args
  ;; with the resulting map.
  ((named/comp f1 f2 {:f f3 :merge-args true}) :a 1)
  => {:f1 1 :f2 2 :a 1 :out 123456}
  
  ;; It won't overwrite conflicting keys.
  ((named/comp f1 f2 {:f f3 :merge-args true}) :out 1111111)
  => {:f1 1 :f2 2 :out 123456}
  
  ((named/comp f1 f2 {:f f3 :merge-args true}) :f2 1111111)
  => {:f1 1 :f2 2 :out 123456}
  
  (defn f3 [& {:as args}] {:x 2})
  ((named/comp f1 f2 {:f f3 :merge-args true}) :x 1111111 :y 4)
  => {:f1 1 :f2 2 :x 2 :y 4})

[[{:tag "comp-usage-map" :title "Mapping output with <code>comp</code>"}]]
^{:refer futils.named/comp :added "1.2"}
(fact

  (defn f1 [& {:as args}] (assoc args :f1 1))
  (defn f2 [& {:as args}] (assoc args :f2 2))
  (defn f3 [& {:as args}] (assoc args :f3 3))
  
  ((named/comp f1 f2 {:f f3 :map-output true}) :a 1 :b 2)
  => {:f1 1 :f2 2 :out {:a 1 :b 2 :f3 3}}
  
  ((named/comp f1 f2 f3 {:map-output true}) :a 1 :b 2)
  => {:f1 1 :out {:f2 2 :out {:a 1 :b 2 :f3 3}}}
  
  ((named/comp f1 f2 {:f f3 :map-output :r}) :a 1 :b 2)
  => {:f1 1 :f2 2 :r {:a 1 :b 2 :f3 3}}
  
  ((named/comp f1 f2 f3 {:map-output :r}) :a 1 :b 2)
  => {:f1 1 :r {:f2 2 :r {:a 1 :b 2 :f3 3}}})

[[{:tag "comp-usage-rename" :title "Renaming results with <code>comp</code>"}]]
^{:refer futils.named/comp :added "1.2"}
(fact

  (defn f1 [& {:as args}] (assoc args :f1 1))
  (defn f2 [& {:as args}] (assoc args :f2 2))
  (defn f3 [& {:as args}] (assoc args :f3 3))
  
  ((named/comp f1 f2 {:f f3 :rename-keys {:a :b :b :c}}) :a 1 :b 2)
  => {:b 1 :c 2 :f1 1 :f2 2 :f3 3}
  
  ((named/comp f1 f2 {:f f3
                      :map-output true
                      :rename-keys {:a :x}}) :a 1)
  => {:f1 1 :f2 2 :out {:f3 3 :x 1}}
  
  ((named/comp f1 f2 {:f f3
                      :map-output true
                      :post-rename {:out :result}}) :a 1)
  => {:f1 1 :f2 2 :result {:f3 3 :a 1}}
  
  ((named/comp f1 f2 {:f f3
                      :map-output true
                      :rename-keys {:a :x}
                      :post-rename {:out :result}}) :a 1)
  => {:f1 1 :f2 2 :result {:f3 3 :x 1}})
