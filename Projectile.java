import java.util.Date;

public class Projectile {
    private double startTime;
    private double xCoordinate;
    private double yCoordinate;
    private double initialXCoordinate;
    private double initialYCoordinate;
    private double angle;
    private double velocity;
    private static double gravity = 9.81;
    private static int defaultVelocity = 75;

    public Projectile(double xCoordinate, double yCoordinate, double angle, double velocityMultiplier) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.initialXCoordinate = xCoordinate;
        //add 0.01 to initial y coordinate so when it first begins projectile motion, it doesn't get stuck in the ground
        this.initialYCoordinate = yCoordinate + 0.01;
        this.angle = angle;
        this.velocity = defaultVelocity + velocityMultiplier * 2;
    }

    public void fire() {
        this.startTime = new Date().getTime();
    }

    public void moveX(double newTime) {
        xCoordinate = velocity * ((newTime - startTime) / 1000) * Math.cos(angle) + initialXCoordinate;
    }
    
    public void moveY(double newTime) {
        double time = ((newTime - startTime) / 1000);
        yCoordinate = velocity * time * Math.sin(angle) - 0.5 * (gravity) * Math.pow(time, 2) + initialYCoordinate;
    }

    //i spent hours and hours and hours and hours and hours trying to figure out how to use projectile motion equations, and failed. so, here's a really awful made-up algorithm instead
    public double getPredictedAngle(double myXCoord, double otherTankXCoord, Terrain terrain) {
        double newAngle;

        double myYCoord = terrain.getTerrainYCoordinate(myXCoord);
        double otherTankYCoord = terrain.getTerrainYCoordinate(otherTankXCoord);

        double distanceBetween = otherTankXCoord - myXCoord;
        double heightBetween = terrain.getTerrainYCoordinate(otherTankXCoord) - terrain.getTerrainYCoordinate(myXCoord);

        //detect for hill to left
        double yToLeft = terrain.getTerrainYCoordinate(myXCoord - 15);

        //detect for hill to right
        double yToRight = terrain.getTerrainYCoordinate(myXCoord + 15);

        // 0 = left, 1 = right
        int direction = 0;
        if (distanceBetween > 0) direction = 1;

        // 0 = below, 1 = above
        int belowOrAbove = 0;
        if (heightBetween > 0) belowOrAbove = 1;

        distanceBetween = Math.abs(distanceBetween);
        heightBetween = Math.abs(heightBetween);

        double xMax = terrain.getXMax();
        double yMax = terrain.getYMax();

        double directional;
        if (direction == 0) {
            directional = yToLeft;
        } else {
            directional = yToRight;
        }

        newAngle = (defaultVelocity / velocity) * (distanceBetween / xMax);
        if (belowOrAbove == 0) {
            if (directional > myYCoord) {
                newAngle = Math.PI / 5 * newAngle;
            } else {
                newAngle = Math.PI / 4.6 * newAngle;
            }
        } else {
            newAngle = Math.PI / 3 * newAngle;
        }

        if (direction == 0) {
            newAngle = newAngle + Math.PI / 2;
        } else {
            newAngle = Math.PI / 2 - newAngle;
        }      

        return newAngle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public boolean isLanded(Terrain terrain) {
        boolean landed = false;
        if (yCoordinate <= terrain.getTerrainYCoordinate(xCoordinate)) landed = true;
        return landed;
    }

    public double getX() {
        return xCoordinate;
    }

    public double getY() {
        return yCoordinate;
    }
}