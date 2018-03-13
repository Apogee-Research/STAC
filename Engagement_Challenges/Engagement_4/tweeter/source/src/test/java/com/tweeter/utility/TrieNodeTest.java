package com.tweeter.utility;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class TrieNodeTest {
    @Test
    public void testTreeInsertsProperlyExtending() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("a");

        assertEquals(1, trie.edges.size());
        assertEquals("a", trie.edges.get(0).label);
        assertTrue(trie.edges.get(0).target.leaf());

        trie.insert("ab");

        assertEquals(1, trie.edges.size());
        assertEquals("a", trie.edges.get(0).label);
        assertEquals(1, trie.edges.get(0).target.edges.size());
        assertEquals("b", trie.edges.get(0).target.edges.get(0).label);
        assertTrue(trie.edges.get(0).target.edges.get(0).target.leaf());

        trie.insert("abc");

        assertEquals(1, trie.edges.size());
        assertEquals("a", trie.edges.get(0).label);
        assertEquals(1, trie.edges.get(0).target.edges.size());
        assertEquals("b", trie.edges.get(0).target.edges.get(0).label);
        assertEquals(1, trie.edges.get(0).target.edges.get(0).target.edges.size());
        assertEquals("c", trie.edges.get(0).target.edges.get(0).target.edges.get(0).label);
        assertTrue(trie.edges.get(0).target.edges.get(0).target.edges.get(0).target.leaf());
    }

    @Test
    public void testTreeLookupsProperlyExtending() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("a");

        assertTrue(trie.lookup("a"));
        assertFalse(trie.lookup("ab"));
        assertFalse(trie.lookup("b"));
        assertFalse(trie.lookup("ba"));
        assertFalse(trie.lookup("abc"));
        assertFalse(trie.lookup("cba"));

        trie.insert("ab");

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertFalse(trie.lookup("b"));
        assertFalse(trie.lookup("ba"));
        assertFalse(trie.lookup("abc"));
        assertFalse(trie.lookup("cba"));

        trie.insert("abc");

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertFalse(trie.lookup("b"));
        assertFalse(trie.lookup("ba"));
        assertTrue(trie.lookup("abc"));
        assertFalse(trie.lookup("cba"));
    }

    @Test
    public void testTreeInsertsProperlyExtending2() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("a");

        assertEquals(1, trie.edges.size());
        assertEquals("a", trie.edges.get(0).label);
        assertTrue(trie.edges.get(0).target.leaf());

        trie.insert("b");

        assertEquals(2, trie.edges.size());
        assertEquals("a", trie.edges.get(0).label);
        assertEquals("b", trie.edges.get(1).label);
        assertTrue(trie.edges.get(0).target.leaf());


        trie.insert("ab");

        assertEquals(2, trie.edges.size());
        assertEquals("a", trie.edges.get(0).label);
        assertEquals(1, trie.edges.get(0).target.edges.size());
        assertEquals("b", trie.edges.get(0).target.edges.get(0).label);
        assertTrue(trie.edges.get(0).target.edges.get(0).target.leaf());

        trie.insert("abc");

        assertEquals(2, trie.edges.size());
        assertEquals("a", trie.edges.get(0).label);
        assertEquals(1, trie.edges.get(0).target.edges.size());
        assertEquals("b", trie.edges.get(0).target.edges.get(0).label);
        assertEquals(1, trie.edges.get(0).target.edges.get(0).target.edges.size());
        assertEquals("c", trie.edges.get(0).target.edges.get(0).target.edges.get(0).label);
        assertTrue(trie.edges.get(0).target.edges.get(0).target.edges.get(0).target.leaf());
    }

    @Test
    public void testTreeLookupsProperlyExtending2() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("a");

        assertTrue(trie.lookup("a"));
        assertFalse(trie.lookup("ab"));
        assertFalse(trie.lookup("b"));
        assertFalse(trie.lookup("ba"));
        assertFalse(trie.lookup("abc"));
        assertFalse(trie.lookup("cba"));

        trie.insert("b");

        assertTrue(trie.lookup("a"));
        assertFalse(trie.lookup("ab"));
        assertTrue(trie.lookup("b"));
        assertFalse(trie.lookup("ba"));
        assertFalse(trie.lookup("abc"));
        assertFalse(trie.lookup("cba"));

        trie.insert("ab");

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertTrue(trie.lookup("b"));
        assertFalse(trie.lookup("ba"));
        assertFalse(trie.lookup("abc"));
        assertFalse(trie.lookup("cba"));

        trie.insert("abc");

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertTrue(trie.lookup("b"));
        assertFalse(trie.lookup("ba"));
        assertTrue(trie.lookup("abc"));
        assertFalse(trie.lookup("cba"));
    }

    @Test
    public void testTreeInsertsProperlyUnique() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("a");
        trie.insert("b");
        trie.insert("c");

        assertEquals(3, trie.edges.size());
        assertEquals("a", trie.edges.get(0).label);
        assertTrue(trie.edges.get(0).target.leaf());
        assertEquals("b", trie.edges.get(1).label);
        assertTrue(trie.edges.get(1).target.leaf());
        assertEquals("c", trie.edges.get(2).label);
        assertTrue(trie.edges.get(2).target.leaf());
    }

    @Test
    public void testTreeLookupsProperlyUnique() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("a");

        assertTrue(trie.lookup("a"));
        assertFalse(trie.lookup("b"));
        assertFalse(trie.lookup("c"));

        trie.insert("b");

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("b"));
        assertFalse(trie.lookup("c"));

        trie.insert("c");

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("b"));
        assertTrue(trie.lookup("c"));
    }

    @Test
    public void testTreeInsertsProperlyComplex() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("abbanogh");

        assertEquals(1, trie.edges.size());
        assertEquals("abbanogh", trie.edges.get(0).label);
        assertTrue(trie.edges.get(0).target.leaf());

        trie.insert("abbalony");

        assertEquals(1, trie.edges.size());
        assertEquals("abba", trie.edges.get(0).label);
        assertEquals("lony", trie.edges.get(0).target.edges.get(0).label);
        assertEquals("nogh", trie.edges.get(0).target.edges.get(1).label);

        assertTrue(trie.edges.get(0).target.edges.get(0).target.leaf());
        assertTrue(trie.edges.get(0).target.edges.get(1).target.leaf());

        trie.insert("abrain");

        assertEquals(1, trie.edges.size());
        assertEquals("ab", trie.edges.get(0).label);
        assertEquals("rain", trie.edges.get(0).target.edges.get(0).label);
        assertTrue(trie.edges.get(0).target.edges.get(0).target.leaf());
        assertEquals("ba", trie.edges.get(0).target.edges.get(1).label);
        assertEquals("lony", trie.edges.get(0).target.edges.get(1).target.edges.get(0).label);
        assertEquals("nogh", trie.edges.get(0).target.edges.get(1).target.edges.get(1).label);
        assertTrue(trie.edges.get(0).target.edges.get(1).target.edges.get(0).target.leaf());
        assertTrue(trie.edges.get(0).target.edges.get(1).target.edges.get(1).target.leaf());
    }

    @Test
    public void testTreeLookupsProperlyComplex() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("abbanogh");

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertTrue(trie.lookup("abb"));
        assertTrue(trie.lookup("abba"));
        assertTrue(trie.lookup("abban"));
        assertTrue(trie.lookup("abbano"));
        assertTrue(trie.lookup("abbanog"));
        assertTrue(trie.lookup("abbanogh"));

        assertFalse(trie.lookup("abbal"));
        assertFalse(trie.lookup("abbalo"));
        assertFalse(trie.lookup("abbalon"));
        assertFalse(trie.lookup("abbalony"));

        trie.insert("abbalony");

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertTrue(trie.lookup("abb"));
        assertTrue(trie.lookup("abba"));
        assertTrue(trie.lookup("abban"));
        assertTrue(trie.lookup("abbano"));
        assertTrue(trie.lookup("abbanog"));
        assertTrue(trie.lookup("abbanogh"));

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertTrue(trie.lookup("abb"));
        assertTrue(trie.lookup("abba"));
        assertTrue(trie.lookup("abbal"));
        assertTrue(trie.lookup("abbalo"));
        assertTrue(trie.lookup("abbalon"));
        assertTrue(trie.lookup("abbalony"));

        trie.insert("abrain");

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertTrue(trie.lookup("abb"));
        assertTrue(trie.lookup("abba"));
        assertTrue(trie.lookup("abban"));
        assertTrue(trie.lookup("abbano"));
        assertTrue(trie.lookup("abbanog"));
        assertTrue(trie.lookup("abbanogh"));

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertTrue(trie.lookup("abb"));
        assertTrue(trie.lookup("abba"));
        assertTrue(trie.lookup("abbal"));
        assertTrue(trie.lookup("abbalo"));
        assertTrue(trie.lookup("abbalon"));
        assertTrue(trie.lookup("abbalony"));

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertTrue(trie.lookup("abr"));
        assertTrue(trie.lookup("abra"));
        assertTrue(trie.lookup("abrai"));
        assertTrue(trie.lookup("abrain"));
    }

    @Test
    public void testTreeInsertsProperlyReducing() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("abc");

        assertEquals(1, trie.countEdges());
        assertEquals("abc", trie.edgeLabel(0));

        trie.insert("ab");
        assertEquals(1, trie.countEdges());
        assertEquals("ab", trie.edgeLabel(0));
        assertEquals(false, trie.edgeTargetLeaf(0));

        trie.insert("a");

        assertEquals(1, trie.edges.size());
        assertEquals("a", trie.edges.get(0).label);
        assertEquals(1, trie.edges.get(0).target.edges.size());
        assertEquals("b", trie.edges.get(0).target.edges.get(0).label);
        assertEquals(1, trie.edges.get(0).target.edges.get(0).target.edges.size());
        assertEquals("c", trie.edges.get(0).target.edges.get(0).target.edges.get(0).label);
    }

    @Test
    public void testTreeLookupsProperlyReducing() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("abc");

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertTrue(trie.lookup("abc"));

        trie.insert("ab");

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertTrue(trie.lookup("abc"));

        trie.insert("a");

        assertTrue(trie.lookup("a"));
        assertTrue(trie.lookup("ab"));
        assertTrue(trie.lookup("abc"));
    }

    @Test
    public void testTreeInsertsProperlySearch() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("search");
        trie.insert("searches");
        trie.insert("searched");
        trie.insert("searching");

        assertEquals(1, trie.edges.size());
        assertEquals("search", trie.edges.get(0).label);
        assertEquals(2, trie.edges.get(0).targetEdgesSize());
        assertEquals("e", trie.edges.get(0).targetEdge(0).label);
        assertEquals(2, trie.edges.get(0).targetEdge(0).targetEdgesSize());
        assertEquals("d", trie.edges.get(0).targetEdge(0).targetEdge(0).label);
        assertEquals("s", trie.edges.get(0).targetEdge(0).targetEdge(1).label);
        assertEquals("ing", trie.edges.get(0).targetEdge(1).label);
    }

    @Test
    public void testTreeLookupsProperlySearch() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("search");

        assertTrue(trie.lookup("s"));
        assertTrue(trie.lookup("se"));
        assertTrue(trie.lookup("sea"));
        assertTrue(trie.lookup("sear"));
        assertTrue(trie.lookup("searc"));
        assertTrue(trie.lookup("search"));

        assertFalse(trie.lookup("searche"));
        assertFalse(trie.lookup("searched"));
        assertFalse(trie.lookup("searchi"));
        assertFalse(trie.lookup("searchin"));
        assertFalse(trie.lookup("searching"));

        trie.insert("searches");

        assertTrue(trie.lookup("s"));
        assertTrue(trie.lookup("se"));
        assertTrue(trie.lookup("sea"));
        assertTrue(trie.lookup("sear"));
        assertTrue(trie.lookup("searc"));
        assertTrue(trie.lookup("search"));
        assertTrue(trie.lookup("searche"));
        assertTrue(trie.lookup("searches"));

        assertFalse(trie.lookup("searched"));
        assertFalse(trie.lookup("searchi"));
        assertFalse(trie.lookup("searchin"));
        assertFalse(trie.lookup("searching"));

        trie.insert("searched");

        assertTrue(trie.lookup("s"));
        assertTrue(trie.lookup("se"));
        assertTrue(trie.lookup("sea"));
        assertTrue(trie.lookup("sear"));
        assertTrue(trie.lookup("searc"));
        assertTrue(trie.lookup("search"));
        assertTrue(trie.lookup("searche"));
        assertTrue(trie.lookup("searched"));

        assertFalse(trie.lookup("searchi"));
        assertFalse(trie.lookup("searchin"));
        assertFalse(trie.lookup("searching"));

        trie.insert("searching");

        assertTrue(trie.lookup("s"));
        assertTrue(trie.lookup("se"));
        assertTrue(trie.lookup("sea"));
        assertTrue(trie.lookup("sear"));
        assertTrue(trie.lookup("searc"));
        assertTrue(trie.lookup("search"));
        assertTrue(trie.lookup("searchi"));
        assertTrue(trie.lookup("searchin"));
        assertTrue(trie.lookup("searching"));
    }

    @Test
    public void testTreeInsertsProperlyLikeLast() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("liking");

        assertEquals(1, trie.edges.size());
        assertTrue(trie.edges.get(0).targetLeaf());
        assertEquals("liking", trie.edges.get(0).label);

        trie.insert("liked");

        assertEquals(1, trie.countEdges());
        assertFalse(trie.edgeTargetLeaf(0));
        assertEquals("lik", trie.edgeLabel(0));
        assertEquals(2, trie.getEdgeTarget(0).countEdges());
        assertEquals("ing", trie.getEdgeTarget(0).edgeLabel(1));
        assertEquals("ed", trie.getEdgeTarget(0).edgeLabel(0));
        assertTrue(trie.getEdgeTarget(0).edgeTargetLeaf(0));
        assertTrue(trie.getEdgeTarget(0).edgeTargetLeaf(1));

        trie.insert("likend");

        assertEquals(1, trie.countEdges());
        assertFalse(trie.edgeTargetLeaf(0));
        assertEquals("lik", trie.edgeLabel(0));
        assertEquals(2, trie.getEdgeTarget(0).countEdges());
        assertEquals("e", trie.getEdgeTarget(0).edgeLabel(1));
        assertEquals("ing", trie.getEdgeTarget(0).edgeLabel(0));
        assertTrue(trie.getEdgeTarget(0).edgeTargetLeaf(0));
        assertFalse(trie.getEdgeTarget(0).edgeTargetLeaf(1));
        assertEquals(2, trie.getEdgeTarget(0).getEdgeTarget(1).countEdges());
        assertEquals("d", trie.getEdgeTarget(0).getEdgeTarget(1).edgeLabel(1));
        assertEquals("nd", trie.getEdgeTarget(0).getEdgeTarget(1).edgeLabel(0));

        trie.insert("last");

        assertEquals(1, trie.countEdges());
        assertFalse(trie.edgeTargetLeaf(0));
        assertEquals("l", trie.edgeLabel(0));
        assertFalse(trie.getEdge(0).targetLeaf());
        assertEquals("ast", trie.getEdge(0).targetEdgeLabel(0));
        assertEquals("ik", trie.getEdge(0).targetEdgeLabel(1));
    }

    @Test
    public void testTreeLookupsProperlyLikeLast() throws Exception {
        TrieNode trie = new TrieNode();
        trie.insert("liking");

        assertTrue(trie.lookup("l"));
        assertTrue(trie.lookup("li"));
        assertFalse(trie.lookup("la"));
        assertFalse(trie.lookup("las"));
        assertTrue(trie.lookup("lik"));
        assertFalse(trie.lookup("last"));
        assertTrue(trie.lookup("liki"));
        assertFalse(trie.lookup("like"));
        assertFalse(trie.lookup("liked"));
        assertTrue(trie.lookup("likin"));
        assertTrue(trie.lookup("liking"));

        trie.insert("liked");

        assertTrue(trie.lookup("l"));
        assertTrue(trie.lookup("li"));
        assertFalse(trie.lookup("la"));
        assertFalse(trie.lookup("las"));
        assertTrue(trie.lookup("lik"));
        assertFalse(trie.lookup("last"));
        assertTrue(trie.lookup("liki"));
        assertTrue(trie.lookup("like"));
        assertFalse(trie.lookup("liken"));
        assertTrue(trie.lookup("liked"));
        assertTrue(trie.lookup("likin"));
        assertFalse(trie.lookup("likend"));
        assertTrue(trie.lookup("liking"));

        trie.insert("likend");

        assertTrue(trie.lookup("l"));
        assertTrue(trie.lookup("li"));
        assertFalse(trie.lookup("la"));
        assertFalse(trie.lookup("las"));
        assertTrue(trie.lookup("lik"));
        assertFalse(trie.lookup("last"));
        assertTrue(trie.lookup("liki"));
        assertTrue(trie.lookup("like"));
        assertTrue(trie.lookup("liken"));
        assertTrue(trie.lookup("liked"));
        assertTrue(trie.lookup("likin"));
        assertTrue(trie.lookup("likend"));
        assertTrue(trie.lookup("liking"));

        trie.insert("last");

        assertTrue(trie.lookup("l"));
        assertTrue(trie.lookup("li"));
        assertTrue(trie.lookup("la"));
        assertTrue(trie.lookup("las"));
        assertTrue(trie.lookup("lik"));
        assertTrue(trie.lookup("last"));
        assertTrue(trie.lookup("liki"));
        assertTrue(trie.lookup("like"));
        assertTrue(trie.lookup("liken"));
        assertTrue(trie.lookup("liked"));
        assertTrue(trie.lookup("likin"));
        assertTrue(trie.lookup("likend"));
        assertTrue(trie.lookup("liking"));
    }
}