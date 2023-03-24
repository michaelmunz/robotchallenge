package thu.robots.components;

public abstract class AbstractRobotFactory {
    /**
     * Instanziiert einen speziellen Roboter (customrobot). Dabei muss dieser auch mit Sensoren ausgestattet werden.
     * @return der erstellte Roboter
     */
    public abstract BaseRobot createRobot();
}
