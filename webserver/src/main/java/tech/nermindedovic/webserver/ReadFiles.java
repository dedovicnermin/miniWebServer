package tech.nermindedovic.webserver;

import java.io.File;

public class ReadFiles {
    public static void main(String[] args) {
        String fileDir;         // create a file obj for your root dir

        // For UNIX:
        File file = new File("./");

        // get all the files and directory under your directory
        File[] strFilesDir = file.listFiles();

        assert strFilesDir != null;
        for (File value : strFilesDir) {
            if (value.isDirectory()) {
                System.out.println("Directory: " + value);
            } else if (value.isFile()) {
                System.out.println("File: " + value + " (" + strFilesDir.length + ")");
            }
        }
    }
}
