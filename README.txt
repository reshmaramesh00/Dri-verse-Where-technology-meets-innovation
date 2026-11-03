DRI-VERSE - PROJECT SETUP

STEP 1: INSTALL REQUIREMENTS
- Install Java (JDK 21 or later)
- Install MySQL Community Server
- Install VS Code or IntelliJ IDEA

STEP 2: SET UP THE DATABASE
1. Open MySQL.
2. Open the file:
   database.sql
3. Run the complete SQL script.
4. This creates the drivers_db database and users table.

STEP 3: CONFIGURE THE DATABASE PASSWORD
Open:

src/main/resources/application.properties

Find:

spring.datasource.password=ENTER_YOUR_MYSQL_PASSWORD

Replace ENTER_YOUR_MYSQL_PASSWORD with your own MySQL root password.

STEP 4: START THE BACKEND
Open a terminal in the project folder.

Mac/Linux:
./mvnw spring-boot:run

Windows:
mvnw.cmd spring-boot:run

Wait until you see:

Started DriversApplication

IMPORTANT:
Keep this terminal running while using the application.

STEP 5: RUN THE APPLICATION
Open:

src/main/java/Dri_verse/view/RegistrationFrame.java

Run the Java file.

The Registration page should open.

You can:
- Register a new student/instructor
- Click LOGIN to open the Login page
- Test login using the registered account

IMPORTANT:
The Spring Boot backend must be running before using Registration or Login.