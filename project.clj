(defproject com.aravindbaskaran/redis-pubsub "0.1.1"
  :author "Aravind Baskaran <https://github.com/aravindbaskaran>"
  :description "A redis pubsub client with keep-alive heart beats on top of the awesome com.taoensso/carmine library"
  :url "https://github.com/aravindbaskaran/redis-pubsub"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "Same as Clojure"}
  :dependencies
  [[org.clojure/clojure "1.8.0"]
   [com.taoensso/carmine "2.16.0"]]
  :profiles
   {;; :default [:base :system :user :provided :dev]
    :dev
    [:test
     {:plugins
      [[lein-codox   "0.10.3"]]}]}
  :test-paths ["test" "src"])
