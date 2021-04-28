/*
    This class was an experiment to see if we could get the SmartDashboard feed to run in a separate thread in order
    to prevent any blocking calls from being made inside time sensitive calls inside subsystems or commands
*/

package frc.robot.values;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class SmartDashboardThread extends Thread {

    // hashmap that is exposed to other classes in the programwith
    private HashMap<String, Object> userValues;

    private HashMap<String, Object> outgoingValues;

    // hashmap for this class only, is modified before data is synced  the dashboard
    private HashMap<String, Object> networkValues;

    private ReentrantLock userValuesLock = new ReentrantLock();
    private ReentrantLock networkValuesLock = new ReentrantLock();

    private AtomicBoolean lastUpdateSuccessful = new AtomicBoolean();

    public SmartDashboardThread() {
        super("SmartDashboardThread");

        userValues = new HashMap<>();
        networkValues = new HashMap<>();
    }

    // attempts to put the value in the table to be sent to the dashboard
    // returns true if successful, or false if it cannot acquire a lock within 5 ms
    public boolean putBoolean(String key, boolean value) {
        try {
            if(userValuesLock.tryLock(5, TimeUnit.MILLISECONDS)) {
                try {
                    outgoingValues.put(key, value);
                    userValues.put(key, value);
                } finally {
                    userValuesLock.unlock();
                }
                return true;
            }
            return false;
        } catch(InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            userValuesLock.lock();
            // use of instanceof seems a little sketchy but it should be safe
            if(userValues.containsKey(key) && userValues.get(key) instanceof Boolean) {
                return (Boolean) userValues.get(key);
            }
        } finally {
            userValuesLock.unlock();
        }

        return defaultValue;
    }

    public boolean putString(String key, String value) {
        try {
            if(networkValuesLock.tryLock(5, TimeUnit.MILLISECONDS)) {
                try {
                    outgoingValues.put(key, value);
                    networkValues.put(key, value);
                } finally {
                    networkValuesLock.unlock();
                }
                return true;
            }
            return false;
        } catch(InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getString(String key, String defaultValue) {
        userValuesLock.lock();
        if(userValues.containsKey(key) && userValues.get(key) instanceof String) {
            userValuesLock.unlock();
            return (String) userValues.get(key);
        }

        userValuesLock.unlock();
        return defaultValue;
    }

    public boolean putDouble(String key, double value) {
        try {
            if(networkValuesLock.tryLock(5, TimeUnit.MILLISECONDS)) {
                try {
                    outgoingValues.put(key, value);
                    networkValues.put(key, value);
                } finally {
                    networkValuesLock.unlock();
                }
                return true;
            }
            return false;
        } catch(InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getDouble(String key, double defaultValue) {
        userValuesLock.lock();
        if(userValues.containsKey(key) && userValues.get(key) instanceof Double) {
            userValuesLock.unlock();
            return (Double) userValues.get(key);
        }

        userValuesLock.unlock();
        return defaultValue;
    }

    public boolean wasLastUpdateSuccessful() {
        return lastUpdateSuccessful.get();
    }

    private void syncDashboard() throws InterruptedException {
        try {

            if(networkValuesLock.tryLock(10, TimeUnit.MILLISECONDS)) {

                String[] outgoingKeys = (String[]) outgoingValues.keySet().toArray();

                for(String s : outgoingKeys) {
                    Object o = outgoingValues.get(s);
                    if(o instanceof Boolean) {
                        SmartDashboard.putBoolean(s, (Boolean) o);
                    } else if(o instanceof Double) {
                        SmartDashboard.putNumber(s, (Double) o);
                    } else if(o instanceof String) {
                        SmartDashboard.putBoolean(s, (Boolean) o);
                    }
                }

                String[] networkKeys = (String[]) networkValues.keySet().toArray();

                for(String s : networkKeys) {
                    Object o = networkValues.get(s);
                    if(o instanceof Boolean) {
                        networkValues.put(s, SmartDashboard.getBoolean(s, (Boolean) networkValues.get(s)));
                    } else if(o instanceof Double) {
                        networkValues.put(s, SmartDashboard.getNumber(s, (Double) networkValues.get(s)));
                    } else if(o instanceof String) {
                        networkValues.put(s, SmartDashboard.getString(s, (String) networkValues.get(s)));
                    }
                }

                outgoingValues = new HashMap<>();

                lastUpdateSuccessful.set(true);
                return;
            }

            lastUpdateSuccessful.set(false);

        } finally {
            networkValuesLock.unlock();
        }

    }

    private void syncUserValues() throws InterruptedException {
        if(userValuesLock.tryLock(10, TimeUnit.MILLISECONDS) && networkValuesLock.tryLock(10, TimeUnit.MILLISECONDS)) {

            try {
                userValues = new HashMap<>();

                for(Map.Entry<String, Object> entry : networkValues.entrySet()) {
                    userValues.put(entry.getKey(), entry.getValue());
                }

            } finally {

                userValuesLock.unlock();
                networkValuesLock.unlock();
            }
        }
    }

    @Override
    public void run() {
        try {
            syncDashboard();
            syncUserValues();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}