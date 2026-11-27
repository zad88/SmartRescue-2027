package smartrescue.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import smartrescue.data.EmergencyRepository;
import smartrescue.data.IdGenerator;
import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceTeam;
import smartrescue.emergency.AssistanceRecord;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;
import smartrescue.hospital.Hospital;
import smartrescue.transfer.HospitalTransfer;
import smartrescue.transfer.Patient;

public class AmbulanceConsoleFrame extends JFrame {

    private final AmbulanceTeam team;
    private final Ambulance ambulance;
    private final EmergencyRepository emergencyRepository;

    private JComboBox<Emergency> emergencyComboBox;
    private JComboBox<Hospital> hospitalComboBox;

    private JTextArea detailsArea;
    private JTextField specialtyField;
    private JTextField minBedsField;

    private JButton updateStatusButton;
    private JButton assistanceButton;
    private JButton registerPatientButton;
    private JButton searchHospitalButton;
    private JButton transferPatientsButton;
    private JButton resolveEmergencyButton;

    public AmbulanceConsoleFrame(AmbulanceTeam team,
                                 Ambulance ambulance) {

        this.team = team;
        this.ambulance = ambulance;
        this.emergencyRepository = new EmergencyRepository();

        setTitle("SmartRescue 2027 - Ambulance Console");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeComponents();
        refreshEmergencies();
    }

    private void initializeComponents() {

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(4, 1, 5, 5));

        topPanel.add(new JLabel(
                "Ambulance: " + ambulance.getLicensePlate()
                + " | ID: " + ambulance.getAmbulanceId()
                + " | Location: " + ambulance.getCurrentLocation()
        ));

        emergencyComboBox = new JComboBox<>();

        ListCellRenderer<? super Emergency> emergencyRenderer =
                (list, emergency, index, selected, focused) -> {

            JLabel label = new JLabel();

            if (emergency != null) {
                label.setText(
                        emergency.getIncidentId()
                        + " - " + emergency.getStatus()
                        + " - " + emergency.getLocation()
                );
            }

            if (selected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }

            return label;
        };

        emergencyComboBox.setRenderer(emergencyRenderer);
        emergencyComboBox.addActionListener(
                event -> displaySelectedEmergency()
        );

        topPanel.add(emergencyComboBox);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        specialtyField = new JTextField(12);
        minBedsField = new JTextField("1", 5);

        searchHospitalButton = new JButton("Search Hospitals");
        searchHospitalButton.addActionListener(
                event -> searchHospitals()
        );

        searchPanel.add(new JLabel("Specialty:"));
        searchPanel.add(specialtyField);
        searchPanel.add(new JLabel("Minimum Beds:"));
        searchPanel.add(minBedsField);
        searchPanel.add(searchHospitalButton);

        topPanel.add(searchPanel);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        hospitalComboBox = new JComboBox<>();

        ListCellRenderer<? super Hospital> hospitalRenderer =
                (list, hospital, index, selected, focused) -> {

            JLabel label = new JLabel();

            if (hospital != null) {
                label.setText(
                        hospital.getName()
                        + " | " + hospital.getLocation()
                        + " | Beds: " + hospital.getAvailableBeds()
                        + " | ID: " + hospital.getHospitalId()
                );
            }

            if (selected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }

            return label;
        };

        hospitalComboBox.setRenderer(hospitalRenderer);
        hospitalComboBox.addActionListener(event -> {
            Hospital selectedHospital =
                    (Hospital) hospitalComboBox.getSelectedItem();

            if (selectedHospital != null) {
                team.selectHospital(selectedHospital);
            }

            displaySelectedEmergency();
        });

        selectionPanel.add(new JLabel("Matching Hospitals:"));
        selectionPanel.add(hospitalComboBox);

        topPanel.add(selectionPanel);

