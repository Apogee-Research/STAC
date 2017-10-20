// Copyright (c) 1996-99 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
// File: Geometry.java
// Classes: Geometry
// Orginal Author: jrobbins@ics.uci.edu
// $Id: Geometry.java 1333 2011-05-22 10:27:24Z bobtarling $
package org.tigris.gef.base;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * A library of functions that do geometric opeations. These are all static
 * methods, so you never need to make an instance of this class.
 * Needs-More-Work: many of these are not done yet or not used yet.
 */
public class Geometry {

    /**
     * Given a Rectangle and a point, set res to be the point on or in the
     * Rectangle that is closest to the given point.
     */
    public static void ptClosestTo(Rectangle r, Point p, Point res) {

        final int NORTHWEST = 0;
        final int NORTH = 1;
        final int NORTHEAST = 2;
        final int WEST = 3;
        final int CENTER = 4;
        final int EAST = 5;
        final int SOUTHWEST = 6;
        final int SOUTH = 7;
        final int SOUTHEAST = 8;

        int x1 = Math.min(r.x, r.x + (r.width - 1));
        int y1 = Math.min(r.y, r.y + (r.height - 1));
        int x2 = Math.max(r.x, r.x + (r.width - 1));
        int y2 = Math.max(r.y, r.y + (r.height - 1));
        int c;
        if (p.x < x1) {
            c = NORTHWEST;
        } else if (p.x > x2) {
            c = NORTHEAST;
        } else {
            c = NORTH;
        }

        if (p.y > y2) {
            c += 6;
        } else if (p.y > y1) {
            c += 3;
            if (c == CENTER) {
                int westDist = p.x - x1;
                int eastDist = x2 - p.x;
                int northDist = p.y - y1;
                int southDist = y2 - p.y;
                int shortDist;
                if (westDist < eastDist) {
                    shortDist = westDist;
                    c = WEST;
                } else {
                    shortDist = eastDist;
                    c = EAST;
                }
                if (northDist < shortDist) {
                    shortDist = northDist;
                    c = NORTH;
                }
                if (southDist < shortDist) {
                    shortDist = southDist;
                    c = SOUTH;
                }
            }
        }

        switch (c) {
            case NORTHWEST:
                res.x = x1;
                res.y = y1;
                return; // above, left
            case NORTH:
                res.x = p.x;
                res.y = y1;
                return; // above
            case NORTHEAST:
                res.x = x2;
                res.y = y1;
                return; // above, right
            case WEST:
                res.x = x1;
                res.y = p.y;
                return; // left
            case CENTER:
                res.x = p.x;
                res.y = p.y;
                return; // inside rect
            case EAST:
                res.x = x2;
                res.y = p.y;
                return; // right
            case SOUTHWEST:
                res.x = x1;
                res.y = y2;
                return; // below, left
            case SOUTH:
                res.x = p.x;
                res.y = y2;
                return; // below
            case SOUTHEAST:
                res.x = x2;
                res.y = y2;
                return; // below right
        }
    }

    /**
     * Given a Rectangle and a point, return a new Point on or in the Rectangle
     * that is closest to the given Point.
     */
    public static Point ptClosestTo(Rectangle r, Point p) {
        Point res = new Point(0, 0);
        ptClosestTo(r, p, res);
        return res;
    }

    /**
     * Return the angle of a line drawn from P1 to P2 as if P1 was the origin of
     * this graph
     *
     * <pre>
     *
     *            90
     *            |
     *            |
     *            |
     *            |
     * 180 -------p1------- 0
     *            |
     *            |
     *            |
     *            |
     *           270
     * </pre>
     */
    public static double segmentAngle(Point p1, Point p2) {
        if (p2.x == p1.x) {
            if (p2.y > p1.y) {
                return 90;
            } else {
                return 270;
            }
        } else if (p2.y == p1.y) {
            if (p2.x > p1.x) {
                return 0;
            } else {
                return 180;
            }
        }
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double m = dy / dx;
        double a = Math.atan(m) * 180 / Math.PI;
        if (dx < 0) {
            a = 180 + a;
        } else if (dy < 0) {
            a = 360 + a;
        }
        return a;
    }

    /**
     * Given two angle values as calculated using segmentAngle calculate the
     * angle gap between the two.
     *
     * @param angle1
     * @param angle2
     * @return the angle difference.
     */
    public static double diffAngle(double angle1, double angle2) {
        double diff = Math.abs(angle1 - angle2);
        if (diff > 180) {
            diff = 360 - diff;
        }
        return diff;
    }

    /**
     * Given the coordinates of the endpoints of a line segment, and a point,
     * set res to be the closest point on the segement to the given point.
     */
    public static void ptClosestTo(int x1, int y1, int x2, int y2, Point p,
            Point res) {
        // segment is a point
        if (y1 == y2 && x1 == x2) {
            res.x = x1;
            res.y = y1;
            return;
        }
        // segment is horizontal
        if (y1 == y2) {
            res.y = y1;
            res.x = mid(x1, x2, p.x);
            return;
        }
        // segment is vertical
        if (x1 == x2) {
            res.x = x1;
            res.y = mid(y1, y2, p.y);
            return;
        }
        int dx = x2 - x1;
        int dy = y2 - y1;
        res.x = dy * (dy * x1 - dx * (y1 - p.y)) + dx * dx * p.x;
        res.x = res.x / (dx * dx + dy * dy);
        res.y = (dx * (p.x - res.x)) / dy + p.y;

        if (x2 > x1) {
            if (res.x > x2) {
                res.x = x2;
                res.y = y2;
            } else if (res.x < x1) {
                res.x = x1;
                res.y = y1;
            }
        } else {
            if (res.x < x2) {
                res.x = x2;
                res.y = y2;
            } else if (res.x > x1) {
                res.x = x1;
                res.y = y1;
            }
        }
    }

