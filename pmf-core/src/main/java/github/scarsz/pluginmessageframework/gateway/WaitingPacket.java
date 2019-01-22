package github.scarsz.pluginmessageframework.gateway;

import github.scarsz.pluginmessageframework.packet.BasePacket;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class WaitingPacket<T extends BasePacket> {

    final Predicate<T> condition;
    final Consumer<T> action;

    public WaitingPacket(Predicate<T> condition, Consumer<T> action) {
        this.condition = condition;
        this.action = action;
    }

    public boolean attempt(T event) {
        if (condition.test(event)) {
            action.accept(event);
            return true;
        }

        return false;
    }

}