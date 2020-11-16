public class Terrain {
    private double[] terrainEquationNumbers;
    private int xMax, yMax;

    public Terrain(double[] terrainEquationNumbers, int xMax, int yMax) {
        this.terrainEquationNumbers = terrainEquationNumbers;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    public double getTerrainYCoordinate(double xCoordinate) {
        //get y coordinate of terrain at specified x coordinate
        return (terrainEquationNumbers[0] * Math.sin(terrainEquationNumbers[1] * xCoordinate + terrainEquationNumbers[2])) + terrainEquationNumbers[0];
    }

    public int getXMax() {
        return xMax;
    }

    public int getYMax() {
        return yMax;
    }
}