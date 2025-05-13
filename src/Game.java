/**
 * импортируем библиотеку для вывода диалогового окна о победе/проигрыше
 */
import javax.swing.JOptionPane;
import java.io.IOException;
import java.util.Arrays;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Game {
    public static int playerShipArray[][];
    public static  int enemyShipArray[][];
    public static GameClient client = new GameClient();
    public static GameServer server = new GameServer();
    public static boolean isHost = false;
    public static String playerName = new String();
    public static String enemyName = new String();

    public static int[][] getPlayerShip() {
        return playerShipArray;
    }

    public static int[][] getEnemyShip() {
        return enemyShipArray;
    }

    public static void setPlayerShipArray(int[][] hostShips) {
        playerShipArray = hostShips;
    }

    public static void setEnemyShipArray(int[][] clientShips) {
        enemyShipArray = clientShips;
    }

    public void initNetwork( String ip_adress){
        String urlString = "jdbc:sqlserver://localhost:1433;databaseName=SeaOfBattle;" +
                "user=Ark;encrypt=false;trustServerCertificate=true;";
        if(isHost){
            try {
                server.start(5555);  // выбранный порт
                // Пример: отправка сообщения
                server.sendMessage("клиент подключен!");
                String response = server.receiveMessage();
                System.out.println("Получено от клиента: " + response);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        else{
            try {
                client.start(ip_adress, 5555);  // IP сервера
                String msg = client.receiveMessage();
                System.out.println("Сообщение от хоста: " + msg);
                client.sendMessage("хост подключен!");

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        try (Connection connection = DriverManager.getConnection(urlString)) {
            System.out.println("Успешное подключение к MS SQL Server");
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к MS SQL Server");
            e.printStackTrace();
        }
    }
    /**
     * Процесс игры
     * 0-игра идет 1-Выиграл игрок 2- Выиграл компьютер
     */
    public static int GameState = 3;
    /**
     * Показывает количество кораблей игрока
     */
    public int P1, P2, P3, P4;
    /**
     * Показывает количество кораблей компьютера
     */
    public int C1, C2, C3, C4;
    public int hostTurnNumber;
    public int clientTurnNumber;

    /**
     * Время паузы при выстреле компьютера в милисекндуах
     */
    public final int pause = 500;
    /**
     * Логическая переменная, true - если сейчас ход игрока
     */
    public boolean playerTurn;
    public boolean enemyTurn;
    /**
     * Все атаки компьютера будут происходить в новом потоке
     */
    Thread thread = new Thread();

    /**
     * Конструктор класса, в котором происходит инициализация
     * двумерных массивов игрока и компьютера
     */
    Game() {
        playerShipArray = new int[10][10];
        enemyShipArray = new int[10][10];
    }

    /**
     * Запуск игры
     * Происходит обнуление массивов и расстановка кораблей
     */
    public void start() throws IOException {
        initNetwork("192.168.0.109");
        clientTurnNumber = 0;
        hostTurnNumber = 0;
        //обнуляем массив
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                playerShipArray[i][j] = 0;
                enemyShipArray[i][j] = 0;
            }
        }

        playerTurn = isHost; //мой ход
        enemyTurn = !isHost;
        GameState = 0;// игра идет
        enemysDeadShips(enemyShipArray);
        playersDeadShips(playerShipArray);
        playerAutoPlacement();
        System.out.println(Arrays.deepToString(playerShipArray));
        if(isHost){
            server.sendArray(playerShipArray);
            enemyShipArray = server.getArray();
            server.sendMessage(playerName);
            enemyName = server.receiveMessage();
        }
        else{
            client.sendArray(playerShipArray);
            enemyShipArray = client.getArray();
            client.sendMessage(playerName);
            enemyName = client.receiveMessage();
        }
        if(!isHost){
            while(enemyTurn){
                try{
                    if(!processEnemyAttack()){
                        playerTurn = true;
                        enemyTurn = false;
                        break;
                    }

                } catch (Exception e) {

                }
            }
        }
    }


    public boolean processEnemyAttack(){
        System.out.println("Начинаю обрабатывать атаку!");
        if(!isHost && enemyTurn){
            System.out.println("Обработка атаки хоста!");
            int[] enemyShot = new int[2];
            enemyShot = client.getShot();
            int i = enemyShot[0];
            int j = enemyShot[1];
            System.out.println(Arrays.toString(enemyShot));
            clientTurnNumber++;
            playerShipArray[i][j] +=7;
            isPartKilled(playerShipArray,i,j);
            testEndGame();
            if(playerShipArray[i][j] >= 8){
                return true;
            }
        }
        else if(isHost && enemyTurn){
            int[] enemyShot = new int[2];
            enemyShot = server.getShot();
            int i = enemyShot[0];
            int j = enemyShot[1];

            clientTurnNumber++;
            playerShipArray[i][j] +=7;
            isPartKilled(playerShipArray,i,j);
            testEndGame();
            if(playerShipArray[i][j] >= 8){
                return true;
            }
        }
        return false;
    }

    public void attack(int mas[][], int i, int j) {
        if(isHost){
            hostTurnNumber++;
            int[] shot = new int[2];
            shot[0] = i;
            shot[1] = j;
            server.sendShot(shot);
            enemyShipArray[i][j] += 7;
            isPartKilled(enemyShipArray, i, j);
            testEndGame();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //если промах
                    if (enemyShipArray[i][j] < 8) {
                        playerTurn = false;
                        enemyTurn = true; //передаем ход компьютеру
                        // Ходит компьютер - пока попадает в цель
                        while (enemyTurn == true) {
                            try {
                                Thread.sleep(pause);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            enemyTurn = processEnemyAttack();
                            //воспроизводим звук при попадании компьютера
                        }
                        playerTurn = true;//передаем ход игроку после промаха компьютера
                    }
                }
            });
            thread.start();
        }

        else{
            hostTurnNumber++;
            int[] shot = new int[2];
            shot[0] = i;
            shot[1] = j;
            client.sendShot(shot);
            enemyShipArray[i][j] += 7;
            isPartKilled(enemyShipArray, i, j);
            testEndGame();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //если промах
                    if (enemyShipArray[i][j] < 8) {
                        playerTurn = false;
                        enemyTurn = true; //передаем ход компьютеру
                        // Ходит компьютер - пока попадает в цель
                        while (enemyTurn == true) {
                            try {
                                Thread.sleep(pause);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            enemyTurn = processEnemyAttack();
                            //воспроизводим звук при попадании компьютера
                        }
                        playerTurn = true;//передаем ход игроку после промаха компьютера
                    }
                }
            });
            thread.start();
        }
    }



    /**
     * Проверка конца игры
     * Сумма массива, когда все корабли убиты, равна 15*4+16*2*3+17*3*2+18*4 = 330
     * Суммируем элементы массива, и если сумма равна 330, то заканчиваем игру
     */
    public void testEndGame() {
        if (GameState == 0) {
            int endScore = 330; //когда все корабли убиты
            int playerScore = 0; // Сумма убитых палуб игрока
            int enemyScore = 0; // Сумма убитых палуб компьютера
            enemysDeadShips(enemyShipArray);
            playersDeadShips(playerShipArray);
            if (GameState == 0) {
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        // Суммируем подбитые палубы
                        if (playerShipArray[i][j] >= 15) playerScore += playerShipArray[i][j];
                        if (enemyShipArray[i][j] >= 15) enemyScore += enemyShipArray[i][j];
                    }
                }

                if (playerScore == endScore) {
                    GameState = 2;
                    System.out.println("Пытаюсь вывести окно поражения");
                    //выводим диалоговое окно игроку
                    JOptionPane.showMessageDialog(null,
                            "Вы проиграли! Попробуйте еще раз",
                            "Вы проиграли", JOptionPane.INFORMATION_MESSAGE);

                }
                if (enemyScore == endScore) {
                    GameState = 1;
                    //выводим диалоговое окно игроку
                    JOptionPane.showMessageDialog(null,
                            "Поздравляю! Вы выиграли!",
                            "Вы выиграли", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    /**
     * Подсчитываем количество убитых кораблей компьютера
     *
     * @param mas
     */
    public void enemysDeadShips(int[][] mas) {
        P4 = 0;
        P3 = 0;
        P2 = 0;
        P1 = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (mas[i][j] == 18) P4 = (P4 + 1);
                if (mas[i][j] == 17) P3 = (P3 + 1);
                if (mas[i][j] == 16) P2 = (P2 + 1);
                if (mas[i][j] == 15) P1 = (P1 + 1);
            }
        }
        P4 /= 4;
        P3 /= 3;
        P2 /= 2;
    }

    /**
     * Считаем убитые корабли игрока
     *
     * @param mas
     */
    public void playersDeadShips(int[][] mas) {
        C4 = 0;
        C3 = 0;
        C2 = 0;
        C1 = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (mas[i][j] == 18) C4 = (C4 + 1);
                if (mas[i][j] == 17) C3 = (C3 + 1);
                if (mas[i][j] == 16) C2 = (C2 + 1);
                if (mas[i][j] == 15) C1 = (C1 + 1);
            }
        }
        C4 /= 4;
        C3 /= 3;
        C2 /= 2;
    }

    /**
     * Метод проверяет убита ли палуба
     * Данный метод вызывает метод analizUbit() для разных видов кораблей.
     *
     * @param mas массив для теста
     * @param i
     * @param j
     */
    private void isPartKilled(int mas[][], int i, int j) {
        if (mas[i][j] == 8) { //Если однопалубный
            mas[i][j] += 7; //прибавляем к убитому +7
            setKilledShipSpaceDecr(mas, i, j);//Уменьшаем окружение убитого на 1
        } else if (mas[i][j] == 9) {
            isShipKilled(mas, i, j, 2);
        } else if (mas[i][j] == 10) {
            isShipKilled(mas, i, j, 3);
        } else if (mas[i][j] == 11) {
            isShipKilled(mas, i, j, 4);
        }
    }

    /**
     * Анализ, убит ли корабль
     *
     * @param mas         двумерный массив
     * @param i
     * @param j
     * @param partsNumber количество палуб
     */
    private void isShipKilled(int[][] mas, int i, int j, int partsNumber) {
        //Количество раненых палуб
        int partsKilledNumber = 0;
        //Выполняем подсчет раненых палуб
        for (int x = i - (partsNumber - 1); x <= i + (partsNumber - 1); x++) {
            for (int y = j - (partsNumber - 1); y <= j + (partsNumber - 1); y++) {
                // Если это палуба раненого корабля
                if (testArrayOut(x, y) && (mas[x][y] == partsNumber + 7)) partsKilledNumber++;
            }
        }
        // Если количество раненых палуб совпадает с количеством палуб
        // корабля, то он убит - прибавляем число 7
        if (partsKilledNumber == partsNumber) {
            for (int x = i - (partsNumber - 1); x <= i + (partsNumber - 1); x++) {
                for (int y = j - (partsNumber - 1); y <= j + (partsNumber - 1); y++) {
                    // Если это палуба раненого корабля
                    if (testArrayOut(x, y) && (mas[x][y] == partsNumber + 7)) {
                        // помечаем палубой убитого корабля
                        mas[x][y] += 7;
                        // уменьшаем на 1 окружение убитого корабля
                        setKilledShipSpaceDecr(mas, x, y);
                    }
                }
            }
        }
    }


    /**
     * Метод уменьшает на 1 значение массива, если значения равны -1 или 6
     *
     * @param mas двумерный массив
     * @param i
     * @param j
     */
    public void setOkrKilled(int mas[][], int i, int j) {
        if (testArrayOut(i, j)) {
            if (mas[i][j] == -1 || mas[i][j] == 6) {
                mas[i][j]--;
            }
        }
    }

    /**
     * Сам метод, который уменьшает на 1 окружение всего убитого корабля
     *
     * @param mas двумерный массив
     * @param i
     * @param j
     */
    private void setKilledShipSpaceDecr(int[][] mas, int i, int j) {
        setOkrKilled(mas, i - 1, j - 1); // сверху слева
        setOkrKilled(mas, i - 1, j); // сверху
        setOkrKilled(mas, i - 1, j + 1); // сверху справа
        setOkrKilled(mas, i, j + 1); // справа
        setOkrKilled(mas, i + 1, j + 1); // снизу справа
        setOkrKilled(mas, i + 1, j); // снизу
        setOkrKilled(mas, i + 1, j - 1); // снизу слева
        setOkrKilled(mas, i, j - 1); // слева
    }

    /**
     * проверка выхода за пределы массива
     *
     * @param i строка
     * @param j столбец
     * @return если выхода за пределы массива нет, то true
     */
    private boolean testArrayOut(int i, int j) {
        if (((i >= 0) && (i <= 9)) && ((j >= 0) && (j <= 9))) {
            return true;
        } else return false;
    }

    /**
     * Вспомогательный метод для устаноки окружения корабля
     * Контролирует выход за пределы массива
     *
     * @param mas двумерный массив
     * @param i
     * @param j
     * @param val значение
     */
    private void setOkr(int[][] mas, int i, int j, int val) {
        if (testArrayOut(i, j) && mas[i][j] == 0) {
            mas[i][j] = val;
        }
    }

    /**
     * перебирает все ячейки вокруг и устанавливает в них нужное значение.
     * Если происходит выход за границы массива – эта ситуация контролируется в методе setOkr().
     *
     * @param mas двумерный массив
     * @param i
     * @param j
     * @param val значение, которое нужно установить
     */
    private void okrBegin(int[][] mas, int i, int j, int val) {
        setOkr(mas, i - 1, j - 1, val);
        setOkr(mas, i - 1, j, val);
        setOkr(mas, i - 1, j + 1, val);
        setOkr(mas, i, j + 1, val);
        setOkr(mas, i, j - 1, val);
        setOkr(mas, i + 1, j + 1, val);
        setOkr(mas, i + 1, j, val);
        setOkr(mas, i + 1, j - 1, val);
    }


    /**
     * Этот метод выполняет проверку выхода за границы массива и значение в ячейке,
     * в которой мы хотим разместить новую палубу.
     * Если это значения 0 или -2, то мы можем разместить там новую палубу.
     *
     * @param mas
     * @param i
     * @param j
     * @return
     */
    private boolean testNewPartPlacement(int[][] mas, int i, int j) {
        if (testArrayOut(i, j) == false) return false;
        if ((mas[i][j] == 0) || (mas[i][j] == -2)) return true;
        else return false;
    }

    /**
     * Пробегаемся по всему массиву
     * Если значение элемента массива -2, то заменяем его на -1
     *
     * @param mas
     */
    private void okrEnd(int[][] mas) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (mas[i][j] == -2)
                    mas[i][j] = -1;
            }
        }
    }


    /**
     * Генерация палуб
     * Выбираем случайное направление построения корабля. 0 - вверх, 1 - вправо, 2 - вниз, 3 - влево
     *
     * @param mas двумерный массив
     */
    private void partAutoPlacement(int[][] mas, int kolPal) {
        int i = 0, j = 0;
        while (true) {
            boolean flag = false;
            i = (int) (Math.random() * 10);
            j = (int) (Math.random() * 10);
            int napr = (int) (Math.random() * 4); // 0 - вверх. 1 - вправо. 2 - вниз. 3 - влево

            // Если можно расположить палубу
            if (testNewPartPlacement(mas, i, j) == true) {
                if (napr == 0) { //вверх
                    if (testNewPartPlacement(mas, i - (kolPal - 1), j) == true)  //если можно расположить палубу вверх, то flag = true
                        flag = true;
                } else if (napr == 1) { // вправо
                    if (testNewPartPlacement(mas, i, j + (kolPal - 1)) == true)
                        flag = true;
                } else if (napr == 2) { // вниз
                    if (testNewPartPlacement(mas, i + (kolPal - 1), j) == true)
                        flag = true;
                } else if (napr == 3) { // влево
                    if (testNewPartPlacement(mas, i, j - (kolPal - 1)) == true)
                        flag = true;
                }
            }
            if (flag == true) {
                //Помещаем в ячейку число палуб
                mas[i][j] = kolPal;
                // Окружаем минус двойками
                okrBegin(mas, i, j, -2);
                if (napr == 0) {// вверх
                    for (int k = kolPal - 1; k >= 1; k--) {
                        mas[i - k][j] = kolPal;
                        okrBegin(mas, i - k, j, -2);
                    }
                } else if (napr == 1) { // вправо
                    for (int k = kolPal - 1; k >= 1; k--) {
                        mas[i][j + k] = kolPal;
                        okrBegin(mas, i, j + k, -2);
                    }
                } else if (napr == 2) { // вниз
                    for (int k = kolPal - 1; k >= 1; k--) {
                        mas[i + k][j] = kolPal;
                        okrBegin(mas, i + k, j, -2);
                    }
                } else if (napr == 3) { // влево
                    for (int k = kolPal - 1; k >= 1; k--) {
                        mas[i][j - k] = kolPal;
                        okrBegin(mas, i, j - k, -2);
                    }
                }
                //прерываем цикл
                break;
            }
        }
        okrEnd(mas); //заменяем -2 на -1
    }

    /**
     * Метод для расстаноки всех кораблей для игрока
     */
    public void playerAutoPlacement() {
        partAutoPlacement(playerShipArray, 4);
        for (int i = 1; i <= 2; i++) {
            partAutoPlacement(playerShipArray, 3);
        }
        for (int i = 1; i <= 3; i++) {
            partAutoPlacement(playerShipArray, 2);
        }
        for (int i = 1; i <= 4; i++) {
            partAutoPlacement(playerShipArray, 1);
        }
    }

    boolean enemyTurn(int[][] shiArr){
        if(isHost){
            if(playerTurn){
                return true;
            }
            else return false;
        }
        else if (!isHost) {
            if(enemyTurn){
                return true;
            }
            else return false;
        }
        else return false;
    }

}
