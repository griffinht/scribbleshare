# Contributing to Board

#### General guidelines

- Make sure to respect line endings (LF)
- Tabs should be 4 spaces

## Building and running `board-server`
You will need to build and run the `board-server` project before using or contributing to the client or server. 

`board-server` is written in Java. Make sure you have [Apache Maven](https://maven.apache.org/index.html) installed on your system before attempting to build the project.

#### Build Steps
- Clone or fork this repository
- Run `mvn clean package` in `board-server` to generate the server artifact in the newly generated `target` folder
- Set the working directory of the generated server artifact to `board-web-client` (copy the `.jar` to `board-web-client`) and run in a terminal using `java -jar board-server-xxx.jar`
 - This starts an HTTP server listening on port 80, which you can connect to at [`localhost`](localhost)
 - Stop the server gracefully by running the `stop` command in the console or by killing the process


You may also want to create a run configuration in your IDE of choice to simplify the run process. 

Modifying `board-server` will require you to recompile the project and restart the server.

Modifying `board-web-client` only requires you to refresh your browser. If you do not see your new changes, then use `Ctrl+F5` to also reset your browser cache.

## Contributing to `board-server`

It is recommended to use an IDE such as [IntelliJ IDEA](https://www.jetbrains.com/idea/) if you plan on contributing to `board-server`.

- Keep things IDE agnostic - avoid checking in IDE specific files (.iml, .idea, etc.) to version control

## Contributing to `board-web-client`

