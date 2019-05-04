# Getting Started

Main purpose of this proxy is to manipulate incoming HTTP requests to filter them according
to a simple policy configuration before delegating them to a configured backend server. The
initial version implements the filter mechanism based on the requirement to only filter
requests of an OGC SOS. It provides filter functionality for HTTP `GET` requests. `POST`
filters are not implemented, yet.

This version is intended to be a prototyped proxy only for request filtering only. Filtering
XML requests may become quite complex, as a full DOM tree has to be constructed to just find
restricted elements for throwing them away. Actual question was how far one can get by doing
a request filter only. 

### Outcomes

* to be added here

