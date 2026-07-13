package com.schoolSys.schooolSys.bulletin;

import com.schoolSys.schooolSys.bulletin.dto.BulletinDTO;
import com.schoolSys.schooolSys.bulletin.dto.BulletinDomaineDTO;
import com.schoolSys.schooolSys.bulletin.dto.BulletinModuleDTO;
import com.schoolSys.schooolSys.settings.SchoolSettings;

import java.util.List;
import java.util.Locale;

/**
 * Construit le HTML XHTML d'un bulletin officiel tunisien à partir d'un
 * {@link BulletinDTO}. L'output est consommé par OpenHTMLToPDF.
 * <p>
 * La mise en forme reprend les éléments du modèle officiel : cadre teal,
 * en-tête délégation + école + année scolaire, pill du trimestre, ligne
 * info élève, tables par domaine (matière, note, moyenne domaine,
 * توصيات المدرس) et panneau latéral (stats, comportement, certificat,
 * signatures).
 */
final class BulletinHtmlTemplate {

    private BulletinHtmlTemplate() {
    }

    private static final String TEAL = "#1ca0c1";
    private static final String TEAL_DARK = "#157d9b";
    private static final String TEAL_LIGHT = "#e8f4f8";

    private static final String[] TRIMESTRE_AR = {
            "الثلاثي الأول", "الثلاثي الثاني", "الثلاثي الثالث"
    };

