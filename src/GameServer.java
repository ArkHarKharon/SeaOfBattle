import java.io.*;
import java.net.*;

public class GameServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private ObjectInputStream obj_in;
    private ObjectOutputStream obj_out;
    private BufferedReader in;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Сервер запущен, ожидание подключения...");
        clientSocket = serverSocket.accept();
        System.out.println("Клиент подключён!");

        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        obj_in = new ObjectInputStream(clientSocket.getInputStream());
        obj_out = new ObjectOutputStream(clientSocket.getOutputStream());
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
        clientSocket.close();
        serverSocket.close();
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
