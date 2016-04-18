package models.framenet;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wojlukas on 3/7/16.
 */
public class AnnotationSet implements Serializable {
    private List<FrameElement> frameElements;
    private int rank;
    private double score;

    public List<FrameElement> getFrameElements() {
        return frameElements;
    }

    public void setFrameElements(List<FrameElement> frameElements) {
        this.frameElements = frameElements;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
