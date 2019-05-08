# Getting Started

Main purpose of this proxy is to manipulate incoming HTTP requests to filter them according
to a simple policy configuration before delegating them to a configured backend server. The
initial version implements the filter mechanism based on the requirement to only filter
requests of an OGC SOS.

The implementation is a prototyped proxy for request filtering only. Filtering XML may become 
quite complex, as a full DOM tree has to be constructed to just find restricted elements for 
removing them. After removing the filter engine would have to decide if the result is still
valid to be passed back to the client, e.g. at least XML has to be validated again.
 
The prosecco-filter gives answers how far one can get by doing a request filter only. 

### Features

* Filter functionality for HTTP `GET` requests (`POST` filters are not implemented, yet.)
* Filtering Capabilities response (to only show permitted parameter values) via XPath
* Simple policy configuration via JSON
* Spring base Security Configuration (hard wired via `WebSecurityConfigurerAdapter`) 

### Usage

Use maven to build via `mvn install` and start application via 

```
java -jar engine-<version>.jar
```

### Outcomes

* to be added here

