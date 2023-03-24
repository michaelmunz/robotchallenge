package thu.robots.gui;



import thu.robots.components.*;
import thu.robots.internal.RobotLoader;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChallengeGUI extends JFrame implements ISensorDataObserver {

    private final CountdownGUI countdownWindow;
    private File[] environmentFiles = null;

    private JPanel pDrawPanel;
    private JPanel pInfoPanel;
    private JPanel pRootPanel;
    private JButton bLoadRobots;
    private JButton bLoadEnvironments;
    private JButton bStart;
    private JList listRobots;
    private JLabel lTimer;
    private JLabel lLastWinner;
    private JPanel pButtonPanel;
    private JLabel lLevel;

    private File environmentFile;
    private EnvironmentObject collisionObj = null;

    private ChallengeController controller = null;
    private List<SensorData> lastSensorData = new LinkedList<>();
    private Object sensorDataLock = new Object();
    private RobotListElement lastWinner;


    @Override
    public void onSensorMeasurement(SensorData data) {
        //do nothing
    }
    public ChallengeGUI() {
        setTitle("Robot Challenge");
        setResizable(false);
        pDrawPanel.setPreferredSize(new Dimension(1024, 768));
        setContentPane(pRootPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);

        countdownWindow = new CountdownGUI();
        


        controller = new ChallengeController(this);

        bLoadRobots.addActionListener(new ActionListener() {
            @Override

            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int returnVal = fc.showOpenDialog(ChallengeGUI.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();

                    removeAllRobots();
                    loadRobotsFromDirectory(file);

                }
            }
        });



        bLoadEnvironments.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
                fc.setMultiSelectionEnabled(true);
                fc.addChoosableFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return (f.getName().endsWith(".env"));
                    }

                    @Override
                    public String getDescription() {
                        return "Environment files (*.env)";
                    }
                });
                int returnVal = fc.showOpenDialog(ChallengeGUI.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] files = fc.getSelectedFiles();
                    setEnvironmentFiles(files);

                }
            }
        });
        bStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadNextLevel();
            }
        });

        //Für schnelleres Testen...
        //File robotsDirectory = new File("out\\artifacts\\Robot_jar");
        //File robotsDirectory = new File("U:\\Lehre\\SOTE2\\Semester\\2018WS\\Projekt\\Robots\\MT");
        //File robotsDirectory = new File("U:\\Lehre\\SOTE2\\Semester\\2018WS\\Projekt\\Robots\\MC");
        //File robotsDirectory = new File("U:\\Lehre\\SOTE2\\Semester\\2018WS\\Projekt\\Robots");
        //File robotsDirectory = new File("C:\\Users\\micha\\Desktop\\Projekt Robots\\SOEN-Wettbewerb Abgabe der Robot.jar-40543");
        //loadRobotsFromDirectory(robotsDirectory);
        //File[] files = new File[]{new File("C:\\Users\\micha\\Desktop\\Projekt Robots\\levels\\L1.env"), new File("C:\\Users\\micha\\Desktop\\Projekt Robots\\levels\\L2.env"), new File("C:\\Users\\micha\\Desktop\\Projekt Robots\\levels\\L3.env"), new File("C:\\Users\\micha\\Desktop\\Projekt Robots\\levels\\L4.env")};
        //File[] files = new File[]{new File("C:\\Users\\micha\\Desktop\\Projekt Robots\\levels\\L3.env"), new File("C:\\Users\\micha\\Desktop\\Projekt Robots\\levels\\L4.env")};
        //setEnvironmentFiles(files);



    }

    private void removeAllRobots() {
        DefaultListModel<RobotListElement> robotsModel = (DefaultListModel<RobotListElement>) listRobots.getModel();
        robotsModel.removeAllElements();
        controller.removeAllRobots();

    }

    private void setEnvironmentFiles(File[] files) {
        environmentFiles = files;
        loadEnvironment(environmentFiles[0]);
    }

    private void loadNextLevel() {
        int currentLevel = controller.getCurrentLevel()+1;
        loadEnvironment(environmentFiles[currentLevel-1]);

        lastWinner = null;

        lLevel.setText("Level "+currentLevel+" von "+this.environmentFiles.length);
        lTimer.setText("-");

        countdownWindow.setString("Level "+currentLevel+" von "+this.environmentFiles.length);
        countdownWindow.updateTime(3);
        countdownWindow.setVisible(true);

        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            private int currentTimeSeconds = 3;
            @Override
            public void run() {

                this.currentTimeSeconds -= 1;
                countdownWindow.updateTime(this.currentTimeSeconds);

                if(this.currentTimeSeconds==0){
                    countdownWindow.setVisible(false);
                    timer.cancel();
                    controller.onNextLevel();
                }
                else{

                }


            }
        }, 1000, 1000);






    }

    private boolean loadEnvironment(File file){
        Environment env = null;
        try {
            env = Environment.loadFromFile(file);
        }
        catch(IllegalArgumentException ex){
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Fehler beim Laden des Environments", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        controller.setEnvironment(env);

        pDrawPanel.setPreferredSize(new Dimension(env.getWidth(), env.getHeight()));

        return true;
    }

    private void loadRobotsFromDirectory(File file) {

        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jar"))||new File(dir, name).isDirectory();
            }
        });

        if(files==null){
            JOptionPane.showMessageDialog(this, "Kann keine Robots aus dem Verzeichnis '" + file + "' laden.", "Fehler beim Laden", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultListModel<RobotListElement> robotsModel = (DefaultListModel<RobotListElement>) listRobots.getModel();

        for(File f : files){
            if(f.isDirectory()){
                loadRobotsFromDirectory(f);
            }
            else {
                try {
                    if(!f.getName().endsWith(".jar")){
                        continue;
                    }
                    BaseRobot robot = RobotLoader.loadRobotFromJar(f);
                    if(robot!=null) {
                        robot.setAutonomousStearing(true);
                        robot.setInitialPose(15 + robot.getLength() / 2, pDrawPanel.getHeight() / 2, 0);

                        RobotListElement le = new RobotListElement(robot, RobotListElement.RobotState.Running, f);
                        controller.addRobot(le);
                        robotsModel.addElement(le);
                    }
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Fehler beim Laden des Jar-Files '" + file + "': " + ex.getMessage(), "Fehler beim Laden", JOptionPane.ERROR_MESSAGE);
                }
                catch (Throwable ex) {
                    JOptionPane.showMessageDialog(this, "Erweiterter Fehler beim Laden des Jar-Files '" + file + "': " + ex.getMessage(), "Fehler beim Laden", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }


    @Override
    public boolean isFocusable() {
        return true;
    }





    private void createUIComponents() {
        listRobots = new JList(new DefaultListModel<RobotListElement>());

        listRobots.setCellRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            RobotListElement.RobotState state = ((RobotListElement) value).getState();
            if(state == RobotListElement.RobotState.Collided){
                c.setBackground(Color.RED);
            }
            else if(state == RobotListElement.RobotState.Finished){
                c.setBackground(Color.GREEN);
            }
            else if(state == RobotListElement.RobotState.Timeout){
                c.setBackground(Color.YELLOW);
            }

            return c;
        }});


        pDrawPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
            super.paintComponent(g);
            List<RobotListElement> robots = controller.getRobots();
            for(RobotListElement le:robots) {
                int level = le.getLevel();
                if(level !=-1 && level!= controller.getCurrentLevel()){
                    continue;

                }
                Color color = Color.RED;
                Graphics2D g2d =(Graphics2D)g;
                // draw robot
                Visualisation.drawRobot(g2d, le.getRobot(), color);

                // draw environment
                Environment environment = controller.getEnvironment();
                Visualisation.drawEnvironment(g2d, environment);
            }
            }
        };
    }



    public void onCollision(RobotListElement le, EnvironmentObject obj) {
        //do nothing
    }


    public void onTargetZoneReached(RobotListElement le) {
        //JOptionPane.showMessageDialog(this, "Roboter '"+robot.getName()+"' hat den Zielbereich erreicht! Herzlichen Glückwunsch!", "Zielbereich erreicht", JOptionPane.INFORMATION_MESSAGE);
        if(le.isFirst()){
            lLastWinner.setText("Letzter Gewinner: " + le.toString());
            lastWinner = le;
        }
    }

    public void onNoMoreRobots() {
        if(environmentFiles.length > controller.getCurrentLevel()) {
            int result = JOptionPane.showConfirmDialog(this, "Das Level " + controller.getCurrentLevel() + " ist beendet. Nächstes Level laden?", "Level beendet", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                loadNextLevel();
            }
        }
        else{
            String lastWinnerStr = "keiner";
            if(lastWinner != null){
                lastWinnerStr = lastWinner.toString();
            }
            JOptionPane.showMessageDialog(this, "Challenge beendet. Gewonnen hat: "+lastWinnerStr+". Herzlichen Glückwunsch!", "Challenge beendet", JOptionPane.INFORMATION_MESSAGE);
        }

    }


    public void onTimeout(RobotListElement le) {
        //do nothing

    }

    public void onTimeoutTick(int remainingSeconds){
        lTimer.setText(""+remainingSeconds);
    }
}
