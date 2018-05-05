# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

```clojure
[com.aravindbaskaran/redis-pubsub "0.1.0"]
```

## [0.1.0] 2018-05-05
### Changed
- Add a new macro `with-new-keepalive-listener` to provide a base keep-alive listener with timely pings.
- Add a new macro `with-new-keepalive-pubsub-listener` to provide a keep-alive pubsub listener.
- Add a new method `subscribe` to subscribe directly on the channel with keep-alive and default error listener.
- Add tests for new keep-alive macros and subscribe with required timeout

[unreleased]: https://github.com/your-name/redis-pubsub/compare/0.1.0...HEAD
