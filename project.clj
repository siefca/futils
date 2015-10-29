(defproject pl.randomseed/futils "0.4.0"
  :description "Function Utilities library"
  :url "https://randomseed.pl/software/futils"
  
  :license {:name "LGPL License", :url "https://opensource.org/licenses/lgpl-3.0.html"}
  :scm     {:name "git", :url "https://github.com/siefca/futils"}
  
  :dependencies [[org.clojure/clojure "1.7.0"]]
  
  :profiles {:dev {:dependencies [[midje "1.8.1"]]
                   :plugins [[lein-midje "3.2"]
                             [lein-midje-doc "0.0.23"]]}}
  
  :documentation {:site   "futils"
                  :description "Function Utilities Library"
                  :owners [{:name    "Paweł Wilk"
                            :email   "pw@gnu.org"
                            :website "https://randomseed.pl/"}]
                  :paths ["src-doc"]
                  :files {"docs/index"
                          {:input     "src-doc/futils/overview.clj"
                           :title     "futils"
                           :subtitle  "Function Utilities Library"
                           :author    "Paweł Wilk"
                           :email     "pw@gnu.org"}}
                  :html {:home "index"
                         :navigation ["home"
                                      {:link "https://github.com/siefca/futils"
                                       :text "source"}]}
                  :link {:auto-tag    true
                         :auto-number true}}
  
  :global-vars {*warn-on-reflection* true})

