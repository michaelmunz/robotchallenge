package thu.robots.gui;

import thu.robots.internal.Controller;

import javax.swing.*;

public class UpdateControlThread extends Thread {

    public static final double UPDATE_TIME_INTERVAL = 0.1;
    private final JFrame gui;
    private final Controller controller;
    private boolean doExit = false;

    public UpdateControlThread( Controller controller, JFrame gui){
        this.controller = controller;
        this.gui = gui;
    }

    public void run(){
        while (!doExit) {
            controller.calculate(UPDATE_TIME_INTERVAL);
            gui.repaint();
            try {
                Thread.sleep((long) (UPDATE_TIME_INTERVAL * 1000));
            } catch (InterruptedException ex) {
                doExit = true;
                break;
            }
        }
    }

    public void doExit() {
        this.doExit = true;
    }
}
