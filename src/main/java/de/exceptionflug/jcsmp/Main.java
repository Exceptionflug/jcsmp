package de.exceptionflug.jcsmp;

import java.io.File;

public class Main {

    public static void main(final String[] args) throws Exception {
        CSMPFile.readFile(new File(args[0]));
    }

}
