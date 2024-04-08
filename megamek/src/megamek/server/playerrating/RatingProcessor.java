package megamek.server.playerrating;

import megamek.common.Player;
import megamek.server.victory.VictoryResult;

class RatingProcessor {
    private final RatingSystem ratingSystem = new ExampleRatingSystem();

    public void setPlayerRating(Player player, VictoryResult victoryResult) {
        double playerNewRating = ratingSystem.getNewRating(player.getRating(), VictoryResult.victory());

        player.setRating(playerNewRating);
    }
}