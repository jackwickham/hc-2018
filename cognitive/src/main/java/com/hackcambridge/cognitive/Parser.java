package com.hackcambridge.cognitive;

import java.nio.Buffer;

public class Parser {

    public static class ExtractedData {
        public String merchant;
        public int totalValue;
        public ExtractedData(String m, int v) {
            merchant = m;
            totalValue = v;
        }
    }

    public static ExtractedData parse(Buffer buffer) {
        return new ExtractedData("Sainsbury's", 2260);
    }

}
