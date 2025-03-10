package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import privateApp.models.ProduitLog;

import java.util.List;

@Repository
public interface ProduitLogRepository extends JpaRepository<ProduitLog, Long> {
    List<ProduitLog> findByProduitIdProduit(Long produitId);
}