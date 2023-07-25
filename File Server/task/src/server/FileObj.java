package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileObj extends File {
    private int identifier;

    public FileObj(String pathname) {
        super(pathname);
    }

    public boolean deleteFile(String pathname) {
        return new File(pathname).delete();
    }

    public static void writeToFile(String filename, String text) {
        try (FileWriter fileWriter = new FileWriter(filename)) {
            fileWriter.write(text);
            // Flush and close the FileWriter to ensure data is written to the file
            fileWriter.flush();
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }
}