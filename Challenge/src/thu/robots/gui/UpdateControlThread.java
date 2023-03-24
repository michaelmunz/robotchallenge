package thu.robots.gui;

public class UpdateControlThread extends Thread {


    private final ChallengeGUI gui;
    private final ChallengeController controller;
    private boolean doExit = false;
    private final double DELTA_T = 0.1;
    private double simSpeedFactor;

    public UpdateControlThread(ChallengeController controller, ChallengeGUI gui){
        this.controller = controller;
        this.gui = gui;
        this.simSpeedFactor = 0.5;

    }

    public void setSimSpeedFactor(double factor){
        this.simSpeedFactor = factor;
    }

    public void run(){
        while (!doExit) {
            controller.calculate(DELTA_T);
            gui.repaint();
            try {
                Thread.sleep((long) (DELTA_T * 1000 / simSpeedFactor));
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
