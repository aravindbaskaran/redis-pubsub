```clojure
[com.aravindbaskaran/redis-pubsub "0.1.1"] ; See CHANGELOG for details
```

[![Clojars Project](https://img.shields.io/clojars/v/com.aravindbaskaran/redis-pubsub.svg)](https://clojars.org/com.aravindbaskaran/redis-pubsub)

# redis-pubsub

A redis pubsub client with keep-alive heart beats on top of the awesome <a href="https://github.com/ptaoussanis/carmine" >com.taoensso/carmine library</a>.

The main purpose of this library is to address the dead connections on redis pubsub clients with no way to re-subscribe or keep alive.

Related GH Issue - https://github.com/ptaoussanis/carmine/issues/15

Existing PR - https://github.com/ptaoussanis/carmine/pull/207

So until this gets merged into the main carmine library in some format, the world keeps spinning and the connections keep dying, this library has use.

## Features
 * Keeps pubsub clients **ALIVE** :)
 * Very tiny Clojure library
 * **Documented**, base macros and direct subscribe API with support for Redis 3.2+
 * **Tested** for connection failures because of socket read timeout, hard disconnects and stale/old connects
 * **Awesome underlying All-Clojure redis library** in <a href="https://github.com/ptaoussanis/carmine">com.taoensso/carmine redis client</a>


## Usage

### with-new-keepalive-pubsub-listener macro
```clojure
(require '[redis-pubsub.core :as pubsub])
(require '[taoensso.carmine :as car])
(pubsub/with-new-keepalive-pubsub-listener {}
  {
   "ps-foo" #(println %) ;handle channel ps-foo, arguments passed ["message" channel-name message-string]
   "ps-baz" #(println %) ;handle channel ps-baz, arguments passed ["message" channel-name message-string]
   "pubsub:ping" #(println %) ;callback on ping, arguments passed ["pong" "pubsub:ping"]
   "pubsub:listener:fail" #(println %) ;callback on listener failures, arguments passed ["pubsub:error" "pubsub:listener:fail" exception-obj]
  }
  ; subsrcibe to required channels
  (car/subscribe "ps-foo" "ps-baz"))

```
### subscribe API

```clojure
(require '[redis-pubsub.core :as pubsub])
(pubsub/subscribe
  {}
  "ps-foo"
  #(println %) ;handle channel ps-foo, arguments passed ["message" channel-name message-string]
  )

```

<a href="https://github.com/ptaoussanis/carmine#listeners--pubsub">Broader documentation of underlying carmine interfaces</a>

## License

Distributed under the [EPL v1.0] \(same as Clojure).  
Copyright &copy; 2018- [Aravind Baskaran].


<!--- Standard links -->
[EPL v1.0]: https://raw.githubusercontent.com/ptaoussanis/carmine/master/LICENSE
[Aravind Baskaran]: https://github.com/aravindbaskaran/
