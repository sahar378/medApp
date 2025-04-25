package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import privateApp.models.Technicien;
import privateApp.repositories.TechnicienRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TechnicienService {

    @Autowired
    private TechnicienRepository technicienRepository;

    public List<Technicien> getAllTechniciens() {
        return technicienRepository.findAll();
    }

    public Technicien getTechnicienById(Long id) {
        return technicienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));
    }

    public Technicien addTechnicien(Technicien technicien) {
    	// Vérifier que l'email est fourni
        if (technicien.getEmail() == null || technicien.getEmail().trim().isEmpty()) {
            throw new RuntimeException("L'email est obligatoire");
        }

        // Vérifier si un technicien avec le même email existe déjà
        Optional<Technicien> existingTechnicien = technicienRepository.findByEmail(technicien.getEmail());
        if (existingTechnicien.isPresent()) {
            throw new RuntimeException("Un technicien avec cet email existe déjà");
        }

        // Si aucun doublon, on ajoute le technicien
        return technicienRepository.save(technicien);
    }

	 @Transactional
	 public void deleteTechnicien(Long id) {
	     Technicien technicien = getTechnicienById(id);
	     technicienRepository.delete(technicien);
	 }
	 //archivage 
	 @Transactional
	 public void archiveTechnicien(Long id) {
	   Technicien technicien = technicienRepository.findById(id)
	     .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));
	   technicien.setArchived(true);
	   technicienRepository.save(technicien);
	 }
//mise à jour de technicien

	 @Transactional
	    public void updateTechnicien(Long id, Technicien updatedTechnicien) {
	        Technicien technicien = getTechnicienById(id);

	        // Vérifier que l'email est fourni
	        if (updatedTechnicien.getEmail() == null || updatedTechnicien.getEmail().trim().isEmpty()) {
	            throw new RuntimeException("L'email est obligatoire");
	        }

	        // Vérifier si l'email modifié existe déjà pour un autre technicien
	        Optional<Technicien> existingTechnicien = technicienRepository.findByEmail(updatedTechnicien.getEmail());
	        if (existingTechnicien.isPresent() && !existingTechnicien.get().getIdTechnicien().equals(id)) {
	            throw new RuntimeException("Un technicien avec cet email existe déjà");
	        }

	        // Mettre à jour les champs
	        technicien.setNom(updatedTechnicien.getNom());
	        technicien.setPrenom(updatedTechnicien.getPrenom());
	        technicien.setTelephone(updatedTechnicien.getTelephone());
	        technicien.setEmail(updatedTechnicien.getEmail());
	        technicien.setSociete(updatedTechnicien.getSociete());
	        technicienRepository.save(technicien);
	    }
		// privateApp.services/TechnicienService.java
		public List<Technicien> getNonArchivedTechniciens() {
		  return technicienRepository.findByArchivedFalse();
		}

		public List<Technicien> getArchivedTechniciens() {
		  return technicienRepository.findByArchivedTrue();
		}
}