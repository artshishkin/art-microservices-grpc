package net.shyshkin.study.grpc.snakesladders.engine;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.snakesladders.models.Die;
import net.shyshkin.study.grpc.snakesladders.models.GameState;
import net.shyshkin.study.grpc.snakesladders.models.Player;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class GameEngineImpl implements GameEngine {

    public static final int FINAL_POSITION = 100;
    public static final int DIE_MIN = 1;
    public static final int DIE_MAX = 6;
    private Player client = Player.newBuilder().setName("player").setPosition(0).build();
    private Player server = Player.newBuilder().setName("computer").setPosition(0).build();

    @Override
    public GameState oneRound(Die die) {

        client = getNewPlayerPosition(client, die.getValue());
        if (client.getPosition() != FINAL_POSITION) {
            int serverDieValue = ThreadLocalRandom.current().nextInt(DIE_MIN, DIE_MAX + 1);
            server = getNewPlayerPosition(server, serverDieValue);
        }
        log.debug("Game round: client: {}; server: {}", client.getPosition(), server.getPosition());
        return getGameState();
    }

    private GameState getGameState() {
        return GameState.newBuilder()
                .addPlayer(server)
                .addPlayer(client)
                .build();
    }

    private Player getNewPlayerPosition(Player player, int dieValue) {
        int newPosition = player.getPosition() + dieValue;
        if (newPosition <= FINAL_POSITION)
            player = player.toBuilder().setPosition(newPosition).build();
        return player;
    }
}
