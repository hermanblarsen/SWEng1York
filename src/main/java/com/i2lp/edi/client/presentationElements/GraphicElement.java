package com.i2lp.edi.client.presentationElements;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;


/**
 * Created by habl on 26/02/2017.
 * Modified by Zain 11/03/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class GraphicElement extends SlideElement {
    protected boolean aspectRatioLock;
    protected float elementAspectRatio;

    //Shape Properties:
    protected String lineColour;
    protected String fillColour;
    private static final double LINE_THICKNESS = 3;

    protected Shape graphicShape;

    //Polygon Properties
    protected boolean isPolygon;
    protected boolean isClosed;
    protected float[] polygonXPoints = {};
    protected float[] polygonYPoints = {};

    //Oval properties
    protected float[] ovalPos = {0,0};//xPos, Ypos
    protected float[] ovalRadii = {0,0};//rVertical, rHorizontal
    protected float rotation;

    protected boolean firstInstance = true; //Is this teh first time we're rendering this elememt?


    private Pane wrapperPane;//Wrap the graphics within its own pane so that absolute positioning works properly.

    public GraphicElement() {

    }

    @Override
    public void doClassSpecificRender() {
        if(firstInstance) {
            if (isPolygon) {
                denormalisePolygonPoints((float) getSlideWidth(), (float) getSlideHeight());
                graphicShape = setupPolygon();
            } else {
                ovalPos[0] *= getSlideWidth();
                ovalPos[1] *= getSlideHeight();
                ovalRadii[1] *= getSlideWidth();
                ovalRadii[0] *= getSlideHeight();


                graphicShape = setupOval();
                graphicShape.setRotate(rotation);
            }

            graphicShape.setFill(parseRGBAString(fillColour));
            graphicShape.setStroke(parseRGBAString(lineColour));
            graphicShape.setStrokeWidth(LINE_THICKNESS);

            wrapperPane.getChildren().add(graphicShape);
            getCoreNode().setPickOnBounds(false);
            firstInstance = false;
        } else {
            //Nothing new to do if this isn't the first render of the element .
        }
    }

    @Override
    public Node getCoreNode() {
        return wrapperPane;
    }

    @Override
    public void setupElement() {
        wrapperPane = new Pane();


    }

    private Shape setupPolygon(){
        if (isClosed){
            return new javafx.scene.shape.Polygon(generateSinglePointsArray());
        } else {
            return new Polyline(generateSinglePointsArray());
        }
    }

    private Shape setupOval(){
        return new Ellipse(ovalPos[0], ovalPos[1], ovalRadii[1], ovalRadii[0]);
    }

    private void denormalisePolygonPoints(float width, float height){
        for (int i=0; i<polygonYPoints.length; i++){
            polygonYPoints[i] *= height;
            polygonXPoints[i] *= width;
        }
    }

    private double[] generateSinglePointsArray(){
        if(polygonXPoints.length != polygonYPoints.length){
            throw new IllegalStateException("Number of x positions does not match the number of y positions.");
        }
        double[] pointsArray = new double[polygonXPoints.length + polygonYPoints.length];
        for(int i=0; i< polygonXPoints.length; i++){
            pointsArray[i*2]= polygonXPoints[i];
            pointsArray[i*2+1] = polygonYPoints[i];
        }
        return pointsArray;
    }

    @Override
    public void destroyElement() {

    }

    public boolean isAspectRatioLock() {
        return aspectRatioLock;
    }

    public GraphicElement setAspectRatioLock(boolean aspectRatioLock) {
        this.aspectRatioLock = aspectRatioLock;
        return this;
    }

    public float getElementAspectRatio() {
        return elementAspectRatio;
    }

    public void setElementAspectRatio(float elementAspectRatio) {
        this.elementAspectRatio = elementAspectRatio;
    }

    public String getLineColour() {
        return lineColour;
    }

    public GraphicElement setLineColour(String lineColour) {
        this.lineColour = lineColour;
        return this;
    }

    public String getFillColour() {
        return fillColour;
    }

    public GraphicElement setFillColour(String fillColour) {
        this.fillColour = fillColour;
        return this;
    }

    public Shape getGraphicShape() {
        return graphicShape;
    }

    public static Color parseRGBAString(String rgba){
       String rgb = rgba.substring(0, 7);
       String alphaString = rgba.substring(7);
       double alpha = (float)Integer.parseInt(alphaString, 16)/255f;
       //Clamp alpha at maximum of 1, else Color.web fails
       if(alpha > 1.0) alpha = 1.0;
       return Color.web(rgb, alpha);
    }

    public void polySetXPoints(float[] points){
        this.polygonXPoints = points;
    }

    public void polySetYPoints(float[] points){
        this.polygonYPoints = points;
    }

    public float[] getPolyXPositions(){
        return this.polygonXPoints;
    }

    public float[] getPolyYPositions(){
        return this.polygonYPoints;
    }

    public void setClosed(boolean isClosed){
        this.isClosed = isClosed;
    }

    public boolean isClosed(){
        return isClosed;
    }

    public void setOvalXPosition(float x){
        this.ovalPos[0] = x;
    }

    public void setOvalYPosition(float y){
        this.ovalPos[1] = y;
    }

    public void setrHorizontal(float r){
        this.ovalRadii[1] = r;
    }

    public double getrHorizontal() {
        return ovalRadii[1];
    }

    public void setrVertical(float r){
        this.ovalRadii[0] = r;
    }

    public double getrVertical() {
        return ovalRadii[0];
    }

    public void setRotation(float rotation){
        this.rotation = rotation;
    }

    public float getRotation() {
        return rotation;
    }

    public void setPolygon(boolean isPolygon){
        this.isPolygon = isPolygon;
    }
}
