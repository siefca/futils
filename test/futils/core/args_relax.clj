(ns futils.core.args-relax
  (:use midje.sweet)
  (:require [futils.core :refer [args-relax mapply argc]]))

[[{:tag "args-relax-usage" :title "Using <code>args-relax</code>"}]]
^{:refer futils.core/args-relax :added "0.1"}
(fact
  
  (defn fun
    ([a b]   (list a b))
    ([a b c] (list a b c)))
  
  (def relaxed (args-relax fun :arities #{2 3}))
  
  (relaxed)         => '(nil nil)
  (relaxed 1)       => '(1 nil)
  (relaxed 1 2)     => '(1 2)
  (relaxed 1 2 3)   => '(1 2 3)
  (relaxed 1 2 3 4) => '(1 2 3))

[[{:tag "args-relax-usage-variadic" :title "Handling variadic arguments by <code>args-relax</code>"}]]
^{:refer futils.core/args-relax :added "0.1"}
(fact
  
  (defn fun
    ([a] (list a))
    ([a b & more] (list* a b more)))
  
  (def relaxed (args-relax fun
                           :arities #{3 1}
                           :variadic true))
  
  (relaxed)         => '(nil)     ; matched arity: [a]
  (relaxed 1)       => '(1)       ; matched arity: [a]
  (relaxed 1 2 3 4) => '(1 2 3 4) ; matched arity: [a b & more]
  
  (defn fun2 [& more] more)
  (def relaxed (args-relax fun2 :arities #{1} :variadic true))
  
  (relaxed)         => nil
  (relaxed 1)       => '(1)
  (relaxed 1 2 3 4) => '(1 2 3 4))

[[{:tag "args-relax-usage-anonymous" :title "Using <code>args-relax</code> on anonymous functions"}]]
^{:refer futils.core/args-relax :added "0.1"}
(fact
  
  (def relaxed
    (args-relax (fn
                  ([a]     (list a))
                  ([a b c] (list a b c)))
                :arities #{3 1}))
  
  (relaxed)         => '(nil)      ; matched arity: [a]
  (relaxed 1)       => '(1)        ; matched arity: [a]
  (relaxed 1 2)     => '(1 2 nil)  ; matched arity: [a b c]
  (relaxed 1 2 3 4) => '(1 2 3)    ; matched arity: [a b c]
  )

[[{:tag "args-relax-usage-padding-val" :title "Custom padding value"}]]
^{:refer futils.core/args-relax :added "0.1"}
(fact
  
  (def relaxed (args-relax #(list %1 %2 %3)
                           :arities #{3}
                           :pad-val :nic))
  
  (relaxed)           => '(:nic :nic :nic)
  (relaxed 1)         => '(1 :nic :nic)
  (relaxed 1 2 3 4)   => '(1 2 3))

[[{:tag "args-relax-usage-padding-fn" :title "Custom padding function"}]]
^{:refer futils.core/args-relax :added "0.1"}
(fact
  
  (defn padder
    [& {:keys [previous] :or {previous -1}}]
    (inc previous))
  
  (def relaxed (args-relax #(list %1 %2 %3)
                           :arities #{3}
                           :pad-fn padder))
  
  (relaxed)         => '(0 1 2)
  (relaxed 1)       => '(1 2 3)
  (relaxed 5)       => '(5 6 7)
  (relaxed 1 8)     => '(1 8 9))

[[{:tag "args-relax-usage-verbose" :title "Verbose mode"}]]
^{:refer futils.core/args-relax :added "0.1"}
(fact
  
  (defn fun
    ([a] (list a))
    ([a b & more] (list* a b more)))
  
  (def relaxed (args-relax fun
                           :arities #{3 1}
                           :variadic true
                           :verbose true))
  
  (relaxed)
  => {:argc-cutted   0
      :argc-padded   1
      :argc-received 0
      :argc-sent     1
      :args-received ()
      :args-sent     '(nil)
      :arities       #{1 3}
      :arity-matched 1
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
      :arities       #{1 3}
      :arity-matched 3
      :result        '(1 2 3)
      :variadic      true
      :variadic-used true
      :verbose       true})

[[{:tag "args-relax-usage-argc" :title "Chaining with <code>argc</code>"}]]
^{:refer futils.core/args-relax :added "0.1"}
(fact
  (defn fun
    ([a b]   (list a b))
    ([a b c] (list a b c)))
  
  (def relaxed (mapply args-relax fun (argc fun)))
  
  (relaxed)         => '(nil nil)
  (relaxed 1)       => '(1 nil)
  (relaxed 1 2)     => '(1 2)
  (relaxed 1 2 3)   => '(1 2 3)
  (relaxed 1 2 3 4) => '(1 2 3))

[[{:tag "args-relax-usage-power" :title "With great power comes great responsibility"}]]
^{:refer futils.core/args-relax :added "0.1"}
(fact
  (defn fun
    ([a b]   (list a b))
    ([a b c] (list a b c)))
  
  (def relaxed (args-relax fun :arities #{1 2 5}))  ; wrong arities!
  
  (relaxed)         => (throws clojure.lang.ArityException)
  (relaxed 1)       => (throws clojure.lang.ArityException)
  (relaxed 1 2)     => '(1 2)
  (relaxed 1 2 3)   => (throws clojure.lang.ArityException)
  (relaxed 1 2 3 4) => (throws clojure.lang.ArityException))

[[{:tag "args-relax-usage-notfun" :title "Handling invalid values"}]]
^{:refer futils.core/args-relax :added "0.1"}
(fact
  (defn fun [])
  (def  notfun)
  
  (args-relax    #())               => (throws java.lang.AssertionError)
  (args-relax    #() #())           => (throws java.lang.IllegalArgumentException)
  (args-relax    nil nil)           => (throws java.lang.IllegalArgumentException)
  (args-relax    :arities #{0} #()) => (throws java.lang.AssertionError)
  (args-relax    #() :arities #{})  => (throws java.lang.AssertionError)
  (args-relax    #() :arities nil)  => (throws java.lang.AssertionError)
  (args-relax    #() :arities 123)  => (throws java.lang.IllegalArgumentException)
  (args-relax      1 :arities #{})  => (throws java.lang.AssertionError)
  (args-relax      1 :arities #{})  => (throws java.lang.AssertionError)
  (args-relax    nil :arities #{0}) => (throws java.lang.AssertionError)
  (args-relax    "a" :arities #{0}) => (throws java.lang.AssertionError)
  (args-relax notfun :arities #{0}) => (throws java.lang.AssertionError)
  (args-relax String :arities #{0}) => (throws java.lang.AssertionError)
  (args-relax  #'fun :arities #{0}) => (throws java.lang.AssertionError))

