package client;

import server.FileObj;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 58543;
//    private static final String CLIENT_DIRECTORY_PATH = System.getProperty("user.dir") // local test
//        + File.separator + "File Server" + File.separator + "task" + File.separator + "src"
//        + File.separator + "client" + File.separator + "data" + File.separator;
    private static final String CLIENT_DIRECTORY_PATH = System.getProperty("user.dir") + File.separator + "src" // unit tests
            + File.separator + "client" + File.separator + "data" + File.separator;

    public static void main(String[] args) throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        System.out.println("Client started!");
        try (
                Socket clientSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
        ) {
                System.out.print("Enter action (1 - get a file, 2 - create a file, 3 - delete a file): ");
                String optionChosen = scanner.next();
                switch (optionChosen) {
                    case "1"    -> getFileRequest(input, output);
                    case "2"    -> createFileRequestBeta(input, output);
                    case "3"    -> deleteFileRequest(input, output);
                    case "exit" -> exitRequest(output);
                    default     -> System.out.println("Not an action!");
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createFileRequestBeta(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Enter name of the file: ");
        String clientFilename = scanner.next();
        System.out.print("\nEnter name of the file to be saved on the server: ");
        scanner.nextLine();
        String serverFilename = scanner.nextLine();
        output.writeUTF("PUT");
        output.writeUTF(serverFilename);
        FileObj file = new FileObj(CLIENT_DIRECTORY_PATH + clientFilename);
        if (file.exists()) {
            byte[] message = Files.readAllBytes(file.toPath());
            output.writeInt(message.length); // write length of the message
            output.write(message);           // write the message
        }
        System.out.println("The request was sent.");
        int statusCode = input.readInt();
        if (statusCode == 200) {
            System.out.println("Response says that file is saved! ID = " + input.readInt());
        } else {
            System.out.println("Response says that creating the file was forbidden!");
        }
    }

    private static void getFileRequest(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): ");
        int option = scanner.nextInt();
        System.out.print("Enter " + (option == 1 ? "filename: " : "id: "));
        String identifier = scanner.next();
        output.writeUTF("GET " + (option == 1 ? "BY_NAME" : "BY_ID"));
        output.writeUTF(identifier);
        System.out.println("The request was sent.");

        int statusCode = input.readInt();
        if (statusCode == 200) {
            serverFileToClientFile(input);
        } else {
            System.out.println("The response says that this file is not found!");
        }
    }

    private static void deleteFileRequest(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Do you want to delete the file by name or by id (1 - name, 2 - id): ");
        int option = scanner.nextInt();
        System.out.print("Enter " + (option == 1 ? "filename: " : "id: "));
        String identifier = scanner.next();
        output.writeUTF("DELETE " + (option == 1 ? "BY_NAME" : "BY_ID"));
        output.writeUTF(identifier);
        System.out.println("The request was sent.");

        int statusCode = input.readInt();
        if (statusCode == 200) {
            System.out.println("The response says that this file was deleted successfully!");
        } else {
            System.out.println("The response says that this file is not found!");
        }
    }

    private static void exitRequest(DataOutputStream output) throws IOException {
        output.writeUTF("EXIT");
        System.out.println("The request was sent.");
    }

    public static void serverFileToClientFile(DataInputStream input) throws IOException {
        int length = input.readInt();                // read length of incoming message
        byte[] message = new byte[length];
        input.readFully(message, 0, message.length); // read the message
        System.out.print("The file was downloaded! Specify a name for it: ");
        String filename = scanner.next();
        try (FileOutputStream fileOutputStream = new FileOutputStream(CLIENT_DIRECTORY_PATH + filename)) {
            fileOutputStream.write(message);
            System.out.println("File saved on the hard drive!");
        }
    }
}