    static String render(BulletinDTO b, SchoolSettings settings) {
        String trimestreAr = TRIMESTRE_AR[Math.max(0, Math.min(2, b.getTrimestre() - 1))];
        String anneeScolaire = settings != null && settings.getAnneeScolaire() != null
                ? settings.getAnneeScolaire() : "2025 / 2026";
        String schoolNameAr = settings != null && settings.getSchoolNameAr() != null
                && !settings.getSchoolNameAr().isBlank()
                ? settings.getSchoolNameAr()
                : (settings != null && settings.getSchoolName() != null
                        ? settings.getSchoolName() : "المدرسة الابتدائية");
        String delegation = settings != null && settings.getDelegationRegionaleAr() != null
                && !settings.getDelegationRegionaleAr().isBlank()
                ? settings.getDelegationRegionaleAr()
                : (settings != null && settings.getDelegationRegionale() != null
                        ? settings.getDelegationRegionale() : "باجة");
        String studentName = b.getStudentNameAr() != null && !b.getStudentNameAr().isBlank()
                ? b.getStudentNameAr() : b.getStudentName();

        StringBuilder html = new StringBuilder(8192);
        html.append("<!DOCTYPE html><html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"ar\" dir=\"rtl\"><head>");
        html.append("<meta charset=\"UTF-8\"/>");
        html.append("<style>");
        html.append("@page{size:A4;margin:8mm;}");
        html.append("*{box-sizing:border-box;}");
        html.append("body{font-family:'Amiri',serif;font-size:11px;color:#0a0a0a;margin:0;padding:0;direction:rtl;line-height:1.5;}");
        html.append(".frame{border:1.5pt solid ").append(TEAL).append(";border-radius:4mm;padding:3mm 4mm;}");
        html.append(".header{display:table;width:100%;border-bottom:1pt solid ").append(TEAL_LIGHT).append(";padding-bottom:2mm;}");
        html.append(".header .col{display:table-cell;width:50%;vertical-align:top;}");
        html.append(".header .col.right{text-align:right;}");
        html.append(".header .col.left{text-align:left;}");
        html.append(".title-pill{text-align:center;margin:3mm 0;}");
        html.append(".title-pill span{display:inline-block;background:").append(TEAL_LIGHT).append(";border:1.5pt solid ").append(TEAL).append(";color:").append(TEAL_DARK).append(";font-weight:700;font-size:15px;padding:1.5mm 12mm;border-radius:5mm;}");
        html.append(".student-info{display:table;width:100%;font-size:11px;margin:1mm 0 3mm 0;}");
        html.append(".student-info .col{display:table-cell;}");
        html.append(".body-row{display:table;width:100%;}");
        html.append(".body-row .main{display:table-cell;vertical-align:top;padding-left:2mm;}");
        html.append(".body-row .side{display:table-cell;vertical-align:top;width:55mm;}");
        html.append(".domaine{border:1pt solid ").append(TEAL).append(";border-radius:2mm;overflow:hidden;margin-bottom:3mm;}");
        html.append(".domaine-title{background:").append(TEAL).append(";color:#fff;text-align:center;font-weight:700;font-size:12.5px;padding:1.2mm 2mm;}");
        html.append("table.domaine-table{width:100%;border-collapse:collapse;font-size:10px;background:#fff;}");
        html.append("table.domaine-table th{background:").append(TEAL_LIGHT).append(";color:").append(TEAL_DARK).append(";padding:1mm 1.5mm;border:0.5pt solid ").append(TEAL).append(";font-weight:700;}");
        html.append("table.domaine-table td{padding:1.2mm 1.5mm;border:0.5pt solid ").append(TEAL).append(";vertical-align:middle;}");
        html.append("table.domaine-table td.center{text-align:center;}");
        html.append("table.domaine-table td.matiere{font-weight:600;color:").append(TEAL_DARK).append(";}");
        html.append("table.domaine-table td.moyenne{text-align:center;font-weight:700;font-size:13px;background:#fafafa;}");
        html.append(".sd-row td{background:#f4fafc;font-weight:700;color:").append(TEAL_DARK).append(";font-size:10.5px;}");
        html.append(".stat-cards{display:table;width:100%;margin-bottom:2mm;}");
        html.append(".stat-cards .card{display:table-cell;width:33%;border:1pt solid ").append(TEAL).append(";border-radius:1.5mm;background:").append(TEAL_LIGHT).append(";text-align:center;padding:1.5mm 0.5mm;}");
        html.append(".stat-cards .card .label{font-size:9px;color:").append(TEAL_DARK).append(";}");
        html.append(".stat-cards .card .value{font-size:16px;font-weight:700;color:").append(TEAL_DARK).append(";margin-top:0.5mm;}");
        html.append(".panel{border:1pt solid ").append(TEAL).append(";border-radius:1.5mm;margin-bottom:2.5mm;background:#fff;}");
        html.append(".panel-title{background:").append(TEAL_LIGHT).append(";color:").append(TEAL_DARK).append(";font-size:10px;font-weight:700;padding:1mm 2mm;border-bottom:0.5pt solid ").append(TEAL).append(";}");
        html.append(".panel-body{padding:2mm;min-height:18mm;font-size:10.5px;}");
        html.append(".panel-body.center{text-align:center;font-weight:700;font-size:13px;padding:5mm 1mm;}");
        html.append(".dotted{display:inline-block;border-bottom:0.5pt dotted #555;min-width:25mm;padding:0 4mm;}");
        html.append(".green{color:#1b5e20;}");
        html.append(".red{color:#b71c1c;}");
        html.append("</style></head><body>");

        html.append("<div class=\"frame\">");

        // Header
        html.append("<div class=\"header\">");
        html.append("<div class=\"col right\">");
        html.append("<div style=\"font-size:16px;font-weight:700;color:").append(TEAL_DARK).append(";\">المندوبية الجهوية للتربية</div>");
        html.append("<div style=\"margin-top:2mm;\">ب <span class=\"dotted\">").append(escape(delegation)).append("</span></div>");
        html.append("</div>");
        html.append("<div class=\"col left\">");
        html.append("<div>المدرسة الابتدائية : <span class=\"dotted\" style=\"font-weight:700;\">").append(escape(schoolNameAr)).append("</span></div>");
        String[] yrParts = anneeScolaire.split(" / ");
        String y1 = lastTwo(yrParts.length > 0 ? yrParts[0] : "2025");
        String y2 = lastTwo(yrParts.length > 1 ? yrParts[1] : "2026");
        html.append("<div style=\"margin-top:2mm;\">السنة الدراسية : <span class=\"dotted\">20").append(y2).append("</span> / <span class=\"dotted\">20").append(y1).append("</span></div>");
        html.append("</div></div>");

        // Trimestre pill
        html.append("<div class=\"title-pill\"><span>").append(trimestreAr).append("</span></div>");

        // Student info
        html.append("<div class=\"student-info\">");
        html.append("<div class=\"col\"><strong>التلميذ(ة)</strong> : ").append(escape(studentName)).append("</div>");
        html.append("<div class=\"col\"><strong>القسم</strong> : ").append(escape(b.getClasse())).append("</div>");
        html.append("<div class=\"col\"><strong>عدد التلاميذ المرسمين</strong> : ").append(b.getTotalEleves()).append("</div>");
        if (b.getMatricule() != null && !b.getMatricule().isBlank()) {
            html.append("<div class=\"col\"><strong>المعرّف الوحيد</strong> : ").append(escape(b.getMatricule())).append("</div>");
        }
        html.append("</div>");

        // Body
        html.append("<div class=\"body-row\"><div class=\"side\">");

        // Side stats
        html.append("<div class=\"stat-cards\">");
        html.append("<div class=\"card\"><div class=\"label\">معدل الثلاثي</div><div class=\"value\">").append(fmt(b.getMoyenneGenerale())).append("</div></div>");
        html.append("<div class=\"card\"><div class=\"label\">أعلى معدل</div><div class=\"value green\">").append(fmt(b.getMoyenneMax())).append("</div></div>");
        html.append("<div class=\"card\"><div class=\"label\">أدنى معدل</div><div class=\"value red\">").append(fmt(b.getMoyenneMin())).append("</div></div>");
        html.append("</div>");

        // Comportement
        html.append("<div class=\"panel\"><div class=\"panel-title\">ملاحظات المدرس(ة) حول السلوك و المواطنة</div>");
        html.append("<div class=\"panel-body\">").append(escape(nullSafe(b.getComportement()))).append("</div></div>");

        // Certificat
        html.append("<div class=\"panel\"><div class=\"panel-title\">الشهادة</div>");
        html.append("<div class=\"panel-body center\">").append(escape(nullSafe(b.getCertificatType()))).append("</div></div>");

        // Director
        html.append("<div class=\"panel\"><div class=\"panel-title\">مدير(ة) المدرسة</div>");
        html.append("<div class=\"panel-body\" style=\"min-height:24mm;\">التاريخ : <span class=\"dotted\"></span><div style=\"color:#666;font-size:9px;margin-top:1mm;\">(الختم و الإمضاء)</div></div></div>");

        // Parent signature
        html.append("<div class=\"panel\"><div class=\"panel-title\">إمضاء الولي</div>");
        html.append("<div class=\"panel-body\" style=\"min-height:14mm;\"></div></div>");

        html.append("</div>"); // side

        // Main : domain tables
        html.append("<div class=\"main\">");
        for (BulletinDomaineDTO d : b.getDomaines()) {
            html.append(renderDomaine(d));
        }
        if (b.getModulesHorsDomaine() != null && b.getModulesHorsDomaine().stream()
                .anyMatch(m -> m.getMoyenneModule() != null && m.getMoyenneModule() > 0)) {
            BulletinDomaineDTO autres = new BulletinDomaineDTO();
            autres.setDomaineName("Autres");
            autres.setDomaineNameAr("مواد أخرى");
            autres.setModules(b.getModulesHorsDomaine());
            autres.setMoyenneDomaine(avg(b.getModulesHorsDomaine()));
            html.append(renderDomaine(autres));
        }
        html.append("</div>"); // main

        html.append("</div>"); // body-row
        html.append("</div>"); // frame
        html.append("</body></html>");
        return html.toString();
    }

