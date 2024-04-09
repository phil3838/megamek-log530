package megamek.rating;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import megamek.server.playerrating.ExampleRatingSystem;
public class ExampleRatingSystemTest {
    @Test
    public void testGetNewRating_Win() {
        ExampleRatingSystem ratingSystem = new ExampleRatingSystem();
        double initialRating = 100;
        boolean isVictory = true;

        double newRating = ratingSystem.getNewRating(initialRating, isVictory);

        assertTrue(newRating > initialRating);
    }

    @Test
    public void testGetNewRating_Loss() {
        ExampleRatingSystem ratingSystem = new ExampleRatingSystem();
        double initialRating = 100;
        boolean isVictory = false;

        double newRating = ratingSystem.getNewRating(initialRating, isVictory);

        assertTrue(newRating < initialRating);
    }
}
