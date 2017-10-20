package fi.iki.elonen;

/*
 * #%L
 * NanoHttpd-Webserver-Java-Plugin
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import org.junit.Ignore;
import org.junit.Test;

import static fi.iki.elonen.URIVerifier.SKIP;
import static org.junit.Assert.*;
import static fi.iki.elonen.URIVerifier.URIElement;

/**
 *
 */
public class URIVerifierTest {

    private final Status status = new Status();

    @Test
    public void testMatches() throws Exception {
        URIVerifier fastMatch = new URIVerifier();
        assertFalse(fastMatch.verify("a"));
        assertTrue(fastMatch.verify("bbn.stac.Class"));
        assertTrue(fastMatch.verify("Index"));
        assertTrue(fastMatch.verify("A"));
        assertTrue(fastMatch.verify("b.Aa"));
        // assertFalse(fastMatch.ensureInheritanceHierarchy("notAMatch"));
    }

    @Test
    public void testA() throws Exception {
        URIElement URIElement, n_1, n_2;

        assertTrue(new URIVerifier(new URIElement(true)).verify(""));

        URIElement = new URIElement();
        n_1 = new URIElement(true);
        URIElement.add('A', n_1);

        assertTrue(new URIVerifier(URIElement).verify("A"));

        URIElement = new URIElement();
        n_1 = new URIElement();
        n_2 = new URIElement(true);
        URIElement.add('A', n_1);
        n_1.add(SKIP, n_2);

        assertTrue(new URIVerifier(URIElement).verify("A"));
    }

    @Test
    public void testRange() throws Exception {
        URIElement n1, n2, n3;

        n1 = new URIElement();
        n2 = new URIElement();
        n3 = new URIElement(true);

        n1.add(SKIP, n2);

        for (int i = 0; i < 2; i++) {
            int cost = 'A' + i;
            URIElement nn = new URIElement();
            n2.add(cost, nn);
            nn.add(SKIP, n3);
        }

        assertTrue(new URIVerifier(n1).verify("A"));
        assertTrue(new URIVerifier(n1).verify("B"));
        assertFalse(new URIVerifier(n1).verify("C"));
        assertFalse(new URIVerifier(n1).verify(String.valueOf('A' - 1)));
        assertFalse(new URIVerifier(n1).verify("AA"));
        assertFalse(new URIVerifier(n1).verify(""));
    }

    @Test
    public void testRangeRange() throws Exception {
        URIElement n1 = new URIElement();
        URIElement n2 = new URIElement();
        URIElement n3 = new URIElement();
        URIElement n4 = new URIElement(true);

        n1.add(SKIP, n2);

        for (int i = 0; i < 2; i++) {
            int cost = 'A' + i;
            URIElement nn = new URIElement();
            n2.add(cost, nn);
            nn.add(SKIP, n3);
        }

        for (int i = 0; i < 2; i++) {
            int cost = 'C' + i;
            URIElement nn = new URIElement();
            n3.add(cost, nn);
            nn.add(SKIP, n4);
        }

        assertTrue(new URIVerifier(n1).verify("AC"));
        assertTrue(new URIVerifier(n1).verify("BD"));
        assertFalse(new URIVerifier(n1).verify("CA"));
        assertFalse(new URIVerifier(n1).verify(String.valueOf('A' - 1)));
        assertFalse(new URIVerifier(n1).verify("AA"));
        assertFalse(new URIVerifier(n1).verify(""));
    }

    @Test
    public void testRangeRangePlus() throws Exception {
        URIElement n1 = new URIElement();
        URIElement n2 = new URIElement();
        URIElement n3 = new URIElement();
        URIElement n4 = new URIElement();
        URIElement n5 = new URIElement(true);

        n1.add(SKIP, n2);

        for (int i = 0; i < 2; i++) {
            int cost = 'A' + i;
            URIElement nn = new URIElement();
            n2.add(cost, nn);
            nn.add(SKIP, n3);
        }

        for (int i = 0; i < 2; i++) {
            int cost = 'C' + i;
            URIElement nn = new URIElement();
            n3.add(cost, nn);
            nn.add(SKIP, n4);
        }

        n4.add(SKIP, n3);
        n4.add(SKIP, n5);

        assertTrue(new URIVerifier(n1).verify("AC"));
        assertTrue(new URIVerifier(n1).verify("ACC"));
        assertTrue(new URIVerifier(n1).verify("ACDDDD"));
        assertTrue(new URIVerifier(n1).verify("BD"));
        assertFalse(new URIVerifier(n1).verify("CA"));
        assertFalse(new URIVerifier(n1).verify(String.valueOf('A' - 1)));
        assertFalse(new URIVerifier(n1).verify("AA"));
        assertFalse(new URIVerifier(n1).verify(""));
    }

    @Test
    public void testRangeRangePlusCost() throws Exception {
        URIElement n1 = new URIElement();
        URIElement n2 = new URIElement();
        URIElement n3 = new URIElement();
        URIElement n4 = new URIElement();
        URIElement n5 = new URIElement(true);

        n1.add(SKIP, n2);

        for (int i = 0; i < 2; i++) {
            int cost = 'A' + i;
            URIElement nn = new URIElement();
            n2.add(cost, nn);
            nn.add(SKIP, n3);
        }

        for (int i = 0; i < 2; i++) {
            int cost = 'C' + i;
            URIElement nn = new URIElement();
            n3.add(cost, nn);
            nn.add(SKIP, n4);
        }

        n4.add(SKIP, n3);
        n4.add('.', n5);

        assertTrue(new URIVerifier(n1).verify("AC."));
        assertTrue(new URIVerifier(n1).verify("ACC."));
        assertTrue(new URIVerifier(n1).verify("BD."));
        assertTrue(new URIVerifier(n1).verify("ACDDDD."));
        assertFalse(new URIVerifier(n1).verify("ACDDDD.A"));
        assertFalse(new URIVerifier(n1).verify("ACDDDD.C"));
        assertFalse(new URIVerifier(n1).verify("CA."));
        assertFalse(new URIVerifier(n1).verify(String.valueOf('A' - 1)));
        assertFalse(new URIVerifier(n1).verify("AA."));
        assertFalse(new URIVerifier(n1).verify(""));
    }

