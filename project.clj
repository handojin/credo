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
                 [liberator "0.11.0"]
                 [ring/ring-devel "1.3.0"]
                
                 ;;stormpath - identity management
                 [com.stormpath.sdk/stormpath-sdk-api "1.0.RC2"]
                 [com.stormpath.sdk/stormpath-sdk-httpclient "1.0.RC2"]
                 [com.stormpath.sdk/stormpath-sdk-oauth "1.0.RC2"]
                 
                 ;;ui
                 [om "0.6.4"]
                 
                 ;;database
                 [com.datomic/datomic-free "0.9.4815"] 

                 ;;logging
                 [com.taoensso/timbre "3.2.1"]]
  
  :plugins [[lein-cljsbuild "1.0.2"]]

  :source-paths ["src"]

  :main credo.core)
