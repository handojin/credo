(defproject credo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [
                 ;;clojure
                 [org.clojure/clojure "1.5.1"]
                 
                 ;;clojurescript
                 [org.clojure/clojurescript "0.0-2173"]
                
                 ;;web stack
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-json "0.3.1"]
                 [http-kit "2.1.16"] 
                 [compojure "1.1.8"]
                 [lib-noir "0.9.1"]
                 [liberator "0.12.2"]
                 [ring/ring-devel "1.3.0"]
                 [org.clojure/data.json "0.2.5"]
                 
                 ;;stormpath - identity management
                 [com.stormpath.sdk/stormpath-sdk-api "1.0.RC2"]
                 [com.stormpath.sdk/stormpath-sdk-httpclient "1.0.RC2"]
                 [com.stormpath.sdk/stormpath-sdk-oauth "1.0.RC2"]
                 
                 ;;ui
                 ;;server-side templating 
                 [enlive "1.1.5"]
                 ;;client-side (later)
                 ;;[om "0.6.4"]
                 
                 ;;database
                 [com.datomic/datomic-free "0.9.4899"] 

                 ;;logging
                 [com.taoensso/timbre "3.2.1"]]
  
  :ring {:handler credo.handler/app,
         :init credo.handler/init,
         :destroy credo.handler/destroy}
  
  :plugins [[lein-cljsbuild "1.0.2"]
            [lein-beanstalk "0.2.7"]
            [com.palletops/pallet-lein "0.8.0-alpha.1"]]

  :source-paths ["src"]

  :main credo.core)
