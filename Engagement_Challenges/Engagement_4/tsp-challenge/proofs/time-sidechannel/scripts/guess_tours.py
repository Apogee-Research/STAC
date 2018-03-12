#!/usr/bin/env python

"""Guess tours and check answers."""

from itertools import count, islice

import argh

from queries import Queries
from responses import Responses
from model import MultiModel
from mst_db import MstDB
from util import getLogger

_logger = getLogger(__name__)


def all_vectors(length, d, p=2):
    """Enumerate all vectors v such that sum(abs(x**p) for x in v) == d."""
    assert length >= 0
    assert d >= 0
    assert p > 0
    if length == 0:
        if d == 0:
            yield []
        return
    for x in count():
        if x**p > d:
            break
        for v in all_vectors(length - 1, d - x**p, p):
            yield [x] + v
            if x > 0:
                yield [-x] + v


def bound(n, min_, max_):
    if n < min_:
        return min_
    if n > max_:
        return max_
    return n


class BaseGuesser(object):

    def __init__(self, model):
        self._model = model

    def predict_edges(self, edge_timings, bound=False):
        """Return vector of "raw" edge index predictions (floating point,
        might be < 0 or > max_index)."""
        guess = [self._model.predict_index(pos, dt)
                 for (pos, dt) in enumerate(edge_timings)]
        if bound:
            guess = [bound(index, 0, self._model.num_edges - 1)
                     for index in guess]
        return guess


class NaiveGuesser(BaseGuesser):

    name = 'naive'

    def guess_tour(self, edge_timings):
        max_index = self._model.num_edges - 1
        prediction = self.predict_edges(edge_timings)
        prediction = [int(round(x)) for x in prediction]
        prediction = [bound(n, 0, max_index) for n in prediction]
        for d in count():
            for offsets in all_vectors(len(prediction), d):
                assert sum(n**2 for n in offsets) == d
                guess = tuple((a + b) for (a, b) in zip(prediction, offsets))
                if all(n >= 0 and n <= max_index for n in guess):
                    yield guess


class MstGuesser(BaseGuesser):

    name = 'mst_aware'

    def __init__(self, model, mst_db):
        BaseGuesser.__init__(self, model)
        self._mst_db = mst_db

    def guess_tour(self, edge_timings):
        prediction = self.predict_edges(edge_timings)
        k0 = 0
        k = 10
        while 1:
            nearest = self._mst_db.nearest(prediction, k)
            for entry in nearest[k0:k]:
                yield entry.edges
            k0 = k
            k *= 10


def check_guesses(guesses, actual, max_guesses=None):
    if max_guesses:
        guesses = islice(guesses, max_guesses)
    for (i, guess) in enumerate(guesses):
        if guess == actual:
            return i + 1
    return None


@argh.arg('-s', '--strategy', choices=['naive', 'mst_aware'])
@argh.arg('-g', '--max-guesses')
def guess_tours(model_dir, target_dir, strategy='mst_aware', mst_db=None, max_guesses=200000):
    model = MultiModel.load(model_dir)
    responses = Responses.load(target_dir)
    queries = Queries.load(target_dir)
    if strategy == 'naive':
        guesser = NaiveGuesser(model)
    elif strategy == 'mst_aware':
        mst_db = MstDB.load(mst_db)
        guesser = MstGuesser(model, mst_db)
    else:
        assert False
    num_successful = [0, 0, 0, 0]
    zipped = responses.zip_with_queries(queries)
    for (i, (response, query)) in enumerate(zipped):
        guesses = guesser.guess_tour(response.edge_timings())
        actual_edges = tuple(index for (index, _edge) in model.mst_edges(query))
        guesses_required = check_guesses(guesses, actual_edges, max_guesses)
        _logger.info(
            "response %d: guesses required = %s",
            i, guesses_required or 'NOT FOUND')
        if isinstance(guesses_required, int):
            for j in xrange(len(num_successful)):
                if guesses_required <= 10**j:
                    num_successful[j] += 1
        num_guessed = i + 1
    for (j, n) in enumerate(num_successful):
        _logger.info(
            "%d/%d responses (%.3g%%) required <= %d guesses",
            n, num_guessed,
            float(n)/float(num_guessed) * 100.0,
            10**j)


if __name__ == '__main__':
    argh.dispatch_command(guess_tours)
