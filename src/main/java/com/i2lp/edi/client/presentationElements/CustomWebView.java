package com.i2lp.edi.client.presentationElements;

import com.sun.webkit.WebPage;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;

import java.lang.reflect.Field;
import java.util.Set;

public final class CustomWebView extends Region {

    final WebView webview = new WebView();
    final WebEngine webEngine = webview.getEngine();
    public boolean isReady = false;

    public CustomWebView(String content) {
        webview.setPrefHeight(5);

        widthProperty().addListener((observable, oldValue, newValue) -> {
            Double width = (Double)newValue;
            System.out.println("WIDTH CHANGE!" + width);
            webview.setPrefWidth(width);
            adjustHeight();
        });

        webview.getEngine().getLoadWorker().stateProperty().addListener((arg0, oldState, newState) -> {
            //If done rendering, adjust height to fit content, set isReady variable for thumbnail generation
            if (newState == Worker.State.SUCCEEDED) {
                adjustHeight();
                isReady = true;
            }
            //Set Background of WebView to transparent (using reflection)
            try {
                // Use reflection to retrieve the WebEngine's private 'page' field.
                Field f = webEngine.getClass().getDeclaredField("page");
                f.setAccessible(true);
                WebPage page = (WebPage) f.get(webEngine);
                // Set the background color of the page to be transparent.
                //page.setBackgroundColor((new java.awt.Color(0, 0, 0, 0)).getRGB());
                page.setBackgroundColor((new java.awt.Color(255, 255, 255, 0)).getRGB());
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        });

        webview.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change -> {
            Set<Node> scrolls = webview.lookupAll(".scroll-bar");
            for (Node scroll : scrolls) {
                scroll.setVisible(false);
            }
        });

        setContent(content);
        getChildren().add(webview);
    }

    public void setContent(final String content) {
        Platform.runLater(() -> {
            webEngine.loadContent(getHtml(content));
            Platform.runLater(() -> adjustHeight());
        });
    }


    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(webview,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
    }

    private void adjustHeight() {
        Platform.runLater(() -> {
            try {
                Object result = webEngine.executeScript("document.getElementById('mydiv').offsetHeight");
                if (result instanceof Integer) {
                    Integer i = (Integer) result;
                    double height = new Double(i);
                    height = height + 20;
                    webview.setPrefHeight(height);
                    webview.getPrefHeight();
                }
            } catch (JSException e) {System.out.println("Error:" + e);}
        });
    }

    private String getHtml(String content) {
        return "<html><body>" +
                "<div id=\"mydiv\">" + content + "</div>" +
                "</body></html>";
    }

}