welcome to the board project

name pending

This project is separates into the server (`board-server`) and the client (`board-web-client`). All files inside `board-web-client` can be served to the browser and are therefore public.

You will need to build and run the `board-server` project before using or contributing to the client or server.

## Building `board-server` 

`board-server` is an HTTP/WebSocket server written in Java 8. Make sure you have the Java 8 JDK and [Apache Maven](https://maven.apache.org/index.html) installed on your system before attempting to build the project.

#### Build Steps
- Clone this repository to your local machine
- Run `mvn clean package` in `board-server` to generate server artifact in the newly created `target` folder

Modifying `board-server` will require you to recompile the project and restart the server.

Modifying `board-web-client` only requires you to refresh your browser. If you do not see your new changes, then use `ctrl+F5` to also reset your browser cache.

## Running `board-server`

Once you have built `board-server`, you may run it using using the following command:

    java -jar board-server-xxx.jar --ssl.keystore http

- This starts an HTTP/WebSocket server listening on port 80, which you can connect in your browser at [http://localhost](http://localhost). 
- All WebSocket connections run on the same port as the HTTP server, and should connect to [ws://localhost/websocket](ws://localhost/websocket). 
- Stop the server gracefully to save persistent data (`ctrl+c` on Windows terminals). Killing the process may result in data loss from any unsaved persistent data.

### Properties

These can be set in several different ways:

##### `board.properties`
Create a file called `board.properties` in the working directory of the server and format as the following

    key=value
    other.key=value with spaces

##### Command line arguments

    java -jar board-server-xxx.jar --key value --other.key "value with spaces"


- ssl.keystore:
	- Path to the PKCS12 `mykeystore.pfx` containing the private key for the server
	- Required, set to `http` to disable encryption and use `http://` for connecting to the server
- ssl.passphrase:
	- Optional
	- Passphrase for `mykeystore.pfx`
- document.root.path:
  - default: documentRoot (file path, relative paths start from the working directory)
  - Sets the folder location of where the HTTP server should look to serve files. If the directory does not exist, one will be made.
  - Should be set to where `board-web-client` is. 
  - HTTP requests to a directory (`localhost` or `localhost/folder/`) will be served the `index.html` file of those directories
  - HTTP requests to a file that do not specify an extension (`localhost/file`) will be served a `.html` that corresponds to the requested name
- data.root.path
  - default: data (file path, relative paths start from the working directory)
  - Sets the folder location of the flat file storage
- debug.log.traffic
	- default: false (boolean)
	- Will print network throughput (read/write) into the console
- autosave.interval
  - default: -1 (integer in seconds, negative values will disable autosave)
  - Sets how often flat file storage will be saved to disk