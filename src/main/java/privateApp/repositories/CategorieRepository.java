package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Categorie;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {
}