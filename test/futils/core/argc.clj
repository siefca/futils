(ns futils.core.argc
  (:use midje.sweet)
  (:require [futils.core :refer [argc]]))

^{:refer futils.core/argc :added "0.1"}
(facts "about argc"
  
  (fact "counts positional arguments"
    
    (argc (fn [x]))
    => (contains
        {:arities  #{1}
         :engine   keyword?
         :f        ifn?
         :variadic false})
    
    (argc (fn ([x y])([z])))
    => (contains
        {:arities  #{1 2}
         :variadic false}))
  
  (fact "counts variadic arguments"
    
    (argc (fn [a & b]))
    => (contains
        {:arities  #{2}
         :variadic true})
    
    (argc (fn ([])([& a])))
    => (contains
        {:arities  #{0 1}
         :variadic true}))
  
  (fact "works on named functions and Vars"
    
    (defn- fun
      ([])
      ([a])
      ([a b])
      ([a b & c]))
    
    (argc fun)
    => (contains
        {:arities  #{0 1 2 3}
         :variadic true})
    
    (argc #'fun)
    => (contains
        {:arities  #{0 1 2 3}
         :variadic true}))
  
  (fact "works on macros"
    
    (defmacro mak
      ([])
      ([a])
      ([a b])
      ([a b & c]))
    
    (argc #'mak)
    => (contains
        {:arities  #{0 1 2 3}
         :macro    true
         :variadic true}))
  
  (fact "returns nil if the given argument is not a function"
    
    (def notfun)
    
    (argc   1)    => nil
    (argc nil)    => nil
    (argc "a")    => nil
    (argc notfun) => nil
    (argc String) => nil))

