#!/usr/bin/env python

import os
from itertools import cycle, islice
import json
import math

import argh

from model import MultiModel
from mst_db import MstDB
from edge_samples import EdgeSamples
from guess_tours import NaiveGuesser, MstGuesser, check_guesses
from util import getLogger, Timer

_logger = getLogger(__name__)


class MstStats(object):

    class Entry(object):

        def __init__(self, mst_index, mst_edges, guesses_required=()):
            self.mst_index = mst_index
            self.mst_edges = tuple(mst_edges)
            self._guesses_required = list(guesses_required)

        def json_encode(self):
            return {'mst_index': self.mst_index,
                    'mst_edges': self.mst_edges,
                    'guesses_required': self._guesses_required}

        def guesses_required(self, p):
            """Num guesses required for given probability of success."""
            assert p >= 0.0 and p <= 1.0
            cutoff = int(math.ceil(len(self._guesses_required) * p))
            return sorted(self._guesses_required)[cutoff - 1]

        def probability_of_success(self, num_guesses):
            """Probability of success with given number of guesses."""
            num_successful = sum(n is not None and n <= num_guesses for n in self._guesses_required)
            return float(num_successful) / float(len(self._guesses_required))

    def __init__(self, filename):
        self._filename = filename

    def __iter__(self):
        with open(self._filename) as fp:
            for line in fp:
                yield self.Entry(**json.loads(line))

    @classmethod
    def create(cls, filename, edge_samples, guesser, mst_db,
               start=0, stop=0, queries_per_mst=200, max_guesses=200000,
               force_overwrite=False):
        if os.path.exists(filename) and not force_overwrite:
            raise RuntimeError("file %s already exists", repr(filename))
        if not stop:
            stop = len(mst_db)
        _logger.info(
            "writing edge stats for MSTs %d-%d (%d MSTs total) to file %s",
            start, (stop - 1), (stop - start), filename)
        _logger.info("queries_per_mst = %d, max_guesses = %d",
                     queries_per_mst, max_guesses)
        timer = Timer(stop - start)
        with open(filename, 'w') as fp:
            num_complete = 0
            timer.start()
            for mst_index in xrange(start, stop):
                #_logger.info("MST %d", mst_index)
                mst_entry = mst_db[mst_index]
                mst_edges = mst_entry.edges
                queries = mst_entry.queries
                entry = cls.Entry(mst_index, mst_edges)
                for (i, query) in enumerate(islice(cycle(queries), queries_per_mst)):
                    edge_timings = [
                        edge_samples.sample(edge_index, pos)
                        for (pos, edge_index) in enumerate(mst_edges)]
                    guesses = guesser.guess_tour(edge_timings)
                    guesses_required = check_guesses(guesses, mst_edges, max_guesses)
                    entry._guesses_required.append(guesses_required)
                    # _logger.info(
                    #     "  trial %d: guesses required = %s",
                    #     i, guesses_required or 'NOT FOUND')
                percentages = tuple(
                    ( float(sum([bool(n) and n <= 10**j for n in entry._guesses_required])) /
                      float(queries_per_mst) * 100.0 )
                    for j in xrange(5) )
                _logger.info(
                    "MST %d results: %.3g%% / %.3g%% / %.3g%% / %.3g%% / %.3g%%" %
                    ((mst_index,) + percentages))
                print >> fp, json.dumps(entry.json_encode())
                fp.flush()
                num_complete += 1
                if num_complete % 10 == 0:
                    timer.update(num_complete)
        timer.stop()

    def guesses_required(self, p, num_results=1):
        """Worst-case guesses required for specified probabilty of success."""
        entries = [(entry.guesses_required(p), entry) for entry in self]
        entries.sort(key=lambda t: t[0])
        return reversed(entries[-num_results:])

    def probability_of_success(self, num_guesses, num_results=1):
        entries = [(entry.probability_of_success(num_guesses), entry)
                   for entry in self]
        entries.sort(key=lambda t: t[0])
        return entries[:num_results]


if __name__ == '__main__':
    import argh

    @argh.arg('-s', '--strategy', choices=['naive', 'mst_aware'])
    @argh.arg('-q', '--queries-per-mst')
    def create(edge_samples_dir, strategy='mst_aware', model_dir=None, mst_db=None,
               start=0, stop=0, queries_per_mst=200, max_guesses=200000,
               force_overwrite=False):
        edge_samples = EdgeSamples.load(edge_samples_dir)
        mst_db = MstDB.load(mst_db)
        if model_dir:
            model = MultiModel.load(model_dir)
        else:
            model = MultiModel.load(edge_samples_dir)
        if strategy == 'naive':
            guesser = NaiveGuesser(model)
        elif strategy == 'mst_aware':
            guesser = MstGuesser(model, mst_db)
        else:
            assert False
        output_file = os.path.join(edge_samples_dir, 'mst_stats-%s.jsonl' % strategy)
        MstStats.create(output_file, edge_samples, guesser, mst_db,
                        start=start, stop=stop, queries_per_mst=queries_per_mst,
                        max_guesses=max_guesses,
                        force_overwrite=force_overwrite)

    def check_edge_samples(edge_samples_dir, min_samples=500, mst_db=None):
        edge_samples = EdgeSamples.load(edge_samples_dir)
        mst_db = MstDB.load(mst_db)
        for index in xrange(mst_db.num_edges):
            for pos in xrange(4):
                if any(entry.edges[pos] == index for entry in mst_db):
                    edge_samples.check(index, pos, min_samples)


    def guesses_required(mst_stats_file, p=0.95, num_results=10):
        mst_stats = MstStats(mst_stats_file)
        results = mst_stats.guesses_required(p, num_results)
        print "worst %d MSTs by number of guesses required for p=%g probability of success:" % \
            (num_results, p)
        for (guesses_required, entry) in results:
            print "  %s (guesses required = %d)" % (entry.mst_edges, guesses_required)

    def probability_of_success(mst_stats_file, num_guesses=10, num_results=10):
        mst_stats = MstStats(mst_stats_file)
        results = mst_stats.probability_of_success(num_guesses, num_results)
        print "worst %d MSTs by probability of success within %d guesses" % \
            (num_results, num_guesses)
        for (p, entry) in results:
            print "  %s (p=%g)" % (entry.mst_edges, p)

    argh.dispatch_commands([
        create,
        check_edge_samples,
        guesses_required,
        probability_of_success])
