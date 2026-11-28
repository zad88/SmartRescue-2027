package smartrescue.dispatch;

import java.util.ArrayList;
import java.util.List;

import smartrescue.data.AmbulanceRepository;
import smartrescue.data.EmergencyRepository;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;

public class AmbulanceReleaseService {

    public List<Ambulance> findReleasableAmbulances() {
        List<Emergency> emergencies = new EmergencyRepository().loadAllEmergencies();
        List<Ambulance> releasable = new ArrayList<>();

        for (Ambulance ambulance : new AmbulanceRepository().loadAllAmbulances()) {
            if (ambulance.getStatus() != AmbulanceStatus.DISPATCHED) {
                continue;
            }

            boolean hasCompleted = false;
            boolean hasActive = false;
            for (Emergency emergency : emergencies) {
                if (emergency.getAmbulance() == null
                        || emergency.getAmbulance().getAmbulanceId()
                                != ambulance.getAmbulanceId()) {
                    continue;
                }

                if (emergency.getStatus() == EmergencyStatus.RESOLVED
                        || emergency.getStatus() == EmergencyStatus.CLOSED) {
                    hasCompleted = true;
                } else {
                    hasActive = true;
                }
            }

            if (hasCompleted && !hasActive) {
                releasable.add(ambulance);
            }
        }

        return releasable;
    }

    public boolean releaseAmbulance(int ambulanceId) {
        for (Ambulance ambulance : findReleasableAmbulances()) {
            if (ambulance.getAmbulanceId() == ambulanceId) {
                ambulance.release();
                new AmbulanceRepository().saveAmbulance(ambulance);
                return true;
            }
        }
        return false;
    }
}
