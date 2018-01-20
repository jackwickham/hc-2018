package com.hackcambridge.cognitive;

import java.nio.ByteBuffer;

public class Parser {

    public static class ExtractedData {
        public String merchant;
        public int totalValue;
        public ExtractedData(String m, int v) {
            merchant = m;
            totalValue = v;
        }
    }

    public static ExtractedData parse(ByteBuffer buffer) {
        return new ExtractedData("Sainsbury's", 2260);
    }

}
