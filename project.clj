(defproject pl.randomseed/futils "1.2.1"
  :description "Function Utilities library"
  :url "https://randomseed.pl/software/futils"

  :license {:name "LGPL License", :url "https://opensource.org/licenses/lgpl-3.0.html"}
  :scm     {:name "git", :url "https://github.com/siefca/futils"}

  :dependencies [[org.clojure/clojure "1.7.0"]]

  :profiles {:dev {:dependencies [[midje "1.8.2"]
                                  [helpshift/hydrox "0.1.3"]
                                  [im.chit/vinyasa.inject "0.3.4"]]
                   :plugins [[lein-midje "3.2"]
                             [lein-environ "1.0.1"]]

                   :injections
                   [(require '[vinyasa.inject :as inject])
                    (inject/in [hydrox.core dive surface generate-docs
                                import-docstring purge-docstring])]

                   :env {:dev-mode true}}}

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
                           :title     "futils"
                           :subtitle  "Function Utilities Library"}}
                  :link {:auto-tag    true
                         :auto-number true}})

