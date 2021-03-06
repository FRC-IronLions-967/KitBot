package frc.robot.values;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class CustomVisionValues {

    // boolean to track whether the vision table has been created
    // can be checked in order to prevent null pointers and other exceptions
    private boolean visionTableExists = false;

    private NetworkTableInstance inst;

    // NetworkTable that contains all of the subtables that track individual pipelines
    private NetworkTable visionTable;

    // subtable that tracks this pipeline
    private NetworkTable pipelineTable;

    // entries for values to be tracked
    private NetworkTableEntry tx;
    private NetworkTableEntry ty;
    private NetworkTableEntry width;
    private NetworkTableEntry height;
    private NetworkTableEntry area;
    private NetworkTableEntry dist;
    private NetworkTableEntry angleOffset;
    private NetworkTableEntry reliability;
    private NetworkTableEntry hasTarget;
    private NetworkTableEntry pipelineName;

    private double angles[] = {10.0, 20.0, 20.0, 30.0,
                               30.0, 40.0, 40.0, 30.0,
                               10.0, 10.0};

    private double rpms[] = {5350.0, 5400.0, 5500.0,
                             5600.0, 5400.0, 5500.0,
                             5600.0, 5600.0, 5600.0,
                             5600.0};

    // creates a new object that pulls data from the specified pipeline
    public CustomVisionValues(String pipeline) {
        inst = NetworkTableInstance.getDefault();

        visionTable = inst.getTable("vision");

        visionTable.addSubTableListener((parent, name, table) -> {
            if(name.equals(pipeline)) {
                pipelineTable = visionTable.getSubTable(name);
                visionTableExists = true;

                tx = pipelineTable.getEntry("tx");
                ty = pipelineTable.getEntry("ty");
                width = pipelineTable.getEntry("width");
                height = pipelineTable.getEntry("height");
                area = pipelineTable.getEntry("area");
                dist = pipelineTable.getEntry("dist");
                angleOffset = pipelineTable.getEntry("angleOffset");
                reliability = pipelineTable.getEntry("reliability");
                hasTarget = pipelineTable.getEntry("hasTarget");
                pipelineName = pipelineTable.getEntry("pipelineName");
            }
        }, false);

    }

    public boolean doesVisionTableExist() {
        return visionTableExists;
    }

    // returns the value of tx, or the maximum value for a double if it can't get the value
    public double getTX() {
        return tx.getDouble(Double.MAX_VALUE);
    }

    // returns the value of ty, or the maximum value for a double if it can't get the value
    public double getTY() {
        return ty.getDouble(Double.MAX_VALUE);
    }

    // returns the width of the bounding rectangle around the found vision target, or 0.0 if the bounding rectangle isn't present
    public double getWidth() {
        return width.getDouble(0.0);
    }

    // returns the height of the bounding rectangle around the found vision target, or 0.0 if the bounding rectangle isn't present
    public double getHeight() {
        return height.getDouble(0.0);
    }

    // returns the area of the bounding rectangle around the found vision target, or 0.0 if the bounding rectangle isn't present
    public double getArea() {
        return area.getDouble(0.0);
    }

    // returns the estimated distance from the robot to the target based on the dimensions of the vision target, or the maximum
    // value for a double if it can't estimate the distance, can't find the target, or can't retrieve the value
    public double getDist() {
        return dist.getDouble(Double.MAX_VALUE);
    }

    // returns the estimated angle between the robot's current position and being pointed straight at the vision target, or the
    // maximum value for a double if it can't estimate the angle, can't find the target, or can't retrieve the value
    public double getAngleOffset() {
        return angleOffset.getDouble(Double.MAX_VALUE);
    }

    // returns the reliability reported by the pipeline, or 0.0 if it cannot retrieve the value or the pipeline has no target
    public double getReliability() {
        return reliability.getDouble(0.0);
    }

    // returns true if the vision pipeline is currently tracking a target within its FOV, false otherwise
    public boolean hasTarget() {
        return hasTarget.getBoolean(false);
    }

    // returns the name of this pipeline or "Unknown Pipeline" if it cannot find the name
    public String getPipelineName() {
        return pipelineName.getString("Unknown Pipeline");
    }

    // method to calculate the best angle setpoint for the hood based off of the vision target measurements
    public double getHoodAngle() {
        double widthVal = width.getDouble(Double.MAX_VALUE);
        if(widthVal == Double.MAX_VALUE) return 0.0;

        return 0.0;
    }

    // method to calculate the best rpm for the flywheel based off of the vision target measurements
    public double getRPM() {
        return 0.0;
    }
}