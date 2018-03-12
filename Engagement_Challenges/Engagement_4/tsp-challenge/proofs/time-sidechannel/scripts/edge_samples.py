#!/usr/bin/env python

import os
from itertools import chain, izip
import json
import random

from matrix import Matrix
from model import Model, MultiModel
from queries import Queries
from responses import Responses
from util import getLogger

_logger = getLogger(__name__)


class EdgeSamples(list):

    @classmethod
    def create(cls, data_dir, matrix=None):
        responses = Responses.load(data_dir)
        queries = Queries.load(data_dir)
        matrix = Matrix.load(matrix)

        _logger.info("collating per-edge samples")

        num_edges_per_query = max(len(q) for q in queries) - 1
        samples = cls([[] for _ in xrange(num_edges_per_query)]
                      for _ in xrange(matrix.num_edges))

        for (response, query) in responses.zip_with_queries(queries):
            events = response.edge_events()
            edges = matrix.mst_edges(query)
            for (pos, (event, (index, _edge))) in enumerate(izip(events, edges)):
                samples[index][pos].append(event.dt)

        return samples

    @classmethod
    def load(cls, filename):
        if os.path.isdir(filename):
            filename = os.path.join(filename, 'edge_samples.jsonl')
        _logger.info("loading edge samples from %s", filename)
        with open(filename) as fp:
            samples = cls(json.loads(line) for line in fp)
        return samples

    @property
    def num_positions(self):
        num_positions = len(self[0])
        assert all(len(entry) == num_positions for entry in self)
        return num_positions

    def save(self, filename):
        if os.path.isdir(filename):
            filename = os.path.join(filename, 'edge_samples.jsonl')
        _logger.info("saving edge samples to %s", filename)
        with open(filename, 'w') as fp:
            for row in self:
                print >> fp, json.dumps(row)

    def histogram(self, edge_index, output_file=None):
        import numpy as np
        import matplotlib.pyplot as plt
        samples = [[dt*1e6 for dt in lst] for lst in self[edge_index]]
        samples = samples[:int(round(len(samples) * 0.96))]
        x_min = min(min(lst) if lst else 1e9999 for lst in samples)
        plt.hist(samples, bins=100, range=(x_min, x_min + 600), stacked=True,
                 color=['magenta', 'green', 'blue', 'cyan'])
        plt.title("edge %d" % edge_index)
        plt.xlabel("dt (microseconds)")
        plt.ylabel("num samples")
        plt.legend(['1st edge', '2nd edge', '3rd edge', '4th edge'])
        if output_file:
            plt.savefig(output_file)

    def scatter(self, output_file=None):
        import numpy as np
        import matplotlib.pyplot as plt
        xs = [
            np.fromiter(
                chain.from_iterable(
                    (edge_index for _dt in samples[pos])
                    for (edge_index, samples) in enumerate(self)),
                dtype=np.int_)
            for pos in xrange(4)
        ]
        ys = [
            np.fromiter(
                chain.from_iterable(
                    (dt*1e6 for dt in samples[pos])
                    for (edge_index, samples) in enumerate(self)),
                dtype=np.float)
            for pos in xrange(4)
        ]
        plt.figure(figsize=(12, 9))
        handles = []
        for (i, color) in enumerate(['magenta', 'green', 'blue', 'cyan']):
            handle = plt.scatter(xs[i], ys[i], s=20, c=color, marker='.',
                                 edgecolors='none', alpha=0.5)
            handles.append(handle)
        plt.xlim((-1, 300))
        plt.ylim((0, 12000))
        plt.xlabel("edge index")
        plt.ylabel("dt (milliseconds)")
        plt.legend(handles,
                   ['1st edge', '2nd edge', '3rd edge', '4th edge'],
                   loc='lower right')
        if output_file:
            plt.savefig(output_file, dpi=300)

    def sample(self, edge_index, pos):
        samples = self[edge_index][pos]
        if not samples:
            raise RuntimeError(
                "no samples for edge_index %d @ pos %d" % (edge_index, pos))
        return random.choice(samples)

    def check(self, edge_index, pos, min_samples):
        samples = self[edge_index][pos]
        if len(samples) < min_samples:
            _logger.error(
                "edge_index %d pos %d has only %d samples",
                edge_index, pos, len(samples))

    def create_model(self):
        import numpy as np
        models = MultiModel()
        for pos in xrange(self.num_positions):
            _logger.info("calculating model for edge position %d", pos)
            edge_indexes = list(chain.from_iterable(
                (edge_index for _dt in timings[pos])
                for (edge_index, timings) in enumerate(self)))
            timings = list(chain.from_iterable(
                timings[pos]
                for (edge_index, timings) in enumerate(self)))
            _logger.info("  num samples = %d", len(timings))
            models.append(Model.fit(edge_indexes, timings))
        return models

    def stats(self):
        import numpy as np
        for (edge_index, edge_entry) in enumerate(self):
            for (pos, samples) in enumerate(edge_entry):
                if not samples:
                    yield (edge_index, pos, None, None, None)
                    continue
                samples = np.array(samples) * 1e6
                mean = np.mean(samples)
                median = np.median(samples)
                stdev = np.std(samples)
                yield (edge_index, pos, mean, median, stdev)


if __name__ == '__main__':
    import argh

    def create(data_dir):
        EdgeSamples.create(data_dir).save(data_dir)

    @argh.arg('edge_index', type=int)
    def histogram(data_dir, edge_index, output_file=None):
        import matplotlib.pyplot as plt
        EdgeSamples.load(data_dir).histogram(edge_index, output_file)
        if not output_file:
            plt.show()

    @argh.arg('edge_index', type=int)
    def compare_histograms(data_dir_1, data_dir_2, edge_index):
        import matplotlib.pyplot as plt
        plt.subplot(2, 1, 1)
        EdgeSamples.load(data_dir_1).histogram(edge_index)
        plt.subplot(2, 1, 2)
        EdgeSamples.load(data_dir_2).histogram(edge_index)
        plt.show()

    def scatter(data_dir, output_file=None):
        import matplotlib.pyplot as plt
        EdgeSamples.load(data_dir).scatter(output_file)
        if not output_file:
            plt.show()

    def create_model(data_dir):
        samples = EdgeSamples.load(data_dir)
        model = samples.create_model()
        model.save(data_dir)

    def stats(data_dir):
        import csv
        samples = EdgeSamples.load(data_dir)
        outfile = os.path.join(data_dir, 'edge_stats.csv')
        with open(outfile, 'w') as fp:
            csvwriter = csv.writer(fp)
            for row in samples.stats():
                csvwriter.writerow(row)

    argh.dispatch_commands([
        create,
        histogram,
        compare_histograms,
        scatter,
        create_model,
        stats])
