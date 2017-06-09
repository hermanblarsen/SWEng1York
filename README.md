### Build Status: [![CircleCI](https://circleci.com/gh/hermanblarsen/SWEng1York.svg?style=shield&circle-token=e154ce67a04d48efa0149fc61c732c65a0dbc871)](https://circleci.com/gh/hermanblarsen/SWEng1York)
## Project Links

- [Trello (Agile Task Organisation)](https://trello.com/b/m53LdUNP/sweng-agile-board)
- [Slack Chat](https://sweng1york.slack.com/)
- [Circle CI](https://circleci.com)

### Running the Code
1. Create a Run Configuration in you IDE (We use IntelliJ) called "Edi", with main class  "com.i2lp.edi.client.managers.EdiManager".
2. Launch Edi
3. Login with Username "Teacher" or "Student" (depending on what you want content to see) with password "password".

### Logging
The active sessions are logged in the target/logs, with varying filters in different logs.

### Maven

Maven is used to generate project structure and create build profiles. Key points:

- We don't have to add .jar files for libraries, can add < dependency> to pom.xml, can find these dependencies here: https://mvnrepository.com/. Latest library version will always be downloaded, and we only have to distribute the pom instead of all jars, reducing project size.

#### I2LP - Group 1 SWENG - Creators of Edi
