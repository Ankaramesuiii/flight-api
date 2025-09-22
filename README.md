Owner : Mohamed Aziz Belhaj
Email : mohamed.aziz.belhajj@gmail.com

Flight App - Backend (Spring Boot + SQLite)
## Prérequis
- Java 17+
- Maven 3.9+
- SQLite (inclus via JDBC)
## Installation
1. Cloner le dépôt et aller dans le dossier backend :
   git clone https://github.com/votre-repo/flight-app.git
   cd flight-app/backend
2. Vérifier la configuration `application.yml` :
   spring: datasource: url: jdbc:sqlite:vols.db driver-class-name: org.sqlite.JDBC jpa: hibernate:
   ddl-auto: update show-sql: true
3. Installer les dépendances :
   mvn clean install
## Exécution
mvn spring-boot:run
Le backend tourne sur http://localhost:8080
## Tests
mvn test
## Build de production
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
