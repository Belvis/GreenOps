# Méthodologie de Développement par Branches & Répartition Technique — GreenOps

## 1. Gouvernance Git, Stratégie de Branches & Releases

### 1.1 Architecture des Branches (`https://github.com/Belvis/GreenOps`)

Afin de garantir l'isolation des développements des 4 membres tout en assurant une intégration continue maîtrisée, le dépôt applique le modèle **Git Feature Branching** :

* **`main` (Production & Livrables Officiels) :** Branche strictement protégée. Elle ne reçoit que les fusions issues de `develop` à l'issue de chaque jalon validé. Chaque fusion sur `main` donne lieu à la création d'un **Tag Git** et d'une **GitHub Release**.

* **`develop` (Intégration Continue) :** Branche pivot de l'équipe. Elle héberge le socle commun (arborescence Maven/Spring Boot, projet Vite/React, configuration PostgreSQL) et reçoit les fonctionnalités validées après revue croisée.

* **`feature/<prenom>-<module>` (Développement Isolé) :** Branches créées exclusivement depuis `develop` pour développer chaque tranche verticale (Base de données + API Java + Tests JUnit + Front-end React) :

  * `feature/shiamamadanki-socle-auth-admin` *(Module 0 — Prioritaire au Jalon 1)*

  * `feature/marwa-web-audit` *(Module 1 — Audit Web & EcoIndex)*

  * `feature/belvis-ci-analyzer` *(Module 2 — Analyseur Git public & CI/CD)*

  * `feature/faizi-cloud-simulator` *(Module 3 — Simulateur Cloud & RGPD)*

  * `feature/shiamamadanki-cron-alerts` *(Module 4 — Surveillance Cron & Alertes Email)*

* **`fix/<prenom>-<description>` (Correctifs) :** Branches courtes tirées depuis `develop` pour corriger une anomalie détectée après intégration.

### 1.2 Cycle de Vie d'une Branche & Règles de Validation

1. **Création de la branche :**

   ```
   git checkout develop
   git pull origin develop
   git checkout -b feature/<prenom>-<module>
   
   ```

2. **Développement atomique & Traçabilité IA :**

   * Commits réguliers respectant la convention *Conventional Commits* : `feat(mod1): ...`, `fix(auth): ...`, `test(mod3): ...`, `docs: ...`.

   * Conformément à la charte d'usage de l'IA (Section 4.1 du Dossier de Lancement), toute contribution significative assistée par IA (génération de mocks JSON, structure de tests) doit être explicitement mentionnée dans le message de commit (`[AI-assisted: ...]`).

3. **Synchronisation avant Pull Request :**

   ```
   git fetch origin
   git merge origin/develop
   
   ```

   Résolution locale des éventuels conflits avant toute demande de fusion.

4. **Pull Request (PR) & Matrice de Revue Croisée :**

   * Interdiction absolue de pousser directement sur `main` ou `develop`.

   * Toute PR vers `develop` doit être relue et approuvée par au moins **un binôme désigné** :

     * **AGBOTON Belvis** $\longleftrightarrow$ **PINEAU Faizi**

     * **RAMALINGAME Shiamamadanki** $\longleftrightarrow$ **HAFSAT Marwa**

5. **Critères d'Acceptation (*Definition of Done*) :**

   * Compilation Maven (`mvn clean verify`) et build React (`npm run build`) sans erreur.

   * Couverture de tests unitaires JUnit 5 / Mockito **> 80 %** sur les services métier (vérifiée via JaCoCo).

   * Suppression de la branche `feature/...` après fusion dans `develop`.

### 1.3 Procédure de Publication des Releases (URLs à remettre au Professeur)

À chaque jalon du projet, l'équipe publie une **Release GitHub** officielle depuis la branche `main`. L'URL de la release correspondante est transmise à l'enseignant comme preuve de livraison datée et figée :

| Jalon | Semaines | Tag Git | URL de la Release à déposer au professeur | Contenu livré | 
| ----- | ----- | ----- | ----- | ----- | 
| **Jalon 1** | S1 – S3 | `v0.1.0-alpha` | `https://github.com/Belvis/GreenOps/releases/tag/v0.1.0-alpha` | Socle PostgreSQL, Auth JWT, Console Admin RBAC, squelettes REST & React | 
| **Jalon 2** | S4 – S6 | `v0.2.0-beta` | `https://github.com/Belvis/GreenOps/releases/tag/v0.2.0-beta` | Moteurs métier complets (Modules 1, 2, 3) + suite JUnit/Mockito > 80 % | 
| **Jalon 3** | S7 – S8 | `v1.0.0` | `https://github.com/Belvis/GreenOps/releases/tag/v1.0.0` | Intégration React complète, Scheduler Cron & Alertes Email (Module 4) | 

