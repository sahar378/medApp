// privateApp.services/InterventionService.java
package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import privateApp.models.Intervention;
import privateApp.models.Machine;
import privateApp.models.User;
import privateApp.repositories.InterventionRepository;
import privateApp.repositories.MachineRepository;

import java.util.List;

@Service
public class InterventionService {

    @Autowired
    private InterventionRepository interventionRepository;

    @Autowired
    private MachineRepository machineRepository;

    public List<Intervention> getAllInterventions() {
        return interventionRepository.findAll();
    }

    public List<Intervention> getOpenInterventions() {
        return interventionRepository.findByEstFermee(false);
    }

    public List<Intervention> getInterventionsByMachine(Long idMachine) {
        return interventionRepository.findByMachineIdMachine(idMachine);
    }

    /*@Transactional
    public Intervention createIntervention(Intervention intervention) {
        // Charger la machine existante depuis la base de données
        Machine machine = machineRepository.findById(intervention.getMachine().getIdMachine())
                .orElseThrow(() -> new RuntimeException("Machine non trouvée"));

        // Mettre à jour la disponibilité si nécessaire
        if (machine.getDisponibilite() != 1) {
            machine.setDisponibilite(1); // Marquer comme en intervention
            machineRepository.save(machine);
        }

        // Associer la machine chargée à l'intervention
        intervention.setMachine(machine);

        // Récupérer l'utilisateur connecté (PERSONNEL_MEDICAL)
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        intervention.setPersonnel(connectedUser);

        // Valider les champs en fonction de nature
        if (intervention.getNature() == 1) { // Réparation
            if (intervention.getPanne() == null || intervention.getDatePanne() == null) {
                throw new RuntimeException("Les champs panne et date_panne sont obligatoires pour une réparation");
            }
        } else if (intervention.getNature() == 0) { // Préventif
            intervention.setPanne(null);
            intervention.setDatePanne(null);
            intervention.setReparation(null);
            intervention.setDateReparation(null);
            intervention.setLieuReparation(null);
        } else {
            throw new RuntimeException("Nature invalide : doit être 0 (préventif) ou 1 (réparation)");
        }

        return interventionRepository.save(intervention);
    }*/
 // privateApp.services/InterventionService.java
    @Transactional
    public Intervention createIntervention(Intervention intervention) {
        // Charger la machine existante depuis la base de données
        Machine machine = machineRepository.findById(intervention.getMachine().getIdMachine())
                .orElseThrow(() -> new RuntimeException("Machine non trouvée"));

        // Récupérer l'utilisateur connecté (PERSONNEL_MEDICAL)
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        intervention.setPersonnel(connectedUser);

        // Logique selon la nature de l'intervention
        if (intervention.getNature() == 0) { // Préventif
            intervention.setEstFermee(true); // Fermer automatiquement
            machine.setDisponibilite(0); // Rendre la machine disponible
            machineRepository.save(machine);
        } else if (intervention.getNature() == 1) { // Réparation
            intervention.setEstFermee(false); // Laisser ouverte
            machine.setDisponibilite(1); // Mettre la machine en intervention
            machineRepository.save(machine);
            // Vérification des champs obligatoires pour une réparation
            if (intervention.getPanne() == null || intervention.getDatePanne() == null) {
                throw new RuntimeException("Les champs panne et date_panne sont obligatoires pour une réparation");
            }
        } else {
            throw new RuntimeException("Nature invalide : doit être 0 (préventif) ou 1 (réparation)");
        }

        // Associer la machine à l'intervention
        intervention.setMachine(machine);

        return interventionRepository.save(intervention);
    }

    @Transactional
    public Intervention closeIntervention(Long idIntervention, String reparation, java.util.Date dateReparation, String lieuReparation) {
        Intervention intervention = interventionRepository.findById(idIntervention)
                .orElseThrow(() -> new RuntimeException("Intervention non trouvée"));
        
        if (intervention.getNature() == 1) { // Réparation
            intervention.setReparation(reparation);
            intervention.setDateReparation(dateReparation);
            intervention.setLieuReparation(lieuReparation); // Champ optionnel
        }
        intervention.setEstFermee(true);

        Machine machine = intervention.getMachine();
        machine.setDisponibilite(0); // Remettre disponible
        machineRepository.save(machine);

        return interventionRepository.save(intervention);
    }
    //archivage 
    @Transactional
    public void archiveIntervention(Long id) {
      Intervention intervention = interventionRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Intervention non trouvée"));
      intervention.setArchived(true);
      interventionRepository.save(intervention);
    }
    public Intervention getInterventionById(Long id) {
    	  return interventionRepository.findById(id)
    	    .orElseThrow(() -> new RuntimeException("Intervention non trouvée"));
    	}

    	@Transactional
    	public void updateIntervention(Long id, Intervention updatedIntervention) {
    	  Intervention intervention = getInterventionById(id);
    	  intervention.setMachine(updatedIntervention.getMachine());
    	  intervention.setTechnicien(updatedIntervention.getTechnicien());
    	  intervention.setDate(updatedIntervention.getDate());
    	  intervention.setNature(updatedIntervention.getNature());
    	  intervention.setDatePanne(updatedIntervention.getDatePanne());
    	  intervention.setPanne(updatedIntervention.getPanne());
    	  intervention.setReparation(updatedIntervention.getReparation());
    	  intervention.setDateReparation(updatedIntervention.getDateReparation());
    	  intervention.setLieuReparation(updatedIntervention.getLieuReparation());
    	  intervention.setEstFermee(updatedIntervention.isEstFermee());
    	  interventionRepository.save(intervention);
    	}
}