        add(topPanel, BorderLayout.NORTH);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        add(new JScrollPane(detailsArea), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh Emergencies");
        refreshButton.addActionListener(
                event -> refreshEmergencies()
        );

        updateStatusButton =
                new JButton("Update Status to ON_SCENE");
        updateStatusButton.addActionListener(
                event -> updateEmergencyStatus()
        );

        assistanceButton =
                new JButton("Record On-Scene Assistance");
        assistanceButton.addActionListener(
                event -> recordOnSceneAssistance()
        );

        registerPatientButton = new JButton("Register Patient");
        registerPatientButton.addActionListener(
                event -> registerPatient()
        );

        transferPatientsButton = new JButton("Transfer Patients");
        transferPatientsButton.addActionListener(
                event -> transferPatients()
        );

        resolveEmergencyButton = new JButton("Resolve Emergency");
        resolveEmergencyButton.addActionListener(
                event -> resolveEmergency()
        );

        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.add(refreshButton);
        buttonPanel.add(updateStatusButton);
        buttonPanel.add(assistanceButton);
        buttonPanel.add(registerPatientButton);
        buttonPanel.add(transferPatientsButton);
        buttonPanel.add(resolveEmergencyButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshEmergencies() {

        emergencyComboBox.removeAllItems();

        List<Emergency> emergencies =
                emergencyRepository.loadAllEmergencies();

        for (Emergency emergency : emergencies) {

            if (emergency.getAmbulance() != null
                    && emergency.getAmbulance().getAmbulanceId()
                            == ambulance.getAmbulanceId()) {

                emergencyComboBox.addItem(emergency);
            }
        }

        if (emergencyComboBox.getItemCount() == 0) {
            detailsArea.setText(
                    "No emergencies are currently assigned to this ambulance."
            );
        }

        displaySelectedEmergency();
    }

    private void displaySelectedEmergency() {

        Emergency emergency =
                (Emergency) emergencyComboBox.getSelectedItem();

        if (emergency == null) {
            updateStatusButton.setEnabled(false);
            assistanceButton.setEnabled(false);
            registerPatientButton.setEnabled(false);
            transferPatientsButton.setEnabled(false);
            resolveEmergencyButton.setEnabled(false);
            return;
        }

        StringBuilder details = new StringBuilder(
                team.viewEmergencyDetails(emergency)
        );

        details.append("\n\nOn-Scene Assistance Records:");

        List<AssistanceRecord> records =
                emergency.getAssistanceRecords();

        if (records.isEmpty()) {
            details.append("\nNo assistance records yet.");
        } else {
            for (AssistanceRecord record : records) {
                details.append("\n\nRecord ID: ")
                        .append(record.getRecordId())
                        .append("\nDescription: ")
                        .append(record.getDescription())
                        .append("\nRecorded At: ")
                        .append(record.getRecordedAt())
                        .append("\nPatients Treated: ")
                        .append(record.getPatientsTreated());
            }
        }

        Hospital selectedHospital =
                (Hospital) hospitalComboBox.getSelectedItem();

        if (selectedHospital != null) {
            details.append("\n\nSelected Hospital:")
                    .append("\nName: ")
                    .append(selectedHospital.getName())
                    .append("\nLocation: ")
                    .append(selectedHospital.getLocation())
                    .append("\nAvailable Beds: ")
                    .append(selectedHospital.getAvailableBeds())
                    .append("\nEmergency Capacity: ")
                    .append(selectedHospital.getEmergencyCapacity());
        }

        details.append("\n\nRegistered Patients:");

        List<Patient> patients = emergency.getPatients();

        if (patients.isEmpty()) {
            details.append("\nNo individual patient records registered.");
        } else {
            for (Patient patient : patients) {
                details.append("\n\nPatient ID: ")
                        .append(patient.getPatientId())
                        .append("\nName: ").append(patient.getName())
                        .append("\nAge: ").append(patient.getAge())
                        .append("\nCondition: ").append(patient.getCondition())
                        .append("\nTreatment Status: ")
                        .append(patient.getTreatmentStatus());
            }
        }

        detailsArea.setText(details.toString());

        updateStatusButton.setEnabled(
                emergency.getStatus() == EmergencyStatus.RESPONDING
        );

        assistanceButton.setEnabled(
                emergency.getStatus() == EmergencyStatus.ON_SCENE
        );

        registerPatientButton.setEnabled(
                emergency.getStatus() == EmergencyStatus.ON_SCENE
                && emergency.getPatients().size() < emergency.getPatientCount()
                && emergency.getHospitalTransfers().isEmpty()
        );

        transferPatientsButton.setEnabled(
                emergency.getStatus() == EmergencyStatus.ON_SCENE
                && selectedHospital != null
                && emergency.getPatients().size() == emergency.getPatientCount()
                && emergency.getHospitalTransfers().isEmpty()
        );

        resolveEmergencyButton.setEnabled(
                emergency.getStatus() == EmergencyStatus.ON_SCENE
        );
    }

    private void searchHospitals() {

        String specialty = specialtyField.getText().trim();
        String minBedsText = minBedsField.getText().trim();

        if (specialty.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a medical specialty."
            );
            return;
        }

        int minBeds;

        try {
            minBeds = Integer.parseInt(minBedsText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Minimum beds must be an integer."
            );
            return;
        }

        hospitalComboBox.removeAllItems();

        List<Hospital> hospitals =
                team.searchHospital(specialty, minBeds);

        for (Hospital hospital : hospitals) {
            hospitalComboBox.addItem(hospital);
        }

        if (hospitals.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No hospitals matched your search."
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    hospitals.size() + " matching hospital(s) found."
            );
        }

        displaySelectedEmergency();
    }

