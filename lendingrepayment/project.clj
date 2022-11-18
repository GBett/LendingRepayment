(defproject lendingrepayment "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.6.3"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.1"]
                 [korma "0.4.3"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [com.taoensso/timbre "5.2.1"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/tools.logging "1.2.4"]
                 [org.postgresql/postgresql "42.5.0"]
                 [clj-http "3.8.0"]
                 [clj-time "0.15.2"]
                 [image-resizer "0.1.10"]
                 [org.apache.pdfbox/pdfbox "1.8.2"]
                 [ring-cors "0.1.13"]
                 [ring/ring-codec "1.2.0"]
                 [crypto-password "0.2.1"]
                 [com.draines/postal "2.0.5"]
                 [midje "1.10.5"]
                 [enlive "1.1.6"]
                 [clj-soup/clojure-soup "0.1.3"]
                 [org.clojure/math.numeric-tower "0.0.5"]
                 [com.climate/claypoole "1.1.4"]            ;completable futures
                 [buddy/buddy-core "1.10.413"]
                 [org.bouncycastle/bcpkix-jdk15on "1.53"]
                 [clojure-interop/javax.crypto "1.0.5"]
                 [commons-codec "1.15"]
                 [clj-ssh-keygen "0.2.3"]
                 [io.replikativ/geheimnis "0.1.1"]          ;https://github.com/replikativ/geheimnis
                 [buddy/buddy-hashers "1.8.158"]
                 [clj-crypto "1.0.2"]
                 [metosin/ring-http-response "0.9.3"]
                 [org.clojure/data.xml "0.0.8"]
                 [clj-oauth "1.5.5"]                        ;for twitter oauth
                 [twitter-api "1.8.0"]
                 [clj-pdf "2.6.1"]
                 [clojurewerkz/quartzite "2.1.0"]
                 [aleph "0.4.7-alpha7"]
                 [org.clj-commons/gloss "0.3.0"]
                 [org.clojure/core.async "1.5.648"]
                 [pdfkit-clj "0.1.7"]                       ;generating pdfs
                 [com.nopolabs/clozxing "0.1.1"]
                 ]
  :plugins [[lein-ring "0.12.6"]]
  :ring {:handler lendingrepayment.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
