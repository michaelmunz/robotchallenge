package thu.robots.gui;

import thu.robots.components.BaseRobot;
import thu.robots.components.Environment;
import thu.robots.components.EnvironmentObject;
import thu.robots.components.SensorData;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;

public class Visualisation {

    public static void drawRobot(Graphics2D g2d, BaseRobot robot, Color color){
        if(robot!=null) {
            Image img = robot.getImage();
            int xr = robot.getPosX();
            int yr = robot.getPosY();
            double orientation = robot.getOrientation();
            int width = robot.getWidth();
            int length = robot.getLength();
            drawRotatedRect(g2d, xr, yr, width, length, orientation, color, img);
        }
    }


    public static void drawRotatedRect(Graphics2D g2d, int x, int y, int width, int length, double orientation, Color color, Image img) {
        g2d.setColor(color);
        AffineTransform oriTrans = g2d.getTransform();
        g2d.rotate(orientation, x, y);

        // x,y has to be transformed to left top position
        g2d.fill3DRect(x - length / 2, y - width / 2, length, width, true);
        if(img!=null){
            g2d.drawImage(img, x-length/2, y-width/2,length, width, null);
        }

        g2d.setTransform(oriTrans);
    }

    public static void drawEnvironmentObject(Graphics2D g2d, EnvironmentObject envObj, Color color) {
        // object is given with top-left coordinates
        drawRotatedRect(g2d, envObj.getX(), envObj.getY(), envObj.getWidth(), envObj.getLength(), envObj.getOrientation(), color, null);
    }

    public static void drawEnvironment(Graphics2D g2d, Environment environment){
        // draw environment
        if(environment != null ){
            for(EnvironmentObject obj: environment.getObjects()){
                drawEnvironmentObject(g2d, obj, obj.getColor());
            }
        }
    }

    public static void drawSensorData(Graphics2D g2d, BaseRobot robot, List<SensorData> lastSensorData) {
        double orientation = robot.getOrientation();
        int xr = robot.getPosX();
        int yr = robot.getPosY();

        final int circleRadius = 5;
        g2d.setColor(Color.RED);
        for (SensorData sd : lastSensorData) {
            AffineTransform oriTrans = g2d.getTransform();
            double alpha = orientation + sd.getRelatedSensor().getOrientationToRobot();
            g2d.rotate(alpha, xr, yr);

            g2d.fillArc(xr+sd.getX()-circleRadius,yr+sd.getY()-circleRadius, 2*circleRadius, 2*circleRadius, 0, 360);
            g2d.setTransform(oriTrans);

        }

    }
}
