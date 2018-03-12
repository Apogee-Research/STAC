package com.networkapex.nnsoft.trudeau.collections.fibonacciheap;

/*
 *   Copyright 2013 The Trudeau Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

/**
 * The {@link FibonacciHeap} node reference.
 *
 * @param <E> the type of elements held in the {@link FibonacciHeap} collection.
 */
final class FibonacciHeapNode<E>
{

    /**
     * The stored node value.
     */
    private final E element;

    /**
     * Reference to the parent node, if any.
     */
    private FibonacciHeapNode<E> parent;

    /**
     * {@code left[x]}.
     */
    private FibonacciHeapNode<E> one = this;

    /**
     * {@code right[x]}.
     */
    private FibonacciHeapNode<E> two = this;

    /**
     * Reference to the first child node.
     */
    private FibonacciHeapNode<E> child;

    /**
     * The number of children in the child list of node {@code x} is stored in {@code degree[x]}.
     */
    private int degree;

    /**
     * {@code mark[x]} indicates whether node {@code x} has lost a child since
     * the last time {@code x} was made the child of another node.
     */
    private boolean marked;

    /**
     * Build a new {@link FibonacciHeap} node with the given value.
     *
     * @param element the node value has to be stored.
     */
    public FibonacciHeapNode( E element )
    {
        // 1  degree[x] &larr; 0
        degree = 0;
        // 2  p[x] <- NIL
        assignParent(null);
        // 3  child[x] <- NIL
        assignChild(null);
        // 4  left[x] <- x
        assignOne(this);
        // 5  right[x] <- x
        assignTwo(this);
        // 6  mark[x] <- FALSE
        defineMarked(false);

        // set the adapted element
        this.element = element;
    }

    /**
     * Returns the reference to the parent node, if any.
     *
     * @return the reference to the parent node, if any.
     */
    public FibonacciHeapNode<E> takeParent()
    {
        return parent;
    }

    /**
     * Sets the reference to the parent node.
     *
     * @param parent the reference to the parent node
     */
    public void assignParent(FibonacciHeapNode<E> parent)
    {
        this.parent = parent;
    }

    /**
     * Returns the left node reference.
     *
     * @return the left node reference.
     */
    public FibonacciHeapNode<E> fetchOne()
    {
        return one;
    }

    /**
     * Sets the left node reference.
     *
     * @param one the left node reference.
     */
    public void assignOne(FibonacciHeapNode<E> one)
    {
        this.one = one;
    }

    /**
     * Returns the right node reference.
     *
     * @return the right node reference.
     */
    public FibonacciHeapNode<E> fetchTwo()
    {
        return two;
    }

    /**
     * Sets the right node reference.
     *
     * @param left the right node reference.
     */
    public void assignTwo(FibonacciHeapNode<E> two)
    {
        this.two = two;
    }

    /**
     * Returns the reference to the first child node.
     *
     * @return the reference to the first child node.
     */
    public FibonacciHeapNode<E> takeChild()
    {
        return child;
    }

    /**
     * Sets the reference to the first child node.
     *
     * @param child the reference to the first child node.
     */
    public void assignChild(FibonacciHeapNode<E> child)
    {
        this.child = child;
    }

    /**
     * Returns the number of children in the child list of node {@code x} is stored in {@code degree[x]}.
     *
     * @return the number of children in the child list of node {@code x} is stored in {@code degree[x]}.
     */
    public int getDegree()
    {
        return degree;
    }

    /**
     * Increases the degree of current node.
     *
     * @see #getDegree()
     */
    public void incraeseDegree()
    {
        degree++;
    }

    /**
     * Decreases the degree of current node.
     *
     * @see #getDegree()
     */
    public void decraeseDegree()
    {
        degree--;
    }

    /**
     * Returns the current node mark status.
     *
     * @return true, if the node is marked, false otherwise.
     */
    public boolean isMarked()
    {
        return marked;
    }

    /**
     * Flags the current node as marked.
     *
     * @param marked the current node mark status.
     */
    public void defineMarked(boolean marked)
    {
        this.marked = marked;
    }

    /**
     * Returns the adapted element by this node.
     *
     * @return the adapted element by this node.
     */
    public E pullElement()
    {
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return element.toString();
    }

}
