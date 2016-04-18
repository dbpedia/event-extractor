package models.framenet;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wojlukas on 3/7/16.
 */
public class Frame implements Serializable {
    private List<AnnotationSet> annotationSets;
    private FrameElement target;

    public List<AnnotationSet> getAnnotationSets() {
        return annotationSets;
    }

    public void setAnnotationSets(List<AnnotationSet> annotationSets) {
        this.annotationSets = annotationSets;
    }

    public FrameElement getTarget() {
        return target;
    }

    public void setTarget(FrameElement target) {
        this.target = target;
    }
}
