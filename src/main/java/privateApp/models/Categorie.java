package privateApp.models;

import jakarta.persistence.*;

@Entity
@Table(name = "categorie")
public class Categorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategorie;

    @Column(name = "libelle_categorie")
    private String libelleCategorie; // "materiel" ou "medicament"

    // Constructeurs
    public Categorie() {}

    public Categorie(String libelleCategorie) {
        this.libelleCategorie = libelleCategorie;
    }

    // Getters et Setters
    public Long getIdCategorie() { return idCategorie; }
    public void setIdCategorie(Long idCategorie) { this.idCategorie = idCategorie; }
    public String getLibelleCategorie() { return libelleCategorie; }
    public void setLibelleCategorie(String libelleCategorie) { this.libelleCategorie = libelleCategorie; }
}
/*La propriété ‘cascade’ permet de définir quel impact l’action sur une entité aura sur son entité associée. 
 *  Le type ‘ALL’ :
 *  signifie que toutes les actions sur l’entité Ctegorie seront propagées sur l’entité Produit. 
 *  Exemple : si on supprime la catégorie, les produits associés seront également supprimés.
 *  impliquerait une cascade dans le cas de la suppression
 *  La propriété fetch possède la valeur EAGER, et cela signifie qu’à la récupération du categorie, tous les produits seront également récupérés.
 *  La propriété fetch possède la valeur LAZY, et cela signifie qu’à la récupération de la catégorie, les produits ne sont pas récupérés. 
 *  Par voie de conséquence, les performances sont meilleures (la requête est plus légère) ; 
 *  cependant, lorsqu'ultérieurement dans votre code vous accéderez aux produits à partir de l’objet Catégorie en question, une nouvelle requête sera exécutée.
 *  l’utilisation du LAZY requiert la mise en place de transactions.
 *  Les transactions sont des blocs de requêtes à exécuter, et ont une très forte valeur ajoutée garantissant le respect des propriétés ACID.
 *  Atomicité : Une transaction s’effectue entièrement, ou pas du tout.

	Cohérence : Le contenu d’une base doit être cohérent au début et à la fin d’une transaction.

	Isolation : Les modifications d’une transaction ne sont visibles/modifiables que quand celle-ci a été validée.

	Durabilité : Une fois la transaction validée, l’état de la base est permanent (non affecté par les pannes ou autre).

	Le respect de ces propriétés est synonyme de fiabilité dans le traitement de vos données.
 *  cascade = { 
            CascadeType.PERSIST, 
            CascadeType.MERGE 
            }  
            je spécifie donc uniquement PERSIST et MERGE, la cascade s’applique donc tant en création qu’en modification.
             
 */