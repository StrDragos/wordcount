# wordcount

Implemented a stream that consumes data from a process the pushes events.
The source of the process, in this case, is a platform dependent binary program that is not included in the repository and it should be deployed
in the resource folder.

In order to run the program an sbt run is sufficient to start and then an HTTP endpoint will be exposed to request the data and consume the stream.
GET request ot 'localhost: 8080 / count' will return the valid results from the bynary.
