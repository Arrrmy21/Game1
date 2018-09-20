package warships.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
public class Coord{
    @JsonProperty("x")
    private int x;
    @JsonProperty("y")
    private int y;

    public Coord() {
    }

    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof Coord){
            Coord to = (Coord) o;
            return to.x == x && to.y == y;
        }
        return super.equals(o);
    }


}
