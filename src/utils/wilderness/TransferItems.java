package utils.wilderness;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.event.Event;
import utils.Sleep;

import java.util.ArrayList;

public class TransferItems extends Event {

    private Area tradeArea = new Area(3135, 3516, 3137, 3519);
    private Area wildernessArea = new Area(3134, 3537, 3136, 3539);
    private String muleAccount;
//    private Player targetPlayer = players.closest((Filter<Player>) player -> !player.getName().equals(myPlayer().getName())); //can change this to a friends name also


    public TransferItems(String targetPlayer) {
        this.muleAccount = targetPlayer.replace(' ', '\u00A0');
    }

    protected String getTargetPlayer() {
        return muleAccount;
    }

    @Override
    public int execute() throws InterruptedException {
        Player muleAccount = getPlayers().closest(o -> o.getName().replaceAll("\\u00a0", " ").equalsIgnoreCase("ADDACCOUNT"));

        if (tradeArea.contains(myPlayer()) && muleAccount != null && muleAccount.isVisible()) {
            getWalking().webWalk(wildernessArea);
        } else {
            Sleep.sleepUntil(() -> (muleAccount != null), 5000);
        } if (wildernessArea.contains(myPlayer())&& (muleAccount != null) && muleAccount.isVisible()) {
            muleAccount.interact("Attack");
        }
        return 0;
    }
}
