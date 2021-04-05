package net.shyshkin.study.grpc.snakesladders.engine;

import net.shyshkin.study.grpc.snakesladders.models.Die;
import net.shyshkin.study.grpc.snakesladders.models.GameState;

public interface GameEngine {
    GameState oneRound(Die die);
}
