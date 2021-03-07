package net.stzups.board.data.objects;

import java.io.Serializable;

public class Point implements Serializable {
    public int dt;
    public short x;
    public short y;

    public Point(int dt, short x, short y) {
        this.dt = dt;
        this.x = x;
        this.y = y;
    }
}
