#!/usr/bin/python -u

import time

from util import DB_PRECISION, Rectangle, get_db_length, \
    set_location, unset_location


# Credentials to use
credentials = [('test1', 'password'),
               ('test2', 'password')]

# How long between making a change and testing the size of the DB
# file.
sleep_interval = 0.1


def get_thresholds():
    rectangle = Rectangle.FULL
    point = rectangle.random_point()

    set_location(credentials[0], point)
    unset_location(credentials[1])
    time.sleep(sleep_interval)
    db_length_initial = get_db_length()

    print 'thresholds = ['

    depth = 0
    while rectangle.width() > DB_PRECISION:
        quadrants = rectangle.quadrants()
        for idx, quadrant in enumerate(quadrants):
            if quadrant.contains(point):
                break

        sibling = quadrants[idx - 1]

        set_location(credentials[1], sibling.center())
        time.sleep(sleep_interval)
        db_length = get_db_length()
        delta = db_length - db_length_initial

        print '    %d,' % (delta,)

        rectangle = quadrant
        depth += 1

    print ']'

    unset_location(credentials[0])
    unset_location(credentials[1])


if __name__ == '__main__':
    get_thresholds()
