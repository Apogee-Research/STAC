This folder contains code to demonstrate this challenge's timing side
channel. See `EL/fulldescription.txt` for a high-level description of
the side channel.


Prerequisites
-------------

On CentOS:

    $ sudo yum install libpcap libpcap-devel numpy scipy

    $ sudo pip install requests argh pypcap dpkt pandas

Optional (to display graphs):

    $ sudo yum install python-matplotlib

In a separate tab/window, start the GraphHopper server:

    $ cd $INSTALL_DIR    # wherever challenge is installed, or EL/source
    $ ./scripts/serve.sh


Distance matrix
---------------

To exploit the timing side channel, an adversary most know the sorted
order of the edges in a server's distance matrix. In this
demonstration code, we have access to the actual distance matrix file
used by the server, in `../../source/data/matrix.csv`. In real life,
the set of places (cities) in the server's distance matrix could be
obtained via its `/places` HTTP endpoint; from there, an adversary
could derive an identical distance matrix using OpenStreetMap data and
GraphHopper's routing algorithms.


Timing model
------------

An adversary must first learn a timing model for a particular
server. To do this, they must first submit a number of queries of
their own, recording both the queries submitted and the server's
responses. The following command generates 100 random queries of five
cities each (the default) and capture the responses using tcpdump:

    $ ./scripts/queries.py execute --random -n 100

The above command will print the (randomly generated) name of the
output directory it creates; we will refer to its output directory as
`data/<model-dir>` in the remaining examples.

The following command parses a .pcap file, heuristically separating
individual HTTP responses and calculating the time between TCP PSH
packets in each response. It outputs a JSON file containing only the
inter-packet timings and TCP data lengths.

    $ ./scripts/responses.py convert data/<model-dir>

The adversary can now identify which packets correspond to MST edges
being added. You don't need to run this step manually, but it's
helpful to see its output to understand the process:

    $ ./scripts/responses.py responses edge-events <model-dir>

Since the adversary knows which actual queries they made, they can
also calculate the sequence of actual edges that would have been added
by the server. This step requires knowing the complete distance matrix
used by the server, as discussed above. What the adversary is
specifically interested in is the *index* of each MST edge among the
sorted edges of the distance matrix.

For example, to see which edges would have been chosen for a given
query:

    $ ./scripts/matrix.py mst-edges Boston Lawrence Lowell Springfield Worcester
    (102, Edge(fr='Boston', to='Lawrence', weight=1912.3394135406334))
    (45, Edge(fr='Lawrence', to='Lowell', weight=1024.6417536953647))
    (154, Edge(fr='Lowell', to='Worcester', weight=2631.063320468058))
    (197, Edge(fr='Springfield', to='Worcester', weight=3441.6092579500228))

The following command will build a table of edges seen in a given set
of queries/responses and the timings observed for each one:

    $ ./scripts/edge_samples.py create <dir>

The adversary can now use linear regression to compute a model
relating inter-edge timing to edge index:

    $ ./scripts/edge_samples.py create-model <dir>

The model will now be in `<dir>/model.json`. It contains a separate
model for each edge position (i.e., when an edge is added to the MST:
first, second, third, fourth).


Guessing a tour
---------------

With a timing model in place, an adversary is ready to observe other
clients' queries and guess the tours they are requesting.

For demonstration purposes, a second set of queries representing those
of another client should be generated:

    $ ./scripts/queries.py execute --random -n 100

    $ ./scripts/responses.py convert <dir>

Using the model generated from the old dataset (`<dir>`), we can now
guess the queries in the second one (`<dir2>`); e.g.:

    $ ./scripts/guess_tours.py data/<model-dir> data/<target-dir>
    ...
    INFO: 57/100 responses (57%) required <= 1 guesses
    INFO: 95/100 responses (95%) required <= 10 guesses
    INFO: 100/100 responses (100%) required <= 100 guesses
    INFO: 100/100 responses (100%) required <= 1000 guesses

Since we have the actual queries available, the above script checks
its guesses against the actual queries to self-check its accuracy
rate.


How guessing works
------------------

The guessing code is in `scripts/guess_tours.py`.

For each inter-packet timing, the linear model predicts an expected
edge index. The timing measurments are noisy, so the predicted edge
index is unlikely to be exactly correct. We treat the sequence of
predicted edge indexes for a given server response as a vector -- for
a tour request of 5 cities, a vector of length 4, each element being
an integer edge index 0-299 (there are 300 possible undirected edges
in our distance matrix).

The guessing code includes a naive guessing algorithm that simply
guesses all possible 4-vectors in order of increasing Euclidean
distance from the vector of edge indexes predicted by the linear
model. This strategy is not very successful.

However, only a relatively tiny number of possible 4-vectors of edge
indexes are actually valid MST constructions. (An *MST construction*
is an MST built in a particular order. The order in which an MST is
constructed depends on which city is listed first in the query,
because the first city is used as the initial point in the set of
spanned vertices. The order of the remaining cities does not matter.)
We simply enumerate all possible MST constructions (by enumerating all
possible queries, calculating their correspond MST constructions, and
keeping track of unique ones). Code to do this is in
`scripts/mst_db.py`, but we have already provided the resulting data
in `data/mst_db-5p.jsonl`. For our particular graph, there are 195,996
possible MSTs.

