# CreatorXp Application

### Full-stack project using:
- React (Frontend)
- Spring Boot (Backend)
- MySql (Database)

---

## Prerequisites

- Node.js
- npm
- Java
- Maven
- MySQL Server + MySQL Workbench
- Docker Desktop

## Check installed versions:

```
node -v
npm -v
java -version
javac -version
mvn -v
```
## Backend Setup
```
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

## Frontend Setup
```
cd frontend
npm install
npm start
```

## MySQL Setup (Docker)
- Install and open Docker Desktop <br/>
- From the project root, start the database container:
```
docker compose up -d
```
- Connect to the database to confirm the table was created:
```
docker exec -it creatorxp-mysql mysql -u YOUR_USER -p creatorxp
```
Fill YOUR_USER with your username before entering, then enter the database password when prompted. Credentials are in your .env file.
