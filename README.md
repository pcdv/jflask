JBootWeb
========

JBootWeb is a template project (i.e. you clone it and customize it) that
generates an executable jar. The jar contains a HTTP server and minimal
web framework thats runs the app.

The generated application is minimal:
 - it uses the JDK's [integrated HTTP server](http://docs.oracle.com/javase/7/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html)
 - initial jar size is ~11k
 - the app does not have any dependency other than a JRE
 - it is trivial to deploy (just copy and run the jar)

It is not a rigid framework: something you don't like in the default
behaviour? Just change it. The codebase is so small that you can fully
grok it in minutes.

**Use cases:**
 - bootstrapping a webapp in 2 minutes
 - internal tools
 - webapp interacting with Java libraries
 - ...

**Disclaimer:**
 - it is an early release, there are probably bugs
 - I'm not sure how it behaves when thousands of users connect simultaneously :)

**TODO**
 - commands to quickly download and install Bootstrap, JQuery, AngularJS etc.
 - more documentation
 - improve the integrated web framework (while remaining small)
