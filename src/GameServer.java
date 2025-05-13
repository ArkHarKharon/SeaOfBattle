import java.io.*;
import java.net.*;

public class GameServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectInputStream obj_in;
    private ObjectOutputStream obj_out;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Сервер запущен, ожидание подключения...");

        clientSocket = serverSocket.accept();
        System.out.println("Клиент подключён: " + clientSocket.getInetAddress());

        // Сначала создаём ObjectOutputStream
        obj_out = new ObjectOutputStream(clientSocket.getOutputStream());
        obj_out.flush();

        // Затем ObjectInputStream
        obj_in = new ObjectInputStream(clientSocket.getInputStream());
    }

    public void stop() throws IOException {
        obj_in.close();
        obj_out.close();
        clientSocket.close();
        serverSocket.close();
    }

    public void sendMessage(String msg) {
        try {
            obj_out.writeObject(msg);
            obj_out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receiveMessage() {
        try {
            return (String) obj_in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendArray(int[][] array) {
        try {
            obj_out.writeObject(array);
            obj_out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[][] getArray() {
        try {
            return (int[][]) obj_in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendShot(int[] array) {
        try {
            obj_out.writeObject(array);
            obj_out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[] getShot() {
        try {
            return (int[]) obj_in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
