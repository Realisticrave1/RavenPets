package RavenMC.ravenPets;

import java.util.Random;

public enum RavenElementType {
    FIRE,
    WATER,
    EARTH,
    AIR,
    LIGHTNING,
    ICE,
    NATURE,
    DARKNESS,
    LIGHT;

    private static final Random RANDOM = new Random();

    public static RavenElementType getRandomElement() {
        RavenElementType[] types = values();
        return types[RANDOM.nextInt(types.length)];
    }
}