    /**
     * Given three ints, return the one with the middle value. I.e., it is not
     * the single largest or the single smallest.
     */
    private static int mid(int a, int b, int c) {
        if (a <= b) {
            if (b <= c) {
                return b;
            } else if (c <= a) {
                return a;
            } else {
                return c;
            }
        }
        if (b >= c) {
            return b;
        } else if (c >= a) {
            return a;
        } else {
            return c;
        }
    }

    /**
     * Given the coordinates of the endpoints of a line segment, and a point,
     * return a new point that is the closest point on the segement to the given
     * point.
     */
    public static Point ptClosestTo(int x1, int y1, int x2, int y2, Point p) {
        Point res = new Point(0, 0);
        ptClosestTo(x1, y1, x2, y2, p, res);
        return res;
    }

    /**
     * Given the endpoints of a line segment, and a point, return a new point
     * that is the closest point on the segement to the given point.
     */
    public static Point ptClosestTo(Point p1, Point p2, Point p) {
        return ptClosestTo(p1.x, p1.y, p2.x, p2.y, p);
    }

    private static Point tempPoint = new Point(0, 0);

    /**
     * Given a polygon and a point, set res to be the point on the perimiter of
     * the polygon that is closest to to the given point.
     */
    public static synchronized void ptClosestTo(int xs[], int ys[], int n,
            Point p, Point res) {
        res.x = xs[0];
        res.y = ys[0];
        int bestDist = (res.x - p.x) * (res.x - p.x) + (res.y - p.y)
                * (res.y - p.y);
        int tDist;
        tempPoint.x = 0;
        tempPoint.y = 0;
        for (int i = 0; i < n - 1; ++i) {
            ptClosestTo(xs[i], ys[i], xs[i + 1], ys[i + 1], p, tempPoint);
            tDist = (tempPoint.x - p.x) * (tempPoint.x - p.x)
                    + (tempPoint.y - p.y) * (tempPoint.y - p.y);
            if (bestDist > tDist) {
                bestDist = tDist;
                res.x = tempPoint.x;
                res.y = tempPoint.y;
            }
        }
        // dont check segment xs[n-1],ys[n-1] to xs[0],ys[0] because I assume
        // xs[n-1] == xs[0] && ys[n-1] == ys[0], if it is a closed polygon
    }

    /**
     * Given a polygon and a point, return a new point on the perimiter of the
     * polygon that is closest to to the given point.
     */
    public static Point ptClosestTo(int xs[], int ys[], int n, Point p) {
        Point res = new Point(0, 0);
        ptClosestTo(xs, ys, n, p, res);
        return res;
    }

    /**
     * Reply true iff the given point is within grip pixels of one of the
     * segments of the given polygon. Needs-more-work: this is never used, I
     * don't know that it is needed now that I use hit rectangles instead.
     */
    public static synchronized boolean nearPolySegment(int xs[], int ys[],
            int n, int x, int y, int grip) {
        for (int i = 0; i < n - 1; ++i) {
            int x1 = xs[i], y1 = ys[i];
            int x2 = xs[i + 1], y2 = ys[i + 1];
            if (Geometry.nearSegment(x1, y1, x2, y2, x, y, grip)) {
                return true;
            }
        }
        return false;
    }

    private static Rectangle tempRect1 = new Rectangle();

    /**
     * Reply true if the given point is within grip pixels of the given segment.
     * Needs-more-work: this is never used, I don't know that it is needed now
     * that I use hit rectangles instead.
     */
    public static synchronized boolean nearSegment(int x1, int y1, int x2,
            int y2, int x, int y, int grip) {
        tempRect1.setBounds(x - grip, y - grip, 2 * grip, 2 * grip);
        return intersects(tempRect1, x1, y1, x2, y2);
    }

    /**
     * Reply true if the given Rectangle intersects the given line segment.
     */
    public static synchronized boolean intersects(Rectangle r, int x1, int y1,
            int x2, int y2) {
        return r.intersectsLine(x1, y1, x2, y2);
    }

    /**
     * Reply true if the given point is counter-clockwise from the vector
     * defined by the position of the given line. This is used as in determining
     * intersection between lines and rectangles. Taken from Algorithms in C by
     * Sedgewick, page 350.
     */
    public static int counterClockWise(int x1, int y1, int x2, int y2, int x,
            int y) {
        int dx1 = x2 - x1;
        int dy1 = y2 - y1;
        int dx2 = x - x1;
        int dy2 = y - y1;
        if (dx1 * dy2 > dy1 * dx2) {
            return +1;
        }
        if (dx1 * dy2 < dy1 * dx2) {
            return -1;
        }
        if ((dx1 * dx2 < 0) || (dy1 * dy2 < 0)) {
            return -1;
        }
        if ((dx1 * dx1 + dy1 * dy1) < (dx2 * dx2 + dy2 * dy2)) {
            return +1;
        }
        return 0;
    }
} /* end class Geometry */
