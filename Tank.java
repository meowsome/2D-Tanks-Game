import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Tank {
    private String name;
    private double xCoordinate;
    private double yCoordinate;
    private double startingHealth;
    private double health;
    private double spacesToMove;
    private double money;
    private double angle;
    private static int defaultHealth = 10;
    private static int defaultSpacesToMove = 50;

    //upgrade multipliers
    private double healthMultiplier, projectileVelocityMultiplier, projectileDamageMultiplier, distanceMultiplier;

    public Tank(String name, Terrain terrain, int startingX, double healthMultiplier, double projectileVelocityMultiplier, double projectileDamageMultiplier, double distanceMultiplier, int initialAngle) {
        this.name = name;
        this.xCoordinate = startingX;
        this.yCoordinate = terrain.getTerrainYCoordinate(xCoordinate);
        this.health = defaultHealth + healthMultiplier;
        this.startingHealth = health;
        this.healthMultiplier = healthMultiplier;
        this.projectileVelocityMultiplier = projectileVelocityMultiplier;
        this.projectileDamageMultiplier = projectileDamageMultiplier;
        this.distanceMultiplier = distanceMultiplier;
        this.angle = Math.toRadians(initialAngle);
        newTurn();
    }

    public void move(int direction, Terrain terrain, double moveDistance) {
        if (direction == 0) {
            xCoordinate -= moveDistance;
        } else {
            xCoordinate += moveDistance;
        }

        yCoordinate = terrain.getTerrainYCoordinate(xCoordinate);

        spacesToMove -= moveDistance;
    }

    public void randomMove(Terrain terrain, double playerXCoord) {
        //blank method to be overwritten
    }
    
    public boolean canMove(int direction, double spaces, double xMax) {
        boolean canMove = false;

        if (spacesToMove > 0) {
            if (direction == 0 && xCoordinate - spaces >= 0){
                canMove = true;
            } else if (direction == 1 && xCoordinate + spaces <= xMax) {
                canMove = true;
            }
        }
        
        return canMove;
    }
    
    public void setAngle(double angle) {
        this.angle = angle;
    }

    public boolean isLegalAngle(double angle) {
        boolean legal = true;

        if (angle < -90 || angle > 270) {
            legal = false;
        }

        return legal;
    }

    public void cannonMove(Terrain terrain, Projectile projectile, double otherTankXCoord) {
        //blank method to be overwritten
    }

    public void newTurn() {
        this.spacesToMove = defaultSpacesToMove + distanceMultiplier * 10;
    }

    public void subtractHealth(boolean critical, double damageMultiplier) {
        if (critical) {
            health -= 5 * (1 + (damageMultiplier * .1));
        } else {
            health -= 2 * (1 + (damageMultiplier * .1));
        }
    }

    public void setMoney(double amount) {
        money = amount;
    }

    public void editFile(int column, double amount) {
        //Columns: 0 = name, 1 = money, 2 = health multiplier, 3 = projectile velocity multiplier, 4 = projectile damage multiplier, 5 = distance multiplier
        
        double newValue;

        List<String> fileContent = new ArrayList<>();
        
        Path path = Paths.get("auth.txt");
        
        try {
            fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
        } catch (Exception err) {
            err.printStackTrace();
        }

        for (int i = 0; i < fileContent.size(); i++) {
            String[] lineColumns = fileContent.get(i).split(" ");

            if (lineColumns[0].equals(name)) {
                newValue = Double.parseDouble(lineColumns[column]) + amount;
                lineColumns[column] = Double.toString(newValue);

                fileContent.set(i, String.join(" ", lineColumns));

                break;
            }
        }

        try {
            Files.write(path, fileContent, StandardCharsets.UTF_8);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public double getMoney() {
        return money;
    }

    public double getStartingHealth() {
        return startingHealth;
    }

    public double getHealth() {
        return health;
    }

    public double getXCoord() {
        return xCoordinate;
    }

    public double getYCoord() {
        return yCoordinate;
    }

    public double getPreviousXCoord() {
        return 0.0;
    }

    public double getPreviousAngle() {
        return 0.0;
    }

    public double randomNumber(double low, double high) {
        //TO BE OVER WRITTEN 
        return 0;
    }

    public double spacesToMove() {
        return spacesToMove;
    }
    
    public double getAngle() {
        return angle;
    }

    public String getName() {
        return name;
    }

    public void incrementHealthMultiplier() {
        healthMultiplier++;
    }

    public double getHealthMultiplier() {
        return healthMultiplier;
    }

    public void incrementProjectileVelocityMultiplier() {
        projectileVelocityMultiplier++;
    }

    public double getProjectileVelocityMultiplier() {
        return projectileVelocityMultiplier;
    }

    public void incrementProjectileDamageMultiplier() {
        projectileDamageMultiplier ++;
    }

    public double getProjectileDamageMultiplier() {
        return projectileDamageMultiplier;
    }

    public void incrementDistanceMultiplier() {
        distanceMultiplier++;
    }

    public double getDistanceMultiplier() {
        return distanceMultiplier;
    }
}