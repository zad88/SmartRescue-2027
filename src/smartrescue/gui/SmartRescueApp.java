package smartrescue.gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import smartrescue.emergency.Witness;
import smartrescue.dispatch.Operator;
import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceTeam;
import smartrescue.data.AmbulanceRepository;

public class SmartRescueApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartRescueApp::showMainMenu);
    }

    private static void showMainMenu() {

        String[] options = {
            "Witness Reporting",
            "Operator Dashboard",
            "Ambulance Console",
            "Hospital Portal",
            "Exit"
        };

        int choice = JOptionPane.showOptionDialog(
            null,
            "Welcome to SmartRescue\n\nSelect an interface:",
            "SmartRescue - Main Menu",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        );

        if (choice == -1 || choice == 4) {
            return;
        }

        try {
            JFrame frame;

            switch (choice) {

                case 0:
                    Witness demoWitness = new Witness(
                        1,
                        "Demo Witness",
                        "0000000000",
                        "Cairo"
                    );

                    frame = new WitnessReportingFrame(demoWitness);
                    break;

                case 1:
                    Operator demoOperator = new Operator(
                        1,
                        "Demo Operator",
                        "Day Shift",
                        1
                    );

                    frame = new OperatorDashboardFrame(demoOperator);
                    break;

                case 2:
                    Ambulance demoAmbulance =
                        new AmbulanceRepository().findAmbulanceById(2);

                    if (demoAmbulance == null) {
                        JOptionPane.showMessageDialog(
                            null,
                            "Ambulance ID 1 was not found in the database.",
                            "Ambulance Not Found",
                            JOptionPane.WARNING_MESSAGE
                        );

                        SwingUtilities.invokeLater(
                            SmartRescueApp::showMainMenu
                        );
                        return;
                    }

                    AmbulanceTeam demoTeam = new AmbulanceTeam(
                        1,
                        "Demo Driver",
                        2,
                        "On Duty",
                        demoAmbulance
                    );

                    frame = new AmbulanceConsoleFrame(
                        demoTeam,
                        demoAmbulance
                    );
                    break;

                case 3:
                    frame = new HospitalPortalFrame();
                    break;

                default:
                    return;
            }

            openChildWindow(frame);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null,
                "Could not open the selected interface:\n"
                    + e.getMessage(),
                "Application Error",
                JOptionPane.ERROR_MESSAGE
            );

            SwingUtilities.invokeLater(SmartRescueApp::showMainMenu);
        }
    }

    private static void openChildWindow(JFrame frame) {

        AtomicBoolean returnedToMenu = new AtomicBoolean(false);

        Runnable backToMenu = () -> {

            if (returnedToMenu.compareAndSet(false, true)) {

                frame.dispose();

                SwingUtilities.invokeLater(
                    SmartRescueApp::showMainMenu
                );
            }
        };

        // Add a visible Back to Main Menu button.
        JButton backButton = new JButton("â† Back to Main Menu");

        backButton.addActionListener(event -> backToMenu.run());

        JMenuBar navigationBar = new JMenuBar();
        navigationBar.add(backButton);

        frame.setJMenuBar(navigationBar);

        // Closing the X button returns to the main menu.
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent event) {
                backToMenu.run();
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
