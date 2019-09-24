# Web & mobile sessions for akka-http

[![Build Status](https://travis-ci.org/softwaremill/akka-http-session.svg?branch=master)](https://travis-ci.org/softwaremill/akka-http-session)
[![Join the chat at https://gitter.im/softwaremill/akka-http-session](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/softwaremill/akka-http-session?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.akka-http-session/core_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.akka-http-session/core_2.11)

[`akka-http`](http://doc.akka.io/docs/akka/2.4.2/scala/http/index.html) is an Akka 
module, originating from [spray.io](http://spray.io), for building *reactive* REST services with an elegant DSL.

`akka-http` is a great toolkit for building backends for single-page or mobile applications. In almost all apps there 
is a need to maintain user sessions, make sure session data is secure and cannot be tampered with.

`akka-http-session` provides directives for client-side session management in web and mobile applications, using cookies
or custom headers + local storage, with optional [Json Web Tokens](http://jwt.io/) format support. 

A [comprehensive FAQ](https://github.com/softwaremill/akka-http-session-faq) is available, along with code examples (in Java, but easy to translate to Scala) which answers many common questions on how sessions work, how to secure them and implement using akka-http.

## What is a session?

Session data typically contains at least the `id` or `username` of the logged in user. This id must be secured so that a 
session cannot be "stolen" or forged easily.

Sessions can be stored on the server, either in-memory or in a database, with the session `id` sent to the client,
or entirely on the client in a serialized format. The former approach requires sticky sessions or additional shared
storage, while using the latter (which is supported by this library) sessions can be easily deserialized on any server.
  
A session is a string token which is sent to the client and should be sent back to the server on every request.

To prevent forging, serialized session data is **signed** using a server secret. The signature is appended to the
session data that is sent to the client, and verified when the session token is received back.

## `akka-http-session` features

* type-safe client-side sessions
* sessions can be encrypted
* sessions contain an expiry date
* cookie or custom header transport
* support for [JWT](http://jwt.io/)
* refresh token support (e.g. to implement "remember me")
* CSRF tokens support
* Java & Scala APIs
* CORS Support 
* Akka Client Support 
* AKKA low level and higher level apis

