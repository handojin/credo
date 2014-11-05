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
                 [lib-noir "0.9.1"]
                 [liberator "0.12.2"]
                 [http-kit "2.1.16"] 
                 
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
                 [com.datomic/datomic-free "0.9.5052"] 
                 ;;schema visualization
                 ;;[datomic-schema-grapher "0.0.1"]

                 ;;logging
                 [com.taoensso/timbre "3.2.1"]]
  
  :ring {:handler credo.handler/app,
         :init credo.handler/init,
         :destroy credo.handler/destroy}
  
  :plugins [[lein-ring "0.8.12"]
            [lein-cljsbuild "1.0.2"]
            [lein-beanstalk "0.2.7"]]

  :source-paths ["src"]

  :main credo.core)
