package thu.robots.internal;

import thu.robots.components.AbstractRobotFactory;
import thu.robots.components.BaseRobot;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class RobotLoader {
    public static BaseRobot loadRobotFromJar(File file) {
        BaseRobot robot = null;
        URL url = null;
        try {
            url = new URL("jar:" + file.toURI().toURL().toString() + "!/");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("No valid URL: " + e);
        }

        URLClassLoader cl = new URLClassLoader(new URL[]{url});
        try {
            Class.forName("thu.robots.components.RobotFactory", true, cl);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (java.lang.UnsupportedClassVersionError ex) {
            throw new IllegalArgumentException("Wrong class version while loading jar file '" + file.getAbsolutePath() + "': " + ex);
        }

        Class<?> c = null;
        try {
            c = cl.loadClass("thu.robots.components.RobotFactory");
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot load class RobotFactory from jar file '" + file.getAbsolutePath() + "': " + e);
        }

        try {
            Constructor<?> constructor = c.getConstructor(null);
            Object instance = constructor.newInstance(null);
            AbstractRobotFactory factory = (AbstractRobotFactory) instance;
            robot = factory.createRobot();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Cannot invoke constructor  in class RobotFactory: " + e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Cannot find constructor in class RobotFactory: " + e);
        }
        return robot;

    }
}