#!/usr/bin/env python

import os
from collections import namedtuple
import csv
import itertools

from places import Places
from util import Results


Edge = namedtuple('Edge', ('fr', 'to', 'weight'))


class Matrix(object):
    """Distance matrix between pairs of named places.

    Emulates the com.graphhopper.tour.Matrix Java class.
    """

    DEFAULT_MATRIX_FILE = \
        os.path.abspath(
            os.path.join(
                os.path.dirname(__file__), '../../../source/data/matrix.csv'))

    def __init__(self, places, weights):
        self._places = places
        self._weights = weights
        self._sorted_edges = self.edges(directed=False, sort=True)

    @classmethod
    def load(cls, path=None):
        path = path or os.environ.get('GH_MATRIX') or cls.DEFAULT_MATRIX_FILE
        with open(path) as fp:
            csvreader = csv.reader(fp)
            return cls.read(csvreader)

    @classmethod
    def read(cls, csvreader):
        places = Places.read(csvreader)
        header = csvreader.next()
        assert header[1:] == [p.name for p in places]
        weights = [map(float, row[1:]) for row in csvreader]
        return cls(places, weights)

    @property
    @Results.decorate(columns=('name', 'lat', 'lon'))
    def places(self):
        return self._places

    @property
    def num_edges(self):
        return len(self._sorted_edges)

    @Results.decorate(columns=('from', 'to', 'weight'))
    def edges(self, directed=False, sort=True):
        if directed:
            edges = list(self._directed_edges())
        else:
            edges = list(self._undirected_edges())
        if sort:
            edges.sort(key=lambda e: e.weight)
        return edges

    def _directed_edges(self):
        n = len(self._places)
        pairs = itertools.permutations(xrange(n), 2)
        for (i, j) in pairs:
            fr = self._places[i].name
            to = self._places[j].name
            yield Edge(fr, to, self._weights[i][j])

    def _undirected_edges(self):
        n = len(self._places)
        pairs = itertools.combinations(xrange(n), 2)
        for (i, j) in pairs:
            fr = self._places[i].name
            to = self._places[j].name
            w1 = self._weights[i][j]
            w2 = self._weights[j][i]
            wm = (w1 + w2) / 2
            fr, to = sorted((fr, to))   # canonicalize order
            yield Edge(fr, to, wm)

    @Results.decorate(columns=('index', 'from', 'to', 'weight'),
                      transform=lambda (index, edge):
                          (index, edge.fr, edge.to, edge.weight))
    def mst_edges(self, query):
        """Return the list of edges chosen by Prim's minimum spanning
        tree algorithm over the specified list of places."""
        known_places = set(p.name for p in self._places)
        for p in query:
            if p not in known_places:
                raise ValueError, "unknown place '%s'" % p
        spanned = {query[0]}
        query = set(query)
        while len(spanned) < len(query):
            for (i, edge) in enumerate(self._sorted_edges):
                if (edge.fr not in query) or (edge.to not in query):
                    continue
                if edge.fr in spanned and edge.to not in spanned:
                    yield (i, edge)
                    spanned.add(edge.to)
                    break
                if edge.to in spanned and edge.fr not in spanned:
                    yield (i, edge)
                    spanned.add(edge.fr)
                    break


if __name__ == '__main__':
    import argh

    def places(matrix_file=None):
        return Matrix.load(matrix_file).places

    def edges(matrix_file=None, directed=False, sort=True):
        return Matrix.load(matrix_file).edges(directed=directed, sort=sort)

    def mst_edges(matrix_file=None, *places):
        return Matrix.load(matrix_file).mst_edges(places)

    argh.dispatch_commands([places, edges, mst_edges])
