import java.util.Random;

public class HardDifficultyOpponent extends Tank {
    private double previousXCoordinate;
    private double previousAngle;

    public HardDifficultyOpponent(Terrain terrain, int initialAngle, int initialXCoord, double healthMultiplier, double projectileVelocityMultiplier, double projectileDamageMultiplier, double distanceMultiplier) {
        super("Opponent", terrain, initialXCoord, healthMultiplier, projectileVelocityMultiplier, projectileDamageMultiplier, distanceMultiplier, initialAngle);
    }

    @Override
    public void randomMove(Terrain terrain, double playerXCoord) {
        previousXCoordinate = getXCoord();

        double currentYCoord = terrain.getTerrainYCoordinate(getXCoord());

        //detect for hill to left
        double yToLeft = terrain.getTerrainYCoordinate(getXCoord() - 25);

        //detect for hill to right
        double yToRight = terrain.getTerrainYCoordinate(getXCoord() + 25);

        double moveDistance = randomNumber(5, spacesToMove());
        
        int direction = 0;

        //if opponent is to the left of the player, go right
        if (getXCoord() < playerXCoord) direction = 1;

        double distanceBetween = Math.abs(getXCoord() - playerXCoord);

        //make sure opponent is not too close to the player
        if (distanceBetween >= moveDistance) {
            //don't move if you're on a large hill because you have an advantage
            if (distanceBetween < 750 && direction == 0 && yToLeft - currentYCoord <= -10) {
                moveDistance = 0;
            } else if (distanceBetween < 750 && direction == 1 && yToRight - currentYCoord <= 10) {
                moveDistance = 0;
            }

            //perform move
            move(direction, terrain, moveDistance);
        }
    }

    @Override
    public void cannonMove(Terrain terrain, Projectile projectile, double otherTankXCoord) {
        double newAngle = projectile.getPredictedAngle(getXCoord(), otherTankXCoord, terrain);

        previousAngle = getAngle();

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