(ns futils.core.argc
  (:use midje.sweet)
  (:require [futils.core :refer [argc]]))

^{:refer futils.core/argc :added "0.1"}
(fact "checks that function counts positional arguments"

      (argc (fn [x])) => (contains {:arities  #{1}
                                    :engine   keyword?
                                    :f        ifn?
                                    :variadic false})


  )

