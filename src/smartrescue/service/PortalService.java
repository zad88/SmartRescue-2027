package smartrescue.service;

import java.util.List;

import smartrescue.data.AmbulanceRepository;
import smartrescue.data.DemoActorRepository;
import smartrescue.data.DataChangeTracker;
import smartrescue.data.EmergencyRepository;
import smartrescue.data.HospitalRepository;
import smartrescue.data.IdGenerator;
import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceTeam;
import smartrescue.dispatch.Operator;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.Witness;
import smartrescue.hospital.Hospital;
import smartrescue.hospital.HospitalStaff;

public class PortalService {
    private final DataChangeTracker changeTracker = new DataChangeTracker();

    public boolean hasDataChanged() {
        return changeTracker.hasChanged();
    }

    public List<Emergency> loadEmergencies() {
        return new EmergencyRepository().loadAllEmergencies();
    }

    public Emergency findEmergencyById(String incidentId) {
        return new EmergencyRepository().findEmergencyById(incidentId);
    }

    public List<Ambulance> loadAmbulances() {
        return new AmbulanceRepository().loadAllAmbulances();
    }

    public List<Hospital> loadHospitals() {
        return new HospitalRepository().loadAllHospitals();
    }

    public int nextPatientId() {
        return IdGenerator.nextPatientId();
    }

    public Witness loadWitness() {
        return new DemoActorRepository().loadWitness();
    }

    public Operator loadOperator() {
        return new DemoActorRepository().loadOperator();
    }

    public AmbulanceTeam loadTeam(Ambulance ambulance) {
        return new DemoActorRepository().loadTeam(ambulance);
    }

    public HospitalStaff loadStaff(Hospital hospital) {
        return new DemoActorRepository().loadStaff(hospital);
    }
}
