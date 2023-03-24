package thu.robots.components;

public abstract class Stearing {
    private int velocity=0;
    private double posX=0;
    private double posY=0;
    private double orientation=0;
    private boolean initialPoseSet;

    public Stearing(){
        this.initialPoseSet = false;
    }

    /**
     *Legt die initiale Position und Orientierung fest
     * @param posX
     * @param posY
     * @param orientation Orientierung in Radiant
     */
    public void setInitialPose(int posX, int posY, double orientation) {
        this.posX = posX;
        this.posY = posY;
        this.orientation = orientation;
        this.initialPoseSet = true;
        this.velocity = 0;
    }

    /**
     * Gibt die aktuelle X-Position zurück
     * @return
     */
    public final double getPosX() {
        return posX;
    }

    /**
     * Gibt die aktuelle Y-Position zurück
     * @return
     */
    public final double getPosY() {
        return posY;
    }

    /**
     * Gibt die aktuelle Orientierung in Radiant zurück
     * @return
     */
    public final double getOrientation() {
        return orientation;
    }

    /**
     * Gibt die aktuelle Geschwindigkeit in Pixel/Sekunde zurück
     * @return
     */
    public final int getVelocity() {
        return velocity;
    }


    /**
     * Berechnet und aktualisiert die neue Position des Roboters. Dazu wird zunächst calculate() aufgerufen, um neue Steuerungsparameter zu ermitteln. Anschließend wird die neue Position berechnet.
     * @param deltaTimeSec Zeitdifferenz, für die die Bewegung berechnet werden soll
     */
    public final void move(double deltaTimeSec) {
        calculate();
        this.posX += this.velocity*deltaTimeSec*Math.cos(this.orientation);
        this.posY += this.velocity*deltaTimeSec*Math.sin(this.orientation);
    }

    /**
     * Beschleundigt um den Betrag amount
     * @param amount
     */
    public final void accelerate(int amount) {
        this.velocity += amount;
    }

    /**
     * Verlangsamt um den Betrag amount
     * @param amount
     */
    public final void decelerate(int amount) {
        this.velocity -= amount;
    }


    /**
     * Rotiert den Roboter
     * @param rotate positive Drehungen nach rechts, negative nach links
     */
    public final void rotate(double rotate) {
        this.orientation += rotate;
        this.orientation = normalizeOrientation(orientation);
    }


    private double normalizeOrientation(double orientation) {
        if (orientation <= -Math.PI) {
            orientation = 2*Math.PI + orientation;
        } else if (orientation > Math.PI) {
            orientation = (orientation - Math.PI*2);
        }
        return orientation;
    }


    /**
     * Berechnet neue Parameter. In einem autonomen Roboter kann hier auf die Sensordaten reagiert werden.
     * In einem normal gesteuerten Roboter ist hier nichts nötig.
     */
    public void calculate(){

    }
}
