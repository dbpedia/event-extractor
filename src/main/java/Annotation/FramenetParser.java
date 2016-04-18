package Annotation;


import models.framenet.AnnotationSet;
import models.framenet.Frame;
import models.framenet.FrameElement;
import models.framenet.Span;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wojlukas on 3/7/16.
 */
public class FramenetParser {

    public static List<Frame> parseFramenetResponse(String responseJson) {

        List<Frame> frames = new ArrayList<>();
        JSONObject jsonObject = (JSONObject)JSONSerializer.toJSON(responseJson);

        JSONArray framesJson = jsonObject.getJSONArray("sentences").getJSONObject(0).getJSONArray("frames");

        for (int i = 0; i < framesJson.size(); i++) {
            Frame frame = new Frame();

            JSONObject frameJson = framesJson.getJSONObject(i);
            JSONArray annotationSetsArr = frameJson.getJSONArray("annotationSets");
            List<AnnotationSet> annotationSets = new ArrayList<>();
            for (int j = 0; j < annotationSetsArr.size(); j++) {
                JSONObject annotationSetJson = annotationSetsArr.getJSONObject(j);
                AnnotationSet annotationSet = new AnnotationSet();
                annotationSet.setRank(annotationSetJson.getInt("rank"));
                annotationSet.setScore(annotationSetJson.getDouble("score"));
                List<FrameElement> frameElements = new ArrayList<>();
                JSONArray frameElementsArr = annotationSetJson.getJSONArray("frameElements");
                for (int k = 0; k < frameElementsArr.size(); k++) {
                    JSONObject frameElemJson = frameElementsArr.getJSONObject(k);
                    FrameElement frameElement = parseFrameElement(frameElemJson);
                    frameElements.add(frameElement);
                }
                annotationSet.setFrameElements(frameElements);
                annotationSets.add(annotationSet);
            }
            frame.setAnnotationSets(annotationSets);
            frame.setTarget(parseFrameElement(frameJson.getJSONObject("target")));
            frames.add(frame);
        }

        return frames;
    }

    private static FrameElement parseFrameElement(JSONObject object) {
        FrameElement result = new FrameElement();

        result.setName(object.getString("name"));
        List<Span> spans = new ArrayList<>();
        JSONArray spansArray = object.getJSONArray("spans");
        for (int i = 0; i < spansArray.size(); i++) {
            JSONObject spanJson = spansArray.getJSONObject(i);

            Span span = new Span();
            span.setEnd(spanJson.getInt("end"));
            span.setStart(spanJson.getInt("start"));
            span.setText(spanJson.getString("text"));
            spans.add(span);
        }
        result.setSpans(spans);

        return result;
    }
}
