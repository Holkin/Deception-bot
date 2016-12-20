package deception;


import java.util.HashMap;

public class GlobalState {
    // board hash by turn number
    public static final HashMap<byte[][], Integer> previousPositions = new HashMap<>(64, 0.75f);
}
