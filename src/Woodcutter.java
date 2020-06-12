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
public final class Woodcutter extends Script  {

    private long startTime;
    private Font font = new Font("Arial", Font.BOLD, 14);

    @Override
    public void onStart() {
        log("This is where the code begins.");
        startTime = System.currentTimeMillis();
        getExperienceTracker().start(Skill.WOODCUTTING);
    }

    public final String formatTime(final long time) {
        long s = time / 1000, m = s / 60, h = m / 60;
        s %= 60; m %= 60; h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private enum State {
        CUT, BANK, WAIT
    };

    private State getState() {
        if (inventory.isFull())
            return State.BANK;
        return State.CUT;
    }
    private Area getTreeArea() {
        if (getSkills().getDynamic(Skill.WOODCUTTING) >= 60) {
            return new Area(3203, 3506, 3225, 3497);
        } else {
            return new Area(3154, 3380, 3172, 3422);
        }
    }

    private String getTreeName() {
        if (getSkills().getDynamic(Skill.WOODCUTTING) >= 60){
            return "Yew";
        } else if (getSkills().getDynamic(Skill.WOODCUTTING) >= 15){
            return "Oak";
        } else {
            return "Tree";
        }
    }

    @Override
    public int onLoop() throws InterruptedException {
        switch (getState()) {
            case CUT:
                if (!getTreeArea().contains(myPlayer()) ) {
                    getWalking().webWalk(getTreeArea());
                } else {
                    RS2Object tree = getObjects().closest(getTreeName());
                    if (!myPlayer().isAnimating() && tree != null) {
                        if (tree.interact("Chop down")) {
                            new ConditionalSleep(5000) {
                                @Override
                                public boolean condition() throws InterruptedException {
                                    return myPlayer().isAnimating();
                                }
                            }.sleep();
                        }
                    }
                }

                break;
            case BANK:
                if (!Banks.VARROCK_WEST.contains(myPlayer())) {
                    getWalking().webWalk(Banks.VARROCK_WEST);
                } else {
                    if (!getBank().isOpen()) {
                        getBank().open();
                    }
                    if (bank.depositAll()) {
                        Sleep.sleepUntil(() -> getInventory().isEmpty(), 5000);
                    }
                    if (bank.close()) {
                        Sleep.sleepUntil(() -> !getBank().isOpen(), 5000);
                    }
                }
                break;
            case WAIT:
                sleep(random(500,700));
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
        g.drawString("XP Gained: "+ getExperienceTracker().getGainedXP(Skill.WOODCUTTING), 10, 250);
        g.drawString("XP / HR: "+ getExperienceTracker().getGainedXPPerHour(Skill.WOODCUTTING), 10, 270);
        g.drawString("Time to LVL: "+ formatTime(getExperienceTracker().getTimeToLevel(Skill.WOODCUTTING)), 10, 290);
        g.drawString("Time Ran: "+ formatTime(runTime), 10, 310);
    }

}