package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.presentationElements.SlideElement;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileNotFoundException;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;

/**
 * externalImageElement.ImageElement Class
 * <p>
 *     This externalImageElement.ImageElement class handles information for images and imageViews. Allowing the setting and getting of variables
 *     for different aspects of the image or imageView. Some of the setters will also directly change the imageVew as well as
 *     change the variables.
 * </p>
 * @author ob610
 * @author Michał Liszewski
 * @version 1.1
 * @since 1.0
 */
public class ImageElement extends SlideElement {

    /**
     * URL or full local path to image file.
     */
    protected String path = null;
    /**
     * The width of the image.
     */
    protected Float width = null;
    /**
     * Height of the image.
     */
    protected Float height = null;
    /**
     * X-position of the top left corner of the image in percentage of the slide’s width.
     */
    protected Float posX = null;
    /**
     * Y-position of the top left corner of the image in percentage of the slide’s width.
     */
    protected Float posY = null;
    /**
     * Central rotation of the image in degrees, anti-clockwise. 0 degrees is along the positive x-axis.
     */
    protected Float rotation = null;
    /**
     * Opacity of image, where 1 is fully opaque and 0 fully transparent.
     */
    protected Double opacity = null;
    /**
     * Aspect ratio of image.
     */
    protected Float aspectRatio = null;
    /**
     * True if the image aspect ratio cannot change.
     */
    protected Boolean aspectRatioLocked = null;
    /**
     * Width of image in pixels.
     */
    protected Integer imagePixelWidth = null;
    /**
     * Height of image in pixels.
     */
    protected Integer imagePixelHeight = null;
    /**
     * For testing purposes of image components
     */
    protected ImageView imageView = null;
    /**
     * For testing of image components.
     */
    protected Image image = null;
    /**
     * True if a border should be displayed around the image.
     */
    protected Boolean isBorder = null;
    /**
     * Border width in pixels, extending into the image.
     */
    protected Float borderWidth = null;
    /**
     * Border colour supplied as aRGB #AARRGGFF.
     */
    protected String borderColour = null;
    /**
     * Java FX image property which allows user to choose which section of the image to show.
     */
    protected Rectangle2D viewPort = null;
    /**
     * Sets image smoothing, False:Lower quality filtering but faster rendering, True: Higher quality filtering but slower rendering.
     */
    protected Boolean isSmooth = null;
    /**
     * Adds a mouse listener to the image, implementing an anonymous class that the client can define.
     */
    protected EventHandler<MouseEvent> eventHandler = null;
    /**
     * Core node of the Image, includes {@link ImageView} and borders.
     */
    private BorderPane coreNode = null;

    /**
     * Empty constructor
     */
    public ImageElement(){

    }

    // Methods

    /**
     * Redraws JavaFx Node on Canvas. Not needed for now.
     */
    @Override
    public void doClassSpecificRender() {
        //Anything which requires the slide width to calculate must be done here, not in setupElement
        //Also anything assicuared with the Image needs to go in here  due to testing requirements

        if (path != null) {
            if (image == null) {
                if (path.startsWith("http") || path.startsWith("file")) {
                    this.image = new Image(path);
                } else {
                    File file = new File(path);
                    String mediaPath = file.toURI().toString();
                    this.image = new Image(mediaPath);
                }
            }
            if (imageView == null) {
                this.imageView = new ImageView(image);
            }


            if (aspectRatioLocked != null) {
                imageView.setPreserveRatio(aspectRatioLocked);
            }
            if (opacity != null) {
                imageView.setOpacity(opacity);
            }
            if (viewPort != null) {
                imageView.setViewport(viewPort);
            }
            if (isSmooth != null) {
                imageView.setSmooth(isSmooth());
            }
            if (imagePixelWidth == null) {
                    imagePixelWidth = (int) image.getWidth();
            }
            if (imagePixelHeight == null) {
                    imagePixelHeight = (int) image.getHeight();
            }
            if (width == null) {
                width = (float) 0.1;
            }
            if (height == null) {
                height = (float) 0.1;
            }
            if (posX == null) {
                posX = 0f;
            }
            if (posY == null) {
                posY = 0f;
            }
            if (aspectRatio == null) {
                aspectRatio = (getWidth()/getHeight());
            }
        }

        coreNode.setCenter(imageView);

        coreNode.setTranslateX(posX*(float)getSlideWidth());
        coreNode.setTranslateY(posY*(float)getSlideHeight());

        try {
            imageView.setFitWidth(width * (float) getSlideWidth());
        } catch (NullPointerException e) {
            //Do nothing
        }
        try {
            imageView.setFitHeight(height*(float)getSlideHeight());
        } catch (NullPointerException e) {
            //Do nothing
        }

        // Border box
        if(isBorder) {
            HBox boxLeft = new HBox();
            HBox boxTop = new HBox();
            HBox boxRight = new HBox();
            HBox boxBottom = new HBox();
            if (borderColour != null) {
                boxLeft.setStyle("-fx-background-color: " + borderColour + ";");
                boxTop.setStyle("-fx-background-color: " + borderColour + ";");
                boxRight.setStyle("-fx-background-color: " + borderColour + ";");
                boxBottom.setStyle("-fx-background-color: " + borderColour + ";");
            }
            if (borderWidth != null) {
                boxLeft.setMinWidth(borderWidth*getSlideWidth());
                boxTop.setMinHeight(borderWidth*getSlideHeight());
                boxRight.setMinWidth(borderWidth*getSlideWidth());
                boxBottom.setMinHeight(borderWidth*getSlideHeight());
            }

            coreNode.setLeft(boxLeft);
            coreNode.setTop(boxTop);
            coreNode.setRight(boxRight);
            coreNode.setBottom(boxBottom);
        }
    }

