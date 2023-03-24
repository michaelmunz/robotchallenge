package thu.robots.gui;

import thu.robots.components.BaseRobot;


import java.io.File;

public class RobotListElement{

    private boolean isFirst;

    public int getLevel() {
        return this.level;
    }

    public enum RobotState{
        Running, Finished, Collided, Timeout
    }

    private final File file;
    private BaseRobot robot;
    private RobotState state;
    private int level;

    public RobotListElement(BaseRobot robot, RobotState state, File file) {
        this.robot = robot;
        this.state = state;
        this.level = -1;
        this.file = file;
    }

    public BaseRobot getRobot() {
        return robot;
    }

    public void setRobot(BaseRobot robot) {
        this.robot = robot;
    }

    public RobotState getState() {
        return state;
    }

    public void setState(RobotState state, int level) {
        this.state = state;
        this.level = level;
    }

    public void setFirstRobot(boolean isFirst){
        this.isFirst = isFirst;
    }

    public boolean isFirst(){
        return isFirst;
    }

    public String toString(){
        String res = robot.getName();

        // detect group name from parent directory;
        String groupName = "?";
        String fname = file.getParent();
        int lastPos = fname.lastIndexOf("\\");
        String lastPart = fname.substring(lastPos+1);
        String[] parts = lastPart.split("-");
        if(parts.length>0) {
            groupName = parts[0];
        }
        res += " [Gruppe "+groupName+"]";
        if(level > 0){
            res +=" (Level "+level+")";
        }
        if(isFirst()&&getState()== RobotState.Finished){
            res +=" ***";
        }
        return res;

    }
}
