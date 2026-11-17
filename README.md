# GreenOps — Plateforme SaaS d'Évaluation et de Pilotage Carbone Logiciel

![Version](https://img.shields.io/badge/version-v0.1.0--alpha-blue?style=flat-square)
![License](https://img.shields.io/badge/license-MIT-emerald?style=flat-square)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen?style=flat-square)
![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen?style=flat-square)
![React](https://img.shields.io/badge/React-TypeScript%20%2B%20Vite%20%2B%20Tailwind-61DAFB?style=flat-square)
![Coverage](https://img.shields.io/badge/coverage-%3E80%25-yellowgreen?style=flat-square)

**GreenOps** est une plateforme SaaS d'écologie numérique conçue pour mesurer, analyser et réduire l'empreinte environnementale d'un projet logiciel sur l'ensemble de son cycle de vie : sites web déployés, usine logicielle (dépôts Git publics & pipelines CI/CD) et hébergement Cloud (croisé avec la conformité RGPD).

* **Dépôt officiel :** [https://github.com/Belvis/GreenOps](https://github.com/Belvis/GreenOps)

---

## 1. Équipe & Répartition des Modules

Le développement suit une approche par **tranches verticales** (*vertical slicing*) : chaque membre développe de bout en bout son modèle de données PostgreSQL, son API REST Java, ses tests unitaires JUnit 5 / Mockito et son interface React.

| Membre | Compte GitHub | Module & Périmètre Technique |
| :--- | :--- | :--- |
| **RAMALINGAME Shiamamadanki** | [@Shiama-ramalingame](https://github.com/Shiama-ramalingame) | **Module 0 & Module 4 :** Socle d'authentification JWT, réinitialisation mot de passe, console Admin RBAC, surveillance planifiée (Cron) et alertes email |
| **HAFSAT Marwa** | [@Marwa-Hafsati](https://github.com/Marwa-Hafsati) | **Module 1 :** Audit Web d'éco-conception en temps réel (EcoIndex, Google PageSpeed / Lighthouse, The Green Web Foundation) |
| **AGBOTON Belvis** | [@Belvis](https://github.com/Belvis) | **Module 2 :** Analyseur d'impact des dépôts publics (GitHub/GitLab via patron *Strategy*) et calcul énergétique des pipelines CI/CD |
| **PINEAU Faizi** | [@GitFaiziT6](https://github.com/GitFaiziT6) | **Module 3 :** Simulateur d'infrastructure Cloud (Electricity Maps, PUE) et matrice de souveraineté RGPD |

---

## 2. Architecture du Dépôt

```text
GreenOps/
├── docker-compose.yml                # Conteneur PostgreSQL 16 pour le développement local
├── README.md                         # Documentation principale du projet
├── LICENSE                           # Licence MIT
├── doc/                              # Documentation officielle versionnée (Markdown + PDF)
│   ├── dossier_lancement.md          # Rendu 1 : Cadrage, architecture et protocole IA
│   ├── dossier_lancement.pdf
│   └── methodologie_developpement.md # Workflow Git, formules mathématiques et plan par branches
├── backend/                          # API REST Java 21 / Spring Boot 3.4 (Maven)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/greenops/
│       │   ├── GreenOpsApplication.java
│       │   ├── auth/                 # Authentification JWT & gestion des comptes
│       │   ├── admin/                # Administration SaaS & gestion des rôles RBAC
│       │   ├── webaudit/             # Moteur EcoIndex & connecteurs PageSpeed / Green Web
│       │   ├── gitanalyzer/          # Analyseurs publics GitHub / GitLab & calcul CI/CD
│       │   ├── cloudsim/             # Modélisation Cloud, Electricity Maps & scoring RGPD
│       │   ├── monitoring/           # Planificateur Cron & notifications SMTP
│       │   └── common/               # Configurations transverses et gestion d'exceptions
│       └── test/java/com/greenops/   # Tests unitaires JUnit 5 & Mockito (JaCoCo > 80 %)
└── frontend/                         # Interface utilisateur SPA (React, TypeScript, Vite, Tailwind CSS)
    ├── package.json
    ├── vite.config.ts
    └── src/
        ├── components/
        │   ├── layout/               # AppLayout (Sidebar fixe RBAC + Topbar)
        │   └── ui/                   # Composants réutilisables (Logo, Button, Card, Badges)
        ├── context/                  # Gestion du thème (Clair/Sombre) et des habilitations RBAC
        └── pages/
            ├── public/               # LandingPage publique (/)
            ├── dashboard/            # Tableau de bord général (/dashboard)
            ├── auth/                 # Connexion, inscription, mot de passe oublié
            ├── admin/                # Console Super-Admin SaaS
            ├── webaudit/             # Interface Module 1 : Audit Web
            ├── gitanalyzer/          # Interface Module 2 : Analyse Git & CI/CD
            └── cloudsim/             # Interface Module 3 : Simulateur Cloud & RGPD
```

---

## 3. Prérequis d'Installation

Avant de lancer le projet en local, assurez-vous d'avoir installé :
* **Node.js** (version 20 ou supérieure) et **npm**
* **Java JDK 21** et **Apache Maven 3.9+**
* **Docker & Docker Compose** (ou une instance locale **PostgreSQL 16**)
* **Git**

---

## 4. Installation et Lancement en Local

### Étape 1 : Cloner le dépôt et se placer sur la branche `develop`
```bash
git clone https://github.com/Belvis/GreenOps.git
cd GreenOps
git checkout develop
```

### Étape 2 : Démarrer la base de données PostgreSQL
À la racine du projet, lancez le conteneur PostgreSQL configuré dans `docker-compose.yml` :
```bash
docker-compose up -d
```
*Paramètres par défaut :*
* **Hôte / Port :** `localhost:5432`
* **Base de données :** `greenops`
* **Utilisateur :** `greenops_user`
* **Mot de passe :** `greenops_password`

### Étape 3 : Lancer le Back-end (API Spring Boot)
Dans un premier terminal, depuis le dossier `backend/` :
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
L'API REST démarre sur **`http://localhost:8080`**.

*(Variables d'environnement optionnelles pour les APIs externes : `PAGESPEED_API_KEY`, `ELECTRICITY_MAPS_API_KEY`, `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`).*

### Étape 4 : Lancer le Front-end (Interface React / Vite / Tailwind CSS)
Dans un second terminal, depuis le dossier `frontend/` :
```bash
cd frontend
npm install
npm run dev
```
Ouvrez votre navigateur sur **`http://localhost:5173/`** pour accéder à l'application.

---

## 5. État Actuel & Fonctionnalités Disponibles (`v0.1.0-alpha`)

### Interface Graphique & Socle Front-end (`frontend/`)
* **Landing Page publique (`/`) :** Présentation des 3 piliers GreenOps, navigation rapide et pied de page.
* **Thème Clair / Sombre :** Basculement dynamique sur l'ensemble de l'application (`#F8FAFC` / vert émeraude `#10B981` / bleu nuit `#0F172A`).
* **App Shell & Sidebar RBAC (`/dashboard`) :**
  * Barre latérale fixe avec verrouillage visuel conditionnel des modules selon le profil actif.
  * Sélecteur de simulation des **5 rôles SaaS** (`Profil Découverte`, `Développeur / DevOps`, `Ingénieur Infra / Cloud`, `Décideur RSE`, `Super-Admin SaaS`) permettant de tester l'ergonomie des droits d'accès avant le branchement complet du JWT.
* **Bibliothèque UI commune (`src/components/ui/`) :** `GreenOpsLogo`, `Button`, `Card`, `EcoGradeBadge` (notes `A` à `G`) et `StatusBadge`.

### Architecture Back-end (`backend/`)
* Projet Maven Spring Boot 3.4 (Java 21) initialisé avec Spring Web, Spring Security, Spring Data JPA, PostgreSQL, JJWT, JavaMail, Jsoup et JaCoCo.

---

## 6. Workflow Git & Contribution

Le projet applique le modèle **Git Feature Branching** détaillé dans [`doc/methodologie_developpement.md`](doc/methodologie_developpement.md) :
1. **`main` :** Branche protégée réservée aux livrables validés et aux **GitHub Releases** (`v0.1.0-alpha`, `v0.2.0-beta`, `v1.0.0`).
2. **`develop` :** Branche centrale d'intégration continue de l'équipe.
3. **`feature/<prenom>-<module>` :** Branches de travail isolées tirées depuis `develop`. Toute fusion vers `develop` passe obligatoirement par une **Pull Request** relue par un binôme (**Belvis $\leftrightarrow$ Faizi**, **Shiamamadanki $\leftrightarrow$ Marwa**) et validée par la suite de tests (`mvn test`).

---

## 7. Licence

Ce projet est distribué sous licence **MIT**. Voir le fichier [`LICENSE`](LICENSE) pour plus de détails.