package frc.robot.subsystems;

public class SubsystemsInstance {
    public DriveSubsystem m_driveSubsystem;
    public NavigationSubsystem m_navSubsystem;

    private static SubsystemsInstance inst;

    private SubsystemsInstance() {
        m_driveSubsystem = new DriveSubsystem();
        m_navSubsystem = new NavigationSubsystem();
    }

    public static SubsystemsInstance getInstance() {
        if(inst == null) inst = new SubsystemsInstance();

        return inst;
    }
}