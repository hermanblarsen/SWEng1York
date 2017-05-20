package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.presentationElements.DrawPane;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-20.
 */
public class EventTransparencyManager {

    private Logger logger = LoggerFactory.getLogger(DrawPane.class);
    private final ArrayList<Node> mouseEnteredArrayList;
    private boolean isActive = true;

    public EventTransparencyManager() {
        mouseEnteredArrayList = new ArrayList<>();
    }

    public void connectNodes(Node sourceNode, Parent targetNode) {
        connectNodes(sourceNode, targetNode, MouseEvent.ANY);
    }

    @SuppressWarnings("Duplicates")
    public void connectNodes(Node sourceNode, Parent targetNode, EventType<MouseEvent> eventType) {
        sourceNode.addEventFilter(eventType, event -> {
            if (isActive) {
                Node target = findEventTarget(event, targetNode);
                if (target != null) {
                    Event.fireEvent(target, event.copyFor(event.getSource(), target));

                    //Emulate MOUSE_ENTERED and MOUSE_EXITED events
                    if (event.getEventType().equals(MouseEvent.MOUSE_MOVED)) {
                        if (!mouseEnteredArrayList.contains(target)) {
                            addToMouseEnteredList(target, event);
                        }
                        removeChildrenFromMouseEnteredList(target, event);
                    }
                    event.consume();
                }
            }
        });
    }

    @SuppressWarnings("Duplicates")
    public void connectNodes(Node sourceNode, Parent parentOfTargetNode, int indexOfTargetInChildrenArrayOfParent) {
        sourceNode.addEventFilter(MouseEvent.ANY, event -> {
            if(isActive) {
                Node target = findEventTarget(event, (Parent) parentOfTargetNode.getChildrenUnmodifiable().get(indexOfTargetInChildrenArrayOfParent));
                if(target != null) {
                    Event.fireEvent(target, event.copyFor(event.getSource(), target));

                    //Emulate MOUSE_ENTERED and MOUSE_EXITED events
                    if(event.getEventType().equals(MouseEvent.MOUSE_MOVED)) {
                        if(!mouseEnteredArrayList.contains(target)) {
                            addToMouseEnteredList(target, event);
                        }
                        removeChildrenFromMouseEnteredList(target, event);
                    }
                    event.consume();
                }
            }
        });
    }

    private void addToMouseEnteredList(Node node, MouseEvent event) {
        if(!mouseEnteredArrayList.contains(node)) {
            mouseEnteredArrayList.add(node);
            MouseEvent emulatedEnteredEvent = event.copyFor(event.getSource(), node, MouseEvent.MOUSE_ENTERED);
            Event.fireEvent(node, emulatedEnteredEvent);
        }

        if(node.getParent() != node.getScene().getRoot())
            addToMouseEnteredList(node.getParent(), event);
    }

    private void removeChildrenFromMouseEnteredList(Node node, MouseEvent event) {
        Parent parent = null;

        try {
            parent = (Parent) node;
        } catch(ClassCastException e) {
            //The exception is thrown when the node is not a parent. Do nothing.
        }

        if(parent != null) {
            for(Node child : parent.getChildrenUnmodifiable()) {
                if(mouseEnteredArrayList.contains(child)) {
                    MouseEvent emulatedExitedEvent = event.copyFor(event.getSource(), child, MouseEvent.MOUSE_EXITED);
                    Event.fireEvent(child, emulatedExitedEvent);
                    mouseEnteredArrayList.remove(child);
                    removeChildrenFromMouseEnteredList(child, event);
                }
            }
        }
    }

    private Node findEventTarget(MouseEvent event, Parent parent) {
        logger.debug("Searching for event target in " + parent.toString());
        Node foundTarget = parent;
        if(parent.getChildrenUnmodifiable().size() != 0) {
            for(Node child : parent.getChildrenUnmodifiable()) {
                Bounds boundsInScene = child.localToScene(child.getBoundsInLocal());
                if(boundsInScene.contains(event.getSceneX(), event.getSceneY()) && child.isVisible() && !child.isMouseTransparent()) {
                    if(child instanceof Parent) {
                        foundTarget = findEventTarget(event, (Parent) child);
                    } else {
                        foundTarget = child;
                    }
                }
            }
        }
        return foundTarget;
    }

    public void setActive(boolean isActive) { this.isActive = isActive; }

}
