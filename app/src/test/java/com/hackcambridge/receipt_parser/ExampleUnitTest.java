package com.hackcambridge.receipt_parser;

import com.hackcambridge.cognitive.Endpoint;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
	@Test
	public void fileTest() {
		File f = new File("C:/...");
		Endpoint e = new Endpoint();
		//System.out.println(e.post(f).toString(4));
	}
}