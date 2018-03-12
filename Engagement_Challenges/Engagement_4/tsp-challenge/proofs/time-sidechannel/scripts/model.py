import os
from collections import namedtuple
import json

from matrix import Matrix
from util import getLogger

_logger = getLogger(__name__)


class Model(
    namedtuple(
        'Model',
        ('slope', 'intercept', 'rvalue', 'pvalue', 'stderr'))):

    def json_encode(self):
        return {
            'slope': self.slope,
            'intercept': self.intercept,
            'rvalue': self.rvalue,
            'pvalue': self.pvalue,
            'stderr': self.stderr,
        }

    @classmethod
    def fit(cls, edge_indexes, timings):
        """Fit event delta times w/ actual edge indices.

        Args:
          edge_indexes: indexes of actual edges that were added
          timings: time since previous packet for each edge event
        """
        assert len(edge_indexes) == len(timings)
        import numpy as np
        from scipy import stats
        timings = np.array(timings) * 1e6
        return cls(*stats.linregress(edge_indexes, timings))

    def predict_index(self, dt):
        return (dt*1e6 - self.intercept) / self.slope


class MultiModel(list):

    def __init__(self, models=(), matrix=None):
        super(MultiModel, self).__init__(models)
        if matrix is None or isinstance(matrix, basestring):
            matrix = Matrix.load(matrix)
        self.matrix = matrix
        self.sorted_edges = self.matrix.edges(directed=False, sort=True)

    @property
    def num_edges(self):
        return len(self.sorted_edges)

    def mst_edges(self, query):
        """Proxy for Matrix.mst_edges()."""
        return self.matrix.mst_edges(query)

    def json_encode(self):
        return [model.json_encode() for model in self]

    @classmethod
    def load(cls, filename):
        if os.path.isdir(filename):
            filename = os.path.join(filename, 'model.json')
        _logger.info("loading model from %s", filename)
        with open(filename) as fp:
            lst = json.load(fp)
        return cls(Model(**dct) for dct in lst)

    def save(self, filename):
        if os.path.isdir(filename):
            filename = os.path.join(filename, 'model.json')
        _logger.info("saving model to %s", filename)
        with open(filename, 'w') as fp:
            json.dump(self.json_encode(), fp, indent=2)

    def predict_index(self, pos, dt):
        return self[pos].predict_index(dt)