Each possible MST construction is represented as a vector of edge
indexes. Once we have a vector of edge indexes as predicted by the
linear model, we guess MST constructions in order of their distance
from this initial guess. To make this guessing strategy fast, we store
the vectors of possible MST constructions in a kd-tree, which allows
fast lookup of the closest vectors to a given query vector.


Worst-case analysis
-------------------

## Method

To bound the success rate of the above strategy for the worst-case
secret(s), we used the following empirical method:

  1. Generate a set of queries that will guarantee each edge is seen
     at least n times (n = 3000, for our largest data set) in each of
     the four possible positions.

        $ ./scripts/mst_db.py queries-for-edges --min-queries-per-edge=3000

  2. Execute those queries and capture the resulting packets.

        $ ./scripts/queries.py execute -f data/<queries-file.jsonl>

  3. Collate the resulting inter-packet timings into a file organized
     by edge index and position.

        $ ./scripts/edge_samples.py create data/<output-dir>

  4. Simulate at least m (m = 200) queries for each possible MST
     construction. For each query, randomly choose timing samples for
     each of the MST edges from the appropriate pools as collated in
     step 3. Run the guessing algorithm on each simulated query, and
     record the number of guesses it requires.

        $ ./scripts/mst_stats.py create -q 200 data/<output-dir>

     This step can take a few hours to run (days for the naive
     guessing strategy, option `-s naive`). It produces a file
     `<output-dir>/mst_stats-mst_aware.jsonl`. This file contains, for
     each MST construction, the number of guesses it took to guess it
     correctly in each of 200 trials.

We use a simulation for step 4 because actually executing 200 queries
for each of 195,996 possible MST constructions would be prohibitively
time-consuming.

For a given simulation run, there are two inputs: the set of per-edge,
per-position timing samples to use, and the linear model to use for
guessing edge indexes from observed timings. This linear model can be
derived from the same set of edge samples being used for the
simulation (the default), but it doesn't have to be.


## Results

The results of a simulation can be summarized by running:

    $ scripts/mst_stats.py guesses-required <output-dir>/mst_stats.mst-aware.jsonl

This will print the worst 10 MSTs by the number of guesses required
for a 0.95 probability of success (i.e., the maximum number of guesses
require in the best 95% of trials).

We ran our simulation on three sets of edge samples; results
follow. Each of these was run using the model derived from the same
set of edge samples as used for the simulation.

For the first dataset, >= 500 samples per edge per position, collected 2016-04-04:

    worst 10 MSTs by number of guesses required for p=0.95 probability of success:
      (8, 10, 65, 92) (guesses required = 77)
      (9, 11, 34, 164) (guesses required = 74)
      (10, 8, 17, 164) (guesses required = 70)
      (9, 11, 69, 91) (guesses required = 69)
      (9, 11, 65, 91) (guesses required = 68)
      (8, 10, 17, 164) (guesses required = 67)
      (3, 8, 10, 164) (guesses required = 66)
      (8, 3, 10, 164) (guesses required = 65)
      (11, 9, 65, 91) (guesses required = 65)
      (10, 8, 65, 93) (guesses required = 64)

For the second dataset, >= 500 samples per edge per position, collected 2016-04-05:

    worst 10 MSTs by number of guesses required for p=0.95 probability of success:
      (8, 10, 66, 92) (guesses required = 71)
      (8, 10, 65, 92) (guesses required = 69)
      (10, 8, 65, 92) (guesses required = 68)
      (11, 9, 65, 91) (guesses required = 66)
      (8, 10, 69, 92) (guesses required = 64)
      (8, 10, 17, 164) (guesses required = 62)
      (9, 11, 65, 91) (guesses required = 62)
      (8, 10, 69, 93) (guesses required = 58)
      (8, 10, 65, 93) (guesses required = 57)
      (10, 8, 65, 93) (guesses required = 57)

Third dataset, with >= 3000 samples per edge per position:

    worst 10 MSTs by number of guesses required for p=0.95 probability of success:
      (8, 10, 65, 92) (guesses required = 73)
      (8, 10, 66, 92) (guesses required = 71)
      (10, 8, 66, 92) (guesses required = 68)
      (11, 9, 65, 91) (guesses required = 68)
      (9, 11, 65, 91) (guesses required = 64)
      (9, 11, 69, 91) (guesses required = 64)
      (10, 8, 65, 92) (guesses required = 61)
      (8, 10, 65, 93) (guesses required = 59)
      (10, 8, 65, 93) (guesses required = 59)
      (10, 8, 69, 93) (guesses required = 59)

We also ran a simulation using the last dataset and the naive guessing
strategy, but the probability of success within 10,000 guesses was
only 6.5%; we'd expect random guessing to succeed within 10,000
guesses about 5% of the time (10,000/200,000), so in our opinion the
naive guessing strategy is too weak to be worth asking about at an
engagement.

We will upload these datasets separately.