    @Test
    public void ltestRangelRangerPlusCostrStar() throws Exception {
        URIElement n1 = new URIElement();
        URIElement n2 = new URIElement();
        URIElement n3 = new URIElement();
        URIElement n4 = new URIElement();
        URIElement n5 = new URIElement();
        URIElement n6 = new URIElement(true);
        URIElement nn;
        int cost;

        n1.add(SKIP, n2);
        n1.add(SKIP, n5);

        for (int i = 0; i < 26; i++) {
            cost = 'a' + i;
            nn = new URIElement();
            n2.add(cost, nn);
            nn.add(SKIP, n3);
        }

        for (int i = 0; i < 26; i++) {
            cost = 'a' + i;
            nn = new URIElement();
            n3.add(cost, nn);
            nn.add(SKIP, n4);
        }

        n4.add(SKIP, n3);
        n4.add('.', n5);

        n5.add(SKIP, n2);

        n5.add(SKIP, n6);

        assertTrue(new URIVerifier(n1).verify(""));
        assertTrue(new URIVerifier(n1).verify("ac."));
        assertTrue(new URIVerifier(n1).verify("acc."));
        assertTrue(new URIVerifier(n1).verify("aaaaaa."));
        assertTrue(new URIVerifier(n1).verify("aaaaaa.aaaaaa."));
        assertTrue(new URIVerifier(n1).verify("aaaaaa.aaaaaa.aaaaaa.aaaaaa.aaaaaa.aaaaaa."));
        assertFalse(new URIVerifier(n1).verify("aaaaaa.aaaaaa"));
        assertFalse(new URIVerifier(n1).verify(String.valueOf('A' - 1)));

    }

    @Test
    public void ltestRangelRangerPlusCostrStarRangelRangerStar() throws Exception {
        URIElement n1 = new URIElement();
        URIElement n2 = new URIElement();
        URIElement n3 = new URIElement();
        URIElement n4 = new URIElement();
        URIElement n5 = new URIElement();
        URIElement n6 = new URIElement();
        URIElement n7 = new URIElement();
        URIElement n8 = new URIElement();
        URIElement n9 = new URIElement();
        URIElement n10 = new URIElement(true);
        URIElement nn;
        int cost;

        n1.add(SKIP, n2);
        n1.add(SKIP, n5);

        for (int i = 0; i < 26; i++) {
            cost = 'a' + i;
            nn = new URIElement();
            n2.add(cost, nn);
            nn.add(SKIP, n3);
        }

        for (int i = 0; i < 26; i++) {
            cost = 'a' + i;
            nn = new URIElement();
            n3.add(cost, nn);
            nn.add(SKIP, n4);
        }

        n4.add(SKIP, n3);
        n4.add('.', n5);

        n5.add(SKIP, n2);

        n5.add(SKIP, n6);

        for (int i = 0; i < 26; i++) {
            cost = 'A' + i;
            nn = new URIElement();
            n6.add(cost, nn);
            nn.add(SKIP, n7);
        }

        n7.add(SKIP, n8);
        n7.add(SKIP, n9);

        for (int i = 0; i < 26; i++) {
            cost = 'A' + i;
            nn = new URIElement();
            n8.add(cost, nn);
            nn.add(SKIP, n9);

            cost = 'a' + i;
            nn = new URIElement();
            n8.add(cost, nn);
            nn.add(SKIP, n9);
        }

        n9.add(SKIP, n10);
        n9.add(SKIP, n8);

        assertTrue(new URIVerifier(n1).verify("A"));
        assertTrue(new URIVerifier(n1).verify("ac.A"));
        assertTrue(new URIVerifier(n1).verify("acc.A"));
        assertTrue(new URIVerifier(n1).verify("aaaaaa.A"));
        assertTrue(new URIVerifier(n1).verify("aaaaaa.aaaaaa.A"));
        assertTrue(new URIVerifier(n1).verify("aaaaaa.aaaaaa.aaaaaa.aaaaaa.aaaaaa.aaaaaa.A"));
        assertTrue(new URIVerifier(n1).verify("aaaaaa.aaaaaa.aaaaaa.aaaaaa.aaaaaa.aaaaaa.AA"));
        assertTrue(new URIVerifier(n1).verify("aaaaaa.aaaaaa.aaaaaa.aaaaaa.aaaaaa.aaaaaa.Aaaaa"));
        assertTrue(new URIVerifier(n1).verify("stac.de.bbn.Com"));
        assertFalse(new URIVerifier(n1).verify("aaaaaa.aaaaaa"));
        assertFalse(new URIVerifier(n1).verify(String.valueOf('A' - 1)));

    }

    @Test
    public void testPlus() throws Exception {
        URIElement n1, n2, n3;

        n1 = new URIElement();
        n2 = new URIElement();
        n3 = new URIElement(true);

        n1.add('A', n2);
        n2.add(SKIP, n1);
        n2.add(SKIP, n3);

        assertFalse(new URIVerifier(n1).verify(""));
        assertTrue(new URIVerifier(n1).verify("A"));
        assertTrue(new URIVerifier(n1).verify("AA"));
        assertTrue(new URIVerifier(n1).verify("AAA"));
        assertTrue(new URIVerifier(n1).verify("AAAA"));
        assertFalse(new URIVerifier(n1).verify("AAAA."));
    }

    @Test
    public void testKleeneStar() throws Exception {
        URIElement n1, n2, n3;

        n1 = new URIElement();
        n2 = new URIElement();
        n3 = new URIElement(true);

        n1.add(SKIP, n3);
        n1.add('A', n2);
        n2.add(SKIP, n1);
        n2.add(SKIP, n3);

        assertTrue(new URIVerifier(n1).verify(""));
        assertTrue(new URIVerifier(n1).verify("A"));
        assertTrue(new URIVerifier(n1).verify("AA"));
        assertTrue(new URIVerifier(n1).verify("AAA"));
        assertTrue(new URIVerifier(n1).verify("AAAA"));
        assertFalse(new URIVerifier(n1).verify("AAAA."));
    }

    @Test
    public void testComplete() throws Exception {
        URIElement n0 = new URIElement(), n1 = new URIElement(), n2 = new URIElement(), n3 = new URIElement(), n4 = new URIElement(), n5 = new URIElement(), n6 = new URIElement(), n7 =
                new URIElement(), n8 = new URIElement(true);

        n0.add(SKIP, n1);
        n0.add(SKIP, n5);

        // (
        n1.add(SKIP, n2);

        // [
        // a-z
        for (int i = 0; i < 26; i++) {
            int cost = 'a' + i;
            URIElement URIElement = new URIElement();
            n2.add(cost, URIElement);
            URIElement.add(SKIP, n3);
        }

        // ]+
        n3.add(SKIP, n2);

        // .
        n3.add('.', n4);

        // )*
        n4.add(SKIP, n1);
        n4.add(SKIP, n5);

        // concat ( minimize epsilon transfers for this branch )
        n1.add(SKIP, n5);

        // [
        // A-Z
        for (int i = 0; i < 26; i++) {
            int cost = 'A' + i;
            URIElement URIElement = new URIElement();
            n5.add(cost, URIElement);
            URIElement.add(SKIP, n6);
        }
        // ]

        // [
        for (int i = 0; i < 26; i++) {
            int cost;
            URIElement URIElement;
            // A-Z
            cost = 'A' + i;
            URIElement = new URIElement();
            n6.add(cost, URIElement);
            URIElement.add(SKIP, n7);
            // a-z
            cost = 'a' + i;
            URIElement = new URIElement();
            n6.add(cost, URIElement);
            URIElement.add(SKIP, n7);
        }
        // ]

        // *
        n7.add(SKIP, n6);
        n7.add(SKIP, n8);
        n6.add(SKIP, n8);

        assertFalse(new URIVerifier(n0).verify("a"));
        assertFalse(new URIVerifier(n0).verify("aA"));
        assertTrue(new URIVerifier(n0).verify("A"));
        assertTrue(new URIVerifier(n0).verify("Aa"));
        assertTrue(new URIVerifier(n0).verify("AA"));
        assertTrue(new URIVerifier(n0).verify("aa.A"));
        assertTrue(new URIVerifier(n0).verify("aaa.A"));
        assertTrue(new URIVerifier(n0).verify("aaaa.A"));
        assertTrue(new URIVerifier(n0).verify("aaaaa.A"));
        assertTrue(new URIVerifier(n0).verify("aaaaa.AA"));
        assertTrue(new URIVerifier(n0).verify("aaaaa.AAA"));
        assertTrue(new URIVerifier(n0).verify("aaaaa.AAAA"));
        assertTrue(new URIVerifier(n0).verify("aaaaa.AAAAA"));
        assertFalse(new URIVerifier(n0).verify("stac.d.bbn.Com."));
        assertTrue(new URIVerifier(n0).verify("stac.D"));
        assertTrue(new URIVerifier(n0).verify("stac.dd.D"));
        assertTrue(new URIVerifier(n0).verify("stac.d.bbn.C"));
    }

