package megamek.server.playerrating;
import java.util.Random;

public class ExampleRatingSystem implements RatingSystem {

    private final Random random = new Random();

    @Override
    public double getNewRating(double playerRating, boolean isVictory) {
        int randomValue = random.nextInt(11) + 10;

        if (isVictory) {
            return playerRating + randomValue;
        } else {
            return playerRating - randomValue;
        }
    }
}
