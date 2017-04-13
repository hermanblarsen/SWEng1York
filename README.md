### Build Status: [![CircleCI](https://circleci.com/gh/hermanblarsen/SWEng1York.svg?style=shield&circle-token=e154ce67a04d48efa0149fc61c732c65a0dbc871)](https://circleci.com/gh/hermanblarsen/SWEng1York)
## Project Links

- [Trello (Agile Task Organisation)](https://trello.com/b/m53LdUNP/sweng-agile-board)
- [Slack Chat](https://sweng1york.slack.com/)
- [Travis CI](https://travis-ci.com/)

### Running the Code

#### Viewer Teams

##### Run Configuration

1. Set Login.AM_I_ON_DB_TEAM = false. All networking and database functionality will be disabled for EdiManager.
2. Create a Run Configuration in IntelliJ called "Edi", with the main class being "client.managers.EdiManager".
3. Launch Edi
4. Login can be 'bypassed' by pressing login button without entering details

#### Database Team

##### Database Management

Use DataGrip/IntelliJ SQL management to connect to the database with the following details:

	db.amriksadhra.com:5432
	database: edi
	user: iilp
	pw: group1SWENG

##### Run Configuration

1. Set Login.AM_I_ON_DB_TEAM = true. All networking and database functionality will be enabled for EdiManager.
2. Create a Run Configuration in IntelliJ called "Server", with the main class being "server.SocketServer"
3. Create a Run Configuration in IntelliJ called "Edi", with the main class being "client.managers.EdiManager"
4. Launch Server, wait for Startup:
	INFO  SocketIOServer:155 - SocketIO server started at port: 8080
	INFO  SocketServer:175 - Successful connection to PostgreSQL database instance
5. Launch Edi
6. Login with "LoginName", "password"

### Testing


### Logging


### Maven

We use Maven to generate project structure and create build profiles. Key points:

- We don't have to add .jar files for libraries, can add < dependency> to pom.xml, can find these dependencies here: https://mvnrepository.com/. Latest library version will always be downloaded, and we only have to distribute the pom instead of all jars, reducing project size.

##### - Amrik
