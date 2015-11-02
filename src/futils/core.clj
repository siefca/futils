(ns
    
    ^{:doc    "futils library, wrapping functions."
      :author "Paweł Wilk"}
    
    futils.core
  
  (:require [futils.utils :refer :all]))

;; used by farg-count-clj:
;; 

(def
  ^{:private true
    :added "0.1"
    :tag Boolean
    :arglists '([^Character c])}
  not-ampersand?
  (partial not= '&))

(def
  ^{:private true
    :added "0.1"
    :tag clojure.lang.ISeq
    :arglists '([^clojure.lang.ISeq coll])}
  obligatory-args
  (partial take-while not-ampersand?))

(def
  ^{:private true
    :added "0.1"
    :tag long
    :arglists '([^clojure.lang.ISeq coll])}
  obligatory-argc
  (comp count obligatory-args))

(def
  ^{:private true
    :added "0.1"
    :tag clojure.lang.IPersistentVector
    :arglists '([^clojure.lang.ISeq coll])}
  fn-arg-counters
  (juxt count obligatory-argc))

;; used by farg-count-jvm:
;; 

(def
  ^{:private true
    :const true
    :added "0.1"
    :tag Boolean
    :arglists '([^String name])}
  is-function?
  #{"invoke" "doInvoke"})

(def
  ^{:private true
    :added "0.1"
    :tag Boolean
    :arglists '([^String name])}
  variadic-fn?
  (comp (partial = "doInvoke") method-name))

(def
  ^{:private true
    :added "0.1"
    :tag clojure.lang.ISeq
    :arglists '([^clojure.lang.ISeq coll])}
  take-fn-methods
  (partial filter (comp is-function? method-name)))

;; used by farg-count-jvm and farg-count-clj:
;;

(def
  ^{:private true
    :added "0.1"
    :tag Boolean
    :arglists '([^Boolean flag])}
  variadic?
  (comp true? first))

(def
  ^{:private true
    :added "0.1"
    :tag Boolean
    :arglists '([^clojure.lang.ISeq coll])}
  is-variadic?
  (partial any? variadic?))

(def
  ^{:private true
    :added "0.1"
    :tag clojure.lang.ISeq
    :arglists '([^clojure.lang.ISeq coll])}
  only-counters
  (partial map last))

(def
  ^{:private true
    :added "0.1"
    :tag clojure.lang.ISeq
    :arglists '([^clojure.lang.ISeq coll])}
  count-meth-args
  (partial map (juxt variadic-fn? method-argc)))

(def
  ^{:private true
    :added "0.1"
    :tag clojure.lang.IPersistentMap
    :arglists '([^clojure.lang.ISeq coll])}
  gen-fargs-map
  (partial zipmap [:variadic :arities]))

(def
  ^{:private true
    :added "0.1"
    :tag clojure.lang.IPersistentVector
    :arglists '([^clojure.lang.ISeq coll])}
  gen-fargs-seq
  (juxt is-variadic? only-counters))

(def
  ^{:private true
    :added "0.1"
    :tag clojure.lang.IPersistentVector
    :arglists '([^clojure.lang.IPersistentVector v])}
  to-sorted-set
  (juxt first (comp (partial into (sorted-set)) second)))

(defn- count-fn-args
  "Takes a sequence of vectors representing arities (first element represents
  the number of obligatory arguments and second the total number of arguments,
  including variadic parameter, if any). It produces a sequence in which each
  element is a vector representing an arity, which the first element is
  a boolean flag that informs whether the arity is variadic and the second is
  a number of its obligatory arguments (including variadic parameter).
    
  It is intented to be used by the farg-count-clj function."
  {:added "0.1"
   :tag clojure.lang.ISeq}
  [^clojure.lang.ISeq arglists]
  (map #(let [[ca cr] (fn-arg-counters %)
              vari    (not= cr ca)
              cntr    (if vari (inc cr) cr)]
          [vari cntr]) arglists))
 
(defn argc-clj
  "Uses the given Var's :arglists metadata value to determine the number of
  arguments taken by a function that the Var is bound to. See (doc argc)
  for more specific documentation."
  {:added "0.1"
   :tag clojure.lang.IPersistentMap}
  [^clojure.lang.Var varobj]
  (when-let [fun (require-fn varobj)]
    (some-> varobj
            meta :arglists
            count-fn-args
            gen-fargs-seq
            to-sorted-set
            gen-fargs-map
            (assoc :engine :clj))))

(defn argc-jvm
  "Uses JVM reflection calls to determine number of arguments the given function
  takes. Returns a map. See (doc argc) for more specific documentation."
  {:added "0.1"
   :tag clojure.lang.IPersistentMap}
  [^clojure.lang.Fn f]
  (when-let [fun (require-fn f)]
    (some-> fun
            class .getDeclaredMethods
            take-fn-methods
            count-meth-args
            gen-fargs-seq
            to-sorted-set
            gen-fargs-map
            (assoc :engine :jvm))))

(defn- macroize-argc
  "Takes argc output (a map), sets :macro to true and updates :arities in a way
  that all numbers are decreased two times."
  {:added "0.2"
   :tag clojure.lang.IPersistentMap}
  [^clojure.lang.IPersistentMap a]
  (when-let [ar a]
    (-> ar
        (update :arities (partial into (sorted-set) (map #(pos- % 2))))
        (assoc  :macro true))))

(defmacro argc
  "Determines the number of arguments that the given function takes and returns
  a map containing these keys:
  
  :arities  – a sorted set of argument counts for all arities,
  :engine   – :clj (if metadata were used to determine arities – DEPRECATED);
              :jvm (if Java reflection methods were used to determine arities),
  :macro    – a flag informing whether the given object is a macro,
  :variadic – a flag informing whether the widest arity is variadic.
  
  Variadic parameter is counted as one of the possible arguments.
  
  Macro flag (:macro) is only present when macro was detected. Otherwise it's
  missing.
  
  If the given argument cannot be used to obtain a Var bound to a functon or
  a function object then it returns nil."
  {:added "0.1"}
  [f]
  (let [m (and (symbol? f) (resolve f))]
    (if (and (var? m) (:macro (meta m)))
      `(#'macroize-argc (argc-jvm ~m))
      `(let [f# ~f r# (argc-jvm f#)]
         (if (and (var? f#) (:macro (meta f#)))
           (#'macroize-argc r#)
           r#)))))

(defn mapply
  "Like apply but works on named arguments. Takes function f and a list of
  arguments to be passed, were the last argument should be a map that will be
  decomposed and passed as named arguments.
  
  Returns the result of calling f."
  {:added "0.2"}
  [^clojure.lang.Fn f & args]
  (apply f (concat (butlast args) (mapcat identity (last args)))))

(defn- frepeat-core
  {:added "0.2"
   :tag clojure.lang.ISeq}
  [^long nr
   ^clojure.lang.Fn f
   ^clojure.lang.IPersistentMap params]
  (lazy-seq
   (let [par (assoc params :iteration nr)
         res (mapply f par)]
     (cons res
           (frepeat-core (inc nr) f (assoc par :previous res))))))

(defn frepeat
  "Returns a lazy sequence of results produced by the given function f
  which should accept named arguments.
  
  If the first argument passed to frepeat is a number and the second is
  a function it will limit the iterations to the specified count. If the numeric
  argument is missing and only a function object is given the frepeat will
  produce infinite sequence of calls.
  
  The last, optional argument should be a map that initializes named arguments
  that will be passed to the first and subsequent calls to f.
  
  Additionally each call to f will pass the following keyword arguments:
  
  :iteration     – a number of current iteration (starting from 1),
  :previous      – a result of the previous call to f.
  
  The first call to f will pass the following:
  
  :iteration     – 1,
  :iterations    – a total number of iterations (if nr was given).
  
  It is possible to set the initial value of :previous if there is a need for
  that (by passing it to frepeat) or shadow the :iterations after the first call
  (by setting it in the passed function f).
  
  Values associated with :iteration and :previous keys will always change during
  each call."
  {:added "0.2"
   :tag clojure.lang.ISeq
   :arglists '([^clojure.lang.Fn f]
               [^clojure.lang.Fn f ^clojure.lang.IPersistentMap kvs]
               [^long n ^clojure.lang.Fn f]
               [^long n ^clojure.lang.Fn f ^clojure.lang.IPersistentMap kvs])}
  ([^clojure.lang.Fn f]
   (frepeat-core 1 f nil))
  ([n-f f-m]
   (if (number? n-f)
     (frepeat (long n-f) f-m nil)
     (frepeat-core 1 n-f f-m)))
  ([^long n
    ^clojure.lang.Fn f
    ^clojure.lang.IPersistentMap kvs]
   (take n (frepeat-core 1 f (assoc kvs :iterations n)))))

(defmacro relax
  "Returns a variadic function object that calls the given function f, adjusting
  the number of passed arguments to a nearest arity of f. It cuts argument list
  or pads it with nil values if necessary.
  
  The arities will be obtained from metadata (if the given object is a symbol
  bound to a Var or a Var object itself) or using JVM reflection calls to
  anonymous class representing a function object (in case of function object).
  
  To determine the number of arguments the nearest arity is picked up by
  matching a number of passed arguments to number of arguments for each arity.
  If there is no exact match then the next arity capable of taking all arguments
  is selected.
  
  If the expected number of arguments is lower than a number of arguments
  actually passed to a wrapper call, the exceeding ones will be ignored.
  
  If the declared number of arguments that the original function expects is
  higher than a number of arguments really passed then nil values will be placed
  as extra arguments.
  
  When a variadic function is detected and its variadic arity is the closest to
  a number of arguments passed then all of them will be used during a function
  call (no argument will be ignored).
  
  It takes optional named arguments:
  
  :pad-fn   – a function that generates values for padding,
  :pad-val  – a value to use for padding instead of nil,  
  :verbose  – a switch (defaults to false) that if set to true causes wrapper to
              return a map containing additional information.
  
  See (doc args-relax) for more information about :pad-fn and :verbose options."
  {:added "0.1"
   :tag clojure.lang.Fn}
  [f & {:as options}]
  `(let [f# (require-fn ~f)
         opts# (into ~options (argc f#))]
     (mapply args-relax f# opts#)))

(defn args-relax
  "Returns a variadic function object that calls the given function, adjusting
  the number of passed arguments to a nearest arity. It cuts argument list or
  pads it with nil values if necessary.
  
  It takes 1 positional, obligatory argument, which should be a function (f) and
  two named, keyword arguments:
  
  :arities  – a sorted set of argument counts for all arities,
  :variadic – a flag informing whether the widest arity is variadic.
  
  It also makes use of optional named arguments:
  
  :pad-fn   – a function that generates values for padding,
  :pad-val  – a value to use for padding instead of nil,  
  :verbose  – a switch (defaults to false) that if set to true causes wrapper
              to return a map containing additional information.
  
  To determine the number of arguments the nearest arity is picked up by
  matching a number of passed arguments to each number from a set (passed
  as :arities keyword argument). If there is no exact match then the next arity
  capable of taking all arguments is selected.
  
  If the expected number of arguments is lower than a number of arguments
  actually passed to a wrapper call, the exceeding ones will be ignored.
  
  If the declared number of arguments that the original function expects is
  higher than a number of arguments really passed then nil values will be placed
  as extra arguments.
  
  When a variadic function is detected and its variadic arity is the closest to
  a number of arguments passed then all of them will be used to call
  a function.
  
  If the :verbose flag is set the result will be a map containing the following:
  
  :argc-received – a number of arguments received by the wrapper,
  :argc-sent     – a number of arguments passed to a function,
  :argc-cutted   – a number of arguments ignored,
  :argc-padded   – a number of arguments padded with nil values,
  :args-received – arguments received by the wrapper,
  :args-sent     – arguments passed to a function,
  :arities       – a sorted set of argument counts for all arities,
  :arity-matched – an arity (as a number of arguments) that matched,
  :engine        – a method used to check arities (:clj or :jvm),
  :result        – a result of calling the original function,
  :variadic      – a flag telling that the widest arity is variadic,
  :variadic-used – a flag telling that a variadic arity was used,
  :verbose       – a verbosity flag (always true in this case).
  
  If a padding function is given (with :pad-fn) it should take keyword
  arguments. Each call will receive the following:
  
  :argc-received – a number of arguments received by the wrapper,
  :arity-matched – an arity (as a number of arguments) that matched,
  :iteration     – a number of current iteration (starting from 1),
  :iterations    – a total number of iterations,
  :previous      – a value of previously calculated argument (the result
                   of a previous call or a value of the last positional
                   argument when padding function is called for the first time).
  
  Values associated with :iteration and :previous keys will change during each
  call, rest of them will remain constant.
  
  If there is no last argument processed at a time when f is called for the
  first time (because no arguments were passed), the :previous key is not added
  to a passed map. That allows to use a default value in a binding map of f or
  to make easy checks if there would be some previous value (nil is too
  ambiguous)."
  {:added "0.1"
   :tag clojure.lang.Fn}
  [^clojure.lang.Fn f
   & {:keys [^clojure.lang.IPersistentSet arities
             ^clojure.lang.Fn              pad-fn
             ^Boolean                    variadic
             ^Boolean                     verbose
                                          pad-val]
      :as uber-args
      :or {verbose false, variadic false}}]
  {:pre [(instance? clojure.lang.Fn f) (not-empty arities)]}
  (fn [& args]
    (let [arcv (or args (list))
          carg (count arcv)
          arit (if (sorted? arities) arities (apply sorted-set arities))
          near (nearest-right arit carg)
          vari (and variadic (= near (last arit)))
          expe (if vari (dec near) near)
          tken (if vari (max expe carg) expe)
          padn (- expe carg)
          pads (when (pos? padn)
                 (if (nil? pad-fn)
                   (repeat padn pad-val)
                   (let [frargs {:args-received arcv
                                 :arity-matched near
                                 :variadic-used vari}
                         prargs (if (zero? carg)
                                  frargs
                                  (assoc frargs :previous (last arcv)))]
                     (frepeat padn pad-fn prargs))))
          adja (take tken (concat arcv pads))
          resu (apply f adja)]
      (if verbose
        (assoc uber-args
               :args-received arcv
               :argc-received carg
               :args-sent     adja
               :argc-sent     tken
               :result        resu
               :arity-matched near
               :arities       arit
               :variadic-used vari
               :argc-padded   (not-negative padn)
               :argc-cutted   (if vari 0 (not-negative (- padn))))
        resu))))

