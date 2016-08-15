package Annotation;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import models.dbpedia.SpotlightAnnotation;
import models.dbpedia.SpotlightResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wojlukas on 2/1/16.
 * Adjusted by Vincent Bohlen (vincent.bohlen@fu-berlin.de)
 */
public class SpotlightAnnotator {
    private final Logger LOGGER = LoggerFactory.getLogger(SpotlightAnnotator.class);

    public SpotlightAnnotation annotate(String text, int supportThreshold, double confidenceThreshold, String endpointUrl)
    {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(endpointUrl);

        SpotlightAnnotation result = null;

        List<NameValuePair> nameValuePairs = new ArrayList<>();

        nameValuePairs.add(new BasicNameValuePair("text", text));
        nameValuePairs.add(new BasicNameValuePair("support", String.valueOf(supportThreshold)));
        nameValuePairs.add(new BasicNameValuePair("confidence", String.valueOf(confidenceThreshold)));

        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
           LOGGER.error(e.getMessage());
        }
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");

        HttpResponse response = null;
        try {
            response = client.execute(post);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return null;
        }

        String jsonText = null;
        try {
            jsonText = IOUtils.toString(response.getEntity().getContent(), "UTF-8").trim();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        boolean testJson = true;
        JSONObject json = null;
        try
        {
            json = (JSONObject) JSONSerializer.toJSON(jsonText);
        }
        catch (JSONException e)
        {
            LOGGER.error(e.getMessage());
            testJson = false;
        }

        if (testJson)
        {
            try {
                result = parseJsonResponseObject(json);
            }
            catch (JSONException e) {
                LOGGER.error(e.getMessage());
            }
        }

        return result;
    }

    private SpotlightAnnotation parseJsonResponseObject(JSONObject response) {
        SpotlightAnnotation result = new SpotlightAnnotation();

        result.setConfidence(Double.parseDouble(response.getString("@confidence")));
        result.setSupport(Integer.parseInt(response.getString("@support")));
        result.setTypes(response.getString("@types"));
        result.setSparql(response.getString("@sparql"));
        result.setPolicy(response.getString("@policy"));

        if (response.containsKey("Resources")) {

            JSONArray resourcesArray = response.getJSONArray("Resources");
            Iterator i = resourcesArray.iterator();
            while (i.hasNext())
            {
                JSONObject jsonResource = (JSONObject) i.next();

                SpotlightResource resource = new SpotlightResource();

                resource.setUri(jsonResource.getString("@URI"));
                resource.setSupport(Integer.parseInt(jsonResource.getString("@support")));
                resource.setTypesString(jsonResource.getString("@types"));
                resource.setSurfaceForm(jsonResource.getString("@surfaceForm"));
                resource.setOffset(Integer.parseInt(jsonResource.getString("@offset")));
                resource.setSimilarityScore(Double.parseDouble(jsonResource.getString("@similarityScore")));
                resource.setPercentageOfSecondRank(Float.parseFloat(jsonResource.getString("@percentageOfSecondRank")));

                result.addResource(resource);
            }
        }

        return result;
    }
}
