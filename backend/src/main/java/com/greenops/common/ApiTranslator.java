package com.greenops.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class ApiTranslator {

    private static final Map<String, String> EXACT_PHRASES = new LinkedHashMap<>();
    private static final Map<Pattern, String> PATTERN_RULES = new LinkedHashMap<>();

    static {
        EXACT_PHRASES.put("Serve images in next-gen formats", "Servir les images dans des formats nouvelle génération (WebP / AVIF)");
        EXACT_PHRASES.put("Properly size images", "Dimensionner correctement les images");
        EXACT_PHRASES.put("Eliminate render-blocking resources", "Éliminer les ressources bloquant le rendu");
        EXACT_PHRASES.put("Minify JavaScript", "Minifier le code JavaScript");
        EXACT_PHRASES.put("Reduce unused JavaScript", "Réduire le JavaScript inutilisé");
        EXACT_PHRASES.put("Minify CSS", "Minifier le code CSS");
        EXACT_PHRASES.put("Reduce unused CSS", "Réduire le CSS inutilisé");
        EXACT_PHRASES.put("Enable text compression", "Activer la compression texte (GZIP / Brotli)");
        EXACT_PHRASES.put("Uses efficient cache policy on static assets", "Utiliser une politique de cache efficace sur les ressources statiques");
        EXACT_PHRASES.put("Serve static assets with an efficient cache policy", "Servir les ressources statiques avec un cache efficace");
        EXACT_PHRASES.put("Avoid enormous network payloads", "Éviter les charges réseau trop lourdes");
        EXACT_PHRASES.put("Avoid an excessive DOM size", "Éviter une taille DOM excessive");
        EXACT_PHRASES.put("Remove unused rules from stylesheets", "Supprimer les règles CSS inutilisées");
        EXACT_PHRASES.put("Defer offscreen images", "Différer le chargement des images hors écran");
        EXACT_PHRASES.put("Reduce impact of third-party code", "Réduire l'impact des scripts tiers");
        EXACT_PHRASES.put("Largest Contentful Paint", "Plus grand élément affiché (LCP)");
        EXACT_PHRASES.put("Cumulative Layout Shift", "Décalage cumulatif de mise en page (CLS)");
        EXACT_PHRASES.put("Total Blocking Time", "Temps total de blocage (TBT)");
        EXACT_PHRASES.put("First Contentful Paint", "Premier affichage de contenu (FCP)");
        EXACT_PHRASES.put("Speed Index", "Indice de vitesse d'affichage");
        EXACT_PHRASES.put("No recent CI/CD pipeline runs found for this public repository.", "Aucun pipeline CI/CD récent trouvé pour ce dépôt public.");

        PATTERN_RULES.put(Pattern.compile("(?i)\\bopportunities\\b"), "Opportunités");
        PATTERN_RULES.put(Pattern.compile("(?i)\\bdiagnostics\\b"), "Diagnostics");
        PATTERN_RULES.put(Pattern.compile("(?i)\\bperformance\\b"), "Performance");
        PATTERN_RULES.put(Pattern.compile("(?i)\\baccessibility\\b"), "Accessibilité");
        PATTERN_RULES.put(Pattern.compile("(?i)\\bbest practices\\b"), "Bonnes pratiques");
        PATTERN_RULES.put(Pattern.compile("(?i)\\bseo\\b"), "Référencement (SEO)");
        PATTERN_RULES.put(Pattern.compile("(?i)\\bwarning\\b"), "Avertissement");
        PATTERN_RULES.put(Pattern.compile("(?i)\\berror\\b"), "Erreur");
        PATTERN_RULES.put(Pattern.compile("(?i)\\bsuccess\\b"), "Succès");
        PATTERN_RULES.put(Pattern.compile("(?i)\\bfailure\\b"), "Échec");
        PATTERN_RULES.put(Pattern.compile("(?i)\\bcompleted\\b"), "Terminé");
        PATTERN_RULES.put(Pattern.compile("(?i)\\bin_progress\\b"), "En cours");
        PATTERN_RULES.put(Pattern.compile("(?i)\\bqueued\\b"), "En file d'attente");
        PATTERN_RULES.put(Pattern.compile("(?i)\\bskipped\\b"), "Ignoré");
    }

    private ApiTranslator() {}

    public static String toFrench(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String trimmed = input.trim();
        if (EXACT_PHRASES.containsKey(trimmed)) {
            return EXACT_PHRASES.get(trimmed);
        }

        String result = input;
        for (Map.Entry<String, String> entry : EXACT_PHRASES.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Pattern, String> entry : PATTERN_RULES.entrySet()) {
            result = entry.getKey().matcher(result).replaceAll(entry.getValue());
        }
        return result;
    }
}