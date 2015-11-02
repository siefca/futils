(ns futils.core.argc
  (:use midje.sweet)
  (:require [futils.core :refer [argc]]))

[[{:tag "argc-usage" :title "Using <code>argc</code> on anonymous functions"}]]
^{:refer futils.core/argc :added "0.1"}
(fact
  
  (argc (fn ([x y]) ([])))
  => {:arities  #{0 2}
      :engine   :jvm
      :variadic false}
  
  (argc (fn [a b & c]))
  => {:arities  #{3}
      :engine   :jvm
      :variadic true})

[[{:tag "argc-usage-named" :title "Using <code>argc</code> on named functions"}]]
^{:refer futils.core/argc :added "0.1"}
(fact
  
  (defn fun ([]) ([a]) ([a b]) ([a b & c]))
  
  (argc fun)
  => {:arities  #{0 1 2 3}
      :engine   :jvm
      :variadic true})
  
[[{:tag "argc-usage-macro" :title "Using <code>argc</code> on macros"}]]
^{:refer futils.core/argc :added "0.1"}
(fact
  
  (defmacro mak ([]) ([a]) ([a b]) ([a b & c]))
  
  (argc #'mak)
  => {:arities  #{0 1 2 3}
      :engine   :jvm
      :macro    true
      :variadic true})

[[{:tag "argc-usage-macro-2" :title "Using <code>argc</code> on macros (by symbols)"}]]
^{:refer futils.core/argc :added "0.1"}
(comment
  (defmacro mak ([]) ([a]))
  
  (argc mak)
  => {:arities  #{0 1}
      :engine   :jvm
      :macro    true
      :variadic false})

[[{:tag "argc-usage-notfun" :title "Handling invalid values by <code>argc</code>"}]]
^{:refer futils.core/argc :added "0.1"}
(fact
  (def notfun)
  
  (argc   1)    => nil
  (argc nil)    => nil
  (argc "a")    => nil
  (argc notfun) => nil
  (argc String) => nil)