    @Test
    public void testTimeoutPositiveMatch() throws Exception {
        long begin = System.currentTimeMillis();

        Thread t = getThread("stac.d.bbn.Com");

        t.run();
        t.join();

        long end = System.currentTimeMillis();
        assertFalse("End time is at most 250 millis greater than the time at the start", end - 250 >= begin);
    }

    @Ignore("Test doesn't have a benign sample yet")
    @Test
    public void testTimeoutBenign() throws Exception {
        long begin = System.currentTimeMillis();

        status.status = Status.States.Start;
        Thread t = getThread("aaaaaaaaaaaaaaaaaaaaaaaaaaaa!");

        t.run();
        t.join();

        long end = System.currentTimeMillis();
        assertFalse("End time is at most 250 millis greater than the time at the start", end - 250 >= begin);
    }

    /* =================== Generated ========================= */

    @Test
    public void GeneratedTimeoutMalicious() throws Exception {
        {// ([abcdefghijklmnopqrstuvwxyz]([abcdefghijklmnopqrstuvwxyz\.])+)*[ABCDEFGHIJKLMNOPQRSTUVWXYZ][ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz]*
            URIElement URIElement0 = new URIElement();
            URIElement URIElement7 = new URIElement(true);
            URIElement URIElement1 = new URIElement();
            URIElement URIElement16 = new URIElement();
            URIElement URIElement10 = new URIElement();
            URIElement URIElement11 = new URIElement();
            URIElement URIElement12 = new URIElement();
            URIElement URIElement17 = new URIElement();
            URIElement URIElement2 = new URIElement();
            URIElement URIElement15 = new URIElement();
            URIElement URIElement13 = new URIElement();
            URIElement URIElement14 = new URIElement();
            URIElement URIElement3 = new URIElement();
            URIElement URIElement4 = new URIElement();
            URIElement URIElement5 = new URIElement();
            URIElement URIElement8 = new URIElement();
            URIElement URIElement9 = new URIElement();
            URIElement URIElement6 = new URIElement();
            URIElement0.add(SKIP, URIElement1);
            URIElement1.add(SKIP, URIElement10);
            URIElement1.add(SKIP, URIElement2);
            URIElement16.add(SKIP, URIElement17);
            URIElement16.add(SKIP, URIElement2);
            URIElement10.add('a', URIElement11);
            URIElement10.add('b', URIElement11);
            URIElement10.add('c', URIElement11);
            URIElement10.add('d', URIElement11);
            URIElement10.add('e', URIElement11);
            URIElement10.add('f', URIElement11);
            URIElement10.add('g', URIElement11);
            URIElement10.add('h', URIElement11);
            URIElement10.add('i', URIElement11);
            URIElement10.add('j', URIElement11);
            URIElement10.add('k', URIElement11);
            URIElement10.add('l', URIElement11);
            URIElement10.add('m', URIElement11);
            URIElement10.add('n', URIElement11);
            URIElement10.add('o', URIElement11);
            URIElement10.add('p', URIElement11);
            URIElement10.add('q', URIElement11);
            URIElement10.add('r', URIElement11);
            URIElement10.add('s', URIElement11);
            URIElement10.add('t', URIElement11);
            URIElement10.add('u', URIElement11);
            URIElement10.add('v', URIElement11);
            URIElement10.add('w', URIElement11);
            URIElement10.add('x', URIElement11);
            URIElement10.add('y', URIElement11);
            URIElement10.add('z', URIElement11);
            URIElement11.add(SKIP, URIElement12);
            URIElement12.add(SKIP, URIElement13);
            URIElement17.add(SKIP, URIElement1);
            URIElement2.add(SKIP, URIElement3);
            URIElement15.add(SKIP, URIElement12);
            URIElement15.add(SKIP, URIElement16);
            URIElement13.add('a', URIElement14);
            URIElement13.add('b', URIElement14);
            URIElement13.add('c', URIElement14);
            URIElement13.add('d', URIElement14);
            URIElement13.add('e', URIElement14);
            URIElement13.add('f', URIElement14);
            URIElement13.add('g', URIElement14);
            URIElement13.add('h', URIElement14);
            URIElement13.add('i', URIElement14);
            URIElement13.add('j', URIElement14);
            URIElement13.add('k', URIElement14);
            URIElement13.add('l', URIElement14);
            URIElement13.add('m', URIElement14);
            URIElement13.add('n', URIElement14);
            URIElement13.add('o', URIElement14);
            URIElement13.add('p', URIElement14);
            URIElement13.add('q', URIElement14);
            URIElement13.add('r', URIElement14);
            URIElement13.add('s', URIElement14);
            URIElement13.add('t', URIElement14);
            URIElement13.add('u', URIElement14);
            URIElement13.add('v', URIElement14);
            URIElement13.add('w', URIElement14);
            URIElement13.add('x', URIElement14);
            URIElement13.add('y', URIElement14);
            URIElement13.add('z', URIElement14);
            URIElement13.add('.', URIElement14);
            URIElement14.add(SKIP, URIElement15);
            URIElement3.add('A', URIElement4);
            URIElement3.add('B', URIElement4);
            URIElement3.add('C', URIElement4);
            URIElement3.add('D', URIElement4);
            URIElement3.add('E', URIElement4);
            URIElement3.add('F', URIElement4);
            URIElement3.add('G', URIElement4);
            URIElement3.add('H', URIElement4);
            URIElement3.add('I', URIElement4);
            URIElement3.add('J', URIElement4);
            URIElement3.add('K', URIElement4);
            URIElement3.add('L', URIElement4);
            URIElement3.add('M', URIElement4);
            URIElement3.add('N', URIElement4);
            URIElement3.add('O', URIElement4);
            URIElement3.add('P', URIElement4);
            URIElement3.add('Q', URIElement4);
            URIElement3.add('R', URIElement4);
            URIElement3.add('S', URIElement4);
            URIElement3.add('T', URIElement4);
            URIElement3.add('U', URIElement4);
            URIElement3.add('V', URIElement4);
            URIElement3.add('W', URIElement4);
            URIElement3.add('X', URIElement4);
            URIElement3.add('Y', URIElement4);
            URIElement3.add('Z', URIElement4);
            URIElement4.add(SKIP, URIElement5);
            URIElement5.add('A', URIElement8);
            URIElement5.add('B', URIElement8);
            URIElement5.add('C', URIElement8);
            URIElement5.add('D', URIElement8);
            URIElement5.add('E', URIElement8);
            URIElement5.add('F', URIElement8);
            URIElement5.add('G', URIElement8);
            URIElement5.add('H', URIElement8);
            URIElement5.add('I', URIElement8);
            URIElement5.add('J', URIElement8);
            URIElement5.add('K', URIElement8);
            URIElement5.add('L', URIElement8);
            URIElement5.add('M', URIElement8);
            URIElement5.add('N', URIElement8);
            URIElement5.add('O', URIElement8);
            URIElement5.add('P', URIElement8);
            URIElement5.add('Q', URIElement8);
            URIElement5.add('R', URIElement8);
            URIElement5.add('S', URIElement8);
            URIElement5.add('T', URIElement8);
            URIElement5.add('U', URIElement8);
            URIElement5.add('V', URIElement8);
            URIElement5.add('W', URIElement8);
            URIElement5.add('X', URIElement8);
            URIElement5.add('Y', URIElement8);
            URIElement5.add('Z', URIElement8);
            URIElement5.add('a', URIElement8);
            URIElement5.add('b', URIElement8);
            URIElement5.add('c', URIElement8);
            URIElement5.add('d', URIElement8);
            URIElement5.add('e', URIElement8);
            URIElement5.add('f', URIElement8);
            URIElement5.add('g', URIElement8);
            URIElement5.add('h', URIElement8);
            URIElement5.add('i', URIElement8);
            URIElement5.add('j', URIElement8);
            URIElement5.add('k', URIElement8);
            URIElement5.add('l', URIElement8);
            URIElement5.add('m', URIElement8);
            URIElement5.add('n', URIElement8);
            URIElement5.add('o', URIElement8);
            URIElement5.add('p', URIElement8);
            URIElement5.add('q', URIElement8);
            URIElement5.add('r', URIElement8);
            URIElement5.add('s', URIElement8);
            URIElement5.add('t', URIElement8);
            URIElement5.add('u', URIElement8);
            URIElement5.add('v', URIElement8);
            URIElement5.add('w', URIElement8);
            URIElement5.add('x', URIElement8);
            URIElement5.add('y', URIElement8);
            URIElement5.add('z', URIElement8);
            URIElement5.add(SKIP, URIElement6);
            URIElement8.add(SKIP, URIElement9);
            URIElement8.add(SKIP, URIElement6);
            URIElement9.add(SKIP, URIElement5);
            URIElement6.add(SKIP, URIElement7);

            long begin = System.currentTimeMillis();

            status.status = Status.States.Start;
            Thread t = getThread(URIElement0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaa!");

            t.run();
            t.join();

            long end = System.currentTimeMillis();
            assertTrue("End time is at least 250 millis greater than the time at the start", end - 250 >= begin);
        }
    }

    @Test
    public void testGeneratedComplete() throws Exception {
        {// ([abcdefghijklmnopqrstuvwxyz]([abcdefghijklmnopqrstuvwxyz\.])+)*[ABCDEFGHIJKLMNOPQRSTUVWXYZ][ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz]*
            URIElement URIElement0 = new URIElement();
            URIElement URIElement7 = new URIElement(true);
            URIElement URIElement1 = new URIElement();
            URIElement URIElement16 = new URIElement();
            URIElement URIElement10 = new URIElement();
            URIElement URIElement11 = new URIElement();
            URIElement URIElement12 = new URIElement();
            URIElement URIElement17 = new URIElement();
            URIElement URIElement2 = new URIElement();
            URIElement URIElement15 = new URIElement();
            URIElement URIElement13 = new URIElement();
            URIElement URIElement14 = new URIElement();
            URIElement URIElement3 = new URIElement();
            URIElement URIElement4 = new URIElement();
            URIElement URIElement5 = new URIElement();
            URIElement URIElement8 = new URIElement();
            URIElement URIElement9 = new URIElement();
            URIElement URIElement6 = new URIElement();
            URIElement0.add(SKIP, URIElement1);
            URIElement1.add(SKIP, URIElement10);
            URIElement1.add(SKIP, URIElement2);
            URIElement16.add(SKIP, URIElement17);
            URIElement16.add(SKIP, URIElement2);
            URIElement10.add('a', URIElement11);
            URIElement10.add('b', URIElement11);
            URIElement10.add('c', URIElement11);
            URIElement10.add('d', URIElement11);
            URIElement10.add('e', URIElement11);
            URIElement10.add('f', URIElement11);
            URIElement10.add('g', URIElement11);
            URIElement10.add('h', URIElement11);
            URIElement10.add('i', URIElement11);
            URIElement10.add('j', URIElement11);
            URIElement10.add('k', URIElement11);
            URIElement10.add('l', URIElement11);
            URIElement10.add('m', URIElement11);
            URIElement10.add('n', URIElement11);
            URIElement10.add('o', URIElement11);
            URIElement10.add('p', URIElement11);
            URIElement10.add('q', URIElement11);
            URIElement10.add('r', URIElement11);
            URIElement10.add('s', URIElement11);
            URIElement10.add('t', URIElement11);
            URIElement10.add('u', URIElement11);
            URIElement10.add('v', URIElement11);
            URIElement10.add('w', URIElement11);
            URIElement10.add('x', URIElement11);
            URIElement10.add('y', URIElement11);
            URIElement10.add('z', URIElement11);
            URIElement11.add(SKIP, URIElement12);
            URIElement12.add(SKIP, URIElement13);
            URIElement17.add(SKIP, URIElement1);
            URIElement2.add(SKIP, URIElement3);
            URIElement15.add(SKIP, URIElement12);
            URIElement15.add(SKIP, URIElement16);
            URIElement13.add('a', URIElement14);
            URIElement13.add('b', URIElement14);
            URIElement13.add('c', URIElement14);
            URIElement13.add('d', URIElement14);
            URIElement13.add('e', URIElement14);
            URIElement13.add('f', URIElement14);
            URIElement13.add('g', URIElement14);
            URIElement13.add('h', URIElement14);
            URIElement13.add('i', URIElement14);
            URIElement13.add('j', URIElement14);
            URIElement13.add('k', URIElement14);
            URIElement13.add('l', URIElement14);
            URIElement13.add('m', URIElement14);
            URIElement13.add('n', URIElement14);
            URIElement13.add('o', URIElement14);
            URIElement13.add('p', URIElement14);
            URIElement13.add('q', URIElement14);
            URIElement13.add('r', URIElement14);
            URIElement13.add('s', URIElement14);
            URIElement13.add('t', URIElement14);
            URIElement13.add('u', URIElement14);
            URIElement13.add('v', URIElement14);
            URIElement13.add('w', URIElement14);
            URIElement13.add('x', URIElement14);
            URIElement13.add('y', URIElement14);
            URIElement13.add('z', URIElement14);
            URIElement13.add('.', URIElement14);
            URIElement14.add(SKIP, URIElement15);
            URIElement3.add('A', URIElement4);
            URIElement3.add('B', URIElement4);
            URIElement3.add('C', URIElement4);
            URIElement3.add('D', URIElement4);
            URIElement3.add('E', URIElement4);
            URIElement3.add('F', URIElement4);
            URIElement3.add('G', URIElement4);
            URIElement3.add('H', URIElement4);
            URIElement3.add('I', URIElement4);
            URIElement3.add('J', URIElement4);
            URIElement3.add('K', URIElement4);
            URIElement3.add('L', URIElement4);
            URIElement3.add('M', URIElement4);
            URIElement3.add('N', URIElement4);
            URIElement3.add('O', URIElement4);
            URIElement3.add('P', URIElement4);
            URIElement3.add('Q', URIElement4);
            URIElement3.add('R', URIElement4);
            URIElement3.add('S', URIElement4);
            URIElement3.add('T', URIElement4);
            URIElement3.add('U', URIElement4);
            URIElement3.add('V', URIElement4);
            URIElement3.add('W', URIElement4);
            URIElement3.add('X', URIElement4);
            URIElement3.add('Y', URIElement4);
            URIElement3.add('Z', URIElement4);
            URIElement4.add(SKIP, URIElement5);
            URIElement5.add('A', URIElement8);
            URIElement5.add('B', URIElement8);
            URIElement5.add('C', URIElement8);
            URIElement5.add('D', URIElement8);
            URIElement5.add('E', URIElement8);
            URIElement5.add('F', URIElement8);
            URIElement5.add('G', URIElement8);
            URIElement5.add('H', URIElement8);
            URIElement5.add('I', URIElement8);
            URIElement5.add('J', URIElement8);
            URIElement5.add('K', URIElement8);
            URIElement5.add('L', URIElement8);
            URIElement5.add('M', URIElement8);
            URIElement5.add('N', URIElement8);
            URIElement5.add('O', URIElement8);
            URIElement5.add('P', URIElement8);
            URIElement5.add('Q', URIElement8);
            URIElement5.add('R', URIElement8);
            URIElement5.add('S', URIElement8);
            URIElement5.add('T', URIElement8);
            URIElement5.add('U', URIElement8);
            URIElement5.add('V', URIElement8);
            URIElement5.add('W', URIElement8);
            URIElement5.add('X', URIElement8);
            URIElement5.add('Y', URIElement8);
            URIElement5.add('Z', URIElement8);
            URIElement5.add('a', URIElement8);
            URIElement5.add('b', URIElement8);
            URIElement5.add('c', URIElement8);
            URIElement5.add('d', URIElement8);
            URIElement5.add('e', URIElement8);
            URIElement5.add('f', URIElement8);
            URIElement5.add('g', URIElement8);
            URIElement5.add('h', URIElement8);
            URIElement5.add('i', URIElement8);
            URIElement5.add('j', URIElement8);
            URIElement5.add('k', URIElement8);
            URIElement5.add('l', URIElement8);
            URIElement5.add('m', URIElement8);
            URIElement5.add('n', URIElement8);
            URIElement5.add('o', URIElement8);
            URIElement5.add('p', URIElement8);
            URIElement5.add('q', URIElement8);
            URIElement5.add('r', URIElement8);
            URIElement5.add('s', URIElement8);
            URIElement5.add('t', URIElement8);
            URIElement5.add('u', URIElement8);
            URIElement5.add('v', URIElement8);
            URIElement5.add('w', URIElement8);
            URIElement5.add('x', URIElement8);
            URIElement5.add('y', URIElement8);
            URIElement5.add('z', URIElement8);
            URIElement5.add(SKIP, URIElement6);
            URIElement8.add(SKIP, URIElement9);
            URIElement8.add(SKIP, URIElement6);
            URIElement9.add(SKIP, URIElement5);
            URIElement6.add(SKIP, URIElement7);

            assertFalse(new URIVerifier(URIElement0).verify("a"));
            assertFalse(new URIVerifier(URIElement0).verify("aA"));
            // assertFalse(new
            // JavaWebServerAssertInheritanceHelper(node0).ensureInheritanceHierarchy("notAMatch"));
            assertTrue(new URIVerifier(URIElement0).verify("A"));
            assertTrue(new URIVerifier(URIElement0).verify("Aa"));
            assertTrue(new URIVerifier(URIElement0).verify("AA"));
            assertTrue(new URIVerifier(URIElement0).verify("aa.A"));
            assertTrue(new URIVerifier(URIElement0).verify("aaa.A"));
            assertTrue(new URIVerifier(URIElement0).verify("aaaa.A"));
            assertTrue(new URIVerifier(URIElement0).verify("aaaaa.A"));
            assertTrue(new URIVerifier(URIElement0).verify("aaaaa.AA"));
            assertTrue(new URIVerifier(URIElement0).verify("aaaaa.AAA"));
            assertTrue(new URIVerifier(URIElement0).verify("aaaaa.AAAA"));
            assertTrue(new URIVerifier(URIElement0).verify("aaaaa.AAAAA"));
            assertFalse(new URIVerifier(URIElement0).verify("stac.d.bbn.Com."));
            assertTrue(new URIVerifier(URIElement0).verify("stac.D"));
            assertTrue(new URIVerifier(URIElement0).verify("stac.dd.D"));
            assertTrue(new URIVerifier(URIElement0).verify("stac.d.bbn.C"));
        }
    }

    @Test
    public void testGeneratedA() throws Exception {
        {//
            URIElement URIElement1 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement0.add(SKIP, URIElement1);

            assertTrue(new URIVerifier(URIElement0).verify(""));
        }

        {// A
            URIElement URIElement0 = new URIElement();
            URIElement URIElement2 = new URIElement(true);
            URIElement URIElement1 = new URIElement();
            URIElement0.add(65, URIElement1);
            URIElement1.add(SKIP, URIElement2);

            assertTrue(new URIVerifier(URIElement0).verify("A"));
        }
    }

    @Test
    public void testGeneratedRange() throws Exception {
        {// [AB]
            URIElement URIElement3 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement URIElement1 = new URIElement();
            URIElement URIElement2 = new URIElement();
            URIElement0.add(SKIP, URIElement1);
            URIElement1.add(65, URIElement2);
            URIElement1.add(66, URIElement2);
            URIElement2.add(SKIP, URIElement3);

            assertTrue(new URIVerifier(URIElement0).verify("A"));
            assertTrue(new URIVerifier(URIElement0).verify("B"));
            assertFalse(new URIVerifier(URIElement0).verify("C"));
            assertFalse(new URIVerifier(URIElement0).verify(String.valueOf('A' - 1)));
            assertFalse(new URIVerifier(URIElement0).verify("AA"));
            assertFalse(new URIVerifier(URIElement0).verify(""));
        }
    }

    @Test
    public void testGeneratedRangeRange() throws Exception {
        {// [AB][CD]
            URIElement URIElement5 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement URIElement1 = new URIElement();
            URIElement URIElement2 = new URIElement();
            URIElement URIElement3 = new URIElement();
            URIElement URIElement4 = new URIElement();
            URIElement0.add(SKIP, URIElement1);
            URIElement1.add(65, URIElement2);
            URIElement1.add(66, URIElement2);
            URIElement2.add(SKIP, URIElement3);
            URIElement3.add(67, URIElement4);
            URIElement3.add(68, URIElement4);
            URIElement4.add(SKIP, URIElement5);

            assertTrue(new URIVerifier(URIElement0).verify("AC"));
            assertTrue(new URIVerifier(URIElement0).verify("BD"));
            assertFalse(new URIVerifier(URIElement0).verify("CA"));
            assertFalse(new URIVerifier(URIElement0).verify(String.valueOf('A' - 1)));
            assertFalse(new URIVerifier(URIElement0).verify("AA"));
            assertFalse(new URIVerifier(URIElement0).verify(""));
        }
    }

    @Test
    public void testGeneratedRangeRangePlus() throws Exception {
        {// [AB][CD]+
            URIElement URIElement5 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement URIElement1 = new URIElement();
            URIElement URIElement2 = new URIElement();
            URIElement URIElement3 = new URIElement();
            URIElement URIElement4 = new URIElement();
            URIElement0.add(SKIP, URIElement1);
            URIElement1.add(65, URIElement2);
            URIElement1.add(66, URIElement2);
            URIElement2.add(SKIP, URIElement3);
            URIElement3.add(67, URIElement4);
            URIElement3.add(68, URIElement4);
            URIElement4.add(SKIP, URIElement3);
            URIElement4.add(SKIP, URIElement5);

            assertTrue(new URIVerifier(URIElement0).verify("AC"));
            assertTrue(new URIVerifier(URIElement0).verify("ACC"));
            assertTrue(new URIVerifier(URIElement0).verify("ACDDDD"));
            assertTrue(new URIVerifier(URIElement0).verify("BD"));
            assertFalse(new URIVerifier(URIElement0).verify("CA"));
            assertFalse(new URIVerifier(URIElement0).verify(String.valueOf('A' - 1)));
            assertFalse(new URIVerifier(URIElement0).verify("AA"));
            assertFalse(new URIVerifier(URIElement0).verify(""));
        }
    }

    @Test
    public void testGeneratedRangeRangePlusCost() throws Exception {
        {// [AB][CD]+\.
            URIElement URIElement6 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement URIElement1 = new URIElement();
            URIElement URIElement2 = new URIElement();
            URIElement URIElement3 = new URIElement();
            URIElement URIElement4 = new URIElement();
            URIElement URIElement5 = new URIElement();
            URIElement0.add(SKIP, URIElement1);
            URIElement1.add('A', URIElement2);
            URIElement1.add('B', URIElement2);
            URIElement2.add(SKIP, URIElement3);
            URIElement3.add('C', URIElement4);
            URIElement3.add('D', URIElement4);
            URIElement4.add(SKIP, URIElement3);
            URIElement4.add('.', URIElement5);
            URIElement5.add(SKIP, URIElement6);

            assertTrue(new URIVerifier(URIElement0).verify("AC."));
            assertTrue(new URIVerifier(URIElement0).verify("ACC."));
            assertTrue(new URIVerifier(URIElement0).verify("BD."));
            assertTrue(new URIVerifier(URIElement0).verify("ACDDDD."));
            assertFalse(new URIVerifier(URIElement0).verify("ACDDDD.A"));
            assertFalse(new URIVerifier(URIElement0).verify("ACDDDD.C"));
            assertFalse(new URIVerifier(URIElement0).verify("CA."));
            assertFalse(new URIVerifier(URIElement0).verify(String.valueOf('A' - 1)));
            assertFalse(new URIVerifier(URIElement0).verify("AA."));
            assertFalse(new URIVerifier(URIElement0).verify(""));
        }
    }

    @Test
    public void GeneratedCostStar() throws Exception {
        {// A.*
            URIElement URIElement4 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement URIElement1 = new URIElement();
            URIElement URIElement2 = new URIElement();
            URIElement URIElement5 = new URIElement();
            URIElement URIElement6 = new URIElement();
            URIElement URIElement3 = new URIElement();
            URIElement0.add('A', URIElement1);
            URIElement1.add(SKIP, URIElement2);
            URIElement2.add('!', URIElement5);
            URIElement2.add('"', URIElement5);
            URIElement2.add('#', URIElement5);
            URIElement2.add('$', URIElement5);
            URIElement2.add('%', URIElement5);
            URIElement2.add('&', URIElement5);
            URIElement2.add('\'', URIElement5);
            URIElement2.add('(', URIElement5);
            URIElement2.add(')', URIElement5);
            URIElement2.add('*', URIElement5);
            URIElement2.add('+', URIElement5);
            URIElement2.add(',', URIElement5);
            URIElement2.add('-', URIElement5);
            URIElement2.add('.', URIElement5);
            URIElement2.add('/', URIElement5);
            URIElement2.add('0', URIElement5);
            URIElement2.add('1', URIElement5);
            URIElement2.add('2', URIElement5);
            URIElement2.add('3', URIElement5);
            URIElement2.add('4', URIElement5);
            URIElement2.add('5', URIElement5);
            URIElement2.add('6', URIElement5);
            URIElement2.add('7', URIElement5);
            URIElement2.add('8', URIElement5);
            URIElement2.add('9', URIElement5);
            URIElement2.add(':', URIElement5);
            URIElement2.add(';', URIElement5);
            URIElement2.add('<', URIElement5);
            URIElement2.add('=', URIElement5);
            URIElement2.add('>', URIElement5);
            URIElement2.add('?', URIElement5);
            URIElement2.add('@', URIElement5);
            URIElement2.add('A', URIElement5);
            URIElement2.add('B', URIElement5);
            URIElement2.add('C', URIElement5);
            URIElement2.add('D', URIElement5);
            URIElement2.add('E', URIElement5);
            URIElement2.add('F', URIElement5);
            URIElement2.add('G', URIElement5);
            URIElement2.add('H', URIElement5);
            URIElement2.add('I', URIElement5);
            URIElement2.add('J', URIElement5);
            URIElement2.add('K', URIElement5);
            URIElement2.add('L', URIElement5);
            URIElement2.add('M', URIElement5);
            URIElement2.add('N', URIElement5);
            URIElement2.add('O', URIElement5);
            URIElement2.add('P', URIElement5);
            URIElement2.add('Q', URIElement5);
            URIElement2.add('R', URIElement5);
            URIElement2.add('S', URIElement5);
            URIElement2.add('T', URIElement5);
            URIElement2.add('U', URIElement5);
            URIElement2.add('V', URIElement5);
            URIElement2.add('W', URIElement5);
            URIElement2.add('X', URIElement5);
            URIElement2.add('Y', URIElement5);
            URIElement2.add('Z', URIElement5);
            URIElement2.add('[', URIElement5);
            URIElement2.add('\\', URIElement5);
            URIElement2.add(']', URIElement5);
            URIElement2.add('^', URIElement5);
            URIElement2.add('_', URIElement5);
            URIElement2.add('`', URIElement5);
            URIElement2.add('a', URIElement5);
            URIElement2.add('b', URIElement5);
            URIElement2.add('c', URIElement5);
            URIElement2.add('d', URIElement5);
            URIElement2.add('e', URIElement5);
            URIElement2.add('f', URIElement5);
            URIElement2.add('g', URIElement5);
            URIElement2.add('h', URIElement5);
            URIElement2.add('i', URIElement5);
            URIElement2.add('j', URIElement5);
            URIElement2.add('k', URIElement5);
            URIElement2.add('l', URIElement5);
            URIElement2.add('m', URIElement5);
            URIElement2.add('n', URIElement5);
            URIElement2.add('o', URIElement5);
            URIElement2.add('p', URIElement5);
            URIElement2.add('q', URIElement5);
            URIElement2.add('r', URIElement5);
            URIElement2.add('s', URIElement5);
            URIElement2.add('t', URIElement5);
            URIElement2.add('u', URIElement5);
            URIElement2.add('v', URIElement5);
            URIElement2.add('w', URIElement5);
            URIElement2.add('x', URIElement5);
            URIElement2.add('y', URIElement5);
            URIElement2.add('z', URIElement5);
            URIElement2.add('{', URIElement5);
            URIElement2.add('|', URIElement5);
            URIElement2.add('}', URIElement5);
            URIElement2.add('~', URIElement5);
            URIElement2.add(SKIP, URIElement3);
            URIElement5.add(SKIP, URIElement6);
            URIElement5.add(SKIP, URIElement3);
            URIElement6.add(SKIP, URIElement2);
            URIElement3.add(SKIP, URIElement4);

            assertTrue(new URIVerifier(URIElement0).verify("A"));
            assertTrue(new URIVerifier(URIElement0).verify("Aa"));
            assertTrue(new URIVerifier(URIElement0).verify("Aab"));
            assertTrue(new URIVerifier(URIElement0).verify("Aabcdefghijklmnopqrstuvwxyz"));
            assertFalse(new URIVerifier(URIElement0).verify(""));
            assertFalse(new URIVerifier(URIElement0).verify("a"));
            assertFalse(new URIVerifier(URIElement0).verify("ab"));
            assertFalse(new URIVerifier(URIElement0).verify("abcdefghijklmnopqrstuvwxyz"));
        }
    }

    @Test
    public void GeneratedlrStar() throws Exception {
        {// (AB\.)*
            URIElement URIElement3 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement URIElement1 = new URIElement();
            URIElement URIElement7 = new URIElement();
            URIElement URIElement4 = new URIElement();
            URIElement URIElement5 = new URIElement();
            URIElement URIElement6 = new URIElement();
            URIElement URIElement8 = new URIElement();
            URIElement URIElement2 = new URIElement();
            URIElement0.add(SKIP, URIElement1);
            URIElement1.add('A', URIElement4);
            URIElement1.add(SKIP, URIElement2);
            URIElement7.add(SKIP, URIElement8);
            URIElement7.add(SKIP, URIElement2);
            URIElement4.add('B', URIElement5);
            URIElement5.add('.', URIElement6);
            URIElement6.add(SKIP, URIElement7);
            URIElement8.add(SKIP, URIElement1);
            URIElement2.add(SKIP, URIElement3);

            assertTrue(new URIVerifier(URIElement0).verify(""));
            assertTrue(new URIVerifier(URIElement0).verify("AB."));
            assertTrue(new URIVerifier(URIElement0).verify("AB.AB."));
            assertFalse(new URIVerifier(URIElement0).verify("ABAB."));
            assertFalse(new URIVerifier(URIElement0).verify("AB.AB"));
        }
    }

    @Test
    public void GeneratedltestRangelRangerPlusCostrStar() throws Exception {
        {// ([AB]([CD])+\.)*
            URIElement URIElement3 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement URIElement1 = new URIElement();
            URIElement URIElement11 = new URIElement();
            URIElement URIElement4 = new URIElement();
            URIElement URIElement5 = new URIElement();
            URIElement URIElement6 = new URIElement();
            URIElement URIElement9 = new URIElement();
            URIElement URIElement7 = new URIElement();
            URIElement URIElement8 = new URIElement();
            URIElement URIElement10 = new URIElement();
            URIElement URIElement12 = new URIElement();
            URIElement URIElement2 = new URIElement();
            URIElement0.add(SKIP, URIElement1);
            URIElement1.add(SKIP, URIElement4);
            URIElement1.add(SKIP, URIElement2);
            URIElement11.add(SKIP, URIElement12);
            URIElement11.add(SKIP, URIElement2);
            URIElement4.add('A', URIElement5);
            URIElement4.add('B', URIElement5);
            URIElement5.add(SKIP, URIElement6);
            URIElement6.add(SKIP, URIElement7);
            URIElement9.add(SKIP, URIElement6);
            URIElement9.add('.', URIElement10);
            URIElement7.add('C', URIElement8);
            URIElement7.add('D', URIElement8);
            URIElement8.add(SKIP, URIElement9);
            URIElement10.add(SKIP, URIElement11);
            URIElement12.add(SKIP, URIElement1);
            URIElement2.add(SKIP, URIElement3);

            assertTrue(new URIVerifier(URIElement0).verify(""));
            assertFalse(new URIVerifier(URIElement0).verify("A."));
            assertTrue(new URIVerifier(URIElement0).verify("AC."));
        }

        {// ([ABC]+\.)*
            URIElement URIElement3 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement URIElement1 = new URIElement();
            URIElement URIElement7 = new URIElement();
            URIElement URIElement4 = new URIElement();
            URIElement URIElement5 = new URIElement();
            URIElement URIElement6 = new URIElement();
            URIElement URIElement8 = new URIElement();
            URIElement URIElement2 = new URIElement();
            URIElement0.add(SKIP, URIElement1);
            URIElement1.add(SKIP, URIElement4);
            URIElement1.add(SKIP, URIElement2);
            URIElement7.add(SKIP, URIElement8);
            URIElement7.add(SKIP, URIElement2);
            URIElement4.add('A', URIElement5);
            URIElement4.add('B', URIElement5);
            URIElement4.add('C', URIElement5);
            URIElement5.add(SKIP, URIElement4);
            URIElement5.add('.', URIElement6);
            URIElement6.add(SKIP, URIElement7);
            URIElement8.add(SKIP, URIElement1);
            URIElement2.add(SKIP, URIElement3);

            assertTrue(new URIVerifier(URIElement0).verify(""));
            assertTrue(new URIVerifier(URIElement0).verify("AC."));
            assertTrue(new URIVerifier(URIElement0).verify("ACC."));
            assertTrue(new URIVerifier(URIElement0).verify("AAAAAA."));
            assertTrue(new URIVerifier(URIElement0).verify("AAAAAA.AAAAAA."));
            assertTrue(new URIVerifier(URIElement0).verify("AAAAAA.AAAAAA.AAAAAA.AAAAAA.AAAAAA.AAAAAA."));
            assertFalse(new URIVerifier(URIElement0).verify("AAAAAA.AAAAAA"));
            assertFalse(new URIVerifier(URIElement0).verify(String.valueOf('A' - 1)));
        }

    }

    @Test
    public void GeneratedltestRangelRangerPlusCostrStarRangelRangerStar() throws Exception {
        {// ([AB]+\.)*([Aa])*
            URIElement URIElement5 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement URIElement1 = new URIElement();
            URIElement URIElement13 = new URIElement();
            URIElement URIElement10 = new URIElement();
            URIElement URIElement11 = new URIElement();
            URIElement URIElement12 = new URIElement();
            URIElement URIElement14 = new URIElement();
            URIElement URIElement2 = new URIElement();
            URIElement URIElement3 = new URIElement();
            URIElement URIElement8 = new URIElement();
            URIElement URIElement6 = new URIElement();
            URIElement URIElement7 = new URIElement();
            URIElement URIElement9 = new URIElement();
            URIElement URIElement4 = new URIElement();
            URIElement0.add(SKIP, URIElement1);
            URIElement1.add(SKIP, URIElement10);
            URIElement1.add(SKIP, URIElement2);
            URIElement13.add(SKIP, URIElement14);
            URIElement13.add(SKIP, URIElement2);
            URIElement10.add('A', URIElement11);
            URIElement10.add('B', URIElement11);
            URIElement11.add(SKIP, URIElement10);
            URIElement11.add('.', URIElement12);
            URIElement12.add(SKIP, URIElement13);
            URIElement14.add(SKIP, URIElement1);
            URIElement2.add(SKIP, URIElement3);
            URIElement3.add(SKIP, URIElement6);
            URIElement3.add(SKIP, URIElement4);
            URIElement8.add(SKIP, URIElement9);
            URIElement8.add(SKIP, URIElement4);
            URIElement6.add('A', URIElement7);
            URIElement6.add('a', URIElement7);
            URIElement7.add(SKIP, URIElement8);
            URIElement9.add(SKIP, URIElement3);
            URIElement4.add(SKIP, URIElement5);

            assertTrue(new URIVerifier(URIElement0).verify("A"));
            assertTrue(new URIVerifier(URIElement0).verify("AB.A"));
            assertTrue(new URIVerifier(URIElement0).verify("AB.A"));
            assertTrue(new URIVerifier(URIElement0).verify("AAAAAA.A"));
            assertTrue(new URIVerifier(URIElement0).verify("AAAAAA.AAAAAA.A"));
            assertTrue(new URIVerifier(URIElement0).verify("AAAAAA.AAAAAA.AAAAAAA.AAAAAA.AAAAAAA.AAAAAA.a"));
            assertTrue(new URIVerifier(URIElement0).verify("AAAAAA.AAAAAA.AAAAAAA.aa"));
            assertTrue(new URIVerifier(URIElement0).verify("AAAAAA.AAAAAA.AAAAAAA.AAAAAA.AAAAAAA.AAAAAA.Aaaaa"));
            assertTrue(new URIVerifier(URIElement0).verify("AAAAAA.AAAAAA"));
            assertFalse(new URIVerifier(URIElement0).verify(String.valueOf('A' - 1)));
        }
    }

    @Test
    public void testGeneratedPlus() throws Exception {
        {// A+
            URIElement URIElement2 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement URIElement1 = new URIElement();
            URIElement0.add('A', URIElement1);
            URIElement1.add(SKIP, URIElement0);
            URIElement1.add(SKIP, URIElement2);

            assertFalse(new URIVerifier(URIElement0).verify(""));
            assertTrue(new URIVerifier(URIElement0).verify("A"));
            assertTrue(new URIVerifier(URIElement0).verify("AA"));
            assertTrue(new URIVerifier(URIElement0).verify("AAA"));
            assertTrue(new URIVerifier(URIElement0).verify("AAAA"));
            assertFalse(new URIVerifier(URIElement0).verify("AAAA."));
        }
        {// [A]+
            URIElement URIElement3 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement URIElement1 = new URIElement();
            URIElement URIElement2 = new URIElement();
            URIElement0.add(SKIP, URIElement1);
            URIElement1.add('A', URIElement2);
            URIElement2.add(SKIP, URIElement1);
            URIElement2.add(SKIP, URIElement3);

            assertFalse(new URIVerifier(URIElement0).verify(""));
            assertTrue(new URIVerifier(URIElement0).verify("A"));
            assertTrue(new URIVerifier(URIElement0).verify("AA"));
            assertTrue(new URIVerifier(URIElement0).verify("AAA"));
            assertTrue(new URIVerifier(URIElement0).verify("AAAA"));
            assertFalse(new URIVerifier(URIElement0).verify("AAAA."));
        }
    }

    @Test
    public void testGeneratedKleeneStar() throws Exception {
        {// A*
            URIElement URIElement2 = new URIElement(true);
            URIElement URIElement0 = new URIElement();
            URIElement URIElement1 = new URIElement();
            URIElement0.add('A', URIElement1);
            URIElement0.add(SKIP, URIElement1);
            URIElement1.add('A', URIElement1);
            URIElement1.add(SKIP, URIElement2);

            assertTrue(new URIVerifier(URIElement0).verify(""));
            assertTrue(new URIVerifier(URIElement0).verify("A"));
            assertTrue(new URIVerifier(URIElement0).verify("AA"));
            assertTrue(new URIVerifier(URIElement0).verify("AAA"));
            assertTrue(new URIVerifier(URIElement0).verify("AAAA"));
            assertFalse(new URIVerifier(URIElement0).verify("AAAA."));
        }
    }

    /* ====================== Helpers ========================= */

    private Thread getThread(final String matchAgainst) {
        return new Thread() {

            @Override
            public void run() {
                status.status = Status.States.Running;
                new URIVerifier().verify(matchAgainst);
                status.status = Status.States.End;
            }
        };
    }

    private Thread getThread(final URIElement root, final String matchAgainst) {
        return new Thread() {

            @Override
            public void run() {
                status.status = Status.States.Running;
                new URIVerifier(root).verify(matchAgainst);
                status.status = Status.States.End;
            }
        };
    }

    public static class Status {

        public enum States {
            Start,
            Running,
            End
        }

        public States status = States.Start;

    }
}
