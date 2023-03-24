package thu.robots.gui;

import thu.robots.components.*;
import thu.robots.internal.Controller;
import thu.robots.internal.RobotLoader;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ControlGUI extends JFrame implements ISensorDataObserver {
    private JPanel pDrawPanel;
    private JPanel pInfoPanel;
    private JTextArea taInfo;
    private JTextArea taRobotStatus;
    private JPanel pRootPanel;
    private JButton bLoadRobot;
    private JButton bLoadEnvironment;
    private JButton bRestart;
    private JList listSensorData;
    private JCheckBox cbAutonomous;

    private final int velocityIncrement = 10;
    private final double orientationIncrement = 1. * Math.PI / 180.;
    private UpdateControlThread updateThread;

    private File robotFile;
    private File environmentFile;
    private EnvironmentObject collisionObj = null;

    private Controller controller = null;
    private List<SensorData> lastSensorData = new LinkedList<>();
    private Object sensorDataLock = new Object();

    public ControlGUI() {
        setTitle("Robot Control");
        setResizable(false);
        pDrawPanel.setPreferredSize(new Dimension(100, 100));
        setContentPane(pRootPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        kfm.addKeyEventDispatcher( new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent evt) {
                BaseRobot robot = controller.getRobot();
                if (robot== null) {
                    return false;
                }
                if(evt.getID()!=KeyEvent.KEY_PRESSED){
                    return false;
                }
                int key = evt.getKeyCode();
                if (key == KeyEvent.VK_LEFT) {
                    robot.rotate(-orientationIncrement);
                } else if (key == KeyEvent.VK_RIGHT) {
                    robot.rotate(orientationIncrement);
                } else if (key == KeyEvent.VK_UP) {
                    robot.accelerate(velocityIncrement);
                } else if (key == KeyEvent.VK_DOWN) {
                    int velocity = robot.getVelocity();
                    robot.decelerate(velocityIncrement);
                }
                else {
                    return false;
                }
                return true;

            }
        });
        pack();

        controller = new Controller(this);



        bLoadRobot.addActionListener(new ActionListener() {
            @Override

            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
                fc.addChoosableFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return (f.getName().endsWith(".jar"));
                    }

                    @Override
                    public String getDescription() {
                        return "Robot jar files (*.jar)";
                    }
                });
                int returnVal = fc.showOpenDialog(ControlGUI.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    loadRobot(file);

                }
            }
        });


        bLoadEnvironment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
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
                int returnVal = fc.showOpenDialog(ControlGUI.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    loadEnvironment(file);

                }
            }
        });
        bRestart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateThread.doExit();
                restart();
            }
        });

        //TODO: Um schneller Testen zu können, können Sie hier den Roboter und das Environment direkt laden.
        //loadRobot(new File("out\\artifacts\\Robot_jar\\Robot.jar"));
        //loadEnvironment(new File("environment.env"));


        startCalculating();
        cbAutonomous.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.getRobot().setAutonomousStearing(cbAutonomous.isSelected());
            }
        });
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
        updateTitle();
        pDrawPanel.setPreferredSize(new Dimension(env.getWidth(), env.getHeight()));
        pack();
        environmentFile = file;
        // if environment has been loaded: reload the robot because gui size maybe changed
        if(robotFile!=null){
            loadRobot(robotFile);
        }
        return true;
    }

    private void loadRobot(File file) {
        BaseRobot robot = RobotLoader.loadRobotFromJar(file);
        robot.setInitialPose(15+robot.getLength()/2 , pDrawPanel.getHeight() /2, 0);
        controller.setRobot(robot);
        updateTitle();
        robotFile = file;

    }


    private void updateTitle() {
        String robotName = controller.getRobot()!=null?controller.getRobot().getName(): "-";
        String environmentName = controller.getEnvironment()!=null?controller.getEnvironment().getName():"-";
        this.setTitle("Robot Control ["+robotName+ " | "+ environmentName+ "]");
    }







    @Override
    public boolean isFocusable() {
        return true;
    }




    private void startCalculating() {
        collisionObj = null;
        updateThread = new UpdateControlThread(controller, this);
        updateThread.start();


    }


    private void createUIComponents() {
        listSensorData = new JList(new DefaultListModel());
        pDrawPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                BaseRobot robot = controller.getRobot();
                if (robot == null) {
                    return;
                }

                int xr = robot.getPosX();
                int yr = robot.getPosY();
                double orientation = robot.getOrientation();


                int velocity = robot.getVelocity();
                int width = robot.getWidth();
                int length = robot.getLength();

                // show robot info
                String statusStr = "X: " + xr + "\n";
                statusStr += "Y: " + yr + "\n";
                statusStr += "Orientierung: " + (int)(orientation*180/Math.PI) + "°\n";
                statusStr += "Geschwindigkeit: " + velocity + " Pixel/s\n";

                taRobotStatus.setText(statusStr);

                // draw robot
                Graphics2D g2d =(Graphics2D)g;
                Visualisation.drawRobot(g2d, robot, Color.RED);

                // draw environment
                Environment environment = controller.getEnvironment();
                Visualisation.drawEnvironment(g2d, environment);

                // update sensor data
                DefaultListModel sensorDataModel = (DefaultListModel) listSensorData.getModel();
                sensorDataModel.clear();

                // draw last measurements
                synchronized(sensorDataLock) {
                    // plot sensor data
                    Visualisation.drawSensorData(g2d, robot, lastSensorData);
                    // update sensor data list
                    for (SensorData sd : lastSensorData) {
                        sensorDataModel.addElement(sd);
                    }



                    lastSensorData.clear();
                }
                // draw collision object
                if(collisionObj != null){
                    Visualisation.drawEnvironmentObject(g2d, collisionObj, Color.RED);

                }


            }
        };
    }


    private void restart(){
        loadEnvironment(environmentFile);
        loadRobot(robotFile);
        cbAutonomous.setSelected(false);
        startCalculating();
    }

    public void onCollision(BaseRobot robot, EnvironmentObject obj) {
        collisionObj = obj;
        repaint();
        updateThread.doExit();
        int res = JOptionPane.showConfirmDialog(this, "Kollision - Roboter '"+robot.getName()+"' zerstört! Neu starten?", "Kollision", JOptionPane.YES_NO_OPTION);
        if(res==JOptionPane.YES_OPTION){
           restart();
        }
    }

    public void onTargetZoneReached(BaseRobot robot) {
        updateThread.doExit();
        JOptionPane.showMessageDialog(this, "Roboter '"+robot.getName()+"' hat den Zielbereich erreicht! Herzlichen Glückwunsch!", "Zielbereich erreicht", JOptionPane.INFORMATION_MESSAGE);
    }


    @Override
    public void onSensorMeasurement(SensorData data) {
        synchronized(sensorDataLock) {
            lastSensorData.add(data);
        }
    }
}
