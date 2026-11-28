package smartrescue.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.JTextField;

import smartrescue.emergency.Emergency;
import smartrescue.hospital.Hospital;
import smartrescue.hospital.HospitalStaff;
import smartrescue.transfer.HospitalTransfer;
import smartrescue.service.PortalService;

public class HospitalPortalFrame extends JFrame {

    private final PortalService service;

    private JComboBox<Hospital> hospitalComboBox;
    private JComboBox<HospitalTransfer> transferComboBox;

    private JLabel hospitalDetailsLabel;
    private JTextArea detailsArea;

    private JButton confirmButton;

    private HospitalStaff hospitalStaff;
    private boolean refreshingHospitals;

    public HospitalPortalFrame() {

        service = new PortalService();

        setTitle("SmartRescue 2027 - Hospital Portal");
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeComponents();
        refreshHospitals();
        AutoRefresh.start(this, service, this::refreshHospitals);
    }

    private void initializeComponents() {

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topPanel.add(new JLabel("Hospital:"));

        hospitalComboBox = new JComboBox<>();

        ListCellRenderer<? super Hospital> hospitalRenderer =
                (list, hospital, index, selected, focused) -> {

            JLabel label = new JLabel();

            if (hospital != null) {
                label.setText(
                        hospital.getName()
                        + " | " + hospital.getLocation()
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
        hospitalComboBox.addActionListener(
                event -> hospitalSelectionChanged()
        );

        topPanel.add(hospitalComboBox);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(
                event -> refreshHospitals()
        );

        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        hospitalDetailsLabel = new JLabel("Hospital information");

        centerPanel.add(hospitalDetailsLabel, BorderLayout.NORTH);

        transferComboBox = new JComboBox<>();

        ListCellRenderer<? super HospitalTransfer> transferRenderer =
                (list, transfer, index, selected, focused) -> {

            JLabel label = new JLabel();

            if (transfer != null) {
                label.setText(
                        "Transfer ID: " + transfer.getTransferId()
                        + " | Emergency: " + transfer.getEmergencyId()
                        + " | Patients: " + transfer.getPatientCount()
                        + " | " + transfer.getTransferStatus()
                );
            }

            if (selected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }

            return label;
        };

        transferComboBox.setRenderer(transferRenderer);
        transferComboBox.addActionListener(
                event -> displaySelectedTransfer()
        );

        JPanel transferPanel = new JPanel(new BorderLayout(5, 5));
        transferPanel.add(
                new JLabel("Incoming Patient Transfers:"),
                BorderLayout.NORTH
        );
        transferPanel.add(transferComboBox, BorderLayout.CENTER);

        centerPanel.add(transferPanel, BorderLayout.CENTER);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        centerPanel.add(
                new JScrollPane(detailsArea),
                BorderLayout.SOUTH
        );

        add(centerPanel, BorderLayout.CENTER);

        confirmButton = new JButton("Confirm Transfer");
        confirmButton.setEnabled(false);
        confirmButton.addActionListener(
                event -> confirmSelectedTransfer()
        );

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton updateHospitalButton = new JButton("Update Hospital Information");
        updateHospitalButton.addActionListener(event -> updateHospitalInformation());
        bottomPanel.add(updateHospitalButton);
        bottomPanel.add(confirmButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void refreshHospitals() {

        HospitalTransfer previousTransfer =
                (HospitalTransfer) transferComboBox.getSelectedItem();
        Integer previousTransferId = previousTransfer == null
                ? null : previousTransfer.getTransferId();
        refreshingHospitals = true;

        Integer previousHospitalId = null;

        Hospital previousHospital =
                (Hospital) hospitalComboBox.getSelectedItem();

        if (previousHospital != null) {
            previousHospitalId = previousHospital.getHospitalId();
        }

        hospitalComboBox.removeAllItems();

        List<Hospital> hospitals =
                service.loadHospitals();

        int selectedIndex = -1;

        for (int i = 0; i < hospitals.size(); i++) {

            Hospital hospital = hospitals.get(i);
            hospitalComboBox.addItem(hospital);

            if (previousHospitalId != null
                    && hospital.getHospitalId() == previousHospitalId) {
                selectedIndex = i;
            }
        }

        if (selectedIndex >= 0) {
            hospitalComboBox.setSelectedIndex(selectedIndex);
        } else if (hospitalComboBox.getItemCount() > 0) {
            hospitalComboBox.setSelectedIndex(0);
        }

        refreshingHospitals = false;

        hospitalSelectionChanged();
        if (previousTransferId != null) {
            for (int i = 0; i < transferComboBox.getItemCount(); i++) {
                if (transferComboBox.getItemAt(i).getTransferId() == previousTransferId) {
                    transferComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void hospitalSelectionChanged() {

        if (refreshingHospitals) {
            return;
        }

        Hospital selectedHospital =
                (Hospital) hospitalComboBox.getSelectedItem();

        transferComboBox.removeAllItems();
        detailsArea.setText("");
        confirmButton.setEnabled(false);

        if (selectedHospital == null) {
            hospitalDetailsLabel.setText("No hospital selected.");
            hospitalStaff = null;
            return;
        }

        hospitalStaff = service.loadStaff(selectedHospital);

        updateHospitalDetails(selectedHospital);
        refreshTransfers();
    }

    private void updateHospitalDetails(Hospital hospital) {

        hospitalDetailsLabel.setText(
                "Hospital: " + hospital.getName()
                + " | Location: " + hospital.getLocation()
                + " | Available Beds: " + hospital.getAvailableBeds()
                + " | Emergency Capacity: " + hospital.getEmergencyCapacity()
        );
    }

    private void updateHospitalInformation() {
        Hospital hospital = (Hospital) hospitalComboBox.getSelectedItem();
        if (hospital == null || hospitalStaff == null) {
            JOptionPane.showMessageDialog(this, "Select a hospital first.");
            return;
        }
        JTextField beds = new JTextField(String.valueOf(hospital.getAvailableBeds()));
        JTextField capacity = new JTextField(String.valueOf(hospital.getEmergencyCapacity()));
        JTextField specialties = new JTextField(String.join("; ", hospital.getMedicalSpecialties()));
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("Available beds:"));
        form.add(beds);
        form.add(new JLabel("Emergency capacity:"));
        form.add(capacity);
        form.add(new JLabel("Specialties (separated by ;):"));
        form.add(specialties);
        if (JOptionPane.showConfirmDialog(this, form, "Update Hospital Information",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        int bedCount;
        int capacityCount;
        try {
            bedCount = Integer.parseInt(beds.getText().trim());
            capacityCount = Integer.parseInt(capacity.getText().trim());
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Beds and capacity must be whole numbers.");
            return;
        }
        List<String> values = new ArrayList<>();
        for (String value : specialties.getText().split(";", -1)) {
            values.add(value.trim());
        }
        try {
            hospitalStaff.updateHospitalInfo(hospital, bedCount, capacityCount, values);
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Invalid Hospital Information", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Hospital Update Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        refreshHospitals();
        JOptionPane.showMessageDialog(this, "Hospital information updated.");
    }

    private void refreshTransfers() {

        transferComboBox.removeAllItems();

        Hospital selectedHospital =
                (Hospital) hospitalComboBox.getSelectedItem();

        if (selectedHospital == null) {
            return;
        }

        List<Emergency> emergencies =
                service.loadEmergencies();

        List<HospitalTransfer> matchingTransfers = new ArrayList<>();

        for (Emergency emergency : emergencies) {

            for (HospitalTransfer transfer :
                    emergency.getHospitalTransfers()) {

                Hospital transferHospital = transfer.getHospital();

                if (transferHospital != null
                        && transferHospital.getHospitalId()
                                == selectedHospital.getHospitalId()) {

                    matchingTransfers.add(transfer);
                }
            }
        }

        for (HospitalTransfer transfer : matchingTransfers) {
            transferComboBox.addItem(transfer);
        }

        if (matchingTransfers.isEmpty()) {
            detailsArea.setText(
                    "There are no incoming transfers for this hospital."
            );
        }

        displaySelectedTransfer();
    }

    private void displaySelectedTransfer() {

        HospitalTransfer transfer =
                (HospitalTransfer) transferComboBox.getSelectedItem();

        if (transfer == null) {
            confirmButton.setEnabled(false);
            return;
        }

        StringBuilder details = new StringBuilder();

        details.append("Transfer ID: ")
                .append(transfer.getTransferId());

        details.append("\nEmergency ID: ")
                .append(transfer.getEmergencyId());

        details.append("\nTransfer Time: ")
                .append(transfer.getTransferTime());

        details.append("\nPatient Count: ")
                .append(transfer.getPatientCount());

        details.append("\nTransfer Status: ")
                .append(transfer.getTransferStatus());

        Hospital hospital = transfer.getHospital();

        if (hospital != null) {
            details.append("\nHospital: ")
                    .append(hospital.getName());

            details.append("\nHospital Location: ")
                    .append(hospital.getLocation());
        }

        details.append("\n\nLinked Patient Records: ")
                .append(transfer.getPatients().size());

        if (transfer.getPatients().isEmpty()) {
            details.append(
                    "\nNo individual patient records are linked to this transfer."
            );
        }

        detailsArea.setText(details.toString());

        confirmButton.setEnabled(
                "PENDING".equals(transfer.getTransferStatus())
                && transfer.getPatients().size() == transfer.getPatientCount()
        );
    }

    private void confirmSelectedTransfer() {

        HospitalTransfer transfer =
                (HospitalTransfer) transferComboBox.getSelectedItem();

        if (transfer == null || hospitalStaff == null) {
            return;
        }

        try {
            hospitalStaff.confirmPatientTransfer(transfer);
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Transfer Changed", JOptionPane.WARNING_MESSAGE);
            refreshHospitals();
            return;
        }

        if ("CONFIRMED".equals(transfer.getTransferStatus())) {

            JOptionPane.showMessageDialog(
                    this,
                    "Patient transfer confirmed successfully."
                    + "\nTransfer ID: " + transfer.getTransferId()
            );

            refreshHospitals();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "The transfer was not confirmed."
                    + "\nPlease check its status and hospital capacity.",
                    "Transfer Not Confirmed",
                    JOptionPane.WARNING_MESSAGE
            );

            refreshHospitals();
        }
    }
}