(ns

    ^{:doc    "futils library, core imports."
      :author "Paweł Wilk"}

    futils.core

  (:require [environ.core :refer [env]]))

(defn init
  []
  (when (env :dev-mode)
    (set! *warn-on-reflection* true)))
