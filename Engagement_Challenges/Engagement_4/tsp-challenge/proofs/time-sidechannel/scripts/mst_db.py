#!/usr/bin/env python

from collections import defaultdict
from itertools import chain, cycle
import json

from matrix import Matrix
from queries import Queries
from util import getLogger

_logger = getLogger(__name__)


class MstDB(object):

    class Entry(object):

        def __init__(self, edges, queries=()):
            self.edges = tuple(edges)
            self.queries = [tuple(q) for q in queries]

        def json_encode(self):
            return {'edges': self.edges,
                    'queries': self.queries}

    def __init__(self, entries=()):
        self._entries = list(entries)
        self._by_edges = dict((entry.edges, entry) for entry in self._entries)
        self._by_query = None
        self._by_position = None
        self._kdtree = None

    @property
    def num_edges_per_query(self):
        return len(self[0].edges)

    @property
    def num_places_per_query(self):
        return len(self[0].queries[0])

    @property
    def num_edges(self):
        return max(max(entry.edges) for entry in self._entries) + 1

    @property
    def num_queries(self):
        return sum(len(entry.queries) for entry in self._entries)

    def __getitem__(self, key):
        if isinstance(key, int):
            return self._entries[key]
        else:
            assert isinstance(key, tuple)
            return self._by_edges[key]

    def __contains__(self, key):
        assert isinstance(key, tuple)
        return key in self._by_edges

    def __iter__(self):
        return iter(self._entries)

    def __len__(self):
        return len(self._entries)

    def get(self, key, default=None):
        try:
            result = self[key]
        except KeyError:
            result = default
        return result

    def append(self, entry):
        key = entry.edges
        assert isinstance(key, tuple)
        assert key not in self._by_edges
        self._entries.append(entry)
        self._by_edges[key] = entry

    def by_query(self, query):
        query = tuple(query)
        if not self._by_query:
            _logger.info("building index of MSTs by query")
            self._by_query = dict(
                chain.from_iterable(
                    ((query, entry) for query in entry.queries)
                    for entry in self))
        return self._by_query[query]

    def by_position(self, index, pos):
        if not self._by_position:
            _logger.info("building index of MSTs by edge index and position")
            self._by_position = defaultdict(list)
            for entry in self:
                for (pos, index) in enumerate(entry.edges):
                    self._by_position[(index, pos)].append(entry)
        return self._by_position[(index, pos)]

    def nearest(self, point, k=1):
        if not self._kdtree:
            _logger.info("building kd-tree")
            from scipy.spatial import cKDTree
            self._kdtree = cKDTree([e.edges for e in self._entries])
        distances, indexes = self._kdtree.query(point, k=k)
        if k == 1:
            distances = [distances]
            indexes = [indexes]
        return [self._entries[i] for i in indexes if i < len(self._entries)]

    @classmethod
    def create(cls, num_places_per_query=5, matrix=None):
        if matrix is None or isinstance(matrix, basestring):
            matrix = Matrix.load(matrix)
        _logger.info("creating MST DB")
        mst_db = MstDB()
        for query in Queries.all(num_places_per_query):
            mst_edges = tuple(index for (index, _) in matrix.mst_edges(query))
            entry = mst_db.get(mst_edges)
            if entry is None:
                entry = cls.Entry(mst_edges)
                mst_db.append(entry)
            entry.queries.append(query)
        return mst_db

    @classmethod
    def load(cls, filename=None):
        if filename is None:
            filename = 'data/mst_db-5p.jsonl'
        _logger.info("loading MST DB from %s", filename)
        with open(filename) as fp:
            db = cls(cls.Entry(**json.loads(line)) for line in fp)
        return db

    def save(self, filename=None):
        if filename is None:
            filename = 'data/mst_db-%dp.jsonl' % self.num_places_per_query
        _logger.info("saving MST DB to %s", filename)
        with open(filename, 'w') as fp:
            for entry in self._entries:
                print >> fp, json.dumps(entry.json_encode())

    def queries_for_edges(self, edges=None, min_queries_per_edge=500):
        if not edges:
            edges = range(self.num_edges)
        else:
            edges.sort()
        counts = defaultdict(int)
        for edge_index in reversed(edges):
            _logger.info("edge %d:", edge_index)
            for pos in xrange(self.num_edges_per_query):
                count = counts[(edge_index, pos)]
                num_to_generate = max(0, min_queries_per_edge - count)
                if num_to_generate == 0:
                    _logger.info("  pos %d: no further queries needed", pos)
                    continue
                entries = self.by_position(edge_index, pos)
                if not entries:
                    _logger.info("  pos %d: no MSTs exist", pos)
                    continue
                _logger.info("  pos %d: generating %d queries", pos, num_to_generate)
                num_generated = 0
                for entry in cycle(entries):
                    for query in entry.queries:
                        yield query
                        for (pos, index) in enumerate(entry.edges):
                            counts[(index, pos)] += 1
                        num_generated += 1
                        if num_generated == num_to_generate:
                            break
                    if num_generated == num_to_generate:
                        break

    def check_queries(self, queries, min_queries_per_edge=500):
        queries.shuffle()
        _logger.info("checking %d queries", len(queries))
        counts = defaultdict(int)
        for query in queries:
            mst_edges = self.by_query(query).edges
            for (pos, index) in enumerate(mst_edges):
                counts[(index, pos)] += 1
        for index in xrange(self.num_edges):
            for pos in xrange(self.num_edges_per_query):
                if not self.by_position(index, pos):
                    continue
                count = counts[(index, pos)]
                if count < min_queries_per_edge:
                    _logger.error(
                        "edge %d pos %d has only %d queries",
                        index, pos, count)


if __name__ == '__main__':
    import argh

    def create(mst_db=None, num_places_per_query=5, matrix=None):
        db = MstDB.create(num_places_per_query, matrix)
        db.save(mst_db)

    def num_edges_per_query(mst_db=None):
        db = MstDB.load(mst_db)
        return db.num_edges_per_query

    def num_places_per_query(mst_db=None):
        db = MstDB.load(mst_db)
        return db.num_places_per_query

    def num_edges(mst_db=None):
        db = MstDB.load(mst_db)
        return db.num_edges

    def num_queries(mst_db=None):
        db = MstDB.load(mst_db)
        return db.num_queries

    @argh.arg('-q', '--min-queries-per-edge')
    def queries_for_edges(
            db_file=None, edges=None, min_queries_per_edge=500,
            output_file=None):
        db = MstDB.load(db_file)
        if edges:
            edges = [int(x) for x in edges.split(',')]
        if not output_file:
            if edges:
                raise argh.CommandError(
                    "output filename must be specified with -e/--edges")
            output_file = 'data/queries-for-all-edges-%dp.jsonl' % db.num_places_per_query
        queries = Queries(db.queries_for_edges(edges, min_queries_per_edge))
        db.check_queries(queries, min_queries_per_edge)
        queries.save(output_file)

    argh.dispatch_commands([
        create,
        num_edges_per_query,
        num_places_per_query,
        num_edges,
        num_queries,
        queries_for_edges])

