import os
from collections import namedtuple
import csv
from itertools import takewhile


Place = namedtuple('Place', ('name', 'lat', 'lon'))


class Places(list):

    DEFAULT_PLACES_FILE = \
        os.path.abspath(
            os.path.join(
                os.path.dirname(__file__), '../../../source/data/places.csv'))

    @classmethod
    def load(cls, places_file=DEFAULT_PLACES_FILE):
        with open(places_file) as fp:
            reader = csv.reader(fp)
            return cls.read(reader)

    @classmethod
    def read(cls, csvreader):
        header = csvreader.next()
        assert header == ['Name', 'Lat', 'Lon']
        return cls(Place(*row) for row in takewhile(len, csvreader))
