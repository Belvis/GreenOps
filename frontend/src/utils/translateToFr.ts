const PHRASE_MAP: Record<string, string> = {
  'Serve images in next-gen formats': 'Servir les images dans des formats nouvelle génération (WebP / AVIF)',
  'Properly size images': 'Dimensionner correctement les images',
  'Eliminate render-blocking resources': 'Éliminer les ressources bloquant le rendu',
  'Minify JavaScript': 'Minifier le code JavaScript',
  'Reduce unused JavaScript': 'Réduire le JavaScript inutilisé',
  'Minify CSS': 'Minifier le code CSS',
  'Reduce unused CSS': 'Réduire le CSS inutilisé',
  'Enable text compression': 'Activer la compression texte (GZIP / Brotli)',
  'Uses efficient cache policy on static assets': 'Utiliser une politique de cache efficace sur les ressources statiques',
  'Serve static assets with an efficient cache policy': 'Servir les ressources statiques avec un cache efficace',
  'Avoid enormous network payloads': 'Éviter les charges réseau trop lourdes',
  'Avoid an excessive DOM size': 'Éviter une taille DOM excessive',
  'Remove unused rules from stylesheets': 'Supprimer les règles CSS inutilisées',
  'Defer offscreen images': 'Différer le chargement des images hors écran',
  'Reduce impact of third-party code': "Réduire l'impact des scripts tiers",
  'Preload Largest Contentful Paint image': "Précharger l'image du plus grand élément affiché (LCP)",
  'Avoid multiple page redirects': 'Éviter les redirections de page multiples',
  'Reduce initial server response time': 'Réduire le temps de réponse initial du serveur',
  'Largest Contentful Paint': 'Plus grand élément affiché (LCP)',
  'Cumulative Layout Shift': 'Décalage cumulatif de mise en page (CLS)',
  'Total Blocking Time': 'Temps total de blocage (TBT)',
  'First Contentful Paint': 'Premier affichage de contenu (FCP)',
  'Speed Index': "Indice de vitesse d'affichage",
  'Best Practices': 'Bonnes pratiques',
  'Accessibility': 'Accessibilité',
  'Performance': 'Performance',
  'SEO': 'Référencement (SEO)',
};

const PATTERN_RULES: Array<[RegExp, string]> = [
  [/Image formats like WebP and AVIF often provide better compression than PNG or JPEG, which means faster downloads and less data consumption\./gi, 'Les formats d’image comme WebP et AVIF offrent souvent une meilleure compression que PNG ou JPEG, ce qui accélère les téléchargements et réduit la consommation de données.'],
  [/Resources are blocking the first paint of your page\. Consider delivering critical JS\/CSS inline and deferring all non-critical JS\/styles\./gi, 'Des ressources bloquent le premier affichage de votre page. Envisagez d’intégrer le JS/CSS critique en ligne et de différer les scripts et styles non critiques.'],
  [/Reduce unused rules from stylesheets and defer CSS not used for above-the-fold content to decrease bytes consumed by network activity\./gi, 'Supprimez les règles CSS inutilisées et différez le CSS non nécessaire au contenu visible immédiatement pour réduire le volume réseau.'],
  [/Reduce unused JavaScript and defer loading scripts until they are required to decrease bytes consumed by network activity\./gi, 'Réduisez le JavaScript inutilisé et différez le chargement des scripts non indispensables afin de limiter la bande passante consommée.'],
  [/Serve images that are appropriately-sized to save cellular data and improve load time\./gi, 'Servez des images correctement dimensionnées pour économiser les données réseau et accélérer le chargement.'],
  [/Text-based resources should be served with compression \(gzip, deflate or brotli\) to minimize total network bytes\./gi, 'Les ressources textuelles doivent être servies avec compression (gzip, deflate ou brotli) afin de réduire le volume total transféré.'],
  [/A long cache lifetime can speed up repeat visits to your page\./gi, 'Une durée de mise en cache longue accélère les visites répétées sur votre page.'],
  [/Potential savings of\s*/gi, 'Économie potentielle de '],
  [/\bLearn more\b/gi, 'En savoir plus'],
  [/\bopportunities\b/gi, 'Opportunités'],
  [/\bdiagnostics\b/gi, 'Diagnostics'],
];

export function translateToFr(text: string | null | undefined): string {
  if (!text) return '';
  const trimmed = text.trim();
  if (PHRASE_MAP[trimmed]) {
    return PHRASE_MAP[trimmed];
  }

  let output = text;
  for (const [en, fr] of Object.entries(PHRASE_MAP)) {
    if (output.includes(en)) {
      output = output.split(en).join(fr);
    }
  }
  for (const [regex, fr] of PATTERN_RULES) {
    output = output.replace(regex, fr);
  }
  return output;
}