package models.dbpedia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wojlukas on 2/1/16.
 */
public class SpotlightResource implements Serializable {
    private String uri;
    private int support;
    private String typesString;
    private String surfaceForm;
    private int offset;
    private double similarityScore;
    private float percentageOfSecondRank;

    private List<String> rdfTypes = new ArrayList<>();
    private List<String> dctSubjects = new ArrayList<>();

    public SpotlightResource() {
    }



    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }

    public String getTypesString() {
        return typesString;
    }

    public void setTypesString(String typesString) {
        this.typesString = typesString;
    }

    public String getSurfaceForm() {
        return surfaceForm;
    }

    public void setSurfaceForm(String surfaceForm) {
        this.surfaceForm = surfaceForm;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public float getPercentageOfSecondRank() {
        return percentageOfSecondRank;
    }

    public void setPercentageOfSecondRank(float percentageOfSecondRank) {
        this.percentageOfSecondRank = percentageOfSecondRank;
    }

    public List<String> getRdfTypes() {
        return rdfTypes;
    }

    public void setRdfTypes(List<String> rdfTypes) {
        this.rdfTypes = rdfTypes;
    }

    public List<String> getDctSubjects() {
        return dctSubjects;
    }

    public void setDctSubjects(List<String> dctSubjects) {
        this.dctSubjects = dctSubjects;
    }
}
