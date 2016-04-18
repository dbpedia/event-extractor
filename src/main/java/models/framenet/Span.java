package models.framenet;

import java.io.Serializable;

/**
 * Created by wojlukas on 3/7/16.
 */
public class Span implements Serializable {
    private int end;
    private int start;
    private String text;

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
