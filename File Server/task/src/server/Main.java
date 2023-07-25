package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static final int PORT = 58543;
    static final Random random = new Random();

//    private static final String SERVER_DIRECTORY_PATH = System.getProperty("user.dir") // local test
//        + File.separator + "File Server" + File.separator + "task" + File.separator + "src"
//        + File.separator + "server" + File.separator + "data" + File.separator;
    private static final String SERVER_DIRECTORY_PATH = System.getProperty("user.dir") + File.separator + "src" // unit tests
            + File.separator + "server" + File.separator + "data" + File.separator;
//
//    private static final String METADATA_PATH = System.getProperty("user.dir") // local test
//            + File.separator + "File Server" + File.separator + "task" + File.separator + "src"
//            + File.separator + "client" + File.separator + "metadata" + File.separator + "metadata.txt";
    private static final String METADATA_PATH = System.getProperty("user.dir") + File.separator + "src" // unit tests
            + File.separator + "client" + File.separator + "metadata" + File.separator + "metadata.txt";
    static ConcurrentHashMap<String, String> metadataMap = loadPropertiesFromFile(METADATA_PATH);

    public static void main(String[] args) {
        System.out.println("Server started!");
        while (true) {
            try (ServerSocket server = new ServerSocket(PORT)) {
                try (
                        Socket socket = server.accept();
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                ) {
                        String[] request = input.readUTF().strip().split(" ");
                        String requestType = request[0];
                        String byParam = "";
                        if (request.length > 1) byParam = request[1];
                        switch (requestType) {
                            case "GET"      -> getFile(input, output, byParam);
                            case "PUT"      -> addFile(input, output);
                            case "DELETE"   -> deleteFile(input, output, byParam);
                            case "EXIT"     -> exitProgram();
                        }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addFile(DataInputStream input, DataOutputStream output) throws IOException {
        String serverFilename = input.readUTF();
        if (serverFilename.isBlank()) serverFilename = generateRandomString(10) + ".txt";
        FileObj file = new FileObj(SERVER_DIRECTORY_PATH + serverFilename);
        if (!file.exists()) {
            System.out.println();
            file.setIdentifier(random.nextInt(999) + 1);
            while (metadataMap.containsKey(String.valueOf(file.getIdentifier()))) {
                file.setIdentifier(random.nextInt(999) + 1);
            }
            metadataMap.put(String.valueOf(file.getIdentifier()), SERVER_DIRECTORY_PATH + serverFilename);
            savePropertiesToFile(metadataMap, METADATA_PATH);
            clientFileToServerFile(input, file);
            output.writeInt(200);
            output.writeInt(file.getIdentifier());
        } else {
            output.writeInt(403);
        }
    }

    private static void getFile(DataInputStream input, DataOutputStream output, String byParam) throws IOException {
        String identifier = input.readUTF();
        FileObj file = null;
        if (byParam.equals("BY_NAME")) {
            identifier = SERVER_DIRECTORY_PATH + identifier;
        } else {
            identifier = metadataMap.get(identifier);
        }
        if (identifier != null) file = new FileObj(identifier);
        if (file != null && file.exists()) {
            output.writeInt(200);
            byte[] message = Files.readAllBytes(file.toPath());
            output.writeInt(message.length); // write length of the message
            output.write(message);           // write the message
        } else {
            output.writeInt(404);
        }
    }

    private static void deleteFile(DataInputStream input, DataOutputStream output, String byParam) throws IOException {
        String identifier = input.readUTF();
        FileObj file = null;
        if (byParam.equals("BY_NAME")) {
            identifier = SERVER_DIRECTORY_PATH + identifier;
        } else {
            identifier = metadataMap.get(identifier);
        }
        if (identifier != null) file = new FileObj(identifier);
        if (file != null && file.exists()) {
            file.deleteFile(identifier);
            output.writeInt(200);
        } else {
            output.writeInt(404);
        }
    }

    private static void exitProgram() {
        System.exit(0);
    }

    public static void clientFileToServerFile(DataInputStream input, FileObj file) throws IOException {
        int length = input.readInt();                // read length of incoming message
        byte[] message = new byte[length];
        input.readFully(message, 0, message.length); // read the message
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(message);
        }
    }

    public static String generateRandomString(int maxLength) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        int length = random.nextInt(maxLength) + 1; // Generate a random length between 1 and maxLength (inclusive)
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    public static ConcurrentHashMap<String, String> loadPropertiesFromFile(String filePath) {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

        try (InputStream input = new FileInputStream(filePath)) {
            Properties properties = new Properties();
            properties.load(input);

            // Copy the properties to the ConcurrentHashMap
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                map.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    public static void savePropertiesToFile(ConcurrentHashMap<String, String> map, String filePath) {
        try (OutputStream output = new FileOutputStream(filePath)) {
            Properties properties = new Properties();

            // Copy the ConcurrentHashMap data to Properties
            for (String key : map.keySet()) {
                String value = map.get(key);
                properties.setProperty(key, value);
            }

            properties.store(output, "Meta Data");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}