export default class ClientMessage {
    constructor(type) {
        this.type = type;
    }

    serialize(writer) {
        writer.writeUint8(this.type);
    }
}