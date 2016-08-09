package models.dbpedia;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wojlukas on 2/1/16.
 */
public class SpotlightAnnotation implements Serializable {
    private double confidence;
    private int support;
    private String types;
    private String sparql;
    private String policy;
    private List<SpotlightResource> resources = new ArrayList<>();

    public SpotlightAnnotation() {
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getSparql() {
        return sparql;
    }

    public void setSparql(String sparql) {
        this.sparql = sparql;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public List<SpotlightResource> getResources() {
        return resources;
    }

    public void setResources(List<SpotlightResource> resources) {
        this.resources = resources;
    }

    public void addResource(SpotlightResource resource) {
        this.resources.add(resource);
    }
}
