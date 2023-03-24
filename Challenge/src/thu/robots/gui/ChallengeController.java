package thu.robots.gui;

import thu.robots.components.BaseRobot;
import thu.robots.components.Environment;
import thu.robots.components.EnvironmentObject;
import thu.robots.components.Sensor;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChallengeController {

    private ChallengeGUI gui  = null;
    private List<RobotListElement> robots = new LinkedList<>();
    private Environment environment = null;
    private boolean firstRobotInTargetZone = false;
    private UpdateControlThread updateThread;
    private Timer timer;
    private static final int TIMEOUT_AFTER_FIRST_ROBOT_SEC = 30;
    private static final double SIM_SPEED_FACTOR = 1;
    private int currentLevel = 0;

    public ChallengeController(ChallengeGUI gui){
        this.gui = gui;
    }

    public void start(){
        updateThread = new UpdateControlThread(this, gui);
        updateThread.setSimSpeedFactor(SIM_SPEED_FACTOR);
        updateThread.start();

    }
    public int getCurrentLevel(){
        return currentLevel;

    }

    public void calculate(double deltaT) {
        boolean activeRobotsAvailable = false;
        for(RobotListElement le: robots) {
            if(le.getState()!= RobotListElement.RobotState.Running){
                continue;
            }
            activeRobotsAvailable = true;
            BaseRobot r = le.getRobot();

            try {

                r.move(deltaT);
                EnvironmentObject colObj = environment.checkCollosion(r);
                if (colObj != null) {
                    gui.onCollision(le, colObj);
                    le.setState(RobotListElement.RobotState.Collided, currentLevel);
                }
                environment.simulateSensorData(r);
                boolean finished = environment.checkTargetZone(r);

                // robot has finished
                if (finished) {
                    le.setState(RobotListElement.RobotState.Finished, currentLevel);
                    if(firstRobotInTargetZone==false){
                        firstRobotInTargetZone  = true;
                        le.setFirstRobot(true);
                        startTimer();
                    }
                    gui.onTargetZoneReached(le);
                }


            }
            catch(Throwable ex){
                System.err.println("Exception for robot '"+r.getName()+"': "+ex);
            }
        }

        // all robots finished or collided
        if(!activeRobotsAvailable){
            updateThread.doExit();
            stopTimer();
            gui.onNoMoreRobots();
        }

    }

    private void onTimeout(){
        for(RobotListElement le: robots){
            if(le.getState()== RobotListElement.RobotState.Running){
                le.setState(RobotListElement.RobotState.Timeout, currentLevel);
                gui.onTimeout(le);
            }
        }
    }

    private void stopTimer() {
        if(timer!=null) {
            timer.cancel();
        }

    }


    public void setEnvironment(Environment env) {
        this.environment = env;
    }

    public void addRobot(RobotListElement le) {
        this.robots.add(le);
        List<Sensor> sensors = le.getRobot().getSensors();
        for(Sensor s: sensors){
            s.registerSensorDataObserver(gui);
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public List<RobotListElement> getRobots() {
        return robots;
    }

    public void onNextLevel() {
        this.currentLevel++;
        int height = environment.getHeight();

        for(RobotListElement le: robots) {
            RobotListElement.RobotState state = le.getState();
            if(state == RobotListElement.RobotState.Collided || state == RobotListElement.RobotState.Timeout){
                continue;
            }
            BaseRobot r = le.getRobot();
            r.setInitialPose(15 + r.getLength() / 2, height / 2, 0);

            le.setFirstRobot(false);
            le.setState(RobotListElement.RobotState.Running, -1);

        }
        firstRobotInTargetZone = false;
        start();

    }

    private void startTimer() {

        timer = new Timer();
        timer.schedule(new TimerTask(){
            private int remainingTimeSeconds = TIMEOUT_AFTER_FIRST_ROBOT_SEC;

            @Override
            public void run() {

                remainingTimeSeconds -=1;
                gui.onTimeoutTick(remainingTimeSeconds);
                if(remainingTimeSeconds ==0){
                    onTimeout();
                }
            }
        }, 1000, 1000);
        gui.onTimeoutTick(TIMEOUT_AFTER_FIRST_ROBOT_SEC);


    }

    public void removeAllRobots() {
        this.robots.clear();
    }
}

