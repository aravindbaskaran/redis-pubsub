(ns redis-pubsub.core-test
  (:require [clojure.test :refer :all]
            [taoensso.carmine :as car :refer [wcar]]
            [redis-pubsub.core :as pubsub :refer :all]))
;;testcases require
;1. local redis instance,
(defmacro wcar* [& body] `(car/wcar {:pool {} :spec {}} ~@body))

(deftest pubsub-single-channel-keepalive-test
  (let [received (atom [])
        listener (pubsub/with-new-keepalive-pubsub-listener
                   {} {"ps-foo" #(swap! received conj %)
                       "pubsub:ping" #(swap! received conj %)}
                   (car/subscribe "ps-foo"))]
    (wcar* (car/publish "ps-foo" "one")
           (car/publish "ps-foo" "two")
           (car/publish "ps-foo" "three"))
    (Thread/sleep 35000)
    (car/close-listener listener)
    (is (= [["subscribe" "ps-foo" 1]
            ["message"   "ps-foo" "one"]
            ["message"   "ps-foo" "two"]
            ["message"   "ps-foo" "three"]
            ["pong" "pubsub:ping"]]
           @received))))

(deftest pubsub-multi-channels-keepalive-test
  (let [received (atom [])
        listener (pubsub/with-new-keepalive-pubsub-listener
                   {} {"ps-foo" #(swap! received conj %)
                       "ps-baz" #(swap! received conj %)
                       "pubsub:ping" #(swap! received conj %)}
                   (car/subscribe "ps-foo" "ps-baz"))]
    (wcar* (car/publish "ps-foo" "one")
           (car/publish "ps-bar" "two")
           (car/publish "ps-baz" "three"))
    (Thread/sleep 35000)
    (car/close-listener listener)
    (is (= [["subscribe" "ps-foo" 1]
            ["subscribe" "ps-baz" 2]
            ["message" "ps-foo" "one"]
            ["message" "ps-baz" "three"]
            ["pong" "pubsub:ping"]]
           @received))))

(deftest pubsub-subscribe-util-test
  (let [received (atom [])
        listener (pubsub/subscribe
                    {}
                    "ps-foo"
                    #(swap! received conj %))]
    (wcar* (car/publish "ps-foo" "one")
           (car/publish "ps-foo" "two")
           (car/publish "ps-foo" "three"))
    (Thread/sleep 500)
    (car/close-listener listener)
    (is (= [["subscribe" "ps-foo" 1]
            ["message" "ps-foo" "one"]
            ["message" "ps-foo" "two"]
            ["message" "ps-foo" "three"]]
           @received))))
