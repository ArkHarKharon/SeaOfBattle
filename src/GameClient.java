import java.io.*;
import java.net.*;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ObjectInputStream obj_in;
    private ObjectOutputStream obj_out;

    public void start(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        System.out.println("Подключено к серверу!");

        // Сначала ObjectOutputStream
        obj_out = new ObjectOutputStream(socket.getOutputStream());
        obj_out.flush(); // обязательно
        obj_in = new ObjectInputStream(socket.getInputStream());

        // Затем текстовые потоки (если они нужны)
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        obj_in.close();
        obj_out.close();
        socket.close();
    }

    public void sendArray(int[][] array) {
        try {
            obj_out.writeObject(array);
            obj_out.flush(); // желательно
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
            obj_out.flush(); // желательно
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
