package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import privateApp.models.Technicien;
import privateApp.repositories.TechnicienRepository;

import java.util.List;

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
		  technicien.setNom(updatedTechnicien.getNom());
		  technicien.setTelephone(updatedTechnicien.getTelephone());
		  technicien.setEmail(updatedTechnicien.getEmail());
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