package net.computerpoint.wrapper;

import java.io.FileOutputStream;
import java.io.IOException;

public class TestyTest {

	public static void main(String[] args) {
		SpiffyWrapper sw = new SpiffyWrapper();
		try {
			sw.squeeze(null, new FileOutputStream("/tmp/foobar"));
		} catch (IOException e) {
		}
	}
}
