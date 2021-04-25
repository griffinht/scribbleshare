export default class MouseMove {
    constructor(reader) {
        this.dt = reader.readUint8();
        this.x = reader.readInt16();
        this.y = reader.readInt16();
    }

    serialize(writer) {
        writer.writeUint8(this.dt);
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
    }

    static create(dt, x, y) {
        let object = Object.create(this.prototype);
        object.dt = dt;
        object.x = x;
        object.y = y;
        return object;
    }
}