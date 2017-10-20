package graph.layout.adv;

//Copyright (C) 2008 Andreas Noack
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA 
/**
 * Weighted graph node.
 *
 * @author Andreas Noack (an@informatik.tu-cottbus.de)
 * @version 13.01.2008
 */
public class NodeX {

    /**
     * Node name.
     */
    public final String name;
    /**
     * Node weight.
     */
    public final double weight;

    /**
     * Initializes the attributes.
     */
    public NodeX(String name, double weight) {
        this.name = name;
        this.weight = weight;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return name + " " + weight;
    }
}
