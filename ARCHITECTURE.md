# Architecture du Projet One Citations 🏗️

Ce document détaille les composants techniques de la plateforme **One Citations**. L'architecture est basée sur des microservices conteneurisés, orchestrés par Docker Compose.

## 📊 Diagramme d'Architecture

```mermaid
graph TD
    subgraph "Client"
        Browser["🖥️ Navigateur / Frontend\n(Port 3000)"]
    end

    subgraph "Infrastructure & Sécurité"
        Kong["🛡️ Kong API Gateway\n(Port 8000)"]
        Keycloak["🔑 Keycloak (IAM)\n(Port 8080/8085)"]
    end

    subgraph "Microservices (Backend)"
        ProfilesAPI["👤 Profiles API"]
        CitationsAPI["📝 Citations API"]
        ImagesAPI["🖼️ Images API"]
    end

    subgraph "Data"
        Mongo["🍃 MongoDB\n(Database)"]
    end

    %% Flows
    Browser -->|API Requests| Kong
    Browser -->|Auth (Login)| Keycloak
    
    Kong -->|Route: /api/v1/profiles| ProfilesAPI
    Kong -->|Route: /api/v1/citations| CitationsAPI
    Kong -->|Route: /api/v1/images| ImagesAPI

    %% Dependencies
    ProfilesAPI --> Mongo
    CitationsAPI --> Mongo
    ImagesAPI --> Mongo
    
    %% Security Validation
    ProfilesAPI -.->|Validate Token| Keycloak
    CitationsAPI -.->|Validate Token| Keycloak
    ImagesAPI -.->|Validate Token| Keycloak
```

## 🧩 Description des Composants

### 1. 🖥️ Frontend (Interface Utilisateur)
- **Tech**: Vanilla JS, Vite, Nginx (Docker).
- **Rôle**: Interface utilisateur pour le public, les rédacteurs (Writers) et les modérateurs.
- **Fonctionnalités**:
  - Connexion via Keycloak.
  - Affichage public des citations et images.
  - Soumission de citations (Rédacteurs).
  - Validation des citations (Modérateurs).

### 2. 🛡️ Kong (API Gateway)
- **Rôle**: Point d'entrée unique pour toutes les requêtes API (`http://localhost:8000`).
- **Sécurité**:
  - Gère les **clés API** (API Key) pour empêcher l'accès non autorisé.
  - Configure le **CORS** pour autoriser le Frontend à communiquer avec le Backend.
- **Routage**: Dirige les requêtes vers le bon microservice (ex: `/api/v1/citations` -> Citations API).

### 3. 🔑 Keycloak (Identity & Access Management)
- **Rôle**: Serveur d'authentification centralisé.
- **Fonctionnalités**:
  - Gère les utilisateurs (`writer`, `moderator`).
  - Délivre les jetons JWT (Tokens) pour l'authentification sécurisée.
  - Les microservices vérifient ces jetons pour autoriser les actions sensibles (ex: validation).

### 4. 👤 Profiles API
- **Tech**: Java, Spring Boot.
- **Rôle**: Gestion des profils utilisateurs.
- **Détail**: Récupère les informations de l'utilisateur connecté depuis son Token JWT.

### 5. 📝 Citations API (Coeur Métier)
- **Tech**: Java, Spring Boot.
- **Rôle**: Gestion du cycle de vie des citations.
- **Flux**:
  - **Soumission**: Un `writer` envoie une citation -> Statut `PENDING`.
  - **Validation**: Un `moderator` valide la citation -> Statut `VALIDATED`.
  - **Consultation**: Le public peut voir une citation aléatoire validée.

### 6. 🖼️ Images API
- **Tech**: Java, Spring Boot.
- **Rôle**: Fournit des images aléatoires.
- **Détail**: Utilise une banque d'images locale ou téléchargée pour servir des visuels via l'API.

### 7. 🍃 MongoDB
- **Rôle**: Base de données NoSQL commune.
- **Usage**: Stocke les profils, les citations et les métadonnées des images. Contient une base de données unique `one-citations`.

---
*Généré par Antigravity*
