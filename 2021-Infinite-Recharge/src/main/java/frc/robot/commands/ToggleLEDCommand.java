// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class ToggleLEDCommand extends CommandBase {

  private boolean done = false;

  /** Creates a new ToggleLEDCommand. */
  public ToggleLEDCommand() {
    // Use addRequirements() here to declare subsystem dependencies.
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable visionTable = inst.getTable("vision");
    NetworkTableEntry ledEntry = visionTable.getEntry("ledOn");
    if(ledEntry.getBoolean(false)) {
      ledEntry.setBoolean(false);
    } else {
      ledEntry.setBoolean(true);
    }

    done = true;
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return done;
  }
}
