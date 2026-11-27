package smartrescue.dispatch;

import smartrescue.Person;
import smartrescue.data.EmergencyRepository;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;
import smartrescue.emergency.Severity;

public class Operator extends Person {

    private int operatorId;
    private String shiftTime;
    private int stationId;

    public Operator(int operatorId, String fullName,
                    String shiftTime, int stationId) {

        super(fullName);

        this.operatorId = operatorId;
        this.shiftTime = shiftTime;
        this.stationId = stationId;
    }

    public void classifyEmergency(Emergency emergency,
                                  String type, Severity severity) {

        emergency.setClassification(type, severity);

        EmergencyRepository repository = new EmergencyRepository();
        repository.saveEmergency(emergency);
    }

    public boolean reviewAndVerifyEmergency(Emergency emergency) {

        if (emergency.getStatus() != EmergencyStatus.REPORTED) {
            return false;
        }

        if (emergency.getType() == null ||
            emergency.getSeverity() == null) {
            return false;
        }

        emergency.changeStatus(EmergencyStatus.VERIFIED);

        EmergencyRepository repository = new EmergencyRepository();
        repository.saveEmergency(emergency);

        return true;
    }

    public void assignAmbulance(Emergency emergency, Ambulance ambulance) {

        if (emergency.getStatus() != EmergencyStatus.VERIFIED) {
            return;
        }

        if (!ambulance.checkAvailability()) {
            return;
        }

        ambulance.assignToEmergency(emergency);
        emergency.changeStatus(EmergencyStatus.RESPONDING);

        EmergencyRepository repository = new EmergencyRepository();
        repository.saveEmergency(emergency);
    }

    public void closeEmergency(Emergency emergency) {

        if (emergency.getStatus() == EmergencyStatus.RESOLVED) {
            emergency.changeStatus(EmergencyStatus.CLOSED);

            EmergencyRepository repository = new EmergencyRepository();
            repository.saveEmergency(emergency);
        }
    }
}
