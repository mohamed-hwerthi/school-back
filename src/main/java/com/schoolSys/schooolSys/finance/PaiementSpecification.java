package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

public final class PaiementSpecification {

    private PaiementSpecification() {}

    public static Specification<Paiement> hasAnneeScolaire(String anneeScolaire) {
        return (root, query, cb) ->
                anneeScolaire == null ? null : cb.equal(root.get("anneeScolaire"), anneeScolaire);
    }

    public static Specification<Paiement> hasMois(String mois) {
        return (root, query, cb) ->
                mois == null ? null : cb.equal(root.get("mois"), mois);
    }

    public static Specification<Paiement> hasStatut(Paiement.StatutPaiement statut) {
        return (root, query, cb) ->
                statut == null ? null : cb.equal(root.get("statut"), statut);
    }

    public static Specification<Paiement> hasModePaiement(Paiement.ModePaiement mode) {
        return (root, query, cb) ->
                mode == null ? null : cb.equal(root.get("modePaiement"), mode);
    }

    public static Specification<Paiement> hasStudentId(UUID studentId) {
        return (root, query, cb) ->
                studentId == null ? null : cb.equal(root.get("student").get("id"), studentId);
    }

    public static Specification<Paiement> hasTypeFraisId(UUID typeFraisId) {
        return (root, query, cb) ->
                typeFraisId == null ? null : cb.equal(root.get("typeFrais").get("id"), typeFraisId);
    }

    public static Specification<Paiement> searchStudent(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("student").get("firstName")), pattern),
                    cb.like(cb.lower(root.get("student").get("lastName")), pattern),
                    cb.like(cb.lower(root.get("reference")), pattern)
            );
        };
    }
}
