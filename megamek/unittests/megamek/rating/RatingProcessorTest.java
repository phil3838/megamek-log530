package megamek.rating;

import megamek.common.Player;
import megamek.server.playerrating.RatingProcessor;
import megamek.server.victory.VictoryResult;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class RatingProcessorTest {

    @Test
    public void testSetPlayerRating_Victory() {
        Player mockPlayer = mock(Player.class);
        VictoryResult mockVictoryResult = mock(VictoryResult.class);
        when(mockPlayer.getRating()).thenReturn(100.0);

        RatingProcessor ratingProcessor = new RatingProcessor();

        ratingProcessor.setPlayerRating(mockPlayer, mockVictoryResult);

        verify(mockPlayer, times(1)).setRating(anyDouble());
    }

    @Test
    public void testSetPlayerRating_Defeat() {
        Player mockPlayer = mock(Player.class);
        VictoryResult mockVictoryResult = mock(VictoryResult.class);
        when(mockPlayer.getRating()).thenReturn(100.0);

        RatingProcessor ratingProcessor = new RatingProcessor();

        ratingProcessor.setPlayerRating(mockPlayer, mockVictoryResult);

        verify(mockPlayer, times(1)).setRating(anyDouble());
    }
}
