import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.util.Random;
@ScriptManifest(author = "Newman", name = "Woody", info = "Attempt 1", version = 0.1, logo = "")
public final class BeerBuyer extends Script  {

    private long startTime;
    private Font font = new Font("Arial", Font.BOLD, 14);

    @Override
    public void onStart() {
        log("This is where the code begins.");
        startTime = System.currentTimeMillis();
    }

    public final String formatTime(final long time) {
        long s = time / 1000, m = s / 60, h = m / 60;
        s %= 60; m %= 60; h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private void withdrawItem(int itemId, int amount) {
        if (bank.withdraw(itemId, amount)) {
            Sleep.sleepUntil(() -> getInventory().contains(itemId) && getInventory().getAmount(itemId) <= amount, 1_000);
        }
    }

    @Override
    public int onLoop() throws InterruptedException {
        if (!Banks.FALADOR_WEST.contains(myPlayer())) {
            getWalking().webWalk(Banks.FALADOR_WEST);
        } else {
            if (!getBank().isOpen()) {
                getBank().open();
            } else {
                if (bank.depositAll()) {
                    Sleep.sleepUntil(() -> getInventory().isEmpty(), 5000);
                }
                if (bank.getAmount(995) >= 28) {
                    withdrawItem(995, 28);
                }

                if (bank.close()) {
                    Sleep.sleepUntil(() -> !getBank().isOpen(), 5000);
                }
            }
        }

        return random (200,300);
    }

    @Override
    public void onPaint(Graphics2D g) {
        Point mP = getMouse().getPosition();
        long runTime = System.currentTimeMillis() - startTime;
        g.setColor(Color.white);
        g.setFont(font);
        g.drawLine(mP.x, 501, mP.x, 0);
        g.drawLine(0, mP.y, 764, mP.y);
        g.drawString("Time Ran: "+ formatTime(runTime), 10, 310);
    }

}