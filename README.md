### Build Status: [![Build Status](https://travis-ci.com/hermanblarsen/SWEng1York.svg?token=8qJcq4nagt6VVZqKxAqF&branch=master)](https://travis-ci.com/SwEng1York)
## Project Links

- [Trello (Agile Task Organisation)][c0cf7e50]
  [c0cf7e50]: https://trello.com/b/m53LdUNP/sweng-agile-board "Trello"
- [Slack Chat][b9db431a]
  [b9db431a]: https://sweng1york.slack.com/ "Slack Chat"
- [Travis CI][a8a89aa4]
  [a8a89aa4]: https://travis-ci.com/ "Travis CI"

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
