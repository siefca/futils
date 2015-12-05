(ns futils.args.relax
  (:use midje.sweet)
  (:require [futils.args :refer [relax]]))

[[{:tag "relax-usage" :title "Using <code>relax</code>"}]]
^{:refer futils.args/relax :added "0.1"}
(fact

  (defn fun
    ([a b]   (list a b))
    ([a b c] (list a b c)))
  
  (def relaxed (relax fun))
  
  (relaxed)         => '(nil nil)
  (relaxed 1)       => '(1 nil)
  (relaxed 1 2)     => '(1 2)
  (relaxed 1 2 3)   => '(1 2 3)
  (relaxed 1 2 3 4) => '(1 2 3)
  
  (def relaxed (relax #'fun))
  
  (relaxed)         => '(nil nil)
  (relaxed 1 2 3 4) => '(1 2 3))

[[{:tag "relax-usage-variadic" :title "Handling variadic arguments by <code>relax</code>"}]]
^{:refer futils.args/relax :added "0.1"}
(fact

  (defn fun
    ([a] (list a))
    ([a b & more] (list* a b more)))
  
  (def relaxed (relax fun))
  
  (relaxed)         => '(nil)      ; matched arity: [a]
  (relaxed 1)       => '(1)        ; matched arity: [a]
  (relaxed 1 2)     => '(1 2)      ; matched arity: [a b & more]
  (relaxed 1 2 3)   => '(1 2 3)    ; matched arity: [a b & more]
  (relaxed 1 2 3 4) => '(1 2 3 4)  ; matched arity: [a b & more]
  
  (defn fun2 [& more] more)
  (def relaxed (relax fun2))
  
  (relaxed)         => nil
  (relaxed 1)       => '(1)
  (relaxed 1 2)     => '(1 2)
  (relaxed 1 2 3)   => '(1 2 3)
  (relaxed 1 2 3 4) => '(1 2 3 4))

[[{:tag "relax-usage-anonymous" :title "Using <code>relax</code> on anonymous functions"}]]
^{:refer futils.args/relax :added "0.1"}
(fact

  (def relaxed
    (relax (fn
             ([a]     (list a))
             ([a b c] (list a b c)))))
  
  (relaxed)         => '(nil)      ; matched arity: [a]
  (relaxed 1)       => '(1)        ; matched arity: [a]
  (relaxed 1 2)     => '(1 2 nil)  ; matched arity: [a b c]
  (relaxed 1 2 3)   => '(1 2 3)    ; matched arity: [a b c]
  (relaxed 1 2 3 4) => '(1 2 3)    ; matched arity: [a b c]
  )

[[{:tag "relax-usage-padding-val" :title "Custom padding value"}]]
^{:refer futils.args/relax :added "0.1"}
(fact

  (def relaxed (relax #(list %1 %2 %3) :pad-val :nic))
  
  (relaxed)           => '(:nic :nic :nic)
  (relaxed 1)         => '(1 :nic :nic)
  (relaxed 1 2)       => '(1 2 :nic)
  (relaxed 1 2 3)     => '(1 2 3)
  (relaxed 1 2 3 4)   => '(1 2 3))

[[{:tag "relax-usage-padding-fn" :title "Custom padding function"}]]
^{:refer futils.args/relax :added "0.1"}
(fact

  (defn padder
    [& {:keys [previous] :or {previous -1}}]
    (inc previous))
  
  (def relaxed (relax #(list %1 %2 %3) :pad-fn padder))
  
  (relaxed)         => '(0 1 2)
  (relaxed 1)       => '(1 2 3)
  (relaxed 5)       => '(5 6 7)
  (relaxed 1 8)     => '(1 8 9))

[[{:tag "relax-usage-verbose" :title "Verbose mode"}]]
^{:refer futils.args/relax :added "0.1"}
(fact

  (defn fun
    ([a] (list a))
    ([a b & more] (list* a b more)))
  
  (def relaxed (relax fun :verbose true))
  
  (relaxed)
  => {:argc-cutted   0
      :argc-padded   1
      :argc-received 0
      :argc-sent     1
      :args-received ()
      :args-sent     '(nil)
      :arities       '(1 3)
      :arity-matched 1
      :engine        :jvm
      :result        '(nil)
      :variadic      true
      :variadic-used false
      :verbose       true} 
  
  (relaxed 1 2 3)
  => {:argc-cutted   0
      :argc-padded   0
      :argc-received 3
      :argc-sent     3
      :args-received '(1 2 3)
      :args-sent     '(1 2 3)
      :arities       '(1 3)
      :arity-matched 3
      :engine        :jvm
      :result        '(1 2 3)
      :variadic      true
      :variadic-used true
      :verbose       true})

[[{:tag "relax-usage-notfun" :title "Handling invalid values"}]]
^{:refer futils.args/relax :added "0.1"}
(fact

  (def notfun)
  
  (relax   1)    => (throws java.lang.AssertionError)
  (relax nil)    => (throws java.lang.AssertionError)
  (relax "a")    => (throws java.lang.AssertionError)
  (relax notfun) => (throws java.lang.AssertionError)
  (relax String) => (throws java.lang.AssertionError))
