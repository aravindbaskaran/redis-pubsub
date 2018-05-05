(ns redis-pubsub.core
  (:require [taoensso.carmine :as car]
            [taoensso.timbre :as timbre]
            [taoensso.carmine.protocol :as protocol]
            [taoensso.carmine.connections :as conns]))

(defrecord Listener [connection handler state future]
  java.io.Closeable
  (close [this] (car/close-listener this)))

(defmacro with-new-keepalive-listener
  "
    Extension of carmine/with-new-listener
    A health check added listener on top of the awesome carmine/with-new-listener macro.
    Use ping-ms in connection spec to override default 30000ms ping timeout.
    For fatal exceptions on the listener, add channel listener to pubsub:listener:fail
    For keep alive ping callbacks, add channel listener to pubsub:ping
  "
  [conn-spec handler initial-state & body]
  `(let [handler-atom# (atom ~handler)
         state-atom#   (atom ~initial-state)
         {:as conn# in# :in ping-ms# :ping-ms} (conns/make-new-connection
                              (assoc (conns/conn-spec ~conn-spec)
                                     :listener? true))
         future# (future-call ; Thread to long-poll for messages
                  (bound-fn []
                    (try
                      (while true ; Closes when conn closes
                        (let [fkeepalive# (future-call
                                            (bound-fn []
                                              ; Wait for 30 seconds or ping ms before redis "ping"
                                              (Thread/sleep (or ping-ms# 30000))
                                              (protocol/with-context conn#
                                                (protocol/execute-requests (not :get-replies) (car/ping "pubsub:ping")))))
                              reply# (protocol/get-unparsed-reply in# {})]
                          ; Received reply, cancel current keep-alive ping;
                          ; will get created in the next while step in long-poll
                          (future-cancel fkeepalive#)
                          (try
                            (@handler-atom# reply# @state-atom#)
                            (catch Throwable t#
                              (timbre/error t# "Listener handler exception")))))
                        (catch Throwable conn-t#
                          (@handler-atom# ["pubsub:error" "pubsub:listener:fail" conn-t#] @state-atom#)
                          (throw conn-t#)))))]

     (protocol/with-context conn# ~@body
       (protocol/execute-requests (not :get-replies) nil))
     (Listener. conn# handler-atom# state-atom# future#)))

(defmacro with-new-keepalive-pubsub-listener
  "A wrapper for `with-new-keepalive-listener`. - Extension of carmine/with-new-pubsub-listener

  Creates a persistent[1] connection to Redis server and a thread to
  handle messages published to channels that you subscribe to with
  `subscribe`/`psubscribe` calls in body, with a keep-alive

  Handlers will receive messages of form:
    [<msg-type> <channel/pattern> <message-content>].

  (with-new-keepalive-pubsub-listener
    {} ; Connection spec, as per `wcar` docstring [1]
    {\"channel1\" (fn [[type match content :as msg]] (prn \"Channel match: \" msg))
     \"user*\"    (fn [[type match content :as msg]] (prn \"Pattern match: \" msg))}
    (subscribe \"foobar\") ; Subscribe thread conn to \"foobar\" channel
    (psubscribe \"foo*\")  ; Subscribe thread conn to \"foo*\" channel pattern
   )

  Returns the Listener to allow manual closing and adjustments to
  message-handlers.

  [1] You probably do *NOT* want a :timeout for your `conn-spec` here."
  [conn-spec message-handlers & subscription-commands]
  `(with-new-keepalive-listener (assoc ~conn-spec :pubsub-listener? true)

     ;; Message handler (fn [message state])
     (fn [[_# source-channel# :as incoming-message#] msg-handlers#]
       (when-let [f# (clojure.core/get msg-handlers# source-channel#)]
         (f# incoming-message#)))

     ~message-handlers ; Initial state
     ~@subscription-commands))

(defn default-error-listener [[t c msg]]
 (timbre/error "Error listening on redis pubsub" msg))

(defn subscribe
  "Subscribes to a particular channel and returns a listener handle. To unsubscribe, keep a reference to the return val"
  [conn-spec channel listener-fn & [on-error]]
  (let [channel-name (name channel)
        msg-handlers { channel-name listener-fn
                      "pubsub:listener:fail" (or default-error-listener on-error)}
        listener (with-new-keepalive-pubsub-listener conn-spec
          msg-handlers
          (car/subscribe channel-name))]
    listener))