**Commandes de création d'une Release après fusion `develop`** $\rightarrow$ **`main` :**

```
git checkout main
git pull origin main
git tag -a v0.1.0-alpha -m "Release Jalon 1 : Socle technique, JWT et RBAC"
git push origin v0.1.0-alpha

```

*(Puis sur GitHub : Onglet **Releases*** $\rightarrow$ ***Draft a new release*** $\rightarrow$ *Sélectionner le tag `v0.1.0-alpha`* $\rightarrow$ *Rédiger le changelog et publier).*

## 2. Initialisation du Socle Commun sur `develop`

Avant l'ouverture des branches individuelles, la branche `develop` est initialisée avec l'architecture technique partagée :

* **`backend/` :** Projet Spring Boot 3.x (Java 21, Maven) structuré par packages métiers (`auth`, `admin`, `webaudit`, `gitanalyzer`, `cloudsim`, `monitoring`, `common`).

* **`frontend/` :** Projet React (Vite) avec routage (`react-router-dom`), client HTTP centralisé (intercepteur Axios injectant le header `Authorization: Bearer <JWT>`) et contexte global `AuthContext`.

* **Contrat d'interface commun :** L'entité `User` et le filtre `JwtAuthenticationFilter` sont livrés en priorité par Shiamamadanki afin que les Modules 1, 2 et 3 puissent associer leurs entités (`user_id`) à l'utilisateur authentifié.

## 3. Répartition Technique Approfondie par Branche & Formules de Calcul

### 3.1 Branche 1 : `feature/shiamamadanki-socle-auth-admin` (Module 0 — Priorité Jalon 1)

**Responsable :** RAMALINGAME Shiamamadanki

* **Base de données (PostgreSQL / JPA) :**

  * `users` (`id`, `email` UNIQUE, `password_hash`, `role` ENUM \[`ROLE_USER`, `ROLE_DEV`, `ROLE_INFRA`, `ROLE_RSE`, `ROLE_ADMIN`\], `is_active`, `created_at`).

  * `password_reset_tokens` (`id`, `token_uuid` UNIQUE, `user_id` FK, `expires_at`, `is_used`).

  * `system_coefficients` (`id`, `param_key` UNIQUE, `param_value`, `unit`, `description`, `updated_at`) stockant les facteurs paramétrables par l'Admin (puissance moyenne runner, PUE par défaut, intensité carbone moyenne).

