import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private static Set<ClientHandler> clients = new HashSet<>();

    public static void main(String[] args) {
        int port = 1234;

        System.out.println("Chat Server started on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);

                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client disconnected");
    }
}

class ClientHandler implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Enter your name:");
            clientName = in.readLine();
            System.out.println(clientName + " joined");

            ChatServer.broadcast(clientName + " joined the chat", this);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(clientName + ": " + message);
                ChatServer.broadcast(clientName + ": " + message, this);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ChatServer.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void sendMessage(String message) {
        out.println(message);
    }
}