    /**
     *
     * @return Core JavaFx {@link Node} object for the image element
     */
    @Override
    public Node getCoreNode() {
        return coreNode;
    }

    /**
     * /**
     * This method acts as the true constructor for the element. Should instantiate all of the JavaFx objects required for rendering the element.
     *
     * @throws IllegalStateException informs the user that crucial information was not provided before setting up the image.
     */
    @Override
    public void setupElement() throws IllegalStateException {
        // Setting fields to their default values


        if (rotation == null) {
            rotation = 0.0f;
        }
        if (opacity == null) {
            opacity = 1d;
        }
        if (aspectRatioLocked == null) {
            aspectRatioLocked = true;
        }
        if (isBorder == null) {
            isBorder = false;
        }
        if (borderWidth == null) {
            borderWidth = 0f;
        }
        if (borderColour == null) {
            borderColour = "#000000FF";
        }
        if (viewPort == null) {
            viewPort = null;
        }
        if (isSmooth == null) {
            isSmooth = false;
        }


        // Borders
        if (coreNode == null) {
            coreNode = new BorderPane();

        }

        coreNode.setRotate(rotation);
        getCoreNode().addEventFilter(MouseEvent.MOUSE_CLICKED, event -> performOnClickAction());
    }

    //Getters and Setters

    /**
     * Getter for the image path
     *
     * @return The path of the image
     */
    public String getPath(){
        return path;
    }

    /**
     *     If the path given passes the checks that it isn't NULL, has a valid extension and is at least long enough to include the extension,
     *     then the path will be set to the one given in the arguments, if it does not pass the checks, a FileNotFoundException is thrown.
     *
     * @param path Argument that the path will be set to
     * @throws FileNotFoundException Exception that will be thrown is the path is invalid
     */
    public void setPath(String path) throws FileNotFoundException {
        //checks if the path is NOT null
        if (path != null){
            //checks if the path length is greater than 3 characters
            if (path.length() > 3) {
                //puts the last 3 characters of the path string into a new string
                String checkString = path.substring(path.length()-3);
                // checks the last 3 characters of the path to see if it is a valid file type
                if (checkString.equals("png") || checkString.equals("jpg") || checkString.equals("gif") || checkString.equals("bmp")
                        || checkString.equals("PNG") || checkString.equals("JPG") || checkString.equals("GIF") || checkString.equals("BMP")){
                    //if it passes all the check the path is set
                    this.path = path;
                } else {
                    //if the path doesn't have a valid extension, throws an error
                    throw new FileNotFoundException("Error - Invalid File Path");
                }
            } else {
                //if the path is shorter than 3 characters, throws an error
                throw new FileNotFoundException("Error - Invalid File Path");
            }
        } else {
            //if the path is null, throws an error
            throw new FileNotFoundException("Error - Invalid File Path");
        }

        setupElement();
    }

