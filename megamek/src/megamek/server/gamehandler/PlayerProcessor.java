package megamek.server.gamehandler;

import megamek.common.Entity;
import megamek.common.IEntityRemovalConditions;
import megamek.common.Player;
import megamek.common.force.Forces;
import megamek.common.net.enums.PacketCommand;
import megamek.common.net.packets.Packet;
import megamek.server.ServerLobbyHelper;
import org.apache.logging.log4j.LogManager;

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerProcessor {

    public void processGameMasterRequest(GameManager gameManager) {
        if (gameManager.playerRequestingGameMaster != null) {
            gameManager.playerProcessor.setGameMaster(gameManager.playerRequestingGameMaster, true, gameManager);
            gameManager.playerRequestingGameMaster = null;
        }
    }

    public void setGameMaster(Player player, boolean gameMaster, GameManager gameManager) {
        player.setGameMaster(gameMaster);
        gameManager.transmitPlayerUpdate(player);
        gameManager.sendServerChat(player.getName() + " set GameMaster: " + player.getGameMaster());
    }

    void processTeamChangeRequest(GameManager gameManager) {
        if (gameManager.playerChangingTeam != null) {
            gameManager.playerChangingTeam.setTeam(gameManager.requestedTeam);
            gameManager.getGame().setupTeams();
            gameManager.transmitPlayerUpdate(gameManager.playerChangingTeam);
            String teamString = "Team " + gameManager.requestedTeam + "!";
            if (gameManager.requestedTeam == Player.TEAM_UNASSIGNED) {
                teamString = " unassigned!";
            } else if (gameManager.requestedTeam == Player.TEAM_NONE) {
                teamString = " lone wolf!";
            }
            gameManager.sendServerChat(gameManager.playerChangingTeam.getName() + " has changed teams to " + teamString);
            gameManager.playerChangingTeam = null;
        }
        gameManager.changePlayersTeam = false;
    }

    public void allowTeamChange(GameManager gameManager) {
        gameManager.changePlayersTeam = true;
    }

    protected void transferAllEntitiesOwnedBy(Player pFrom, Player pTo, GameManager gameManager) {
        for (Entity entity : gameManager.game.getEntitiesVector().stream().filter(e -> e.getOwner().equals(pFrom)).collect(Collectors.toList())) {
            entity.setOwner(pTo);
        }
        gameManager.game.getForces().correct();
        ServerLobbyHelper.correctLoading(gameManager.game);
        ServerLobbyHelper.correctC3Connections(gameManager.game);
        gameManager.send(gameManager.createFullEntitiesPacket());
    }

    void disconnectPlayer(Player player, GameManager gameManager) {
        if (gameManager.getGame().getPhase().isLounge()) {
            List<Player> gms = gameManager.game.getPlayersList().stream().filter(p -> p.isGameMaster()).collect(Collectors.toList());

            if (gms.size() > 0) {
                transferAllEntitiesOwnedBy(player, gms.get(0), gameManager);
            } else {
                gameManager.removeAllEntitiesOwnedBy(player);
            }
        }

        // if a player has active entities, he becomes a ghost
        // except the VICTORY_PHASE when the disconnected
        // player is most likely the Bot disconnected after receiving
        // the COMMAND_END_OF_GAME command
        // see the Bug 1225949.
        // Ghost players (Bots mostly) are now removed during the
        // resetGame(), so we don't need to do it here.
        // This fixes Bug 3399000 without reintroducing 1225949
        if (gameManager.getGame().getPhase().isVictory() || gameManager.getGame().getPhase().isLounge() || player.isObserver()) {
            gameManager.getGame().removePlayer(player.getId());
            gameManager.send(new Packet(PacketCommand.PLAYER_REMOVE, player.getId()));
            // Prevent situation where all players but the disconnected one
            // are done, and the disconnecting player causes the game to start
            if (gameManager.getGame().getPhase().isLounge()) {
                gameManager.resetActivePlayersDone();
            }
        } else {
            player.setGhost(true);
            player.setDone(true);
            gameManager.transmitPlayerUpdate(player);
        }

        if (gameManager.getGame().getPhase().hasTurns() && (null != gameManager.getGame().getTurn())) {
            if (gameManager.getGame().getTurn().isValid(player.getId(), gameManager.getGame())) {
                gameManager.sendGhostSkipMessage(player);
            }
        } else {
            gameManager.checkReady();
        }

        gameManager.sendServerChat(player.getName() + " disconnected.");
        LogManager.getLogger().info("s: removed player " + player.getName());

        if (0 == gameManager.getGame().getNoOfPlayers()) {
            gameManager.resetGame();
        }
    }

    void removeEntities(Player player, GameManager gameManager) {
        int pid = player.getId();
        Forces forces = gameManager.game.getForces();

        forces.getAllForces().stream()
                .filter(f -> !f.isTopLevel())
                .filter(f -> f.getOwnerId() != pid)
                .filter(f -> forces.getForce(f.getParentId()).getOwnerId() == pid)
                .forEach(forces::promoteForce);

        gameManager.game.getEntitiesVector().stream()
                .filter(e -> e.getOwnerId() != pid)
                .filter(Entity::partOfForce)
                .filter(e -> forces.getForce(e.getForceId()).getOwnerId() == pid)
                .forEach(forces::removeEntityFromForces);

        forces.deleteForces(forces.getAllForces().stream()
                .filter(f -> f.getOwnerId() == pid)
                .filter(f -> f.isTopLevel() || !forces.getOwner(f.getParentId()).equals(player))
                .collect(Collectors.toList()));

        Collection<Entity> delEntities = gameManager.game.getEntitiesVector().stream()
                .filter(e -> e.getOwner().equals(player))
                .collect(Collectors.toList());

        delEntities.forEach(forces::removeEntityFromForces);
        ServerLobbyHelper.lobbyUnload(gameManager.game, delEntities);
        ServerLobbyHelper.performC3Disconnect(gameManager.game, delEntities);

        delEntities.forEach(e -> gameManager.game.removeEntity(e.getId(), IEntityRemovalConditions.REMOVE_NEVER_JOINED));
        gameManager.send(gameManager.createFullEntitiesPacket());
    }

    void changeTeam(int team, Player player, GameManager gameManager) {
        gameManager.requestedTeam = team;
        gameManager.playerChangingTeam = player;
        gameManager.changePlayersTeam = false;
    }
}