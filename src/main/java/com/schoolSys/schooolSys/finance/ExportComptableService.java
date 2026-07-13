package com.schoolSys.schooolSys.finance;

import com.schoolSys.schooolSys.depense.Depense;
import com.schoolSys.schooolSys.depense.DepenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportComptableService {

    private final PaiementRepository paiementRepository;
    private final DepenseRepository depenseRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String exportCSV(String anneeScolaire) {
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Libelle,Debit,Credit,Reference,Type\n");

        // Add paiements as credits
        List<Paiement> paiements = paiementRepository.findByAnneeScolaire(anneeScolaire);
        for (Paiement p : paiements) {
            String date = p.getDatePaiement() != null ? p.getDatePaiement().format(DATE_FMT) : "";
            String libelle = escapeCSV("Paiement - " + p.getStudent().getFirstName() + " "
                    + p.getStudent().getLastName() + " - " + p.getTypeFrais().getNom());
            String reference = p.getReference() != null ? escapeCSV(p.getReference()) : "";

            sb.append(date).append(",")
                    .append(libelle).append(",")
                    .append(",") // debit empty
                    .append(p.getMontantPaye()).append(",")
                    .append(reference).append(",")
                    .append("RECETTE")
                    .append("\n");
        }

        // Add depenses as debits
        List<Depense> depenses = depenseRepository.findByAnneeScolaire(anneeScolaire);
        for (Depense d : depenses) {
            String date = d.getDateDepense() != null ? d.getDateDepense().format(DATE_FMT) : "";
            String libelle = escapeCSV(d.getLibelle());
            String reference = d.getReference() != null ? escapeCSV(d.getReference()) : "";

            sb.append(date).append(",")
                    .append(libelle).append(",")
                    .append(d.getMontant()).append(",")
                    .append(",") // credit empty
                    .append(reference).append(",")
                    .append("DEPENSE")
                    .append("\n");
        }

        return sb.toString();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
