package eu.dissco.digitisers.clients.wiki;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.dissco.digitisers.utils.NetUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jena.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;


public class WikiDataClient extends WikiClient{

    private final static Logger logger = LoggerFactory.getLogger(WikiDataClient.class);

    private static WikiDataClient instance=null;

    //private constructor to avoid client applications to use constructor
    //as we use the singleton pattern
    private WikiDataClient(){
        super("https://query.wikidata.org/sparql");
    }

    public static WikiDataClient getInstance(){
        if (instance==null){
            instance = new WikiDataClient();
        }
        return instance;
    }

    @Override
    public String getClientName() {
        return "wikidata";
    }


    @Override
    public JsonObject getWikiInformation(String canonicalName, String kingdom) throws Exception {
        JsonObject wikiInfoObj = null;
        JsonObject wikiDataInfo = null;
        if (this.getMapPageInfoByCanonicalNameAndKingdom().containsKey(canonicalName+"#"+kingdom)) {
            wikiInfoObj=this.getMapPageInfoByCanonicalNameAndKingdom().get(canonicalName+"#"+kingdom);
        } else {
            JsonObject taxonInfo = this.getTaxonConceptInfo(canonicalName,kingdom);
            JsonArray results = taxonInfo.getAsJsonObject("results").getAsJsonArray("bindings");

            if (results.size()==1){
                String wikidataPageURL = results.get(0).getAsJsonObject().getAsJsonObject("item").get("value").getAsString();
                String wikidataId = NetUtils.getLastSegmentOfUrl(wikidataPageURL);
                wikiInfoObj = new JsonObject();
                wikiInfoObj.addProperty("wikidataId",wikidataId);
                wikiInfoObj.addProperty("wikidataPageURL",wikidataPageURL);

                //Obtain all external taxon identifiers found in the taxon concept page
                JsonObject taxonIdentifiers = this.getTaxonIdentifiersByEntityId(wikidataId);
                results = taxonIdentifiers.getAsJsonObject("results").getAsJsonArray("bindings");
                JsonArray wikiDataTaxonIdentifiers = new JsonArray();
                for (JsonElement identifier: results) {
                    JsonObject wikiDataTaxonIdentifier = new JsonObject();
                    wikiDataTaxonIdentifier.addProperty("name",identifier.getAsJsonObject().getAsJsonObject("name").get("value").getAsString());
                    wikiDataTaxonIdentifier.addProperty("value",identifier.getAsJsonObject().getAsJsonObject("value").get("value").getAsString());
                    wikiDataTaxonIdentifier.addProperty("link",identifier.getAsJsonObject().getAsJsonObject("link").get("value").getAsString());
                    wikiDataTaxonIdentifiers.add(wikiDataTaxonIdentifier);
                }
                wikiInfoObj.add("identifiers",wikiDataTaxonIdentifiers);

                //Obtain if article for this concept is found in wikipedia
                JsonObject wikipediaInfo = this.getWikipediaPageURL(wikidataId);
                JsonArray wikipediaResults = wikipediaInfo.getAsJsonObject("results").getAsJsonArray("bindings");
                if (wikipediaResults.size()==1) {
                    wikiInfoObj.addProperty("wikipediaURL",wikipediaResults.get(0).getAsJsonObject().getAsJsonObject("article").get("value").getAsString());
                }
            }
            this.getMapPageInfoByCanonicalNameAndKingdom().put(canonicalName+"#"+kingdom,wikiInfoObj);
        }

        return wikiInfoObj;
    }

    @Override
    public String getPageURL(JsonObject wikiInfoObj){
        String pageUrl = null;
        if (wikiInfoObj!=null){
            pageUrl = wikiInfoObj.get("wikidataPageURL").getAsString();
        }
        return pageUrl;
    }

    private JsonObject getTaxonConceptInfo(String canonicalName, String kingdom) throws Exception {
        String querySelectTaxon = this.getQueryTaxonConcept(canonicalName);
        JsonObject taxonInfo = this.executeSparqlSelectQuery(querySelectTaxon);
        JsonArray taxonResults = taxonInfo.getAsJsonObject("results").getAsJsonArray("bindings");

        String querySelectKingdom = this.getQueryTaxonConcept(kingdom);
        JsonObject kingdomInfo = this.executeSparqlSelectQuery(querySelectKingdom);
        JsonArray kingdomResults = kingdomInfo.getAsJsonObject("results").getAsJsonArray("bindings");

        for (Iterator<JsonElement> iter = taxonResults.iterator(); iter.hasNext(); ) {
            JsonObject taxonResult = (JsonObject) iter.next();
            if (kingdomResults.size() != 1) {
                iter.remove();
            } else {
                String taxonWikidataPageURL = taxonResult.getAsJsonObject().getAsJsonObject("item").get("value").getAsString();
                String taxonWikidataId = NetUtils.getLastSegmentOfUrl(taxonWikidataPageURL);
                String kingdomWikidataPageURL = kingdomResults.get(0).getAsJsonObject().getAsJsonObject("item").get("value").getAsString();
                String kingdomWikidataId = NetUtils.getLastSegmentOfUrl(kingdomWikidataPageURL);

                String querySelectTaxonInKingdom= this.getQueryIsTaxonInKingdom(taxonWikidataId,kingdomWikidataId);
                JsonObject taxonInKingdomInfo = this.executeSparqlSelectQuery(querySelectTaxonInKingdom);
                if (taxonInKingdomInfo.getAsJsonObject("results").getAsJsonArray("bindings").size()!=1){
                    iter.remove();
                };
            }
        }
        return taxonInfo;
    }

