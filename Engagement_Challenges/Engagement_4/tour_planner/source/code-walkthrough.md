Code Walkthrough
================

## Challenge summary

This challenge program is based on [GraphHopper][], an open-source
HTTP routing service that uses OpenStreetMap data. The challenge
augments GraphHopper with the ability to calculate a Traveling
Salesman tour between any user-specified subset of the 25 largest
cities in Massachusetts.

Note that the challenge only supports tours between a fixed set of
places, not through arbitrary points like GraphHopper's existing
routing API. This limitation is imposed because the challenge's
vulnerability depends on the use of a precomputed distance matrix that
provides a cost metric between every pair of places (cities). This
distance matrix implicitly defines a list of edges ordered by weight,
which is what's used during the actual tour calculation.

The distance matrix is precomputed and stored in `data/matrix.csv`, to
be loaded at startup time by the HTTP server.


## Code overview

Most of the interesting action takes place in three classes:

  - [TourCalculator][], which does the actual tour calculation.

  - [Matrix][], which implements the distance matrix data structure
    from which `TourCalculator` gets its edges.

  - [TourServlet][], which implements the HTTP server's `/tour`
    endpoint, including sending the progress updates that expose a
    timing channel.

You might also need to briefly reference the following supporting
classes:

  - [TourResponse][], a container for tour results.

  - [TourSerializer][], which serializes `TourResponse`s.

  - The utility classes [Edge][] and [Graph][] used by `Matrix` and
    `TourCalculator`.

  - [GHBaseServlet][] and [PlacesServlet][], which `TourServlet`
    extends.

  - The `main` entry point of the GraphHopper web server is in
    [GHServer][]. It uses Google Guice to inject dependencies.

  - `GraphHopper` and `TourServlet` are instantiated in
    [DefaultModule][].


Classes that aren't actually executed in server mode:

  - [MatrixCalculator][]. This is/was used to precomputed the distance
    matrix in `data/matrix.csv`, which the web server loads at startup
    time from `data/matrix.csv`.

  - [PathCalculator][], which is used by [MatrixCalculator][] to call
    the actual GraphHopper engine.

  - Any of the CLI classes in the [com.graphhopper.tour.tools][]
    package.


## Building and running the code

First you must download OpenStreetMap data:

    $ ./build/wget-osm-data.sh

Preprocess the OSM data for faster startup:

    $ ./graphhopper.sh import data/massachusetts-latest.osm.pbf

Build the remaining Java code (`import` will build some):

    $ ./build/build.sh

Start the server:

    $ ./scripts/web.sh

To query the server, run `./scripts/query.sh`. This script has two
subcommands: `places`, to see the list of available places one can
visit on a tour; and `tour`, which takes a list of places between
which to calculate a tour. Run `query.sh -h` for more details.

An example tour query:

    $ ./scripts/query.sh tour -P Cambridge Springfield Worcester Lowell Chicopee



## Other information

`test/query.js` is a PhantomJS script to validate that responses from
the server in `progress=true` mode are in fact recognized by a
client-side JavaScript `EventSource` implementation.

The distance matrix computation can be accessed via the CLI wrapper
`tour/tools/matrix.sh`. The following will compute a distance matrix
between the set of places specified as arguments:

    $ ./tour/tools/matrix.sh osmreader.osm=data/ places.csv=data/places.csv \
          PLACE...


[GraphHopper]: https://github.com/graphhopper/graphhopper

[TourCalculator]: tour/src/main/java/com/graphhopper/tour/TourCalculator.java
[Matrix]: tour/src/main/java/com/graphhopper/tour/Matrix.java
[TourServlet]: web/src/main/java/com/graphhopper/http/TourServlet.java

[TourResponse]: tour/src/main/java/com/graphhopper/tour/TourResponse.java
[TourSerializer]: web/src/main/java/com/graphhopper/http/TourSerializer.java
[Edge]: tour/src/main/java/com/graphhopper/tour/util/Edge.java
[Graph]: tour/src/main/java/com/graphhopper/tour/util/Graph.java
[GHBaseServlet]: web/src/main/java/com/graphhopper/http/GHBaseServlet.java
[PlacesServlet]: web/src/main/java/com/graphhopper/http/PlacesServlet.java
[GHServer]: web/src/main/java/com/graphhopper/http/GHServer.java
[DefaultModule]: web/src/main/java/com/graphhopper/http/PlacesServlet.java

[MatrixCalculator]: tour/src/main/java/com/graphhopper/tour/MatrixCalculator.java
[PathCalculator]: tour/src/main/java/com/graphhopper/tour/PathCalculator.java
[com.graphhopper.tour.tools]: tour/src/main/java/com/graphhopper/tour/tools/