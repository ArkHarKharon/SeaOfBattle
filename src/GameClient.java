import java.io.*;
import java.net.*;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ObjectInput obj_in;
    private ObjectOutput obj_out;

    public void start(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        System.out.println("Подключено к серверу!");

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
        socket.close();
    }

    public void sendArray(int[][] array) {
        try {
            obj_out.writeObject(array);
        }
        catch (IOException e) {}
    }

    public int[][] getArray() {
        try {
            int[][] arr = (int[][]) obj_in.readObject();
            return arr;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
