import java.util.Random;

public class EasyDifficultyOpponent extends Tank {
    private double previousXCoordinate;
    private double previousAngle;
    
    public EasyDifficultyOpponent(Terrain terrain, int initialAngle, int initialXCoord) {
        //set all multipliers to 0 to make it easy
        super("Opponent", terrain, initialXCoord, 0, 0, 0, 0, initialAngle);
    }

    @Override
    public void randomMove(Terrain terrain, double playerXCoord) {
        previousXCoordinate = getXCoord();

        double moveDistance = randomNumber(5, spacesToMove());
        int direction = 0;

        //if opponent is to the left of the player, go right
        if (getXCoord() < playerXCoord) direction = 1;

        //make sure opponent is not too close to the player
        if (Math.abs(getXCoord() - playerXCoord) >= moveDistance) {
            if (getXCoord() >= terrain.getXMax()) {
                //if within distance from right wall, go opposite direction than wall
                direction = 0;
            } else if (getXCoord() <= 0) {
                //if within distance from left wall, go opposite direction than wall
                direction = 1;
            }

            //perform move
            move(direction, terrain, moveDistance);
        }
    }

    @Override
    public void cannonMove(Terrain terrain, Projectile projectile, double otherTankXCoord) {
        double newAngle = projectile.getPredictedAngle(getXCoord(), otherTankXCoord, terrain);

        previousAngle = getAngle();

        //randomly adjust angle to make it easier than the hard opponent
        if ((int) randomNumber(0, 1) == 1) {
            newAngle += randomNumber(-20, 20);
        }

        //exception handling
        if (!isLegalAngle(newAngle)) {
            if (newAngle < -90) {
                newAngle = -90;
            } else if (newAngle > 270) {
                newAngle = 270;
            }
        }

        setAngle(newAngle);
    }

    @Override
    public double getPreviousXCoord() {
        return previousXCoordinate;
    }

    @Override
    public double getPreviousAngle() {
        return previousAngle;
    }

    @Override
    public double randomNumber(double low, double high) {
        return new Random().nextDouble() * (high - low) + low;
    }
}