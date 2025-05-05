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

    /**
     * Récupère toutes les machines.
     * @return Liste des machines.
     */
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    /**
     * Récupère une machine par son ID.
     * @param id L'ID de la machine.
     * @return La machine correspondante.
     */
    public Machine getMachineById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine non trouvée"));
    }

    /**
     * Récupère les machines par disponibilité.
     * @param disponibilite Statut (0 = disponible, 1 = en intervention, 2 = réformé).
     * @return Liste des machines correspondantes.
     */
    public List<Machine> getMachinesByDisponibilite(int disponibilite) {
        if (disponibilite < 0 || disponibilite > 2) {
            throw new IllegalArgumentException("Statut de disponibilité invalide");
        }
        return machineRepository.findByDisponibilite(disponibilite);
    }
    
    /**
     * Ajoute une nouvelle machine.
     * @param machine Les données de la machine.
     * @return La machine ajoutée.
     */
    @Transactional
    public Machine addMachine(Machine machine) {
        if (machine.getDisponibilite() < 0 || machine.getDisponibilite() > 2) {
            throw new IllegalArgumentException("Statut de disponibilité invalide");
        }
        return machineRepository.save(machine);
    }

    /**
     * Met à jour la disponibilité d'une machine.
     * - Crée une intervention si disponibilite = 1 (en intervention).
     * @param idMachine L'ID de la machine.
     * @param disponibilite Nouveau statut (0 = disponible, 1 = en intervention, 2 = réformé).
     * @return La machine mise à jour.
     */
    @Transactional
    public Machine updateDisponibilite(Long idMachine, int disponibilite) {
        if (disponibilite < 0 || disponibilite > 2) {
            throw new IllegalArgumentException("Statut de disponibilité invalide");
        }
        Machine machine = getMachineById(idMachine);
        machine.setDisponibilite(disponibilite);

        if (disponibilite == 1) { // Si en intervention
            Intervention intervention = new Intervention();
            intervention.setMachine(machine);
            intervention.setNature(1); // Réparation par défaut
            intervention.setDatePanne(new java.util.Date()); // Date actuelle
            interventionRepository.save(intervention);
        }

        return machineRepository.save(machine);
    }
    
    /**
     * Supprime une machine.
     * @param id L'ID de la machine.
     */
    @Transactional
    public void deleteMachine(Long id) {
        Machine machine = getMachineById(id);
        machineRepository.delete(machine);
    }

    /**
     * Archive une machine et marque comme réformée (disponibilite = 2).
     * @param id L'ID de la machine.
     */
    @Transactional
    public void archiveMachine(Long id) {
        Machine machine = getMachineById(id);
        machine.setArchived(true);
        machine.setDisponibilite(2); // Réformé lorsqu'archivé
        machineRepository.save(machine);
    }
    
    /**
     * Met à jour les informations d'une machine.
     * @param id L'ID de la machine.
     * @param updatedMachine Les nouvelles données.
     */
    @Transactional
    public void updateMachine(Long id, Machine updatedMachine) {
        Machine machine = getMachineById(id);
        if (updatedMachine.getDisponibilite() < 0 || updatedMachine.getDisponibilite() > 2) {
            throw new IllegalArgumentException("Statut de disponibilité invalide");
        }
        machine.setDateMiseEnService(updatedMachine.getDateMiseEnService());
        machine.setDisponibilite(updatedMachine.getDisponibilite());
        machine.setType(updatedMachine.getType());
        machine.setConstructeur(updatedMachine.getConstructeur());
        machine.setFournisseur(updatedMachine.getFournisseur());
        machine.setCaracteristique(updatedMachine.getCaracteristique());
        machine.setVoltage(updatedMachine.getVoltage());
        machineRepository.save(machine);
    }
    
    /**
     * Récupère les machines non archivées.
     * @return Liste des machines non archivées.
     */
    public List<Machine> getNonArchivedMachines() {
        return machineRepository.findByArchivedFalse();
    }

    /**
     * Récupère les machines archivées.
     * @return Liste des machines archivées.
     */
    public List<Machine> getArchivedMachines() {
        return machineRepository.findByArchivedTrue();
    }
}