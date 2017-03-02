### Build Status: [![Build Status](https://travis-ci.com/hermanblarsen/SWEng1York.svg?token=8qJcq4nagt6VVZqKxAqF&branch=master)](https://travis-ci.com/SwEng1York)
## Project Links

- [Trello (Agile Task Organisation)][c0cf7e50]
  [c0cf7e50]: https://trello.com/b/m53LdUNP/sweng-agile-board "Trello"
- [Slack Chat][b9db431a]
  [b9db431a]: https://sweng1york.slack.com/ "Slack Chat"
- [Travis CI][a8a89aa4]
  [a8a89aa4]: https://travis-ci.com/ "Travis CI"

### Running the Code

I'll add stuff here soon

### Testing


### Logging



### Maven

We use Maven to generate project structure and create build profiles. Key points:

- We don't have to add .jar files for libraries, can add < dependency> to pom.xml, can find these dependencies here: https://mvnrepository.com/. Latest library version will always be downloaded, and we only have to distribute the pom instead of all jars, reducing project size.

##### - Amrik