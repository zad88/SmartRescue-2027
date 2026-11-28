package smartrescue.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceReleaseService;
import smartrescue.dispatch.AmbulanceStatus;
import smartrescue.dispatch.Operator;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;
import smartrescue.emergency.Severity;
import smartrescue.service.PortalService;

public class OperatorDashboardFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final Operator operator;
    private final PortalService service;

    private List<Emergency> emergencies;

    private JTable emergencyTable;
    private DefaultTableModel tableModel;

    private JTextArea detailsArea;
    private JTextField typeField;
    private JComboBox<Severity> severityComboBox;
    private JComboBox<Ambulance> ambulanceComboBox;
    private JLabel ambulanceAvailabilityLabel;

    private JButton classifyButton;
    private JButton verifyButton;
    private JButton assignAmbulanceButton;
    private JButton closeEmergencyButton;

    public OperatorDashboardFrame(Operator operator) {

        super("SmartRescue 2027 - Operator Dashboard");

        this.operator = operator;
        this.service = new PortalService();
        this.emergencies = new ArrayList<>();

        initializeUI();

        refreshEmergencies(null);
        refreshAmbulances();
        AutoRefresh.start(this, service, this::refreshAll);
    }

    private void initializeUI() {

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        setContentPane(mainPanel);

        // Header
        JLabel titleLabel = new JLabel("Operator Dashboard");

        titleLabel.setFont(titleLabel.getFont().deriveFont(22f));

        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Emergency table
        String[] columns = {
            "Incident ID",
            "Location",
            "Type",
            "Severity",
            "Patients",
            "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        emergencyTable = new JTable(tableModel);

        emergencyTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        emergencyTable.getSelectionModel().addListSelectionListener(
                event -> {
                    if (!event.getValueIsAdjusting()) {
                        displaySelectedEmergency();
                    }
                }
        );

        JScrollPane tableScrollPane =
                new JScrollPane(emergencyTable);

        tableScrollPane.setBorder(
                BorderFactory.createTitledBorder("Emergency Reports")
        );

        // Emergency details
        detailsArea = new JTextArea();

        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        JScrollPane detailsScrollPane =
                new JScrollPane(detailsArea);

        detailsScrollPane.setBorder(
                BorderFactory.createTitledBorder("Emergency Details")
        );

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                tableScrollPane,
                detailsScrollPane
        );

        splitPane.setResizeWeight(0.65);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Classification controls
        JPanel classificationPanel = new JPanel(
                new GridLayout(2, 2, 10, 8)
        );

        classificationPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Emergency Classification"
                )
        );

        typeField = new JTextField();

        severityComboBox = new JComboBox<>(Severity.values());

        classificationPanel.add(new JLabel("Emergency Type:"));
        classificationPanel.add(typeField);

        classificationPanel.add(new JLabel("Severity:"));
        classificationPanel.add(severityComboBox);

        // Ambulance selection
        JPanel assignmentPanel = new JPanel(
                new BorderLayout(5, 8)
        );

        assignmentPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Ambulance Assignment"
                )
        );

        ambulanceComboBox = new JComboBox<>();

        ambulanceComboBox.setRenderer(
                new DefaultListCellRenderer() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus) {

                        super.getListCellRendererComponent(
                                list, value, index,
                                isSelected, cellHasFocus
                        );

                        if (value instanceof Ambulance ambulance) {

                            setText(
                                    "ID: " + ambulance.getAmbulanceId()
                                    + " | " + ambulance.getLicensePlate()
                                    + " | " + ambulance.getCurrentLocation()
                            );
                        }

                        return this;
                    }
                }
        );

        assignAmbulanceButton =
                new JButton("Assign Ambulance");

        assignAmbulanceButton.addActionListener(
                event -> assignSelectedAmbulance()
        );

        assignmentPanel.add(
                ambulanceComboBox,
                BorderLayout.CENTER
        );

        ambulanceAvailabilityLabel = new JLabel();
        JPanel assignmentActions = new JPanel(new GridLayout(3, 1, 5, 5));
        assignmentActions.add(ambulanceAvailabilityLabel);
        assignmentActions.add(assignAmbulanceButton);
        JButton recoverAmbulancesButton = new JButton("Release Completed Ambulances");
        recoverAmbulancesButton.addActionListener(event -> releaseCompletedAmbulances());
        assignmentActions.add(recoverAmbulancesButton);
        assignmentPanel.add(assignmentActions, BorderLayout.SOUTH);

        // Arrange classification and assignment controls
        JPanel controlsPanel = new JPanel(
                new GridLayout(1, 2, 10, 0)
        );

        controlsPanel.add(classificationPanel);
        controlsPanel.add(assignmentPanel);

        // Main buttons
        classifyButton = new JButton("Classify Emergency");
        verifyButton = new JButton("Verify Emergency");
        closeEmergencyButton = new JButton("Close Emergency");

        JButton refreshButton = new JButton("Refresh");

        classifyButton.addActionListener(
                event -> classifySelectedEmergency()
        );

        verifyButton.addActionListener(
                event -> verifySelectedEmergency()
        );

        closeEmergencyButton.addActionListener(
                event -> closeSelectedEmergency()
        );

        refreshButton.addActionListener(
                event -> refreshAll()
        );

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 5)
        );

        buttonPanel.add(classifyButton);
        buttonPanel.add(verifyButton);
        buttonPanel.add(closeEmergencyButton);
        buttonPanel.add(refreshButton);

        JPanel bottomPanel = new JPanel(
                new BorderLayout(5, 5)
        );

        bottomPanel.add(controlsPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        updateButtonStates();
    }

    private void refreshAll() {

        String selectedId = getSelectedIncidentId();
        String pendingType = typeField.getText();
        Severity pendingSeverity = (Severity) severityComboBox.getSelectedItem();
        Ambulance selectedAmbulance = (Ambulance) ambulanceComboBox.getSelectedItem();
        Integer ambulanceId = selectedAmbulance == null ? null : selectedAmbulance.getAmbulanceId();

        refreshEmergencies(selectedId);
        refreshAmbulances();
        if (selectedId != null && selectedId.equals(getSelectedIncidentId())) {
            typeField.setText(pendingType);
            severityComboBox.setSelectedItem(pendingSeverity);
        }
        if (ambulanceId != null) {
            for (int i = 0; i < ambulanceComboBox.getItemCount(); i++) {
                if (ambulanceComboBox.getItemAt(i).getAmbulanceId() == ambulanceId) {
                    ambulanceComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        updateButtonStates();
    }

    private void refreshEmergencies(String selectedId) {

        emergencies = service.loadEmergencies();

        tableModel.setRowCount(0);

        int selectedRow = -1;

        for (int i = 0; i < emergencies.size(); i++) {

            Emergency emergency = emergencies.get(i);

            Object[] row = {
                emergency.getIncidentId(),
                emergency.getLocation(),
                emergency.getType() == null
                        ? "" : emergency.getType(),
                emergency.getSeverity() == null
                        ? "" : emergency.getSeverity(),
                emergency.getPatientCount(),
                emergency.getStatus()
            };

            tableModel.addRow(row);

            if (selectedId != null &&
                selectedId.equals(emergency.getIncidentId())) {

                selectedRow = i;
            }
        }

        if (selectedRow >= 0) {

            emergencyTable.setRowSelectionInterval(
                    selectedRow, selectedRow
            );

        } else {

            emergencyTable.clearSelection();
            detailsArea.setText("");
        }

        updateButtonStates();
    }

    private void refreshAmbulances() {

        ambulanceComboBox.removeAllItems();

        List<Ambulance> ambulances =
                service.loadAmbulances();

        for (Ambulance ambulance : ambulances) {

            if (ambulance.checkAvailability()) {
                ambulanceComboBox.addItem(ambulance);
            }
        }

        ambulanceAvailabilityLabel.setText(ambulanceComboBox.getItemCount() == 0
                ? "No available ambulances. Refresh or release completed ambulances."
                : ambulanceComboBox.getItemCount() + " ambulance(s) available.");
        updateButtonStates();
    }

    private void releaseCompletedAmbulances() {
        AmbulanceReleaseService service = new AmbulanceReleaseService();
        List<Ambulance> ambulances = service.findReleasableAmbulances();

        if (ambulances.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No dispatched ambulances have completed assignments without active incidents.");
            return;
        }

        StringBuilder ids = new StringBuilder();
        for (Ambulance ambulance : ambulances) {
            if (ids.length() > 0) {
                ids.append(", ");
            }
            ids.append(ambulance.getAmbulanceId());
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Make these ambulances available: " + ids + "?\n"
                + "They have completed assignments and no active incidents.",
                "Release Completed Ambulances", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        int released = 0;
        for (Ambulance ambulance : ambulances) {
            if (service.releaseAmbulance(ambulance.getAmbulanceId())) {
                released++;
            }
        }
        refreshAll();
        JOptionPane.showMessageDialog(this, released + " ambulance(s) made available."
                + (released < ambulances.size()
                    ? "\nSome were no longer eligible; refresh and check their assignments."
                    : ""));
    }

    private void displaySelectedEmergency() {

        int selectedRow = emergencyTable.getSelectedRow();

        if (selectedRow < 0 ||
            selectedRow >= emergencies.size()) {

            detailsArea.setText("");
            updateButtonStates();
            return;
        }

        Emergency emergency = emergencies.get(selectedRow);

        StringBuilder details = new StringBuilder(emergency.getDetails());
        if (!emergency.getHospitalTransfers().isEmpty()) {
            details.append("\n\nHospital Transfers:");
            emergency.getHospitalTransfers().forEach(transfer ->
                    details.append("\nTransfer ID: ").append(transfer.getTransferId())
                            .append(" | Status: ").append(transfer.getTransferStatus()));
        }
        detailsArea.setText(details.toString());

        typeField.setText(
                emergency.getType() == null
                        ? "" : emergency.getType()
        );

        if (emergency.getSeverity() != null) {

            severityComboBox.setSelectedItem(
                    emergency.getSeverity()
            );
        }

        updateButtonStates();
    }

    private Emergency getSelectedEmergency() {

        int selectedRow = emergencyTable.getSelectedRow();

        if (selectedRow < 0 ||
            selectedRow >= emergencies.size()) {

            return null;
        }

        return emergencies.get(selectedRow);
    }

    private String getSelectedIncidentId() {

        Emergency emergency = getSelectedEmergency();

        return emergency == null
                ? null : emergency.getIncidentId();
    }

    private void classifySelectedEmergency() {

        Emergency emergency = getSelectedEmergency();

        if (emergency == null) {
            return;
        }

        String type = typeField.getText().trim();

        Severity severity =
                (Severity) severityComboBox.getSelectedItem();

        operator.classifyEmergency(
                emergency,
                type,
                severity
        );

        JOptionPane.showMessageDialog(
                this,
                "Emergency classification saved.",
                "Classification",
                JOptionPane.INFORMATION_MESSAGE
        );

        refreshEmergencies(emergency.getIncidentId());
    }

    private void verifySelectedEmergency() {

        Emergency emergency = getSelectedEmergency();

        if (emergency == null) {
            return;
        }

        boolean verified =
                operator.reviewAndVerifyEmergency(emergency);

        if (verified) {

            JOptionPane.showMessageDialog(
                    this,
                    "Emergency verified successfully.",
                    "Verification",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Emergency could not be verified. "
                    + "Check its status and classification.",
                    "Verification Failed",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        refreshEmergencies(emergency.getIncidentId());
    }

    private void assignSelectedAmbulance() {

        Emergency emergency = getSelectedEmergency();

        Ambulance ambulance =
                (Ambulance) ambulanceComboBox.getSelectedItem();

        if (emergency == null || ambulance == null) {
            return;
        }

        if (emergency.getStatus() != EmergencyStatus.VERIFIED) {

            JOptionPane.showMessageDialog(
                    this,
                    "Select a verified emergency first.",
                    "Assignment",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!ambulance.checkAvailability()) {

            JOptionPane.showMessageDialog(
                    this,
                    "The selected ambulance is not available.",
                    "Assignment",
                    JOptionPane.WARNING_MESSAGE
            );

            refreshAmbulances();
            return;
        }

        // Use the existing backend assignment method.
        operator.assignAmbulance(emergency, ambulance);

        if (emergency.getStatus() != EmergencyStatus.RESPONDING ||
            ambulance.getStatus() != AmbulanceStatus.DISPATCHED) {

            JOptionPane.showMessageDialog(
                    this,
                    "The ambulance assignment was not completed.",
                    "Assignment Failed",
                    JOptionPane.WARNING_MESSAGE
            );

            refreshAll();
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Ambulance assigned successfully.\n"
                + "Incident: " + emergency.getIncidentId()
                + "\nAmbulance: " + ambulance.getLicensePlate()
                + "\nEmergency status: RESPONDING",
                "Assignment Successful",
                JOptionPane.INFORMATION_MESSAGE
        );

        refreshEmergencies(emergency.getIncidentId());
        refreshAmbulances();
    }

    private void closeSelectedEmergency() {

        Emergency emergency = getSelectedEmergency();

        if (emergency == null) {
            return;
        }

        if (emergency.getStatus() != EmergencyStatus.RESOLVED) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to close emergency "
                        + emergency.getIncidentId() + "?",
                "Confirm Emergency Closure",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        // Use the existing Operator business method.
        operator.closeEmergency(emergency);

        if (emergency.getStatus() == EmergencyStatus.CLOSED) {

            JOptionPane.showMessageDialog(
                    this,
                    "Emergency " + emergency.getIncidentId()
                    + " has been closed successfully.",
                    "Emergency Closed",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "The emergency could not be closed.",
                    "Closure Failed",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        refreshEmergencies(emergency.getIncidentId());
    }

    private void updateButtonStates() {

        Emergency emergency = getSelectedEmergency();

        boolean selected = emergency != null;

        classifyButton.setEnabled(
                selected &&
                emergency.getStatus() == EmergencyStatus.REPORTED
        );

        verifyButton.setEnabled(
                selected &&
                emergency.getStatus() == EmergencyStatus.REPORTED &&
                emergency.getType() != null &&
                emergency.getSeverity() != null
        );

        Ambulance selectedAmbulance =
                (Ambulance) ambulanceComboBox.getSelectedItem();

        assignAmbulanceButton.setEnabled(
                selected &&
                emergency.getStatus() == EmergencyStatus.VERIFIED &&
                selectedAmbulance != null &&
                selectedAmbulance.checkAvailability()
        );

        closeEmergencyButton.setEnabled(
                selected &&
                emergency.getStatus() == EmergencyStatus.RESOLVED
        );
    }
}