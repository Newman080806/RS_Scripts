package utils.events;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Settings;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.Event;
import org.osbot.rs07.input.mouse.PointDestination;
import org.osbot.rs07.input.mouse.WidgetDestination;
import org.osbot.rs07.utility.Condition;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Supplier;

public final class ZoomControl extends Event {

    /**
     * Determines if the camera Z scale is within a requested Zoom
     *
     * @param currentZoom the current Z scale of the camera
     * @param requestedZoom the requested Z scale of the camera
     * @return true if we are within the range, otherwise false
     */
    public static boolean isInRange(int currentZoom, int requestedZoom) {
        int currentPos = (int) getPosition(currentZoom);
        int requestedPos = (int) getPosition(requestedZoom);
        return currentPos > requestedPos - 2 &&
                currentPos < requestedPos + 2;
    }

    /**
     * Public method to execute event
     * Min (Zoomed out): 181
     * Max (Zoomed in): 1448
     *
     * @param bot the bot instance
     * @param requestedZoom the zoom value to set
     * @return true if the event finished successfully
     */
    public static boolean setZoom(Bot bot, int requestedZoom) {
        Event event = new ZoomControl(requestedZoom);
        bot.getEventExecutor().execute(event);
        return event.hasFinished();
    }

    private final int zoom;

    private int eventLoopCounter = 0;

    private ZoomControl(int zoom) {
        this.zoom = zoom;
    }

    /**
     * Determine if the display tab is open in the settings tab
     *
     * @return true if the Display tab is open in the Settings tab
     */
    private boolean isDisplayOpen() {
        RS2Widget displayTab = getWidgets().singleFilter(w -> w != null &&
                w.getInteractActions() != null &&
                Arrays.asList(w.getInteractActions()).contains("Display"));
        return displayTab != null && displayTab.getSpriteIndex1() == 762;
    }

    /**
     * Generates the would-be X position of the slider if the Z scale was the given value
     *
     * @param zoom the camera Z scale
     * @return the X position of the slider
     */
    private static double getPosition(int zoom) {
        double a = 415.7837;
        double power = 0.07114964;

        return a * Math.pow(zoom, power);
    }

    /**
     * Creates a new Condition, representing a Mouse Drag from the current position
     * of the slider to the requested position to achieve the correct zoom
     *
     * @param slider the slider widget object
     * @return the Condition to pass to a continualClick method
     */
    private Condition moveMouse(RS2Widget slider) {
        return new Condition() {

            // Generate the X position for the slider of the requested zoom
            double widgetDestX = getPosition(zoom);

            // Supplies the mouse's end X position for moving the slider
            Supplier<Integer> mouseDestX = () -> (int) widgetDestX +
                    (int) (getMouse().getPosition().getX() - slider.getBounds().getX());

            // Supplies the mouse's end Y position for moving the slider
            Supplier<Integer> mouseDestY = () -> (int) slider.getPosition().getY() + (int) (slider.getHeight() / 2.00);

            // Fail-safe loop counter
            int counter = 0;

            @Override
            public boolean evaluate() {
                if (counter++ > 10) {
                    log("Too many loops");
                    return true;
                }
                // Get the current slider's bounds
                Rectangle currentLoc = slider.getBounds();
                if (!currentLoc.contains(getMouse().getPosition())) {
                    // Exit loop if mouse is not on top of slider
                    return true;
                } else if (currentLoc.getX() > widgetDestX - 2 && currentLoc.getX() < widgetDestX + 2) {
                    // Exit loop if the slider is in the correct position for the requested zoom
                    return true;
                } else {
                    // Drag the mouse to the correct X position
                    getMouse().move(new PointDestination(getBot(), mouseDestX.get(), mouseDestY.get()));
                }
                return false;
            }
        };
    }

    /**
     * Execution method
     *
     * @return the sleep time in between loops
     */
    @Override
    public int execute() throws InterruptedException {
        if (eventLoopCounter++ > 10) {
            log("Too many loops");
            setFailed();
        } else if (isInRange(getCamera().getScaleZ(), zoom)) {
            setFinished();
            return 0;
        } else if (!getTabs().isOpen(Tab.SETTINGS)) {
            getSettings().open();
        } else if (!isDisplayOpen()) {
            getSettings().open(Settings.SettingsTab.DISPLAY);
        } else {
            RS2Widget slider = getWidgets().getWidgetContainingSprite(261, 1201);
            if (slider != null) {
                getMouse().continualClick(new WidgetDestination(getBot(), slider, 3), moveMouse(slider));
            }
        }
        return 600;
    }

}