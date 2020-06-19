import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.event.Event;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import utils.Prices;
import utils.QuickExchange;
import utils.Sleep;
import utils.events.EnableFixedModeEvent;
import utils.events.ToggleRoofsHiddenEvent;
import utils.events.ZoomControl;
import utils.wilderness.TransferItems;

import java.awt.*;

@ScriptManifest(author = "Newman", name = "Beer", info = "Attempt 1", version = 0.1, logo = "")
public final class BeerBuyer extends Script  {

    private long startTime;
    private String state = "Initializing";
    private final String muleName = "TODO".replace(' ', '\u00A0');
    private Font font = new Font("Arial", Font.BOLD, 14);
    private Area faladorPub = new Area(2954, 3366, 2961, 3373);



    @Override
    public void onStart() {
        log("This is where the code begins.");
        startTime = System.currentTimeMillis();

        if (!EnableFixedModeEvent.isFixedModeEnabled(this)) {
            execute(new EnableFixedModeEvent());
        }
        // Disabled roofs
        if (!getSettings().areRoofsEnabled()) {
            ToggleRoofsHiddenEvent toggleRoofsHiddenEvent = new ToggleRoofsHiddenEvent();
            execute(toggleRoofsHiddenEvent);
        }
        // Set the zoom
        if (!ZoomControl.isInRange(getCamera().getScaleZ(), 500)) {
            ZoomControl.setZoom(getBot(), 500);
        }


    }

    public final String formatTime(final long time) {
        long s = time / 1000, m = s / 60, h = m / 60;
        s %= 60; m %= 60; h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }





    private int buyfromGE (String itemName) {
        Double i = Double.valueOf(Prices.get(itemName, Prices.Property.SELL_AVERAGE).get());
        i = i * 1.5;
        int I = (int)Math.round(i);
        return I;
    }

    private QuickExchange getQuickExchange() {
       return new QuickExchange(this);
    }

    @Override
    public int onLoop() throws InterruptedException {
        if (getInventory().getAmount(995) == 0 && !getInventory().contains(1908) && !Banks.GRAND_EXCHANGE.contains(myPlayer())) {
            if (Banks.FALADOR_WEST.contains(myPlayer())) {
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
                    if ((bank.getAmount(995) >= 84) && (bank.getAmount(1907) < 200)) {
                        withdrawItem(995, 84);
                    } else if (bank.contains(1907)){
                        if (bank.getWithdrawMode().equals(Bank.BankMode.WITHDRAW_NOTE)) {
                            state = "Switching to noted mode";

                            if (bank.withdrawAll("Wizard's mind bomb")) {
                                Sleep.sleepUntil(() -> getInventory().contains("Wizard's mind bomb"), 5000);
                            }
                            if (bank.close()) {
                                Sleep.sleepUntil(() -> !getBank().isOpen(), 5000);
                            }
                            state = "Walking to GE...";
                            getWalking().webWalk(Banks.GRAND_EXCHANGE);

                        } else { // If the bank item is NOTE form then swap it to ITEM form
                            bank.enableMode(Bank.BankMode.WITHDRAW_NOTE);
                            sleep(random(300, 800));
                        }
                    } else {
                        //TODO handle less than 84GP
                    }
                    //close the bank

                }
                //If we aren't in the bank area then walk there
            } else {
                state = "Walking to FALADOR bank...";
                getWalking().webWalk(Banks.FALADOR_WEST);
            } //If we have under 200 beers in the bank and enough gold we walk to the pub to buy beer
        } else if (getInventory().contains(995) && !Banks.GRAND_EXCHANGE.contains(myPlayer())) {
            if (faladorPub.contains(myPlayer())) {
                state = "Buying beer";
                NPC closest = getNpcs().closest("Emily","Kaylee");
                //Buy beer
                if (!dialogues.inDialogue()) {
                    if (closest != null) {
                        if (closest.isVisible()) {
                            if (closest.interact("Talk-to")) {
                                Sleep.sleepUntil(() -> dialogues.inDialogue(), 5000);
                            }
                        } else {
                            getCamera().toEntity(closest);
                        }
                    }
                } else {
                    if (dialogues.isPendingOption()) {
                        dialogues.completeDialogue("I'll try the Mind Bomb");

                        Sleep.sleepUntil(() -> dialogues.isPendingContinuation(), 5000);
                    }
                    if (dialogues.isPendingContinuation()) {
                        dialogues.clickContinue();
                        Sleep.sleepUntil(() -> dialogues.isPendingContinuation(), 5000);
                    }
                }
            } else {
                state = "Walking to Pub..";
                getWalking().webWalk(faladorPub);
            }
            //If we have over 200 beers we take them out in noted form and walk to GE
        } else if (getInventory().contains(1908)){
            log("Test");
            state = "Selling beer..";
            QuickExchange quickExchange = getQuickExchange();
            if (Banks.GRAND_EXCHANGE.contains(myPlayer())){
                if (!getGrandExchange().isOpen()) { //Checks if ge is open
                    quickExchange.open();

                } else {
                    quickExchange.quickSell("Wizard's mind bomb", (int) getInventory().getAmount((1908))); //https://osbot.org/forum/topic/150022-quickexchange-yet-another-ge-api/?tab=comments#comment-1799860
                }
            }  else {
                state = "Walking to GE...";
                getWalking().webWalk(Banks.GRAND_EXCHANGE);
            }

        } else if (Banks.GRAND_EXCHANGE.contains(myPlayer()) && getInventory().isEmpty()) {
            if (!getBank().isOpen()) {
                getBank().open();
            } else {
                if (getBank().getAmount(995) > buyfromGE("Rune sword")) {
                    withdrawItem(995, buyfromGE("Rune sword"));
                } else {
                    withdrawItem(995, 84);
                    getWalking().webWalk(faladorPub);
                }
                if (bank.close()) {
                    Sleep.sleepUntil(() -> !getBank().isOpen(), 5000);
                }
            }
        } else if (Banks.GRAND_EXCHANGE.contains(myPlayer()) && getInventory().getAmount(995) > 84 && !getInventory().contains(1289)){
            QuickExchange quickExchange = getQuickExchange();
            if (!getGrandExchange().isOpen()) { //Checks if ge is open
                quickExchange.open();

            } else {
                quickExchange.quickBuy("Rune sword", 1, false);
                quickExchange.close();
            }

        } else if (Banks.GRAND_EXCHANGE.contains(myPlayer()) && getInventory().contains(995) && getInventory().contains(1289)) {
            if (!getBank().isOpen()) {
                getBank().open();
            } else {
                bank.depositAll(995);
            }
            if (bank.close()) {
                Sleep.sleepUntil(() -> !getBank().isOpen(), 5000);
            }
        } else {
            state = "Ready for muling";
            new TransferItems(muleName);
        }
        return random (200,300);
    }

    private void withdrawItem(int itemId, int amount) {
        if (bank.withdraw(itemId, amount)) {
            Sleep.sleepUntil(() -> getInventory().contains(itemId) && getInventory().getAmount(itemId) <= amount, 1_000);
        }
    }

    private void transferItem() {

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
