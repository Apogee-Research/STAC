package com.tweeter.service;

import com.tweeter.utility.TrieNode;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class SpellingTest {

    private static Spelling spelling;
    private static TrieNode possibles;

    @BeforeClass
    public static void setUp() throws Exception {
        spelling = new Spelling(ClassLoader.getSystemResourceAsStream("dictionary.txt"));
        possibles = new TrieNode();
        Spelling.loadDictionary(ClassLoader.getSystemResourceAsStream("dictionary.txt"), possibles, null);
    }

    @Test
    public void testRemove() throws Exception {
        assertEquals("arm", spelling.removeCharAt("farm", 0));
        assertEquals("frm", spelling.removeCharAt("farm", 1));
        assertEquals("fam", spelling.removeCharAt("farm", 2));
        assertEquals("far", spelling.removeCharAt("farm", 3));
    }

    @Test
    public void testAdd() throws Exception {
        assertEquals("afarm", spelling.addCharAt("farm", 0, 'a'));
        assertEquals("faarm", spelling.addCharAt("farm", 1, 'a'));
        assertEquals("faarm", spelling.addCharAt("farm", 2, 'a'));
        assertEquals("faram", spelling.addCharAt("farm", 3, 'a'));
        assertEquals("farma", spelling.addCharAt("farm", 4, 'a'));
    }

    @Test
    public void testTranspose() throws Exception {
        assertEquals("afrm", spelling.swapCharsAt("farm", 0));
        assertEquals("fram", spelling.swapCharsAt("farm", 1));
        assertEquals("famr", spelling.swapCharsAt("farm", 2));
    }

    @Test
    public void testReplace() throws Exception {
        assertEquals("zarm", spelling.replaceCharAt("farm", 0, 'z'));
        assertEquals("fzrm", spelling.replaceCharAt("farm", 1, 'z'));
        assertEquals("fazm", spelling.replaceCharAt("farm", 2, 'z'));
        assertEquals("farz", spelling.replaceCharAt("farm", 3, 'z'));
    }

    @Test
    public void testAPossible() throws Exception {
        assertTrue(spelling.isPossible("a"));
    }

    @Test
    public void testAAPossible() throws Exception {
        assertTrue(spelling.isPossible("aa"));
    }

    @Test
    public void testAAAPossible() throws Exception {
        assertTrue(spelling.isPossible("aaa"));
    }

    @Test
    public void testAAAAPossible() throws Exception {
        assertFalse(spelling.isPossible("aaaa"));
    }

    @Test
    public void testAAAAAPossible() throws Exception {
        assertFalse(spelling.isPossible("aaaaa"));
    }

    @Test
    public void testAAAAAAPossible() throws Exception {
        assertFalse(spelling.isPossible("aaaaaa"));
    }

    @Test
    public void testAAAAAAAPossible() throws Exception {
        assertFalse(spelling.isPossible("aaaaaaa"));
    }

    @Test
    public void testAPossiblePrefixes() throws Exception {
        assertTrue(spelling.possiblePrefixes("a"));
    }

    @Test
    public void testAAPossiblePrefixes() throws Exception {
        assertTrue(spelling.possiblePrefixes("aa"));
    }

    @Test
    public void testAAAPossiblePrefixes() throws Exception {
        assertTrue(spelling.possiblePrefixes("aaa"));
    }

    @Test
    public void testAAAAPossiblePrefixes() throws Exception {
        assertFalse(spelling.possiblePrefixes("aaaa"));
    }

    @Test
    public void testAAAAAPossiblePrefixes() throws Exception {
        assertFalse(spelling.possiblePrefixes("aaaaa"));
    }

    @Test
    public void testAAAAAAPossiblePrefixes() throws Exception {
        assertFalse(spelling.possiblePrefixes("aaaaaa"));
    }

    @Test
    public void testAAAAAAAPossiblePrefixes() throws Exception {
        assertFalse(spelling.possiblePrefixes("aaaaaaa"));
    }

    @Test
    public void testDictionaryLoad() throws Exception {
        Spelling.loadDictionary(ClassLoader.getSystemResourceAsStream("dictionary.txt"), possibles, null);
    }

    @Test
    public void testAllPossibles() throws Exception {
        assertTrue(possibles.lookup("a"));
        assertTrue(possibles.lookup("e"));
        assertTrue(possibles.lookup("el"));
        assertTrue(possibles.lookup("ele"));
        assertTrue(possibles.lookup("elec"));
        assertTrue(possibles.lookup("elect"));
        assertTrue(possibles.lookup("electr"));
        assertTrue(possibles.lookup("electro"));
        assertTrue(possibles.lookup("electroe"));
        assertTrue(possibles.lookup("electroen"));
        assertTrue(possibles.lookup("electroenc"));
        assertTrue(possibles.lookup("electroence"));
        assertTrue(possibles.lookup("electroencep"));
        assertTrue(possibles.lookup("electroenceph"));
        assertTrue(possibles.lookup("electroencepha"));
        assertTrue(possibles.lookup("electroencephal"));
        assertTrue(possibles.lookup("electroencephalo"));
        assertTrue(possibles.lookup("electroencephalog"));
        assertTrue(possibles.lookup("electroencephalogr"));
        assertTrue(possibles.lookup("electroencephalogra"));
        assertTrue(possibles.lookup("electroencephalograp"));
        assertTrue(possibles.lookup("electroencephalograph"));
        assertTrue(possibles.lookup("electroencephalographi"));
        assertTrue(possibles.lookup("electroencephalographie"));
        assertTrue(possibles.lookup("electroencephalographies"));
        assertTrue(possibles.lookup("h"));
        assertTrue(possibles.lookup("ha"));
        assertTrue(possibles.lookup("haa"));
        assertTrue(possibles.lookup("haar"));
        assertFalse(possibles.lookup("haaa"));

        assertFalse(possibles.lookup("electroencephalographiesa"));
        assertFalse(possibles.lookup("electroencephalographiesb"));
        assertFalse(possibles.lookup("electroencephalographiesc"));
        assertFalse(possibles.lookup("electroencephalographiesd"));
        assertFalse(possibles.lookup("electroencephalographiese"));
        assertFalse(possibles.lookup("electroencephalographiesf"));
        assertFalse(possibles.lookup("electroencephalographiesg"));
        assertFalse(possibles.lookup("electroencephalographiesh"));
        assertFalse(possibles.lookup("electroencephalographiesi"));
        assertFalse(possibles.lookup("electroencephalographiesj"));
        assertFalse(possibles.lookup("electroencephalographiesk"));
        assertFalse(possibles.lookup("electroencephalographiesl"));
        assertFalse(possibles.lookup("electroencephalographiesm"));
        assertFalse(possibles.lookup("electroencephalographiesn"));
        assertFalse(possibles.lookup("electroencephalographieso"));
        assertFalse(possibles.lookup("electroencephalographiesp"));
        assertFalse(possibles.lookup("electroencephalographiesq"));
        assertFalse(possibles.lookup("electroencephalographiesr"));
        assertFalse(possibles.lookup("electroencephalographiess"));
        assertFalse(possibles.lookup("electroencephalographiest"));
        assertFalse(possibles.lookup("electroencephalographiesu"));
        assertFalse(possibles.lookup("electroencephalographiesv"));
        assertFalse(possibles.lookup("electroencephalographiesw"));
        assertFalse(possibles.lookup("electroencephalographiesx"));
        assertFalse(possibles.lookup("electroencephalographiesy"));
        assertFalse(possibles.lookup("electroencephalographiesz"));

    }
}