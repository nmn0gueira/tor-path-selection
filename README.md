# Tor Path Selection

## Geolocation
*word this better probably*

Geolocation is done using the MaxMind client API, provided with their free downloadable database. A web API is also available to allow querying a more complete database but we do not use it for lack of necessity only. 

A more complete system would have use the local database for faster lookups and the web API as a fallback when that failed.