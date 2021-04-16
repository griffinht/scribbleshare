export default class ClientMessage {
    constructor(type) {
        this.type = type;
    }

    /**
     * How many bytes this message needs for serialization.
     * The buffer must have at least this much space.
     * @returns {number}
     */
    getBufferSize() {
        return 1;
    }

    serialize(writer) {
        writer.writeUint8(this.type);
    }
}