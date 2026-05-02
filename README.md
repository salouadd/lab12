# Projet Localisation (Android + PHP/MySQL + OSMDroid)

Ce projet permet de récupérer la position GPS d'un smartphone Android, de l'envoyer en temps réel à un serveur PHP via la bibliothèque Volley, et de l'afficher sur une carte OpenStreetMap.

##  Fonctionnalités
- Récupération de la Latitude, Longitude, Altitude et Précision.
- Envoi automatique des données au serveur toutes les 5 secondes (si mouvement).
- Stockage en base de données MySQL.
- Affichage de la position sur une carte OpenStreetMap (OSMDroid).
- Gestion des permissions Runtime (Android 6.0+).

##  Partie Android
### Dépendances (build.gradle)
- **Volley** : Pour les requêtes HTTP POST vers le serveur.
- **OSMDroid** : Pour l'affichage de la carte OpenStreetMap.

### Configuration
1. Dans `MainActivity.java`, remplacez l'adresse IP dans `insertUrl` par l'adresse IPv4 de votre PC (utilisez `ipconfig` dans le terminal).
   ```java
   private final String insertUrl = "http://192.168.x.x/localisation2/createPosition.php";
   ```
2. Assurez-vous que le téléphone et le PC sont sur le **même réseau Wi-Fi**.

### Permissions
L'application requiert :
- `INTERNET`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `WRITE_EXTERNAL_STORAGE` (pour le cache de la carte)

##  Partie PHP / MySQL
### Structure des dossiers (WAMP/XAMPP)
Le dossier `localisation2` doit contenir :
- `classe/Position.php` : Entité Position.
- `connexion/Connexion.php` : Connexion PDO à la base de données.
- `dao/IDao.php` : Interface DAO.
- `service/PositionService.php` : Implémentation de l'insertion SQL.
- `createPosition.php` : Point d'entrée pour l'application Android.

### Base de données
- **Nom de la base** : `localisation2`
- **Table** : `localisation2`
- **Structure** :
  - `id` (INT, AI, PK)
  - `latitude` (DOUBLE)
  - `longitude` (DOUBLE)
  - `date` (DATETIME)
  - `imei` (VARCHAR)

## 🛠 Installation & Test
1. Importez la base de données via phpMyAdmin.
2. Placez les fichiers PHP dans `www/localisation2/` ou `htdocs/localisation2/`.
3. Désactivez le **Pare-feu Windows** ou autorisez le port 80.
4. Lancez l'application Android, acceptez la permission GPS.
5. Vérifiez la table MySQL après quelques secondes.
