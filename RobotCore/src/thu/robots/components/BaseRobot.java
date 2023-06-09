package thu.robots.components;

import java.awt.*;
import java.util.List;

public abstract class BaseRobot {
    protected Stearing stearing;
    private String name;

    /**
     * Maximale Beschleunigung / Abbremsen in pixel/s^2
     */
    public static final int MAX_ACCELERATE = 10;

    /**
     * Maximale Geschwindigkeit in pixel/s
     */
    public static final int MAX_VELOCITY = 50;


    /**
     * erstellt einen Roboter
     * @param stearing Steuerungs-Objekt zur Steuerung des Roboters
     */
    public BaseRobot(String name, Stearing stearing) {
        this.name = name;
        this.stearing = stearing;
    }

    /**
     * setzt das stearing-objekt neu
     * @param stearing das Steuerungs-Objekt
     */
    public void setStearing(Stearing stearing){
        this.stearing = stearing;
    }


    /**
     * Gibt die Liste aller Sensoren zurück
     * @return
     */
    public abstract List<Sensor> getSensors();

    /**
     * Fügt einen Sensor hinzu
     * @param sensor*/
    public abstract void addSensor(Sensor sensor);

    /**
     * Aktiviert oder deaktiviert das autonome Fahren
     * @param enabled
     */
    public abstract void setAutonomousStearing(boolean enabled);

    /**
     *  gibt ein Bild zurück, das zur Darstellung des Sensors verwendet werden kann.
      * @return Ein Bild oder null (dann wird kein Bild angezeigt)
     */
    public abstract Image getImage();

    /**
     * Legt die initiale Position und Orientierung fest
     * @param posX
     * @param posY
     * @param orientation Orientierung in Radiant
     */
    public final void setInitialPose(int posX, int posY, double orientation){
        this.stearing.setInitialPose(posX, posY, orientation);
    }

    /**
     * Rotiert den Roboter (wird weitergeleitet/delegiert an Stearing)
     * @param rotate positive Drehungen nach rechts, negative nach links
     */
    public void rotate(double rotate) {
        stearing.rotate(rotate);
    }

    /**
     * Gibt den Namen des Roboters zurück
     * @return
     */
    public String getName() {
        return name;
    }
    /**
     * Liefert die X-Position zurück (wird weitergeleitet/delegiert an Stearing)
     * @return
     */
    public final int getPosX() {
        return (int)stearing.getPosX();
    }

    /**
     * Liefert die Y-Position zurück (wird weitergeleitet/delegiert an Stearing)
     * @return
     */
    public final int getPosY() {
        return (int)stearing.getPosY();
    }

    /**
     * Liefert die Orientierung zurück (wird weitergeleitet/delegiert an Stearing)
     * @return
     */
    public final double getOrientation() {
        return stearing.getOrientation();
    }

    /**
     * Liefert die Geschwindigkeit zurück (wird weitergeleitet/delegiert an Stearing)
     * @return
     */
    public final int getVelocity() {
        return stearing.getVelocity();
    }

    /**
     * Liefert die Breite des Roboters zurück
     * @return
     */
    public abstract int getWidth();

    /**
     * Liefert die Länge des Roboters zurück
     * @return
     */
    public abstract int getLength();


    /**
     * Beschleunigt um den Betrag amount. Maximale Beschleunigung darf MAX_ACCELERATE betragen (wird geclippt).
     * @param amount
     */
    public final void accelerate(int amount){
        this.stearing.accelerate(Math.min(MAX_ACCELERATE, amount));
        if(getVelocity()>MAX_VELOCITY) {
            this.stearing.decelerate(amount);
        }
    }

    /**
     * Verlangsamt um den Betrag amount. Maximales Abbremsen darf MAX_ACCELERATE betragen (wird geclippt).
     * @param amount
     */
    public final void decelerate(int amount){
        this.stearing.decelerate(Math.min(MAX_ACCELERATE, amount));
        if(getVelocity()<-MAX_VELOCITY) {
            this.stearing.accelerate(amount);
        }
    }


    /**
     * Berechnet und aktualisiert die neue Position des Roboters (wird weitergeleitet/delegiert an Stearing)
     * @param deltaTimeSec Zeitdifferenz, für die die Bewegung berechnet werden soll
     */
    public final void move(double deltaTimeSec){
        this.stearing.move(deltaTimeSec);
    }


}
