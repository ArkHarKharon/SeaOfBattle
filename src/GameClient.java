import java.io.*;
import java.net.*;

public class GameClient {
    private Socket socket;
    private ObjectInputStream obj_in;
    private ObjectOutputStream obj_out;

    public void start(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        System.out.println("Подключено к серверу!");

        // Сначала ObjectOutputStream
        obj_out = new ObjectOutputStream(socket.getOutputStream());
        obj_out.flush();

        // Затем ObjectInputStream
        obj_in = new ObjectInputStream(socket.getInputStream());
    }

    public void stop() throws IOException {
        obj_in.close();
        obj_out.close();
        socket.close();
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
