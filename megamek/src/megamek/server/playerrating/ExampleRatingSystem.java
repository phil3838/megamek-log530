package megamek.server.playerrating;
import java.util.Random;

public class ExampleRatingSystem implements RatingSystem {

    private final Random random = new Random();

    //Ce systeme de rating peut etre remplacer par nimporte quelle logique de calcul de rating (Systeme ELO, etc..)
    //Pour le but de ce lab nous ajoutons (ou enlevons) un nombre aleatoire en 10 et 20
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
