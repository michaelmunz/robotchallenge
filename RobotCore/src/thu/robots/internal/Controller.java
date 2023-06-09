package thu.robots.internal;

import thu.robots.components.BaseRobot;
import thu.robots.components.Environment;
import thu.robots.components.EnvironmentObject;
import thu.robots.components.Sensor;
import thu.robots.gui.ControlGUI;

import java.util.List;

public class Controller {

    private ControlGUI controlGUI  = null;
    private BaseRobot robot;
    private Environment environment = null;


    public Controller(ControlGUI controlGUI){
        this.controlGUI = controlGUI;
    }

    public void calculate(double deltaT) {
        robot.move(deltaT);
        EnvironmentObject colObj = environment.checkCollosion(robot);
        if(colObj!=null){
            controlGUI.onCollision(robot, colObj);
        }


        environment.simulateSensorData(robot);
        boolean finished = environment.checkTargetZone(robot);
        if(finished){
            controlGUI.onTargetZoneReached(robot);
        }
    }

    public ControlGUI getControlGUI() {
        return controlGUI;
    }

    public BaseRobot getRobot() {
        return robot;
    }

    public void setEnvironment(Environment env) {
        this.environment = env;
    }

    public void setRobot(BaseRobot robot) {
        this.robot = robot;
        List<Sensor> sensors = robot.getSensors();
        for(Sensor s: sensors){
            s.registerSensorDataObserver(controlGUI);
        }

    }

    public Environment getEnvironment() {
        return environment;
    }
}

