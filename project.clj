(defproject pl.randomseed/futils "0.5.0"
  :description "Function Utilities library"
  :url "https://randomseed.pl/software/futils"
  
  :license {:name "LGPL License", :url "https://opensource.org/licenses/lgpl-3.0.html"}
  :scm     {:name "git", :url "https://github.com/siefca/futils"}
  
  :dependencies [[org.clojure/clojure "1.7.0"]]
  
  :profiles {:dev {:dependencies [[midje "1.8.1"]
                                  [helpshift/hydrox "0.1.2"]
                                  [im.chit/hara.class "2.2.7"]
                                  [im.chit/hara.reflect "2.2.7"]
                                  [im.chit/vinyasa.inject "0.3.4"]]
                   :plugins [[lein-midje "3.2"]]

                   :injections
                   [(require '[vinyasa.inject :as inject])
                    (inject/in [hydrox.core dive surface generate-docs
                                import-docstring purge-docstring])]}
             } 
  
  :documentation {:site "futils"
                  :description "Function Utilities Library"
                  :owners [{:name    "Paweł Wilk"
                            :email   "pw@gnu.org"
                            :website "https://randomseed.pl/"}]
                  :output "docs"
                  :paths ["src-doc"]
                  :template {:path "template"
                             :copy ["assets"]
                             :defaults {:template     "article.html"
                                        :navbar       [:file "partials/navbar.html"]
                                        :dependencies [:file "partials/deps-web.html"]
                                        :navigation   :navigation
                                        :article      :article}}
                  :files {"index"
                          {:input     "src-doc/futils/overview.clj"
                           :title     "futils overview"
                           :subtitle  "Function Utilities Library"}}
                  :link {:auto-tag    true
                         :auto-number true}}
  
  :global-vars {*warn-on-reflection* true})