* **Couche API REST & Services Java :**

  * `AuthController` :

    * `POST /api/auth/register` (inscription par défaut avec `ROLE_USER`).

    * `POST /api/auth/login` (retourne le token JWT signé via `jjwt` contenant `sub` et `role`).

    * `POST /api/auth/forgot-password` (génère un UUID valable 15 minutes et l'envoie via `JavaMailSender`).

    * `POST /api/auth/reset-password` (vérifie validité + usage unique et met à jour le hash BCrypt).

  * `AdminController` (protégé par `@PreAuthorize("hasRole('ADMIN')")`) :

    * `GET /api/admin/users` & `PATCH /api/admin/users/{id}/role` (attribution cumulative des rôles SaaS).

    * `PATCH /api/admin/users/{id}/status` (activation / suspension de compte).

    * `GET /api/admin/quotas` (compteur d'appels journaliers vers PageSpeed, GreenWeb, Electricity Maps).

    * `PUT /api/admin/coefficients/{key}` (mise à jour à chaud des coefficients en base).

* **Tests JUnit 5 & Mockito :**

  * `JwtServiceTest` : génération, parsing des claims, rejet des tokens expirés ou altérés.

  * `PasswordResetServiceTest` : vérification du rejet d'un token expiré ou déjà consommé.

  * `AdminRbacSecurityTest` : vérification du retour HTTP `403 Forbidden` lorsqu'un utilisateur non-admin tente d'accéder à `/api/admin/**`.

* **Front-end React (Vite) :**

  * `AuthContext` & composant `ProtectedRoute` (filtrage dynamique des menus et routes selon le rôle RBAC).

  * Vues : `LoginPage`, `RegisterPage`, `ForgotPasswordPage`, `ResetPasswordPage`, et `AdminConsolePage` (gestion des utilisateurs/rôles, jauges de quotas API, édition des coefficients).

### 3.2 Branche 2 : `feature/marwa-web-audit` (Module 1 — Audit Web & EcoIndex)

**Responsable :** HAFSAT Marwa

* **Modèle Mathématique Perfectionné (EcoIndex & Empreinte Environnementale) :**
  À partir des trois métriques techniques extraites de la page ($N_{\text{dom}}$ : nombre d'éléments DOM, $N_{\text{req}}$ : nombre de requêtes HTTP, $S_{\text{kb}}$ : poids total transféré en Ko) :

  1. Calcul des quantiles normalisés ($Q_{\text{dom}}, Q_{\text{req}}, Q_{\text{size}} \in [0, 100]$) par interpolation linéaire sur les bornes de référence du référentiel EcoIndex :
     

     $$
     \text{EcoIndex} = 100 - \frac{5 \times \left(3 \cdot Q_{\text{dom}} + 2 \cdot Q_{\text{req}} + Q_{\text{size}}\right)}{6}
     $$

  2. Attribution de la classe environnementale de **`A`** ($\text{EcoIndex} > 80$) à **`G`** ($\text{EcoIndex} \le 10$).

  3. Calcul des impacts physiques par page vue (selon le modèle GreenIT / EcoIndex) :
     

     $$
     \text{GES } (\text{gCO}_2\text{eq}) = 2 + \frac{2 \times (50 - \text{EcoIndex})}{100} \qquad ; \qquad \text{Eau } (\text{cl}) = 3 + \frac{3 \times (50 - \text{EcoIndex})}{100}
     $$

* **Base de données (PostgreSQL / JPA) :**

  * `web_audits` (`id`, `user_id` FK, `target_url`, `device_strategy` \[`DESKTOP`/`MOBILE`\], `eco_index_score`, `grade`, `co2_grams`, `water_cl`, `dom_nodes`, `http_requests`, `page_size_kb`, `is_green_hosted`, `lighthouse_audits_json` JSONB, `created_at`).

  * `favorite_urls` (`id`, `user_id` FK, `url`, `alias`).

* **Couche API REST & Services Java :**

  * `PageSpeedClient` (interrogation de l'API Google PageSpeed Insights v5) & `GreenWebClient` (interrogation de `api.thegreenwebfoundation.org`).

  * `DomParserService` (analyse complémentaire du HTML via **Jsoup**).

  * `LighthouseRecommendationExtractor` : extraction stricte des opportunités officielles renvoyées par Lighthouse (`audits` avec `details.type == "opportunity"`, titre officiel, description Google, gain exact en `overallSavingsBytes` et `overallSavingsMs`, sans aucune invention).

  * `WebAuditController` : `POST /api/audits/web`, `GET /api/audits/web/history`, `GET /api/audits/web/compare?baseId={id1}&targetId={id2}`, `GET /api/audits/web/{id}/export` (PDF / JSON).

* **Tests JUnit 5 & Mockito :**

  * `EcoIndexCalculatorTest` : tests paramétrés vérifiant le score EcoIndex, la note A-G, les $\text{gCO}_2\text{eq}$ et les cl d'eau sur des profils calibrés (page minimaliste vs page lourde).

  * `LighthouseExtractorTest` : mock complet d'une réponse JSON Google PageSpeed pour vérifier l'extraction fidèle des recommandations officielles.

* **Front-end React (Vite) :**

  * `WebAuditDashboard` : formulaire URL + sélecteur Desktop/Mobile, jauge de score A-G, badge d'hébergement vert, graphique radar des 3 facteurs ($DOM$, requêtes, poids) et liste accordéon des opportunités officielles Lighthouse.

  * `AuditComparatorView` : comparaison côte à côte (Avant / Après) avec calcul des écarts et export PDF/JSON.

### 3.3 Branche 3 : `feature/belvis-ci-analyzer` (Module 2 — Analyseur Git Public & CI/CD)

**Responsable :** AGBOTON Belvis

* **Modèle Mathématique Perfectionné (Énergie & Carbone des Pipelines CI/CD) :**
  Pour chaque exécution de pipeline récupérée sur le dépôt public, de durée $d$ (en secondes) sur un runner de type $r$ :

  1. Temps de calcul machine en heures : $t_h = \frac{d}{3600}$.

  2. Puissance électrique allouée au runner virtuel ($P_{\text{runner}}$ en kW, paramétrable dans `system_coefficients`, par défaut : `0.025 kW` pour un runner Linux 2-vCPU standard, `0.045 kW` pour Windows, `0.060 kW` pour macOS) multipliée par le $\text{PUE}$ moyen du datacenter CI ($\text{PUE}_{\text{CI}} = 1.20$) :
     

     $$
     E_{\text{build}} (\text{kWh}) = t_h \times P_{\text{runner}} \times \text{PUE}_{\text{CI}}
     $$

  3. Émissions de gaz à effet de serre associées (avec intensité carbone moyenne des runners hébergés $I_{\text{CI}}$ en $\text{gCO}_2\text{eq/kWh}$) :
     

     $$
     M_{\text{CO}_2} (\text{gCO}_2\text{eq}) = E_{\text{build}} \times I_{\text{CI}}
     $$

* **Base de données (PostgreSQL / JPA) :**

  * `git_repositories` (`id`, `user_id` FK, `repo_url`, `provider` \[`GITHUB`/`GITLAB`\], `owner`, `repo_name`, `last_analyzed_at`).

  * `ci_pipeline_runs` (`id`, `repository_id` FK, `external_run_id`, `workflow_name`, `status`, `duration_seconds`, `runner_os`, `energy_kwh`, `co2_grams`, `run_date`).

* **Couche API REST & Services Java :**

  * `GitUrlParser` : validation stricte du format `https://github.com/{owner}/{repo}` ou `https://gitlab.com/{owner}/{repo}` (aucun token privé demandé ni stocké).

  * **Patron Strategy :** Interface `GitProviderStrategy` avec `GitHubPublicStrategy` (appel REST `/repos/{owner}/{repo}/actions/runs`) et `GitLabPublicStrategy` (appel REST `/api/v4/projects/{encodedPath}/pipelines`).

  * `CiDebtAnalyzerService` : analyse de l'arbre public (présence de `pom.xml` / `package.json`, absence de directive `cache` dans `.github/workflows/*.yml` ou `.gitlab-ci.yml`, taux d'échec des builds) pour générer les recommandations d'optimisation.

  * `GitAnalyzerController` : `POST /api/git/analyze`, `GET /api/git/repositories`, `GET /api/git/repositories/{id}/metrics`.

* **Tests JUnit 5 & Mockito :**

  * `GitUrlParserTest` : validation des URLs valides GitHub/GitLab et rejet des URLs invalides ou non HTTPS.

  * `GitProviderStrategyTest` : vérification du routage automatique vers la bonne implémentation et mock des flux JSON GitHub/GitLab.

  * `CiCarbonCalculatorTest` : vérification des calculs $E_{\text{build}}$ (kWh) et $M_{\text{CO}_2}$ ($\text{gCO}_2\text{eq}$) sur des séries temporelles de runs.

* **Front-end React (Vite) :**

  * `GitAnalyzerPage` : champ de saisie d'URL publique, cartes de synthèse (énergie cumulée, coût moyen par build, taux d'échec), courbe temporelle d'évolution des builds et liste des pistes d'optimisation CI/CD.

### 3.4 Branche 4 : `feature/faizi-cloud-simulator` (Module 3 — Simulateur Cloud & Matrice RGPD)

**Responsable :** PINEAU Faizi

* **Modèle Mathématique Perfectionné (Consommation Serveur & Scoring Multicritère RGPD) :**
  Pour une infrastructure configurée avec $N_{\text{vCPU}}$ cœurs virtuels, $M_{\text{RAM}}$ Go de mémoire vive, $S_{\text{SSD}}$ Go de stockage, fonctionnant $H$ heures/jour sur $D$ jours, évaluée sur une région $i$ :

  1. **Puissance informatique IT (**$P_{\text{IT}}$ **en kW) :**
     

     $$
     P_{\text{IT}} = \frac{\left(N_{\text{vCPU}} \times P_{\text{vCPU}}\right) + \left(M_{\text{RAM}} \times 0.375\right) + \left(\frac{S_{\text{SSD}}}{1000} \times 1.2\right)}{1000}
     $$

     
     *(avec* $P_{\text{vCPU}} \approx 10\text{ W}$ *par vCPU à charge moyenne,* $0.375\text{ W/Go}$ *pour la DDR4/DDR5, et* $1.2\text{ W/To}$ *pour le stockage SSD NVMe).*

  2. **Énergie totale et empreinte carbone par région** $i$ **:**
     

     $$
     E_i (\text{kWh}) = P_{\text{IT}} \times \text{PUE}_i \times (H \times D) \qquad ; \qquad C_i (\text{gCO}_2\text{eq}) = E_i \times I_{\text{elec}, i}
     $$

     
     *(où* $I_{\text{elec}, i}$ *est l'intensité carbone en direct en* $\text{gCO}_2\text{eq/kWh}$ *fournie par Electricity Maps).*

  3. **Algorithme de sélection de la recommandation optimale (Écologie + Souveraineté RGPD) :**

     * Chaque région possède un statut juridique $R_i \in \{\text{UE\_RGPD}, \text{ADEQUAT}, \text{HORS\_UE\_CLOUD\_ACT}\}$.

     * En mode strict (par défaut), les régions soumises aux lois extraterritoriales (`HORS_UE_CLOUD_ACT`) sont exclues de l'éligibilité à la recommandation optimale. Parmi les régions conformes (`UE_RGPD` / `ADEQUAT`), le moteur sélectionne celle minimisant $C_i$ (et calcule l'écart de gain $\Delta \text{CO}_2$ par rapport à la moyenne du classement).

* **Base de données (PostgreSQL / JPA) :**

  * `cloud_regions` (`zone_code` PK, `display_name`, `country`, `pue`, `gdpr_status`, `legal_note`).

  * `carbon_intensity_cache` (`zone_code` PK/FK, `carbon_intensity_gco2_kwh`, `fetched_at`).

  * `cloud_simulations` (`id`, `user_id` FK, `name`, `vcpu`, `ram_gb`, `storage_gb`, `hours_per_day`, `duration_days`, `is_favorite`, `created_at`).

* **Couche API REST & Services Java :**

  * `ElectricityMapsClient` & `CarbonIntensityCacheService` (interrogation en direct avec TTL de cache de 60 minutes et fallback automatique en base si l'API est indisponible).

  * `CloudEnergyEngine` & `RgpdOptimizationService` (classement complet de toutes les régions + désignation argumentée de la meilleure alternative).

  * `CloudSimulatorController` : `POST /api/cloud/simulate`, `GET /api/cloud/regions`, `GET /api/cloud/favorites`, `POST /api/cloud/favorites`.

* **Tests JUnit 5 & Mockito :**

  * `CloudEnergyEngineTest` : validation mathématique exacte de $P_{\text{IT}}$, $E_i$ et $C_i$.

  * `RgpdOptimizationServiceTest` : vérification qu'une région européenne conforme RGPD est élue recommandation optimale même face à une région hors-UE (Cloud Act) légèrement moins carbonée, et test du mécanisme de fallback sur le cache.

* **Front-end React (Vite) :**

  * `CloudSimulatorPage` : configurateur interactif par curseurs (vCPU, RAM, stockage, durée), carte mise en avant **« Recommandation Optimale (Écologie & RGPD) »**, tableau comparatif triable avec filtre « Conforme RGPD uniquement » et sauvegarde des configurations favorites.

### 3.5 Branche 5 : `feature/shiamamadanki-cron-alerts` (Module 4 — Surveillance & Alertes Email)

**Responsable :** RAMALINGAME Shiamamadanki

* **Base de données (PostgreSQL / JPA) :**

  * `monitoring_subscriptions` (`id`, `user_id` FK, `target_type` \[`WEB_AUDIT`/`GIT_REPO`\], `target_id`, `cron_expression`, `threshold_percent`, `is_active`, `last_run_at`).

* **Couche API REST & Services Java :**

  * `MonitoringScheduler` (`@Scheduled`) : exécute périodiquement la ré-analyse des cibles actives en invoquant les services métier des Modules 1 et 2.

  * `DeltaEvaluatorService` : compare la métrique précédente $V_{t-1}$ à la nouvelle $V_t$. Déclenche une alerte si la dégradation dépasse le seuil configuré ($\frac{V_t - V_{t-1}}{V_{t-1}} \times 100 > \text{threshold}$ pour les émissions, ou baisse de note EcoIndex).

  * `EmailAlertService` : envoi exclusif par **email (SMTP / JavaMail)** avec template HTML récapitulatif + endpoint `POST /api/monitoring/test-email`.

* **Tests JUnit 5 & Mockito :**

  * `DeltaEvaluatorServiceTest` : vérification de l'absence d'envoi si la variation reste sous le seuil, et déclenchement exact en cas de dépassement.

  * `EmailAlertServiceTest` : mock de `JavaMailSender` vérifiant le destinataire, l'objet et le contenu HTML de l'alerte.

* **Front-end React (Vite) :**

  * `MonitoringSettingsPanel` : activation/désactivation de la surveillance sur les URLs ou dépôts suivis, réglage du seuil de tolérance (%) et bouton « Envoyer un email de test ».