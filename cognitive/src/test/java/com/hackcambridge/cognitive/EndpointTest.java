package com.hackcambridge.cognitive;

import org.junit.Test;

import java.io.File;

public class EndpointTest {
    @Test
    public void rest_localTest() throws Exception {
        File f = new File("C:/Users/ajbon/CloudStation/Uni work/Part IB/Other/Hackathons/Receipt1.jpg");
        Endpoint caller = new Endpoint();
        caller.post(f);
    }
}