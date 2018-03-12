package stac.communications;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class NameGeneratorTest {
    @Test
    public void testRandomNameGeneration() throws Exception {
        for (int i = 0; i < 50000; i++) {
            Assert.assertNotNull(NameGenerator.randomName());
        }
    }
}