(defproject pfchangsindex "0.1.0-SNAPSHOT"
  :description "approximation for the foodiness of a city based on number of restaurants ranked higher than P.F. Chang's"
  :url "https://github.com/university-of-antarctica/pfchangsindex"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;; resource paths are available in at runtime and in prod.
  :resource-paths ["resources/db"
                   "resources/cache"]
  ;; only in development.
  :profiles {:dev {:resource-paths ["resources/"]}}
  ;; JRE needs location of folders on cp when run as a jar.
  ;; java -cp "/etc/myapp:/usr/local/lib/myapp.jar" myapp.core $*
  ;; https://stackoverflow.com/questions/8009829/resources-in-clojure-applications
  :dependencies [[com.datomic/datomic-free "0.9.5359"]
                 [org.clojure/clojure "1.8.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [clj-http "3.1.0"]
                 [org.clojure/data.json "0.2.6"]
                 [places "0.2.0"]
                 [fs-utils "0.1.0-SNAPSHOT"]
                 [geocoder-clj "0.2.6"]]
  :main pfchangsindex.core
  :aot [pfchangsindex.core])
