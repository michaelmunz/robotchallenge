package thu.robots.components;

import java.util.List;


public abstract class Sensor {
    protected double orientationToRobot;
    protected int measurementRate;
    protected double beamWidth;

    /**
     * Erstellt einen neuen Sensor
     * @param orientationToRobot Orientierung (Blickrichtung) des Sensors relativ zur Roboter-Ausrichtung
     * @param beamWidth Strahlbreite in rad
     * @param measurementRate Messrate in Hertz
     */
    public Sensor(double orientationToRobot, double beamWidth, int measurementRate) {
        this.orientationToRobot = orientationToRobot;
        this.beamWidth = beamWidth;
        this.measurementRate = measurementRate;
    }

    public final int getMaxRange() {
        // max range depends on:
        // beam width and measurement rate
        // Area scanned per second is constant k:
        // (pi*r^2*alpha)/(2*pi)*f=k
        // => r = sqrt(2*k/(f*alpha))
        double k = 40000;
        double r = Math.sqrt(2*k/(getMeasurementRate()*getBeamWidth()));

        return (int)r;
    }

    /**
     * Gibt die Strahlbreite zurück
     * @return Strahlbreite in Radiant
     */
    public final double getBeamWidth() {
        return beamWidth;
    }

    /**
     * Gibt die Orientierung relativ zum Roboter zurück
     * @return Orientierung in radiant
     */
    public final double getOrientationToRobot() {
        return orientationToRobot;
    }

    /**
     * Gibt die Messrate als Faktor der Rrfresh-Rate der GUI (UPDATE_TIME_INTERVAL  in der Klasse UpdateControlThread) zurück
     * @return Messrate als Faktor der Refresh-Rate der GUI (UPDATE_TIME_INTERVAL  in der Klasse UpdateControlThread) zurück
     */
    public final int getMeasurementRate() {
        return measurementRate;
    }

    /**
     * Registrieren /Eintragen eines Observers, der bei Vorliegen neuer Sensordaten benachrichtigt werden soll
     * @param obs der Observer
     */
    public abstract void registerSensorDataObserver(ISensorDataObserver obs);

    /**
     * Wird aufgerufen, wenn neue Messdaten aus der Umgebung simuliert wurden
     * @param data die empfangenen / simulierten Messdaten aus der Umgebung des Roboters für diesen Sensor
     */
    public abstract void measurementFromEnvironment(List<SensorData> data);


}