    private String getQueryTaxonConcept(String canonicalName){
        String query = this.getWikiDataBuiltInPrefixes() +
                "SELECT ?item ?itemLabel\n" +
                "WHERE \n" +
                "{\n" +
                "  BIND('"+canonicalName+"' AS ?canonicalName)\n" +
                "  ?item wdt:P225 ?canonicalName\n" +
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language '[AUTO_LANGUAGE],en'. }  \n" +
                "}";
        return query;
    }

    private String getQueryIsTaxonInKingdom(String taxonEntityId, String kingdomEntityId){
        String query = this.getWikiDataBuiltInPrefixes() +
                "SELECT ?taxon ?taxonLabel\n" +
                "WHERE \n" +
                "{\n" +
                "  BIND(wd:"+taxonEntityId+" AS ?taxon)\n" +
                "  BIND(wd:"+kingdomEntityId+" AS ?kindgom) \n" +
                "  ?taxon wdt:P171+ ?kindgom .\n" +
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language '[AUTO_LANGUAGE],en'. }    \n" +
                "}";
        return query;
    }

    private JsonObject getTaxonIdentifiersByEntityId(String entityId) throws Exception {
        String querySelect = this.getWikiDataBuiltInPrefixes() +
                "SELECT ?property ?name ?value ?link\n" +
                "WHERE\n" +
                "{\n" +
                "  BIND(wd:Q42396390 AS ?taxonIdentifier)\n" +
                "  BIND(wd:"+entityId+" AS ?taxon)\n" +
                "  ?taxon ?propUrl ?value .\n" +
                "  ?property ?ref ?propUrl .\n" +
                "  ?property wdt:P31/wdt:P279* ?taxonIdentifier .\n" +
                "  ?property rdfs:label ?name .  \n" +
                "  ?property wdt:P1630 ?formatterurl .\n" +
                "  FILTER (lang(?name) = 'en')\n" +
                "  FILTER isliteral(?value)             \n" +
                "  BIND(IRI(REPLACE(?value, '^(.+)$', ?formatterurl)) AS ?link)\n" +
                "}";
        return this.executeSparqlSelectQuery(querySelect);
    }

    private JsonObject getWikipediaPageURL(String entityId) throws Exception {
        String querySelect = this.getWikiDataBuiltInPrefixes() +
                "SELECT ?article\n" +
                "WHERE\n" +
                "{\n" +
                "  BIND(wd:"+entityId+" AS ?taxon)\n" +
                "   ?article schema:about ?taxon ; schema:isPartOf <https://en.wikipedia.org/> .\n" +
                "   SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\" }\n" +
                "}";

        return this.executeSparqlSelectQuery(querySelect);
    }

    private String getWikiDataBuiltInPrefixes(){
        String prefixes = "PREFIX wd: <http://www.wikidata.org/entity/>\n" +
                "PREFIX wds: <http://www.wikidata.org/entity/statement/>\n" +
                "PREFIX wdv: <http://www.wikidata.org/value/>\n" +
                "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
                "PREFIX wikibase: <http://wikiba.se/ontology#>\n" +
                "PREFIX p: <http://www.wikidata.org/prop/>\n" +
                "PREFIX ps: <http://www.wikidata.org/prop/statement/>\n" +
                "PREFIX pq: <http://www.wikidata.org/prop/qualifier/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX bd: <http://www.bigdata.com/rdf#>\n" +
                "\n" +
                "PREFIX wdref: <http://www.wikidata.org/reference/>\n" +
                "PREFIX psv: <http://www.wikidata.org/prop/statement/value/>\n" +
                "PREFIX psn: <http://www.wikidata.org/prop/statement/value-normalized/>\n" +
                "PREFIX pqv: <http://www.wikidata.org/prop/qualifier/value/>\n" +
                "PREFIX pqn: <http://www.wikidata.org/prop/qualifier/value-normalized/>\n" +
                "PREFIX pr: <http://www.wikidata.org/prop/reference/>\n" +
                "PREFIX prv: <http://www.wikidata.org/prop/reference/value/>\n" +
                "PREFIX prn: <http://www.wikidata.org/prop/reference/value-normalized/>\n" +
                "PREFIX wdno: <http://www.wikidata.org/prop/novalue/>\n" +
                "PREFIX wdata: <http://www.wikidata.org/wiki/Special:EntityData/>\n" +
                "\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX prov: <http://www.w3.org/ns/prov#>\n" +
                "PREFIX bds: <http://www.bigdata.com/rdf/search#>\n" +
                "PREFIX gas: <http://www.bigdata.com/rdf/gas#>\n" +
                "PREFIX hint: <http://www.bigdata.com/queryHints#>\n";
        return prefixes;
    }

    private JsonObject executeSparqlSelectQuery(String querySelect) throws Exception {
        String endpointUrl = this.getApiUrl();
        JsonObject jsonObject = null;
        CloseableHttpClient httpClient = NetUtils.buildUnsafeSslHttpClient();
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointUrl,querySelect,httpClient);
        try {
            ResultSet results = qexec.execSelect();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(os, results);
            String json = os.toString("UTF-8");
            Gson gson = new Gson();
            jsonObject = gson.fromJson(json, JsonObject.class);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            qexec.close();
            httpClient.close();
        }
        return jsonObject;
    }
}