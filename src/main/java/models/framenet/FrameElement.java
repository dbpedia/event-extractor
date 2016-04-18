package models.framenet;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wojlukas on 3/7/16.
 */
public class FrameElement implements Serializable {
    private String name;
    private List<Span> spans;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Span> getSpans() {
        return spans;
    }

    public void setSpans(List<Span> spans) {
        this.spans = spans;
    }
}
