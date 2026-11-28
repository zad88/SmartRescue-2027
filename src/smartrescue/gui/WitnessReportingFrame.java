package smartrescue.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import smartrescue.emergency.Emergency;
import smartrescue.emergency.Witness;

public class WitnessReportingFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final Witness witness;

    private JTextField locationField;
    private JTextArea descriptionArea;
    private JTextField injuredCountField;

    public WitnessReportingFrame(Witness witness) {
        super("SmartRescue 2027 - Emergency Reporting");

        this.witness = witness;

        initializeUI();
    }

    private void initializeUI() {

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(650, 520);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 20));
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        setContentPane(mainPanel);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));

        JLabel titleLabel = new JLabel(
                "Emergency Reporting",
                SwingConstants.CENTER
        );
        titleLabel.setFont(titleLabel.getFont().deriveFont(24f));

        JLabel subtitleLabel = new JLabel(
                "Enter the emergency details below.",
                SwingConstants.CENTER
        );

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(
                BorderFactory.createTitledBorder("Emergency Details")
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.anchor = GridBagConstraints.WEST;

        locationField = new JTextField(witness.provideLocation(), 25);
        descriptionArea = new JTextArea(6, 25);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        injuredCountField = new JTextField("1", 25);

        addFormRow(
                formPanel, gbc, 0,
                "Emergency Location:",
                locationField
        );

        addFormRow(
                formPanel, gbc, 1,
                "Emergency Description:",
                new JScrollPane(descriptionArea)
        );

        addFormRow(
                formPanel, gbc, 2,
                "Number of Injured People:",
                injuredCountField
        );

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Submit button
        JButton submitButton = new JButton("Submit Emergency Report");
        submitButton.addActionListener(event -> submitReport());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(submitButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            Component input
    ) {

        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(input, gbc);
    }

    private void submitReport() {

        String location = locationField.getText().trim();
        String description = descriptionArea.getText().trim();
        String injuredText = injuredCountField.getText().trim();

        if (location.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter the emergency location and description.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int injuredCount;

        try {
            injuredCount = Integer.parseInt(injuredText);
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid number of injured people.",
                    "Invalid Number",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (injuredCount <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "The number of injured people must be greater than zero.",
                    "Invalid Number",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Call the existing backend method.
        Emergency emergency = witness.reportEmergency(
                location,
                description,
                injuredCount
        );

        JOptionPane.showMessageDialog(
                this,
                "Emergency report submitted successfully!"
                + "\n\nIncident ID: " + emergency.getIncidentId()
                + "\nStatus: " + emergency.getStatus(),
                "Report Submitted",
                JOptionPane.INFORMATION_MESSAGE
        );

        descriptionArea.setText("");
        injuredCountField.setText("1");
    }
}