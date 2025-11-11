# Tor Path Selection

## Compiling
```bash
mvn clean compile package
```

## Running
```bash
mvn exec:java -Dexec.args="--consensus <consensus_file> --traffic <traffic_file>"
```
If arguments are not specified, the files in the `example` directory will be used instead.

## Other information
### Geolocation
*word this better probably*

Geolocation is done using the MaxMind client API, provided with their free downloadable database. A web API is also available to allow querying a more complete database but we do not use it for lack of necessity only. 

A more complete system would have use the local database for faster lookups and the web API as a fallback when that failed.