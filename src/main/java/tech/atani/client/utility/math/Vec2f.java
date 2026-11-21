package tech.atani.client.utility.math;

public record Vec2f(float x, float y) {

    public double distanceSquared(double x, double y) {
        double dx = x - this.x;
        double dy = y - this.y;
        return dx * dx + dy * dy;
    }

    public double distanceSquared(Vec2f position) {
        return distanceSquared(position.x, position.y);
    }

    public double distance(double x, double y) {
        return Math.sqrt(distanceSquared(x, y));
    }

    public double distance(Vec2f position) {
        return distance(position.x, position.y);
    }

}
