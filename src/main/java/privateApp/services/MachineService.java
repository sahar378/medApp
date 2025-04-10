package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import privateApp.models.Machine;
import privateApp.models.Intervention;
import privateApp.repositories.MachineRepository;
import privateApp.repositories.InterventionRepository;

import java.util.List;

@Service
public class MachineService {

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private InterventionRepository interventionRepository;

    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    public Machine getMachineById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine non trouvée"));
    }

    public List<Machine> getMachinesByDisponibilite(int disponibilite) {
        return machineRepository.findByDisponibilite(disponibilite);
    }

    @Transactional
    public Machine addMachine(Machine machine) {
        return machineRepository.save(machine);
    }

    @Transactional
    public Machine updateDisponibilite(Long idMachine, int disponibilite) {
        Machine machine = getMachineById(idMachine);
        machine.setDisponibilite(disponibilite);

        if (disponibilite == 1) { // Si en intervention
            // Créer une intervention automatiquement (technicien par défaut ou à assigner)
            Intervention intervention = new Intervention();
            intervention.setMachine(machine);
            // Pour cet exemple, on suppose un technicien par défaut (à ajuster selon ton besoin)
            intervention.setNature(1); // Réparation par défaut
            intervention.setDatePanne(new java.util.Date()); // Date actuelle
            interventionRepository.save(intervention);
        }

        return machineRepository.save(machine);
    }
	 // Ajouter cette méthode
	 @Transactional
	 public void deleteMachine(Long id) {
	     Machine machine = getMachineById(id);
	     machineRepository.delete(machine);
 }
	 //archivage
	 @Transactional
	    public void archiveMachine(Long id) {
	        Machine machine = machineRepository.findById(id)
	                .orElseThrow(() -> new RuntimeException("Machine non trouvée"));
	        machine.setArchived(true);
	        machine.setDisponibilite(2); // Réformé lorsqu'archivé
	        machineRepository.save(machine);
	    }
	 @Transactional
	 public void updateMachine(Long id, Machine updatedMachine) {
	   Machine machine = getMachineById(id);
	   machine.setDateMiseEnService(updatedMachine.getDateMiseEnService());
	   machine.setDisponibilite(updatedMachine.getDisponibilite());
	   machine.setType(updatedMachine.getType());
	   machine.setConstructeur(updatedMachine.getConstructeur());
	   machine.setFournisseur(updatedMachine.getFournisseur());
	   machine.setCaracteristique(updatedMachine.getCaracteristique());
	   machine.setVoltage(updatedMachine.getVoltage());
	   machineRepository.save(machine);
	 }
	// privateApp.services/MachineService.java
	 public List<Machine> getNonArchivedMachines() {
	   return machineRepository.findByArchivedFalse();
	 }

	 public List<Machine> getArchivedMachines() {
	   return machineRepository.findByArchivedTrue();
	 }
}