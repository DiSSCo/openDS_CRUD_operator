package eu.dissco.digitisers.clients.wiki;

import com.google.gson.JsonObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class WikipediaClientTest {

    private static final Logger logger = LoggerFactory.getLogger(WikipediaClientTest.class);

    private static WikiClient wikiClient;

    @BeforeClass
    public static void setup() throws Exception {
        wikiClient = WikiClient.getInstance("wikipedia");
    }

    @AfterClass
    public static void tearDown() {

    }

    @Test
    public void getWikiInformation_species_found() throws Exception {
        String canonicalName="Agathis montana";
        String kingdom="Plantae";
        JsonObject wikiInfo = wikiClient.getWikiInformation(canonicalName,kingdom);
        System.out.println("quevedo " + wikiInfo.toString());
        assertEquals("The wiki page url should be ", "https://en.wikipedia.org/wiki/Agathis_montana",wikiClient.getPageURL(wikiInfo));
    }

    @Test
    public void getWikiInformation_species_notFound() throws Exception {
        String canonicalName="Fran test";
        String kingdom="Plantae";
        JsonObject wikiInfo = wikiClient.getWikiInformation(canonicalName,kingdom);
        assertNull("The wiki page shouldn't exist", wikiInfo);
    }
}