#!/usr/bin/python -u

from util import DB_PRECISION, Rectangle


def worst_case_rectangle():
    rectangle = Rectangle.FULL
    while rectangle.width() > DB_PRECISION:
        rectangle = rectangle.quadrants()[-1]
    return rectangle


if __name__ == '__main__':
    print worst_case_rectangle()
