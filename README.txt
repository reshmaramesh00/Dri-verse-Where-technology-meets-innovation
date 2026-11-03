# 🚗 Dri-verse – Where Technology Meets Innovation

**A Smart Driving School Management System**

Dri-verse is a software-based Driving School Management System designed to simplify and digitize driving school operations. It provides a centralized platform for learners, instructors and administrators to manage driving lessons, instructor requests, payments and learner activities.

The project aims to make driving school management more efficient, organized and accessible through technology.

## ✨ Key Features

### 👨‍🎓 Learner Module
- User registration and login
- Learner dashboard
- Browse available driving instructors
- Send instructor requests
- Class scheduling
- Learner test registration
- Payment submission and payment status tracking
- Mock test support

### 🚘 Instructor Module
- Instructor dashboard
- Manage learner requests
- View assigned learners
- Manage driving lesson schedules
- Review learner payment requests

### 🛠️ Admin Module
- Administrative dashboard
- Manage users and system activities
- Monitor driving school operations

### 🔐 Additional Features
- Forgot password functionality
- Database connectivity
- Payment approval workflow
- Role-based access

## 💻 Technologies Used

| Technology | Purpose |
|---|---|
| Java | Core programming language |
| Java Swing | Desktop user interface |
| Spring Boot | Backend development |
| JDBC | Database connectivity |
| MySQL | Database management |
| Maven | Project build and dependency management |
| VS Code | Development environment |
| Git & GitHub | Version control |

## 📂 Project Structure

```text
Dri-verse/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── Dri_verse/
│   │   │       ├── controller/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       └── view/
│   │   │
│   │   └── resources/
│   │       └── images/
│   │
│   └── test/
│
├── database.sql
├── pom.xml
├── mvnw
├── README.md
└── .gitignore
```

## ⚙️ Installation and Setup

### Prerequisites

Make sure you have installed:

- Java JDK
- MySQL Server
- Visual Studio Code (or another Java IDE)
- Git

### 1. Clone the repository

```bash
git clone https://github.com/reshmaramesh00/Dri-verse-Where-technology-meets-innovation.git
```

### 2. Open the project

cd Dri-verse-Where-technology-meets-innovation

3. Set up the database

Start your MySQL server.

Create a database named drivers_db.

Import or execute the provided database.sql file.

Configure your local database credentials.

4. Configure database credentials

Create src/main/resources/application.properties locally:

spring.application.name=driverse
spring.datasource.url=jdbc:mysql://localhost:3306/drivers_db
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

Set the DB_PASSWORD environment variable to your MySQL password. The local configuration file is excluded from GitHub.

5. Build the project

./mvnw clean package

6. Run the Spring Boot application

./mvnw spring-boot:run

🎯 Project Objective

The main objective of Dri-verse is to transform traditional driving school management into a more efficient digital system by connecting learners, instructors and administrators through a unified platform.

🏫 Academic Project

Developed as part of the B.Tech Computer Science and Engineering programme.

Project Name: Dri-verse – Where Technology Meets Innovation

Making driving school management smarter, simpler and more connected. 🚗
