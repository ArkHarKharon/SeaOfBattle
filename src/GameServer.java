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

        // Сначала ObjectOutputStream
        obj_out = new ObjectOutputStream(clientSocket.getOutputStream());
        obj_out.flush();
        obj_in = new ObjectInputStream(clientSocket.getInputStream());

        // Затем текстовые потоки
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
        clientSocket.close();
        serverSocket.close();
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
}
