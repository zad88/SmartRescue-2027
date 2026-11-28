package smartrescue.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceTeam;
import smartrescue.emergency.AssistanceRecord;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;
import smartrescue.hospital.Hospital;
import smartrescue.transfer.HospitalTransfer;
import smartrescue.transfer.Patient;
import smartrescue.service.PortalService;

public class AmbulanceConsoleFrame extends JFrame {

    private final AmbulanceTeam team;
    private final Ambulance ambulance;
    private final PortalService service;

    private JComboBox<Emergency> emergencyComboBox;
    private JComboBox<Hospital> hospitalComboBox;
    private JComboBox<String> specialtyComboBox;

    private JTextArea detailsArea;
    private JTextField minBedsField;
    private boolean refreshingSpecialties;

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
        this.service = new PortalService();

        setTitle("SmartRescue 2027 - Ambulance Console");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeComponents();
        refreshSpecialties(service.loadHospitals());
        refreshEmergencies();
        AutoRefresh.start(this, service, this::refreshConsole);
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
                        + (emergency.getAmbulance() == null
                                ? " - Unassigned" : "")
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

        specialtyComboBox = new JComboBox<>();
        specialtyComboBox.setPrototypeDisplayValue("Choose a specialty");
        specialtyComboBox.addActionListener(event -> {
            if (!refreshingSpecialties) {
                hospitalComboBox.removeAllItems();
                team.selectHospital(null);
                displaySelectedEmergency();
            }
        });
        minBedsField = new JTextField("1", 5);

        searchHospitalButton = new JButton("Search Hospitals");
        searchHospitalButton.addActionListener(
                event -> searchHospitals()
        );

        searchPanel.add(new JLabel("Specialty:"));
        searchPanel.add(specialtyComboBox);
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

