#!/usr/bin/env python

"""Generate or analyze a series of queries."""

import os
import csv
from itertools import combinations
import json
import random as random_
import time

from ghserver import GHServer, DEFAULT_HOST, DEFAULT_PORT
from matrix import Matrix
from tcpdump import TcpDump
from util import getLogger, Timer

_logger = getLogger(__name__)


class Queries(list):
    """A list of queries."""

    @classmethod
    def random(cls, num_queries=30, num_places_per_query=5):
        """Generate a sequence of random queries."""
        matrix = Matrix.load()
        place_names = [p.name for p in matrix.places]
        _logger.info("generating %d random queries", num_queries)
        return cls(random_.sample(place_names, num_places_per_query)
                   for _ in xrange(num_queries))

    @classmethod
    def all(cls, num_places_per_query=5):
        """Enumerate all possible queries resulting in possibly unique MST
        constructions."""
        matrix = Matrix.load()
        place_names = sorted(p.name for p in matrix.places)
        _logger.info(
            "enumerating all queries of %d places with possibly unique MST constructions",
            num_places_per_query)
        num_generated = 0
        for first in place_names:
            others = sorted(set(place_names) - {first})
            for rest in combinations(others, num_places_per_query - 1):
                yield [first] + list(rest)
                num_generated += 1
                if num_generated % 10000 == 0:
                    _logger.info("%d", num_generated)

    def execute(self, output_dir=None, host=DEFAULT_HOST, port=DEFAULT_PORT,
                capture_interface=None):
        timestamp = time.strftime('%Y%m%d-%H%M%S')

        if output_dir is None:
            prefix = ''.join(random_.choice('abcdefghijklmnopqrstuvwxyz')
                             for _ in xrange(5))
            output_dir = os.path.join('data', '%s-%s' % (prefix, timestamp))

        if os.path.exists(output_dir):
            raise RuntimeError(
                "output directory %s already exists" % repr(output_dir))
        os.mkdir(output_dir)
        with open(os.path.join(output_dir, 'TIMESTAMP'), 'w') as fp:
            print >> fp, timestamp

        self.save(output_dir)

        pcap_out = os.path.join(output_dir, 'responses.pcap')
        with TcpDump(pcap_out, port=port, interface=capture_interface):
            _logger.info("executing %d queries", len(self))
            server = GHServer(host=host, port=port)
            timer = Timer(len(self)).start()
            for (i, query) in enumerate(self):
                if i > 0 and i % 100 == 0:
                    timer.update(i)
                server.tour(query)
            timer.stop()

    def shuffle(self):
        _logger.info("shuffling queries")
        random_.shuffle(self)

    @classmethod
    def load(cls, queries_file_or_dir):
        if os.path.isdir(queries_file_or_dir):
            queries_file = os.path.join(queries_file_or_dir, 'queries.jsonl')
            if not os.path.isfile(queries_file):
                queries_file = os.path.join(queries_file_or_dir, 'queries.csv')
        else:
            queries_file = queries_file_or_dir
        _logger.info("loading queries from %s", queries_file)
        with open(queries_file) as fp:
            if queries_file.endswith('.jsonl'):
                return cls(json.loads(line) for line in fp)
            else:
                reader = csv.reader(fp)
                return cls(reader)

    def save(self, queries_file_or_dir):
        if os.path.isdir(queries_file_or_dir):
            queries_file = os.path.join(queries_file_or_dir, 'queries.jsonl')
        else:
            queries_file = queries_file_or_dir
        _logger.info("saving queries to %s", queries_file)
        with open(queries_file, 'w') as fp:
            if queries_file.endswith('.jsonl'):
                for query in self:
                    print >> fp, json.dumps(query)
            else:
                writer = csv.writer(fp)
                for query in self:
                    writer.writerow(query)


if __name__ == '__main__':
    import argh
    from argh.exceptions import CommandError

    @argh.arg('-f', '--input-file')
    @argh.arg('-r', '--random')
    @argh.arg('-n', '--num-queries')
    @argh.arg('-p', '--num-places-per-query')
    @argh.arg('-H', '--host')
    @argh.arg('-P', '--port')
    @argh.arg('-i', '--capture-interface')
    @argh.arg('-o', '--output-dir')
    @argh.arg('-s', '--shuffle')
    def execute(input_file=None,
                random=False,
                shuffle=False,
                num_queries=100,
                num_places_per_query=5,
                host=DEFAULT_HOST,
                port=DEFAULT_PORT,
                capture_interface=None,
                output_dir=None):
        if input_file:
            queries = Queries.load(input_file)
            if shuffle:
                queries.shuffle()
        elif random:
            queries = Queries.random(
                num_queries=num_queries,
                num_places_per_query=num_places_per_query)
        else:
            raise CommandError(
                "one of -f/--input-file or -r/--random must be specified")
        queries.execute(host=host, port=port,
                        capture_interface=capture_interface,
                        output_dir=output_dir)

    argh.dispatch_commands([execute])
