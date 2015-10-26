(defproject pl.randomseed/futils "0.1.0"
  :description "Function Utilities library"
  :url "https://randomseed.pl/en/futils"
  :license {:name "LGPL License", :url "https://opensource.org/licenses/lgpl-3.0.html"}
  :scm     {:name "git", :url "https://github.com/siefca/futils"}
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]
                             [lein-midje-doc "0.0.23"]]}}
  :documentation {:files {"docs/index"
                          {:input     "src-doc/futils/overview.clj"
                           :title     "futils"
                           :sub-title "Function Utilities Library"
                           :author    "Paweł Wilk"
                           :email     "pw@gnu.org"}}}
  :global-vars {*warn-on-reflection* true})
