# Mémoires vives

**Mémoires vives** est une plateforme participative permettant aux membres de partager des souvenirs liés à des lieux.
Faits historiques, événements culturels ou sportifs, catastrophes naturelles, anecdotes du quotidien… toutes et tous peuvent trouver leur place sur cette carte vivante qui relie les personnes et les époques.

---

## Fonctionnalités principales

- Carte interactive pour explorer et déposer des souvenirs.
- Possibilité d’ajouter des photos et des descriptions pour chaque événement.
- Filtrage des souvenirs par thème, période ou type d’événement.
- Gestion de comptes utilisateurs (pseudonyme, e-mail, mot de passe, photo de profil).
- Modération a posteriori pour garantir un contenu respectueux.
- Licence libre Creative Commons BY-SA pour les contributions.

---

## Technologies utilisées

- **Backend :** Java, Spring Boot, Spring Security
- **Frontend :** Thymeleaf, HTML5, Tailwind CSS
- **Base de données :** MySQL
- **Build :** Gradle Kotlin DSL (`build.gradle.kts`)
- **Serveur et hébergement :** OVH
- **Autres outils :** Git, Docker (optionnel pour la base de données), Leaflet.js (pour la carte interactive)

---

## Installation et lancement local

### Prérequis

- Java 17 ou supérieur
- Gradle
- MySQL
- Docker (optionnel pour la base de données)

### Étapes

1. Cloner le dépôt :

```bash
git clone https://github.com/ton-utilisateur/memoires-vives.git
cd memoires-vives
```

2. Configurer la base de données dans `src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/memoires_vives?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=motdepasse
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

3. Lancer le projet avec Gradle :

```bash
./gradlew bootRun
```

4. Accéder au site dans le navigateur : [http://localhost:8080](http://localhost:8080)

---

## Contribution

Les contributions sont les bienvenues !  
Vous pouvez contribuer pour :

- Ajouter des fonctionnalités
- Corriger des bugs
- Améliorer le contenu et la documentation
- Tester le site et proposer des améliorations

**Important :** toutes les contributions doivent respecter la licence **CC BY-SA** pour les contenus publiés et ne pas violer de droits d’auteur.

**Processus pour contribuer :**

```bash
# Forker le dépôt
git checkout -b feature/ma-fonctionnalite
# Faire vos modifications
git commit -am "Ajout d'une fonctionnalité"
git push origin feature/ma-fonctionnalite
# Ouvrir une Pull Request sur GitHub
```

---

## Licence

Ce projet est publié sous licence [**Creative Commons BY-SA 4.0**](https://creativecommons.org/licenses/by-sa/4.0/).

---

## Contact

Pour toute question ou suggestion :  
📧 Email : contact@memoires-vives.fr
