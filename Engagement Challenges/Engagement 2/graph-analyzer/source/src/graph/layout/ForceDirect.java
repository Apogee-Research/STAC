package graph.layout;

import graph.*;
import graph.cluster.*;
import graph.rep.*;
import java.awt.*;
import java.util.*;

/**
 * A force-directed placement algorithm originally implemented by �lfar
 * Erlingsson at Cornell/RPI and modified to fit into this system.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class ForceDirect implements Action {

    public void apply(Graph g) {
    } //XXX

    public void init(Graph g) {
    } //XXX

    public void step(Graph g) {
    } //XXX

    public void finish(Graph g) {
    } //XXX

    /*
     protected Blackboard _bb;
     public ForceDirect( Blackboard black ) {
     _bb = black;
     }

     protected double _time;
     protected final double fa(double x) {
     double k = _bb.globals.k();
     double ac = _bb.globals.ac();
     double ae = _bb.globals.ae();
     return ac*Math.pow(x,ae)/k;
     }
     protected final double fr(double x) {
     double k = _bb.globals.k();
     double rc = _bb.globals.rc();
     double re = _bb.globals.re();
     return rc*(k*k)/Math.pow(x,re);
     }
     protected final double temp(double t) {
     return _bb.globals.L()/(2*_bb.globals.D())/(1+Math.exp(t/8-5));
     }

     // Implementation of embedder interface, Init and Embed.
     //
     public final void Init() {
     _bb.removeDummies();
     double L = _bb.globals.L();
     _time = 1.0;
     _bb.setArea( -L/2, -L/2, L/2, L/2 );
     }

     public final void Embed() {
     force_directed_placement();
     _bb.Update();
     }
     private final double __min( double a, double b ) { return (a<b)?a:b; }
     private final double __max( double a, double b ) { return (a>b)?a:b; }

     protected synchronized void force_directed_placement() {
     // Calculate repulsive forces
     //
     double k = _bb.globals.k();
     Vector nodes = _bb.nodes();
     int nodecnt = nodes.size();
     for (int i = 0; i < nodecnt; ++i ) {
     Node u = (Node) nodes.elementAt(i);
     u.stabilize();
     for (int j = 0; j < nodecnt; ++j ) {
     Node v = (Node) nodes.elementAt(j);
     double distance = _bb.Norm( u, v );
     if( distance > 3*k ) 
     continue;
     if( distance == 0.0 ) distance = 0.0001;
     u.forced( v, 1.0/distance*fr(distance) );
     }
     }
     // Calculate attractive forces
     //
     Vector edges = _bb.edges();
     int edgecnt = edges.size();
     for( int i = 0; i < edgecnt; ++i ) {
     Edge e = (Edge) edges.elementAt(i);
     Node from = e.from();
     Node to = e.to();
     double distance = _bb.Norm( to, from );
     if( distance > 0.00001 ) {
     to.forced( from, -1.0/distance*fa(distance) );
     from.forced( to, -1.0/distance*fa(distance) );
     }
     }
     // Displace with regard to temperature
     //
     double Temp = temp(_time)+_bb.globals.minTemp();
     _bb.globals.Temp( Temp );
     for (int j = 0; j < nodecnt; ++j ) {
     Node v = (Node) nodes.elementAt(j);
     double force = v.deltaForce();
     if( force < 0.00001 ) continue;
     if ( !v.fixed() && !v.picked() ) {
     v.moveDelta( 1.0/force * __min(force,Temp) );
     }
     }
     _time += 1.0;
     }
     */
}
