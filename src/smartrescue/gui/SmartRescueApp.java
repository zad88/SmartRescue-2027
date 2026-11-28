package smartrescue.gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;

import smartrescue.emergency.Witness;
import smartrescue.dispatch.Operator;
import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceTeam;
import smartrescue.service.PortalService;

public class SmartRescueApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartRescueApp::showMainMenu);
    }

    private static void showMainMenu() {

        JFrame menuFrame = new JFrame("SmartRescue - Main Menu");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel menuPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        menuPanel.setBorder(
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        );

        addMenuButton(menuPanel, menuFrame, "Witness Reporting", 0);
        addMenuButton(menuPanel, menuFrame, "Operator Dashboard", 1);
        addMenuButton(menuPanel, menuFrame, "Ambulance Console", 2);
        addMenuButton(menuPanel, menuFrame, "Hospital Portal", 3);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(event -> System.exit(0));
        menuPanel.add(exitButton);

        menuFrame.add(menuPanel);
        menuFrame.setSize(420, 350);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setVisible(true);
    }

    private static void addMenuButton(
        JPanel menuPanel,
        JFrame menuFrame,
        String buttonText,
        int choice
    ) {
        JButton button = new JButton(buttonText);

        button.addActionListener(event -> {
            menuFrame.dispose();
            openSelectedInterface(choice);
        });

        menuPanel.add(button);
    }

    private static void openSelectedInterface(int choice) {

        try {
            JFrame frame;
            PortalService service = new PortalService();

            switch (choice) {

                case 0:
                    Witness demoWitness = service.loadWitness();

                    frame = new WitnessReportingFrame(demoWitness);
                    break;

                case 1:
                    Operator demoOperator = service.loadOperator();

                    frame = new OperatorDashboardFrame(demoOperator);
                    break;

                case 2:
                    List<Ambulance> ambulances =
                        service.loadAmbulances();

                    if (ambulances.isEmpty()) {
                        JOptionPane.showMessageDialog(
                            null,
                            "No ambulances were found in the database.",
                            "Ambulance Not Found",
                            JOptionPane.WARNING_MESSAGE
                        );

                        showMainMenu();
                        return;
                    }

                    JComboBox<Ambulance> ambulanceChoices =
                        new JComboBox<>(ambulances.toArray(new Ambulance[0]));
                    ambulanceChoices.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(
                                JList<?> list, Object value, int index,
                                boolean selected, boolean focused) {
                            super.getListCellRendererComponent(
                                    list, value, index, selected, focused);
                            if (value instanceof Ambulance selectedAmbulance) {
                                setText("ID: " + selectedAmbulance.getAmbulanceId()
                                    + " | " + selectedAmbulance.getLicensePlate()
                                    + " | " + selectedAmbulance.getStatus());
                            }
                            return this;
                        }
                    });
                    int choiceResult = JOptionPane.showConfirmDialog(
                        null, ambulanceChoices, "Select Ambulance",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (choiceResult != JOptionPane.OK_OPTION) {
                        showMainMenu();
                        return;
                    }
                    Ambulance demoAmbulance =
                        (Ambulance) ambulanceChoices.getSelectedItem();

                    AmbulanceTeam demoTeam = service.loadTeam(demoAmbulance);

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

            showMainMenu();
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
        JButton backButton = new JButton("Back to Main Menu");

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