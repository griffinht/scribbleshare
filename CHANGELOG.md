## v0.1
Add real time drawing

- Change file structure and how files are served
- Fix custom invite URL
- Automatic connection

## v0.2
Basic functionality to allow for more features to be added
- Instant live documents
- Data persistence
- User identity, separation between browser client and user

### v0.2.1
Room to document based model

- Add sidebar
- Change offset points to absolute points in draw
- Refactor packets
- Refactor js
- Event based WebSocketHandler
- Change how URLs work

### v0.2.2
Add document opening/closing

- Change how queued packets work
- Documents are saved before closed
- Change protocol again

### v0.2.3
Flat file data persistence

- Remove ConsoleManager
- Add shutdown hooks for saving
- Change handshake
- Functioning Java serialization
- Complete rework of config system
- SSL support

### v0.2.4
User identity

- Change URL parsing
- Add HTTP cache as a config option
- Session management
- Sessions are linked to documents
- Improve toString debug

### v0.2.5
Multiple clients per user

- Change how sessions are handled
- Clean up script loading
- Fix hashcode issues

### v0.2.6
Design

- Clean up CSS
- Add changelog
- Add invite/connected users toolbar
- "Fix" canvas resize
- Change/clean up config system
- Improve client debug
- Better local user with different handshake

## v0.3
Architecture rework

### v0.3.1
SQL data persistence

- Dockerized server with Maven dependencies
- Add environment variable config method
- Fix send rate of client
- Remove flatfile, add runtime (in memory only) and PostgresSQL database
- Move frontend to independent HTTP server (nginx)

### v0.3.2
Rework protocol, document serialization

- Canvas objects instead of points
- Rename packets to messages
- Add better serialization/deserialization methods

### v0.3.3
Rework db, implement document persistence

- Canvas is saved in db
- Rework user authentication

### v0.3.4
Document update/delete operations, overhaul db and config

- Fix changes not being saved on canvas close
- Fix canvas serialization
- Fix connected clients list
- Add db creation scripts into version control
- Overhaul config - improve exceptions
- Add document update and delete functionality

## v0.4
Better state management, invite codes, features

### v0.4.0
Invite codes, better state management
- Client can get invite codes for a document, persisted in db
- Client can join with an invite code
- Better server state management

### v0.4.1
Docker deployment improvements
- Fix credential typo
- Minify javascript
- Add retry logic to board-room