import java.util.Random;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Manager {
    private static int minDistance = 2;
    private static int maxDistance = 15;
    private Tank playerTank, opponentTank;
    private Terrain terrain;
    static private int totalTurns = 50;
    private int turnNumber;
    private int difficulty;
    private double healthMultiplier, projectileVelocityMultiplier, projectileDamageMultiplier, distanceMultiplier;

    public Manager() {
        generateTerrainEquationNumbers();

        turnNumber = 0;
    }

    public void calculateProjectileDamage(Projectile projectile) {
        double distanceBetweenForPlayer = Math.abs(projectile.getX() - playerTank.getXCoord());
        if (distanceBetweenForPlayer > minDistance && distanceBetweenForPlayer <= maxDistance) {
            playerTank.subtractHealth(false, opponentTank.getProjectileDamageMultiplier());
        } else if (distanceBetweenForPlayer <= minDistance) {
            playerTank.subtractHealth(true, opponentTank.getProjectileDamageMultiplier());
        }

        //figure out if the projectile hit opponent
        double distanceBetweenForOpponent = Math.abs(projectile.getX() - opponentTank.getXCoord());
        if (distanceBetweenForOpponent > minDistance && distanceBetweenForOpponent <= maxDistance) {
            opponentTank.subtractHealth(false, playerTank.getProjectileDamageMultiplier());
        } else if (distanceBetweenForOpponent <= minDistance) {
            opponentTank.subtractHealth(true, playerTank.getProjectileDamageMultiplier());
        }
    }

    public boolean isGameOver() {
        boolean gameOver = false;

        if (turnNumber >= totalTurns || playerTank.getHealth() <= 0 || opponentTank.getHealth() <= 0) {
            gameOver = true;
        }

        return gameOver;
    }

    public double calculateMoney(double damageTaken) {
        double distancePortion = ((double)(totalTurns - turnNumber)) / totalTurns;
        double healthPortion = playerTank.getHealth() / (playerTank.getStartingHealth() + playerTank.getHealthMultiplier());
        double avg = (distancePortion + healthPortion) / 2;
        return 500 * avg;
    }

    public Terrain generateTerrainEquationNumbers() {
        //example equation generated: 50sin(0.01x+5)+50 with 50, 0.01, and 5 being the random numbers in the array
        double[] terrainEquationNumbers = {randomNumber(100, 250), randomNumber(0.0025, 0.007), randomNumber(0, 5)};

        Terrain newTerrain = new Terrain(terrainEquationNumbers, 1000, 600);

        terrain = newTerrain;

        return terrain;
    }

    public String[] findUser(String username) {
        String[] userInformation = {};

        try {
            FileReader fr = new FileReader("auth.txt");
            BufferedReader br = new BufferedReader(fr);
            String line;

            while ((line = br.readLine()) != null) {
                String[] lineColumns = line.split(" ");

                try {
                    if (lineColumns[0].equals(username)) {
                        userInformation = lineColumns;
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            br.close();
        } catch (Exception err) {
            err.printStackTrace();
        }

        return userInformation;
    }

    public void createUser(String username) {
        try {
            FileWriter fw = new FileWriter("auth.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);

            //format: username money healthMultiplier projectileVelocityMultiplier projectileDamageMultiplier distanceMultiplier
            bw.write(username + " 0 0 0 0 0");
            bw.newLine();

            bw.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void generateTank(String[] tankInformation) {
        this.healthMultiplier = Double.parseDouble(tankInformation[2]);
        this.projectileVelocityMultiplier = Double.parseDouble(tankInformation[3]);
        this.projectileDamageMultiplier = Double.parseDouble(tankInformation[4]);
        this.distanceMultiplier = Double.parseDouble(tankInformation[5]);

        playerTank = new Tank(tankInformation[0], terrain, 10, healthMultiplier, projectileVelocityMultiplier, projectileDamageMultiplier, distanceMultiplier, 45);

        playerTank.setMoney(Double.parseDouble(tankInformation[1]));
    }

    public void generateOpponent() {
        if (difficulty == 0) {
            opponentTank = new EasyDifficultyOpponent(terrain, 135, terrain.getXMax() - 10);
        } else {
            opponentTank = new HardDifficultyOpponent(terrain, 135, terrain.getXMax() - 10, (int) randomNumber(5, 10), (int) randomNumber(5, 10), (int) randomNumber(5, 10), (int) randomNumber(5, 10));
        }
    }

    public void upgradeMultiplier(int code, Tank tank) {
        double multiplierValue = 0;
        double incrementValue = 1;

        switch(code) {
            case 2:
                multiplierValue = tank.getHealthMultiplier();
                break;
            case 3:
                multiplierValue = tank.getProjectileVelocityMultiplier();
                break;
            case 4:
                multiplierValue = tank.getProjectileDamageMultiplier();
                break;
            case 5:
                multiplierValue = tank.getDistanceMultiplier();
                break;
        }

        //edit money
        tank.editFile(1, -getUpgradeCost(multiplierValue));
        tank.setMoney(tank.getMoney() - getUpgradeCost(multiplierValue));

        //edit multiplier
        tank.editFile(code, incrementValue);
        
        switch(code) {
            case 2:
                tank.incrementHealthMultiplier();
                break;
            case 3:
                tank.incrementProjectileVelocityMultiplier();
                break;
            case 4:
                tank.incrementProjectileDamageMultiplier();
                break;
            case 5:
                tank.incrementDistanceMultiplier();
                break;
        }
    }

    public double getUpgradeCost(double multipliers) {
        return 100 * (multipliers + 1);
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public double randomNumber(double low, double high) {
        return new Random().nextDouble() * (high - low) + low;
    }

    public void nextTurn() {
        turnNumber++;
    }

    public Tank getPlayer() {
        return playerTank;
    }

    public Tank getOpponent() {
        return opponentTank;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public int getTurnsRemaining() {
        return totalTurns - turnNumber;
    }

    public Terrain getTerrain() {
        return terrain;
    }
}