package com.schoolSys.schooolSys.common.annee;

import java.util.UUID;

public final class AnneeContext {

    private static final ThreadLocal<String> CURRENT_LABEL = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CURRENT_ID = new ThreadLocal<>();

    private AnneeContext() {}

    public static String getCurrentLabel() {
        return CURRENT_LABEL.get();
    }

    public static UUID getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void setCurrentLabel(String label) {
        CURRENT_LABEL.set(label);
    }

    public static void setCurrentId(UUID id) {
        CURRENT_ID.set(id);
    }

    public static void clear() {
        CURRENT_LABEL.remove();
        CURRENT_ID.remove();
    }
}