    /**
     *     Takes in a integer that dictates what kind of MouseEvent is created for the given eventHandles. For more detail
     *     on the MouseEvent and the types, see https://docs.oracle.com/javase/8/javafx/api/javafx/scene/input/MouseEvent.html
     * <p>
     *     EventType = 1 gives MouseEvent.ANY
     * </p>
     * <p>
     *     EventType = 2 gives MouseEvent.DRAG_DETECTED
     * </p>
     * <p>
     *     EventType = 3 gives MouseEvent.MOUSE_CLICKED
     * </p>
     * <p>
     *     EventType = 4 gives MouseEvent.MOUSE_DRAGGED
     * </p>
     * <p>
     *     EventType = 5 gives MouseEvent.MOUSE_ENTERED
     * </p>
     * <p>
     *     EventType = 6 gives MouseEvent.MOUSE_ENTERED_TARGET
     * </p>
     * <p>
     *     EventType = 7 gives MouseEvent.MOUSE_EXITED
     * </p>
     * <p>
     *     EventType = 8 gives MouseEvent.MOUSE_EXITED_TARGET
     * </p>
     * <p>
     *     EventType = 9 gives MouseEvent.MOUSE_MOVED
     * </p>
     * <p>
     *     EventType = 10 gives MouseEvent.MOUSE_PRESSED
     * </p>
     * <p>
     *     EventType = 11 gives MouseEvent.MOUSE_RELEASED
     * </p>
     * @param EventType Integer that dictates the type of event
     * @param eventHandler The event handler that is the MouseEvent it created around
     */
    public void addEventHandler (int EventType, EventHandler<MouseEvent> eventHandler){
        if (imageView == null) {
            imageView = new ImageView();
        }
        switch (EventType){
            case 1: {
                imageView.addEventHandler(MouseEvent.ANY, eventHandler);
                break;
            }
            case 2: {
                imageView.addEventHandler(MouseEvent.DRAG_DETECTED, eventHandler);
                break;
            }
            case 3: {
                imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
                break;
            }
            case 4: {
                imageView.addEventHandler(MouseEvent.MOUSE_DRAGGED, eventHandler);
                break;
            }
            case 5: {
                imageView.addEventHandler(MouseEvent.MOUSE_ENTERED, eventHandler);
                break;
            }
            case 6: {
                imageView.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, eventHandler);
                break;
            }
            case 7: {
                imageView.addEventHandler(MouseEvent.MOUSE_EXITED, eventHandler);
                break;
            }
            case 8: {
                imageView.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, eventHandler);
                break;
            }
            case 9: {
                imageView.addEventHandler(MouseEvent.MOUSE_MOVED, eventHandler);
                break;
            }
            case 10: {
                imageView.addEventHandler(MouseEvent.MOUSE_PRESSED, eventHandler);
                break;
            }
            case 11: {
                imageView.addEventHandler(MouseEvent.MOUSE_RELEASED, eventHandler);
                break;
            }
        }

        this.eventHandler = eventHandler;

        setupElement();
    }

    /**
     *
     * @return image width
     */
    public float getWidth(){
        return width;
    }

    /**
     *
     * @param width image width
     * @throws IllegalArgumentException when out of bounds (0-infinity)
     */
    public void setWidth(float width) throws IllegalArgumentException {
        if (width >= 0) {
            this.width = width;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     *
     * @return image height
     */
    public float getHeight(){
        return height;
    }


    public void setHeight(float height) throws IllegalArgumentException {
        if (height >= 0) {
            this.height = height;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public float getPosX(){
        return posX;
    }

    public void setPosX(float posX){
        this.posX = posX;
    }

    public float getPosY(){
        return posY;
    }

    public void setPosY(float posY){
        this.posY = posY;
    }

    public float getRotation(){
        return rotation;
    }

    public void setRotation(float rotation) throws IllegalArgumentException {
        if ((rotation >= 0.0) && (rotation <= 360.0)) {
            this.rotation = rotation;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public double getOpacity(){
        return opacity;
    }

    public void setOpacity(double opacity) throws IllegalArgumentException {
        if ((opacity >= 0) && (opacity <= 1)) {
            this.opacity = opacity;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public float getAspectRatio(){
        return aspectRatio;
    }

    public void setAspectRatio(float aspectRatio) throws IllegalArgumentException {
        if (aspectRatio > 0.0) {
            this.aspectRatio = aspectRatio;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean aspectRatioLocked(){
        return aspectRatioLocked;
    }

    public void aspectRatioLock(boolean aspectRatioLocked){
        this.aspectRatioLocked = aspectRatioLocked;
    }

    public int getImagePixelWidth(){
        if (imagePixelWidth == null) {
            imagePixelWidth = (int) image.getWidth();
        }

        return imagePixelHeight;
    }

    public int getImagePixelHeight(){
        if (imagePixelHeight == null) {
            imagePixelHeight = (int) image.getHeight();
        }

        return imagePixelHeight;
    }

    public ImageView getImageView(){
        return imageView;
    }

    public void setImageView(ImageView imageView){
        this.imageView = imageView;
    }

    public Image getImage(){
        return image;
    }

    public void setImage(Image image){
        this.image = image;
        imageView = new ImageView(image);
    }

    public boolean isBorder(){
        return isBorder;
    }

    public void setBorder(boolean isBorder){
        this.isBorder = isBorder;
    }

    public float getBorderWidth(){
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) throws IllegalArgumentException {
        if (slideCanvas != null) {
            if ((borderWidth >= 0.0) && (borderWidth <= Double.min(0.05*width, 0.05*height))) {
                this.borderWidth = borderWidth;
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            this.borderWidth = borderWidth;
        }
    }

    public String getBorderColour(){
        return borderColour;
    }

    public void setBorderColour(String borderColour){
        this.borderColour = borderColour;
    }

    public Rectangle2D getViewPort(){
        return viewPort;
    }

    public void setViewPort(Rectangle2D viewPort){
        this.viewPort = viewPort;
    }

    public boolean isSmooth(){
        return imageView.isSmooth();
    }

    public void setSmooth(boolean smooth){
        this.isSmooth = smooth;
    }

    @Override
    public void destroyElement(){

    }
}
