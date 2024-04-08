package megamek.server.playerrating;

public interface RatingSystem {
    double getNewRating(double playerRating, boolean isVictory);
}
