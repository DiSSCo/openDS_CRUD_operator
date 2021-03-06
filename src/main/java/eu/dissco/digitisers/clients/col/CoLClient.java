package eu.dissco.digitisers.clients.col;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.dissco.digitisers.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CoLClient {

    /**************/
    /* ATTRIBUTES */
    /**************/

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static CoLClient instance=null;
    private final String apiUrl ="http://webservice.catalogueoflife.org/col/webservice";
    private Map<String, Optional<JsonObject>> mapTaxonByScientificNameAndKingdom; //Map to improve efficiency of this class, so it doesn't need to call the external APIs when we already got results


    /***********************/
    /* GETTERS AND SETTERS */
    /***********************/

    protected Logger getLogger() {
        return logger;
    }

    protected String getApiUrl() {
        return apiUrl;
    }

    protected Map<String, Optional<JsonObject>> getMapTaxonByScientificNameAndKingdom() {
        return mapTaxonByScientificNameAndKingdom;
    }

    protected void setMapTaxonByScientificNameAndKingdom(Map<String, Optional<JsonObject>> mapTaxonByScientificNameAndKingdom) {
        this.mapTaxonByScientificNameAndKingdom = mapTaxonByScientificNameAndKingdom;
    }


    /****************/
    /* CONSTRUCTORS */
    /****************/

    /**
     * Private constructor to avoid client applications to use constructor as we use the singleton design pattern
     */
    private CoLClient(){
        this.mapTaxonByScientificNameAndKingdom = new ConcurrentHashMap<String,Optional<JsonObject>>();
    }


    /*******************/
    /* PUBLIC METHODS */
    /******************/

    /**
     * Method to get an instance of CoLClient as we use the singleton design pattern
     * @return
     */
    public static CoLClient getInstance(){
        if (instance==null){
            instance = new CoLClient();
        }
        return instance;
    }

    /**
     * Function that returns the information found in the Catalogue of Life for the taxon concept requested (canonicalName,rank,kindgdom)
     * @param canonicalName Canonical name of the taxon concept
     * @param rank nane of the rank of the taxon concept
     * @param kingdomName name of the kingdom where this taxon concept belongs to
     * @return JsonObject with the information found in the Catalogue of Life for the taxon concept, or null if the taxon concept
     * was not found or if there was a problem getting it
     * @throws Exception
     */
    public JsonObject getTaxonInformation(String canonicalName, String rank, String kingdomName) throws Exception {
        JsonObject taxonInfoObj = null;

        if (this.getMapTaxonByScientificNameAndKingdom().containsKey(canonicalName + "#" + kingdomName)) {
            taxonInfoObj=this.getMapTaxonByScientificNameAndKingdom().get(canonicalName + "#" + kingdomName).orElse(null);
        } else {
            String canonicalNameEncoded = URLEncoder.encode(canonicalName, "UTF-8");
            JsonObject colResponse = (JsonObject) NetUtils.doGetRequestJson(this.getApiUrl() +
                    "?name=" + canonicalNameEncoded + "&rank=" + rank + "&format=json&response=full");

            if (colResponse != null && colResponse.get("number_of_results_returned").getAsInt() > 0) {
                JsonArray colResults = colResponse.getAsJsonArray("results");
                if (colResults.size() == 1) {
                    taxonInfoObj = colResults.get(0).getAsJsonObject();
                } else if (colResults.size() > 1) {
                    //The Catalogue of Life has several taxa for the same scientific name (Hemihomonyms like "Agathis montana") =>
                    //In this case only return the taxon in the kingdom we are interested
                    String jsonPath = "$[?(@.classification[0].rank=~/Kingdom/i && @.classification[0].name=~/" + kingdomName + "/i)]";
                    net.minidev.json.JSONArray resultsInKingdom = (net.minidev.json.JSONArray) JsonUtils.filterJson(colResults, jsonPath);
                    if (resultsInKingdom.size() == 1) {
                        Gson gson = new Gson();
                        JsonArray jsonArray = gson.fromJson(resultsInKingdom.toJSONString(), JsonArray.class);
                        taxonInfoObj = jsonArray.get(0).getAsJsonObject();
                    }
                }
            }
            this.getMapTaxonByScientificNameAndKingdom().put(canonicalName + "#" + kingdomName,Optional.ofNullable(taxonInfoObj));
        }
        return taxonInfoObj;
    }
}
