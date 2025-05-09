import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Frame extends JFrame {

    private JMenuBar menuBar;
    private JMenu menuGameStart;
    private JMenuItem itemStartAuto;
    private JMenuItem itemExit;
    private JMenu networkMenu;
    private JMenuItem hostRole;
    private JMenuItem clientRole;


    Frame() {
        super("Sea Of Battle");
        Panel pole=new Panel();
        menuBar=new JMenuBar();
        networkMenu = new JMenu("Сеть");
        hostRole= new JMenuItem("Стать хостом");
        clientRole = new JMenuItem("Подключиться");
        menuGameStart = new JMenu("Новая игра");
        itemStartAuto =new JMenuItem("Расставить корабли");
        itemStartAuto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pole.start();
            }
        });


        itemExit=new JMenuItem("Выход");
        itemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pole.exit();
            }
        });


        hostRole.addActionListener(e -> {
            Game.isHost = true;
            pole.start();
        });

        clientRole.addActionListener(e -> {
            Game.isHost = false;
            pole.start();
        });


        menuGameStart.add(itemStartAuto);
        menuBar.add(networkMenu);
        networkMenu.add(hostRole);
        networkMenu.add(clientRole);
        setJMenuBar(menuBar);
        Container container=getContentPane();
        container.add(pole);
        setSize(pole.getSize());
        setResizable(false);
        //setLayout(null); //возможность произвольной расстановки
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        try {
            setIconImage(ImageIO.read(getClass().getResource("image/icon.jpeg")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setVisible(true);


    }

}

