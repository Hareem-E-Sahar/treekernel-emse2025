package com.kenstevens.stratinit.world;

import java.util.Random;
import com.kenstevens.stratinit.model.SectorCoords;

public class ElectronCloud {

    private static final double EDGE_CHARGE = 0.2;

    private static final double PLAYER_CHARGE = 1.0;

    final int numPlayers;

    final int size;

    final SectorCoords[] playerCoords;

    public ElectronCloud(int size, int numPlayers) {
        this.size = size;
        this.numPlayers = numPlayers;
        playerCoords = new SectorCoords[numPlayers];
    }

    public ElectronCloud(int size, int numPlayers, SectorCoords[] playerCoords) {
        this.size = size;
        this.numPlayers = numPlayers;
        this.playerCoords = playerCoords;
    }

    public void init() {
        Random random = new Random();
        for (int i = 0; i < numPlayers; ++i) {
            int x;
            int y;
            do {
                x = 1 + random.nextInt(size - 1);
                y = 1 + random.nextInt(size - 1);
            } while (alreadyPicked(i, x, y));
            playerCoords[i] = new SectorCoords(x, y);
        }
    }

    private boolean alreadyPicked(int i, int x, int y) {
        for (int j = 0; j < i; ++j) {
            if (playerCoords[j].x == x && playerCoords[j].y == y) {
                return true;
            }
        }
        return false;
    }

    public ElectronCloud drift() {
        ElectronCloud cloud = this;
        ElectronCloud betterCloud = cloud.nudgeToLowerEnergy();
        while (betterCloud != null) {
            cloud = betterCloud;
            betterCloud = cloud.nudgeToLowerEnergy();
        }
        return cloud;
    }

    public double getEnergy(int i, SectorCoords coords) {
        double energy = 0.0;
        for (int j = 0; j < numPlayers; ++j) {
            if (i == j) {
                continue;
            }
            int distanceSquared = playerCoords[j].euclidianDistanceSquared(coords);
            energy += PLAYER_CHARGE / distanceSquared;
        }
        int neighbourToEdge = coords.euclidianDistanceSquared(size);
        energy += EDGE_CHARGE / neighbourToEdge;
        return energy;
    }

    public ElectronCloud nudgeToLowerEnergy() {
        for (int i = 0; i < numPlayers; ++i) {
            SectorCoords sectorCoords = playerCoords[i];
            double startEnergy = getEnergy(i, sectorCoords);
            for (SectorCoords neighbour : sectorCoords.getNeighbours(size, 1)) {
                if (alreadyExists(i, neighbour)) {
                    continue;
                }
                double newEnergy = getEnergy(i, neighbour);
                if (newEnergy < startEnergy) {
                    ElectronCloud nudged = this.clone();
                    nudged.setPlayerCoord(i, neighbour);
                    return nudged;
                }
            }
        }
        return null;
    }

    private boolean alreadyExists(int i, SectorCoords neighbour) {
        for (int j = 0; j < numPlayers; ++j) {
            if (i == j) {
                continue;
            }
            if (neighbour.equals(playerCoords[j])) {
                return true;
            }
        }
        return false;
    }

    private void setPlayerCoord(int i, SectorCoords neighbour) {
        playerCoords[i] = neighbour;
    }

    @Override
    public ElectronCloud clone() {
        SectorCoords[] coords = new SectorCoords[numPlayers];
        for (int i = 0; i < numPlayers; ++i) {
            coords[i] = playerCoords[i].clone();
        }
        return new ElectronCloud(size, numPlayers, coords);
    }

    public void print() {
        for (SectorCoords coords : playerCoords) {
            System.out.println(coords);
        }
    }

    SectorCoords getPlayerCoord(int i) {
        return playerCoords[i];
    }
}
