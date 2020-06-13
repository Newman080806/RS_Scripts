import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Dialogues;
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
@ScriptManifest(author = "Newman", name = "Beer", info = "Attempt 1", version = 0.1, logo = "")
public final class BeerBuyer extends Script  {

    private long startTime;
    private String state = "Initializing";
    private Font font = new Font("Arial", Font.BOLD, 14);
    private Area faladorPub = new Area(2954, 3366, 2960, 3374);
    private final String itemName = "Wizard mind bomb";
    private final int itemPrice = 200;

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
        GrandExchange GE = new GrandExchange(this);
        //If we aren't in the bank area then walk there
        if (!Banks.FALADOR_WEST.contains(myPlayer()) && getInventory().getAmount(995) == 0) {
            state = "Walking to bank...";
            getWalking().webWalk(Banks.FALADOR_WEST);
            //If we are in the bank and have no coins open the banks
        } else if (Banks.FALADOR_WEST.contains(myPlayer()) && getInventory().getAmount(995) == 0) {
            state = "Banking...";
            if (!getBank().isOpen()) {
                getBank().open();
            } else {
                //If our inventory isn't empty, deposit everything
                if (!getInventory().isEmpty()) {
                    if (bank.depositAll()) {
                        Sleep.sleepUntil(() -> getInventory().isEmpty(), 5000);
                    }
                }
                //If we have enough coins for a full inventory of beer take out that amount of coins
                if (bank.getAmount(995) >= 84) {
                    withdrawItem(995, 84);
                }
                //close the bank
                if (bank.close()) {
                    Sleep.sleepUntil(() -> !getBank().isOpen(), 5000);
                }
            }

        }
        //If the player has coins and is not in the pub, walk to the pub
        if (!faladorPub.contains(myPlayer()) && getInventory().contains(995)) {
            state = "Walking to Pub..";
            getWalking().webWalk(faladorPub);
            //If we are in the pub buy beers
        } else if (faladorPub.contains(myPlayer())) {
                state = "Buying beer";
                NPC Emily = getNpcs().closest("Emily");

                if (!dialogues.inDialogue()) {
                    if (Emily != null) {
                        if (Emily.isVisible()) {
                            if (Emily.interact("Talk-to")) {
                                new ConditionalSleep(random(1000, 2000)) {
                                    public boolean condition() throws InterruptedException {
                                        return dialogues.inDialogue();
                                    }
                                }.sleep();
                            }
                        } else {
                            getCamera().toEntity(Emily);
                        }
                    }
                } else {
                    if (dialogues.isPendingOption()) {
                        dialogues.completeDialogue("I'll try the Mind Bomb");
                        sleep(random(700, 900));
                    }
                    if (dialogues.isPendingContinuation()) {
                        dialogues.clickContinue();
                        sleep(random(700, 900));
                    }
                }
            }

        // sends player to GE to begin selling the beer
        if (!Banks.GRAND_EXCHANGE.contains(myPlayer())) {
            state = "Walking to GE...";
            getWalking().webWalk(Banks.GRAND_EXCHANGE);
        } else if (Banks.GRAND_EXCHANGE.contains(myPlayer())) {
            state = "Banking...";
            if (!getBank().isOpen()) {
                getBank().open();
            } else {
                //If our inventory isn't empty, deposit everything
                if (!getInventory().isEmpty()) {
                    if (bank.depositAll()) {
                        Sleep.sleepUntil(() -> getInventory().isEmpty(), 5000);
                    }
                }
                //If we have enough coins for a full inventory of beer take out that amount of coins
                if (bank.enableMode(Bank.BankMode.WITHDRAW_NOTE)) {
                    state = "Switching to noted mode";
                    bank.withdrawAll("Wizard mind bomb");
                }
                //close the bank
                if (bank.close()) {
                    Sleep.sleepUntil(() -> !getBank().isOpen(), 5000);
                }
            }

        } else if (!getGrandExchange().isOpen()) { //Checks if ge is open
            GE.openGE(); //open ge randomly using booth or npc
        } else {
            GE.collectItems(false); //collect items (boolean true -> to bank, false -> inventory)
            GE.createSellOffer(itemName, itemPrice, 0); //Sells all of the specified items in inventory at specified price ( 0 = all, int = specified amount)
        }

        return random (200,300);
    }

    @Override
    public void onPaint(Graphics2D g) {
        Point mP = getMouse().getPosition();
        long runTime = System.currentTimeMillis() - startTime;
        g.setColor(Color.white);
        g.setFont(font);
        g.drawString("State: " + state, 10, 210);
        g.drawLine(mP.x, 501, mP.x, 0);
        g.drawLine(0, mP.y, 764, mP.y);
        g.drawString("Time Ran: "+ formatTime(runTime), 10, 310);
    }

}


//                if (Emily.exists()) {
//                    if (Emily.isVisible() && !dialogues.inDialogue()) {
//                        Emily.interact("Talk-to");
//                        return  dialogues.inDialogue();
//                    } else if (dialogues.isPendingContinuation()) {
//                            dialogues.clickContinue();
//                        } else if (getDialogues().isPendingOption()) {
//                             getDialogues().selectOption("I'll try the Mind Bomb");
//                         }
//                }