package com.schoolSys.schooolSys.depense;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

public final class DepenseSpecification {

    private DepenseSpecification() {}

    public static Specification<Depense> hasAnneeScolaire(String anneeScolaire) {
        return (root, query, cb) ->
                anneeScolaire == null ? null : cb.equal(root.get("anneeScolaire"), anneeScolaire);
    }

    public static Specification<Depense> hasCategorieId(UUID categorieId) {
        return (root, query, cb) ->
                categorieId == null ? null : cb.equal(root.get("categorie").get("id"), categorieId);
    }

    public static Specification<Depense> hasModePaiement(Depense.ModePaiement mode) {
        return (root, query, cb) ->
                mode == null ? null : cb.equal(root.get("modePaiement"), mode);
    }

    public static Specification<Depense> isRecurrente(Boolean recurrente) {
        return (root, query, cb) ->
                recurrente == null ? null : cb.equal(root.get("recurrente"), recurrente);
    }

    public static Specification<Depense> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("libelle")), pattern),
                    cb.like(cb.lower(root.get("fournisseur")), pattern),
                    cb.like(cb.lower(root.get("reference")), pattern)
            );
        };
    }
}
