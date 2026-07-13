package com.schoolSys.schooolSys.common.annee;

import org.springframework.stereotype.Component;

@Component
public class AnneeScolaireProvider {

    public String getCurrentAnneeScolaire() {
        return AnneeContext.getCurrentLabel();
    }

    public String resolveAnneeScolaire(String anneeScolaire) {
        return anneeScolaire != null ? anneeScolaire : getCurrentAnneeScolaire();
    }
}