    private static String renderDomaine(BulletinDomaineDTO d) {
        StringBuilder s = new StringBuilder(2048);
        String title = d.getDomaineNameAr() != null && !d.getDomaineNameAr().isBlank()
                ? d.getDomaineNameAr() : d.getDomaineName();
        s.append("<div class=\"domaine\"><div class=\"domaine-title\">").append(escape(title)).append("</div>");
        s.append("<table class=\"domaine-table\"><thead><tr>");
        s.append("<th style=\"width:22%;\">المادة</th>");
        s.append("<th style=\"width:10%;\">العدد /20</th>");
        s.append("<th style=\"width:10%;\">معدل المجال</th>");
        s.append("<th>توصيات المدرس(ة)</th>");
        s.append("<th style=\"width:11%;\">أدنى</th>");
        s.append("<th style=\"width:11%;\">أعلى</th>");
        s.append("</tr></thead><tbody>");

        List<BulletinModuleDTO> modules = d.getModules() != null ? d.getModules() : List.of();
        int rowCount = modules.size();
        String reco = nullSafe(d.getRecommandation());
        if (modules.isEmpty()) {
            s.append("<tr><td colspan=\"6\" class=\"center\" style=\"color:#888;\">—</td></tr>");
        } else {
            for (int i = 0; i < modules.size(); i++) {
                BulletinModuleDTO m = modules.get(i);
                Double note = m.getMoyenneModule();
                boolean has = note != null && note > 0;
                String noteCls = !has ? "" : (note >= 10 ? "green" : "red");
                s.append("<tr>");
                s.append("<td class=\"matiere\">").append(escape(
                        m.getModuleNameAr() != null && !m.getModuleNameAr().isBlank()
                                ? m.getModuleNameAr() : m.getModuleName())).append("</td>");
                s.append("<td class=\"center ").append(noteCls).append("\" style=\"font-weight:700;\">")
                        .append(has ? fmt(note) : "—").append("</td>");
                if (i == 0) {
                    String mDom = d.getMoyenneDomaine() >= 10 ? "green" : "red";
                    s.append("<td class=\"moyenne ").append(mDom).append("\" rowspan=\"").append(rowCount).append("\">")
                            .append(fmt(d.getMoyenneDomaine())).append("</td>");
                    s.append("<td rowspan=\"").append(rowCount).append("\">").append(escape(reco)).append("</td>");
                }
                s.append("<td class=\"center\">").append(fmt(m.getModuleMin())).append("</td>");
                s.append("<td class=\"center\">").append(fmt(m.getModuleMax())).append("</td>");
                s.append("</tr>");
            }
        }
        s.append("</tbody></table></div>");
        return s.toString();
    }

    private static double avg(List<BulletinModuleDTO> mods) {
        return mods.stream()
                .map(BulletinModuleDTO::getMoyenneModule)
                .filter(v -> v != null)
                .mapToDouble(Double::doubleValue)
                .average().orElse(0.0);
    }

    private static String fmt(Double v) {
        if (v == null) return "—";
        return String.format(Locale.US, "%.2f", v);
    }

    private static String lastTwo(String s) {
        if (s == null) return "";
        s = s.trim();
        return s.length() >= 2 ? s.substring(s.length() - 2) : s;
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