            team.selectHospital(selectedHospital);

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

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 5, 5));

        buttonPanel.add(refreshButton);
        buttonPanel.add(updateStatusButton);
        buttonPanel.add(assistanceButton);
        buttonPanel.add(registerPatientButton);
        buttonPanel.add(transferPatientsButton);
        buttonPanel.add(resolveEmergencyButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshEmergencies() {

        Emergency selected = (Emergency) emergencyComboBox.getSelectedItem();
        String selectedId = selected == null ? null : selected.getIncidentId();
        emergencyComboBox.removeAllItems();

        List<Emergency> emergencies =
                service.loadEmergencies();

        for (Emergency emergency : emergencies) {

            if (isVisibleToAmbulance(emergency, ambulance)) {
                emergencyComboBox.addItem(emergency);
                if (emergency.getIncidentId().equals(selectedId)) {
                    emergencyComboBox.setSelectedItem(emergency);
                }
            }
        }

        if (emergencyComboBox.getItemCount() == 0) {
            detailsArea.setText(
                    "No active unassigned emergencies or emergencies assigned to this ambulance."
            );
        }

        displaySelectedEmergency();
    }

    private void refreshConsole() {
        List<Hospital> hospitals = service.loadHospitals();
        refreshSpecialties(hospitals);
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < hospitalComboBox.getItemCount(); i++) {
            ids.add(hospitalComboBox.getItemAt(i).getHospitalId());
        }
        Hospital selected = (Hospital) hospitalComboBox.getSelectedItem();
        Integer selectedId = selected == null ? null : selected.getHospitalId();
        if (!ids.isEmpty()) {
            hospitalComboBox.removeAllItems();
            for (Hospital hospital : hospitals) {
                if (ids.contains(hospital.getHospitalId())) {
                    hospitalComboBox.addItem(hospital);
                    if (Integer.valueOf(hospital.getHospitalId()).equals(selectedId)) {
                        hospitalComboBox.setSelectedItem(hospital);
                    }
                }
            }
        }
        refreshEmergencies();
    }

    static List<String> specialtiesFor(List<Hospital> hospitals) {
        Set<String> specialties = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Hospital hospital : hospitals) {
            for (String specialty : hospital.getMedicalSpecialties()) {
                if (!specialty.isBlank()) {
                    specialties.add(specialty.trim());
                }
            }
        }
        return new ArrayList<>(specialties);
    }

    private void refreshSpecialties(List<Hospital> hospitals) {
        List<String> specialties = specialtiesFor(hospitals);
        String previous = (String) specialtyComboBox.getSelectedItem();
        boolean unchanged = specialtyComboBox.getItemCount() == specialties.size();
        for (int i = 0; unchanged && i < specialties.size(); i++) {
            unchanged = specialties.get(i).equals(specialtyComboBox.getItemAt(i));
        }
        if (unchanged) {
            return;
        }

        refreshingSpecialties = true;
        specialtyComboBox.removeAllItems();
        for (String specialty : specialties) {
            specialtyComboBox.addItem(specialty);
            if (previous != null && specialty.equalsIgnoreCase(previous)) {
                specialtyComboBox.setSelectedItem(specialty);
            }
        }
        refreshingSpecialties = false;

        String selected = (String) specialtyComboBox.getSelectedItem();
        if (previous != null && (selected == null || !previous.equalsIgnoreCase(selected))) {
            hospitalComboBox.removeAllItems();
            team.selectHospital(null);
        }
    }

    static boolean isVisibleToAmbulance(Emergency emergency, Ambulance ambulance) {
        return emergency.getStatus() != EmergencyStatus.RESOLVED
                && emergency.getStatus() != EmergencyStatus.CLOSED
                && (emergency.getAmbulance() == null
                    || isAssignedToAmbulance(emergency, ambulance));
    }

    static boolean isAssignedToAmbulance(Emergency emergency, Ambulance ambulance) {
        return emergency.getAmbulance() != null
                && emergency.getAmbulance().getAmbulanceId()
                        == ambulance.getAmbulanceId();
    }

    static String transferProblem(Emergency emergency, Ambulance ambulance, Hospital hospital) {
        if (emergency == null) {
            return "Select an incident first.";
        }
        if (!isAssignedToAmbulance(emergency, ambulance)) {
            return "An operator must assign this ambulance to the incident.";
        }
        if (emergency.getStatus() != EmergencyStatus.ON_SCENE) {
            return "The incident must be ON_SCENE.";
        }
        if (!emergency.getHospitalTransfers().isEmpty()) {
            return "A transfer has already been created for this incident.";
        }
        if (emergency.getPatients().size() != emergency.getPatientCount()) {
            return "Register all reported patients first ("
                    + emergency.getPatients().size() + " of "
                    + emergency.getPatientCount() + " registered).";
        }
        if (hospital == null) {
            return "Choose a specialty, search hospitals and select a matching hospital.";
        }
        return null;
    }

    static String resolutionProblem(Emergency emergency, Ambulance ambulance) {
        if (emergency == null) {
            return "The incident was not found. Refresh and select it again.";
        }
        if (!isAssignedToAmbulance(emergency, ambulance)) {
            return "An operator must assign this ambulance to the incident.";
        }
        if (emergency.getStatus() != EmergencyStatus.ON_SCENE) {
            return "The incident must be ON_SCENE before it can be resolved.";
        }
        if (emergency.getAssistanceRecords().isEmpty()) {
            return "Record On-Scene Assistance before resolving this incident.";
        }
        for (HospitalTransfer transfer : emergency.getHospitalTransfers()) {
            if (!"CONFIRMED".equals(transfer.getTransferStatus())) {
                return "Hospital transfer " + transfer.getTransferId()
                        + " must be confirmed before resolving this incident.";
            }
        }
        return null;
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

        if (emergency.getAmbulance() == null) {
            details.append("\n\nUnassigned: an operator must assign this ambulance "
                    + "before the team can respond.");
        }

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

        details.append("\n\nHospital Transfers:");
        if (emergency.getHospitalTransfers().isEmpty()) {
            details.append("\nNo hospital transfer.");
        } else {
            for (HospitalTransfer transfer : emergency.getHospitalTransfers()) {
                details.append("\nTransfer ID: ").append(transfer.getTransferId())
                        .append(" | Status: ").append(transfer.getTransferStatus());
                if (transfer.getHospital() != null) {
                    details.append(" | Hospital: ").append(transfer.getHospital().getName());
                }
            }
        }

        String transferProblem = transferProblem(emergency, ambulance, selectedHospital);
        details.append("\n\nTransfer Patients: ")
                .append(transferProblem == null ? "Ready to transfer." : transferProblem);

        String resolutionProblem = resolutionProblem(emergency, ambulance);
        details.append("\n\nResolve Emergency: ")
                .append(resolutionProblem == null ? "Ready to resolve." : resolutionProblem);

        detailsArea.setText(details.toString());

        boolean assigned = isAssignedToAmbulance(emergency, ambulance);

        updateStatusButton.setEnabled(assigned &&
                emergency.getStatus() == EmergencyStatus.RESPONDING
        );

        assistanceButton.setEnabled(assigned &&
                emergency.getStatus() == EmergencyStatus.ON_SCENE
        );

        registerPatientButton.setEnabled(assigned &&
                emergency.getStatus() == EmergencyStatus.ON_SCENE
                && emergency.getPatients().size() < emergency.getPatientCount()
                && emergency.getHospitalTransfers().isEmpty()
        );

        transferPatientsButton.setEnabled(assigned &&
                emergency.getStatus() == EmergencyStatus.ON_SCENE
        );

        resolveEmergencyButton.setEnabled(assigned &&
                emergency.getStatus() == EmergencyStatus.ON_SCENE
        );
    }

    private void searchHospitals() {

        String specialty = (String) specialtyComboBox.getSelectedItem();
        String minBedsText = minBedsField.getText().trim();

        if (specialty == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No medical specialties are available. Update a hospital's specialties first."
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

        if (minBeds <= 0) {
            JOptionPane.showMessageDialog(this, "Minimum beds must be greater than zero.");
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

        int patientId = service.nextPatientId();

        Patient patient = new Patient(
                patientId,
                name,
                age,
                condition,
                treatmentStatus,
                emergency.getIncidentId()
        );

        if (team.registerPatient(emergency, patient)) {

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

        JTextField description = new JTextField(24);
        JTextField treated = new JTextField(
                String.valueOf(emergency.getPatientCount()), 5);
        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.add(new JLabel("Assistance provided:"));
        form.add(description);
        form.add(new JLabel("Patients treated:"));
        form.add(treated);
        if (JOptionPane.showConfirmDialog(this, form, "On-Scene Assistance",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        int count;
        try {
            count = Integer.parseInt(treated.getText().trim());
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Enter a valid number of patients treated.");
            return;
        }
        if (description.getText().isBlank() || count < 0
                || count > emergency.getPatientCount()) {
            JOptionPane.showMessageDialog(this,
                    "Enter assistance details and a patient count from 0 to "
                    + emergency.getPatientCount() + ".");
            return;
        }

        AssistanceRecord record = team.provideOnSceneAssistance(
                emergency, description.getText().trim(), count);

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

        String problem = transferProblem(emergency, ambulance, hospital);
        if (problem != null) {
            JOptionPane.showMessageDialog(
                    this,
                    problem,
                    "Transfer Not Ready",
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
                    "Transfer Failed",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        refreshEmergencies();
    }

    private void resolveEmergency() {

        Emergency emergency =
                (Emergency) emergencyComboBox.getSelectedItem();

        if (emergency == null) {
            return;
        }

        String incidentId = emergency.getIncidentId();
        emergency = service.findEmergencyById(incidentId);
        String problem = resolutionProblem(emergency, ambulance);
        if (problem != null) {
            JOptionPane.showMessageDialog(this, problem,
                    "Emergency Not Ready", JOptionPane.WARNING_MESSAGE);
            refreshEmergencies();
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

        emergency = service.findEmergencyById(incidentId);
        problem = resolutionProblem(emergency, ambulance);
        if (problem != null) {
            JOptionPane.showMessageDialog(this, problem,
                    "Emergency Not Ready", JOptionPane.WARNING_MESSAGE);
            refreshEmergencies();
            return;
        }

        // Call the existing AmbulanceTeam business method.
        team.resolveEmergency(emergency);

        if (emergency.getStatus() == EmergencyStatus.RESOLVED) {

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