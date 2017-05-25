package com.i2lp.edi.client.utilities;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


/**
 * Created by Kacper on 2017-05-22.
 */
public class ResizeHandler implements EventHandler<MouseEvent> {

    private Logger logger = LoggerFactory.getLogger(ResizeHandler.class);
    private final Region region;
    private static final double DEFAULT_MARGIN = 5;
    private double marginFromEdge;
    private ArrayList<ResizeDirection> resizeDirections;
    private boolean isActive;
    private double prevX = 0;
    private double prevY = 0;
    private boolean right, left, up, down;
    private boolean isMouseDown = false;
    private boolean isCursorResize = false;
    private Cursor previousCursor;

    public ResizeHandler(Region region, ArrayList<ResizeDirection> resizeDirections, double marginFromEdge) {
        this.region = region;
        this.marginFromEdge = marginFromEdge;
        this.resizeDirections = resizeDirections;
        this.isActive = true;
        right = left = up = down = false;
    }

    public ResizeHandler(Region region, ArrayList<ResizeDirection> resizeDirections) {
        this(region, resizeDirections, DEFAULT_MARGIN);
    }

    public ResizeHandler(Region region) {
        this(region, new ArrayList<>());

        resizeDirections.add(ResizeDirection.DOWN);
        resizeDirections.add(ResizeDirection.UP);
        resizeDirections.add(ResizeDirection.LEFT);
        resizeDirections.add(ResizeDirection.RIGHT);

    }

    @Override
    public void handle(MouseEvent event) {
        if(isActive) {
            if (region.contains(event.getX(), event.getY()) && !isMouseDown) {

                right = (event.getX() > region.getBoundsInLocal().getWidth() - marginFromEdge) && resizeDirections.contains(ResizeDirection.RIGHT);
                left = (event.getX() < marginFromEdge) && resizeDirections.contains(ResizeDirection.LEFT);
                up = (event.getY() < marginFromEdge) && resizeDirections.contains(ResizeDirection.UP);
                down = (event.getY() > region.getBoundsInLocal().getHeight() - marginFromEdge) && resizeDirections.contains(ResizeDirection.DOWN);

                if (!isCursorResize)
                    previousCursor = region.getScene().getCursor();

                if (event.getEventType().equals(MouseEvent.MOUSE_MOVED)) {
                    if (right && !left && !up && !down) {
                        region.getScene().setCursor(Cursor.H_RESIZE);
                        logger.debug("Resize right");
                        isCursorResize = true;
                    } else if (!right && left && !up && !down) {
                        region.getScene().setCursor(Cursor.H_RESIZE);
                        logger.debug("Resize left");
                        isCursorResize = true;
                    } else if (!right && !left && up && !down) {
                        region.getScene().setCursor(Cursor.V_RESIZE);
                        logger.debug("Resize up");
                        isCursorResize = true;
                    } else if (!right && !left && !up && down) {
                        region.getScene().setCursor(Cursor.V_RESIZE);
                        logger.debug("Resize down");
                        isCursorResize = true;
                    } else if (right && !left && up && !down) {
                        region.getScene().setCursor(Cursor.NE_RESIZE);
                        logger.debug("Resize NE");
                        isCursorResize = true;
                    } else if (right && !left && !up && down) {
                        region.getScene().setCursor(Cursor.SE_RESIZE);
                        logger.debug("Resize SE");
                        isCursorResize = true;
                    } else if (!right && left && !up && down) {
                        region.getScene().setCursor(Cursor.SW_RESIZE);
                        logger.debug("Resize SW");
                        isCursorResize = true;
                    } else if (!right && left && up && !down) {
                        region.getScene().setCursor(Cursor.NW_RESIZE);
                        logger.debug("Resize NW");
                        isCursorResize = true;
                    } else {
                        isCursorResize = false;
                        region.getScene().setCursor(previousCursor);
                    }
                }

                if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
                    prevX = event.getX();
                    prevY = event.getY();
                    isMouseDown = true;
                }
            } else if (isCursorResize && !isMouseDown) {
                region.getScene().setCursor(previousCursor);
            }

            //Will be implemented if we need any other directions
            if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
                double dx = event.getX() - prevX;
                double dy = prevY - event.getY();

                if (right && !left && !up && !down) {

                } else if (!right && left && !up && !down) {

                } else if (!right && !left && up && !down) {
                    region.setPrefHeight(region.getHeight() + dy);
                } else if (!right && !left && !up && down) {

                } else if (right && !left && up && !down) {

                } else if (right && !left && !up && down) {

                } else if (!right && left && !up && down) {

                } else if (!right && left && up && !down) {

                }
            }

            if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
                isMouseDown = false;
                region.getScene().setCursor(previousCursor);
            }
        }
    }

    public void setDirections(ArrayList<ResizeDirection> resizeDirections) { this.resizeDirections = resizeDirections; }

    public void addDirection(ResizeDirection direction) { resizeDirections.add(direction); }

    public void removeDirection(ResizeDirection direction) { resizeDirections.remove(direction); }

    public void setActive(boolean active) { this.isActive = active; }
}
