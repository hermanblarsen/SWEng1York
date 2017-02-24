package utilities;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by habl on 23/02/2017.
 */
public class Presentation {
    private String documentID;
    private String author;
    private Float version;
    private Float documenAspectRatio;
    private String tags;
    private Theme theme;
    private boolean autoplayMedia;

    private int groupFormat;

    


    private List<Slide> slideList;

    public Presentation () {
        slideList = new ArrayList<Slide>();
        this.theme = new Theme ();
    }

    public void addSlide(int slideIndex, Slide newSlide) {
        this.slideList.add(slideIndex, newSlide);
    }
    public void addSlide(Slide newSlide) {
        this.slideList.add(this.slideList.size(), newSlide);
    }

    public void deleteSlideIndex(int slideIndex) {

    }
    public void deleteSlideID(int slideID) {

    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Float getVersion() {
        return version;
    }

    public void setVersion(Float version) {
        this.version = version;
    }

    public Float getDocumenAspectRatio() {
        return documenAspectRatio;
    }

    public void setDocumenAspectRatio(Float documenAspectRatio) {
        this.documenAspectRatio = documenAspectRatio;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public boolean isAutoplayMedia() {
        return autoplayMedia;
    }

    public void setAutoplayMedia(boolean autoplayMedia) {
        this.autoplayMedia = autoplayMedia;
    }

    public int getGroupFormat() {
        return groupFormat;
    }

    public void setGroupFormat(int groupFormat) {
        this.groupFormat = groupFormat;
    }

    public List<Slide> getSlideList() {
        return slideList;
    }

    public void setSlideList(List<Slide> slideList) {
        this.slideList = slideList;
    }
}