    private void updateEmergencyStatus() {

        Emergency emergency =
                (Emergency) emergencyComboBox.getSelectedItem();

        if (emergency == null) {
            return;
        }

        team.updateEmergencyStatus(
                emergency,
                EmergencyStatus.ON_SCENE
        );

        if (emergency.getStatus() == EmergencyStatus.ON_SCENE) {

            JOptionPane.showMessageDialog(
                    this,
                    "Emergency status updated to ON_SCENE."
            );

            refreshEmergencies();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "The emergency status was not updated."
            );
        }
    }

    private void registerPatient() {

        Emergency emergency =
                (Emergency) emergencyComboBox.getSelectedItem();

        if (emergency == null
                || emergency.getStatus() != EmergencyStatus.ON_SCENE) {
            return;
        }

        if (emergency.getPatients().size() >= emergency.getPatientCount()) {
            JOptionPane.showMessageDialog(
                    this,
                    "All reported patients have already been registered.",
                    "Patient Limit Reached",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!emergency.getHospitalTransfers().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Patients cannot be registered after a transfer has been created.",
                    "Transfer Already Exists",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JTextField nameField = new JTextField(15);
        JTextField ageField = new JTextField(5);
        JTextField conditionField = new JTextField(15);
        JTextField treatmentStatusField = new JTextField("Pending", 12);

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));

        form.add(new JLabel("Patient Name:"));
        form.add(nameField);
        form.add(new JLabel("Age:"));
        form.add(ageField);
        form.add(new JLabel("Condition:"));
        form.add(conditionField);
        form.add(new JLabel("Treatment Status:"));
        form.add(treatmentStatusField);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Register Patient - " + emergency.getIncidentId(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String condition = conditionField.getText().trim();
        String treatmentStatus = treatmentStatusField.getText().trim();

        if (name.isEmpty() || condition.isEmpty()
                || treatmentStatus.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please complete all patient fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int age;

        try {
            age = Integer.parseInt(ageField.getText().trim());

            if (age < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid non-negative age.",
                    "Invalid Age",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int patientId = IdGenerator.nextPatientId();

        Patient patient = new Patient(
                patientId,
                name,
                age,
                condition,
                treatmentStatus,
                emergency.getIncidentId()
        );

        if (team.registerPatient(emergency, patient)) {

            emergencyRepository.saveEmergency(emergency);

            JOptionPane.showMessageDialog(
                    this,
                    "Patient registered successfully."
                    + "\nPatient ID: " + patientId
                    + "\nEmergency: " + emergency.getIncidentId()
            );

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Patient registration was rejected.",
                    "Registration Failed",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        refreshEmergencies();
    }

    private void recordOnSceneAssistance() {

        Emergency emergency =
                (Emergency) emergencyComboBox.getSelectedItem();

        if (emergency == null
                || emergency.getStatus() != EmergencyStatus.ON_SCENE) {
            return;
        }

        AssistanceRecord record =
                team.provideOnSceneAssistance(emergency);

        JOptionPane.showMessageDialog(
                this,
                "On-scene assistance recorded successfully."
                + "\nRecord ID: " + record.getRecordId()
                + "\nRecorded At: " + record.getRecordedAt()
                + "\nPatients Treated: " + record.getPatientsTreated()
        );

        refreshEmergencies();
    }

    private void transferPatients() {

        Emergency emergency =
                (Emergency) emergencyComboBox.getSelectedItem();

        Hospital hospital =
                (Hospital) hospitalComboBox.getSelectedItem();

        if (emergency == null || hospital == null) {
            return;
        }

        if (emergency.getStatus() != EmergencyStatus.ON_SCENE) {
            return;
        }

        if (!emergency.getHospitalTransfers().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "A patient transfer has already been created for this emergency.",
                    "Transfer Already Exists",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            HospitalTransfer transfer =
                    team.transferPatients(emergency, hospital);

            JOptionPane.showMessageDialog(
                    this,
                    "Patient transfer created successfully."
                    + "\nTransfer ID: " + transfer.getTransferId()
                    + "\nPatient Count: " + transfer.getPatientCount()
                    + "\nTransfer Status: " + transfer.getTransferStatus()
            );

        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Transfer Already Exists",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        refreshEmergencies();
    }

    private void resolveEmergency() {

        Emergency emergency =
                (Emergency) emergencyComboBox.getSelectedItem();

        if (emergency == null
                || emergency.getStatus() != EmergencyStatus.ON_SCENE) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to resolve emergency "
                        + emergency.getIncidentId() + "?",
                "Confirm Emergency Resolution",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        // Call the existing AmbulanceTeam business method.
        team.resolveEmergency(emergency);

        if (emergency.getStatus() == EmergencyStatus.RESOLVED) {

            // Persist only after the backend successfully resolves it.
            emergencyRepository.saveEmergency(emergency);

            JOptionPane.showMessageDialog(
                    this,
                    "Emergency " + emergency.getIncidentId()
                    + " has been resolved successfully."
            );

            refreshEmergencies();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "The emergency could not be resolved.\n\n"
                    + "Please check that:\n"
                    + "- The emergency is ON_SCENE.\n"
                    + "- At least one assistance record exists.\n"
                    + "- All associated hospital transfers are CONFIRMED.",
                    "Resolution Requirements Not Met",
                    JOptionPane.WARNING_MESSAGE
            );

            refreshEmergencies();
        }
    }
}