package eu.dissco.digitisers.clients.digitalObjectRepository;

import com.google.common.collect.MapDifference;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.dissco.digitisers.utils.DigitalSpecimenUtils;
import eu.dissco.digitisers.utils.JsonUtils;
import net.cnri.cordra.api.CordraClient;
import net.cnri.cordra.api.CordraException;
import net.cnri.cordra.api.HttpCordraClient;
import net.cnri.cordra.api.VersionInfo;
import net.dona.doip.DoipRequestHeaders;
import net.dona.doip.InDoipMessage;
import net.dona.doip.client.*;
import net.dona.doip.client.transport.DoipClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DigitalObjectRepositoryClient implements AutoCloseable {

    /**************/
    /* ENUM TYPES */
    /**************/

    public enum DIGITAL_OBJECT_OPERATION {
        INSERT,
        UPDATE,
        DELETE
    }

    /**************/
    /* ATTRIBUTES */
    /**************/

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static DigitalObjectRepositoryClient instance=null;
    private final DigitalObjectRepositoryInfo digitalObjectRepositoryInfo;
    private final DoipClient doipClient;
    private final CordraClient restClient;
    private final AuthenticationInfo authInfo;
    private final ServiceInfo serviceInfo;
    private static Map<String,Optional<DigitalObject>> mapDigitalObjectSchemas = new ConcurrentHashMap<String,Optional<DigitalObject>>(); //For efficiency, keep in memory schemas obtained by broker


    /***********************/
    /* GETTERS AND SETTERS */
    /***********************/

    protected Logger getLogger() {
        return logger;
    }

    protected DigitalObjectRepositoryInfo getDigitalObjectRepositoryInfo() {
        return digitalObjectRepositoryInfo;
    }

    protected DoipClient getDoipClient() {
        return doipClient;
    }

    protected AuthenticationInfo getAuthInfo() {
        return authInfo;
    }

    protected ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public Map<String, Optional<DigitalObject>> getMapDigitalObjectSchemas() {
        return mapDigitalObjectSchemas;
    }

    public void setMapSchemasRepository(Map<String, Optional<DigitalObject>> mapDObjSchemas) {
        mapDigitalObjectSchemas = mapDObjSchemas;
    }

    protected CordraClient getRestClient() {
        return restClient;
    }


    /****************/
    /* CONSTRUCTORS */
    /****************/

    /**
     *  Create a new DigitalObjectRepositoryClient
     * @param digitalObjectRepositoryInfo
     * @throws DigitalObjectRepositoryException
     */
    public DigitalObjectRepositoryClient(DigitalObjectRepositoryInfo digitalObjectRepositoryInfo) throws DigitalObjectRepositoryException {
        try{
            this.digitalObjectRepositoryInfo=digitalObjectRepositoryInfo;
            this.doipClient=new DoipClient();
            this.authInfo= new PasswordAuthenticationInfo(digitalObjectRepositoryInfo.getUsername(), digitalObjectRepositoryInfo.getPassword());
            this.serviceInfo = new ServiceInfo(digitalObjectRepositoryInfo.getServiceId(), digitalObjectRepositoryInfo.getHostAddress(), digitalObjectRepositoryInfo.getDoipPort());
            this.restClient = new HttpCordraClient(digitalObjectRepositoryInfo.getUrl(),digitalObjectRepositoryInfo.getUsername(),digitalObjectRepositoryInfo.getPassword());
        } catch (Exception e){
            throw new DigitalObjectRepositoryException("Error setting up DigitalObjectRepositoryClient " + e.getMessage(),e);
        }
    }



    /*******************/
    /* PUBLIC METHODS */
    /******************/

    /***
     * Function that returns all the schemas in the repository
     * @return List of digital objects with all the schemas in the repository
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getAllSchemas() throws DigitalObjectRepositoryException {
        return getAllDigitalObjectsBySchema("Schema");
    }

    /**
     * Function that returns all the users in the repository
     * Note: The admin user is special user and it is not returned by the function getAllUsers as it is not a digital object
     * @return List of digital objects with all the users in the repository
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getAllUsers() throws DigitalObjectRepositoryException {
        return getAllDigitalObjectsBySchema("User");
    }

    /***
     * Function that returns all the digital specimens in the repository
     * @return List of digital objects with all the digital specimens in the repository
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getAllDigitalSpecimens() throws DigitalObjectRepositoryException {
        return getAllDigitalObjectsBySchema("DigitalSpecimen");
    }

    /**
     * Function that returns all the digital objects by the schema passed as parameter
     * @param schemaName Name of the schema for which we want to retrieve all its digital object
     * @return List of all the digital objects by the schema passed as parameter
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getAllDigitalObjectsBySchema(String schemaName) throws DigitalObjectRepositoryException {
        String query = "type:" + escapeQueryParamValue(schemaName);
        return this.searchAll(query);
    }

    /**
     * Function that returns all the digital objects in the repository
     * @return List of all the digital objects in the repository
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getAllDigitalObjects() throws DigitalObjectRepositoryException {
        return this.searchAll("type:[* TO *]");
    }

    /***
     * Function that returns the schema for the searched name
     * @param name Name of the schema we want to returned
     * @return Digital object with the searched schema or null if there isn't any schema with the name searched
     * or the name is not unique
     * @throws DigitalObjectRepositoryException
     */
    public DigitalObject getSchemaByName(String name) throws DigitalObjectRepositoryException {
        DigitalObject schema = null;
        if (this.getMapDigitalObjectSchemas().containsKey(name)){
            schema = this.getMapDigitalObjectSchemas().get(name).orElse(null);
        } else{
            String query = "type:Schema AND /name:" + escapeQueryParamValue(name);
            List<DigitalObject> searchResults = this.searchAll(query);
            if (searchResults.size()==1){
                schema = searchResults.get(0);
            } else{
                schema = null;
            }
            this.getMapDigitalObjectSchemas().put(name,Optional.ofNullable(schema));
        }
        return schema;
    }

    /***
     * Function that returns the user for the searched username
     * @param username username of the user we want to returned
     * @return Digital object with the searched user or null if there isn't any user with the username searched
     * or the name is not unique
     * @throws DigitalObjectRepositoryException
     */
    public DigitalObject getUserByUsername(String username) throws DigitalObjectRepositoryException {
        String query = "type:User AND /username:" + escapeQueryParamValue(username);
        List<DigitalObject> searchResults = this.searchAll(query);
        if (searchResults.size()==1){
            return searchResults.get(0);
        } else{
            return null;
        }
    }

    /***
     * Function that returns the digital specimen in the repository by its scientific name, institution code and physical specimen id
     * @param scientificName scientific name
     * @param institutionCode institution code
     * @param physicalSpecimenId physical specimen id
     * @return Digital object with the searched digital specimen or null if there isn't any digital specimen with the
     * criteria searched or there are more than one
     * @throws DigitalObjectRepositoryException
     */
    public DigitalObject getDigitalSpecimen(String scientificName, String institutionCode, String physicalSpecimenId) throws DigitalObjectRepositoryException {
        String query = "type:DigitalSpecimen "
                + " AND /scientificName:" + escapeQueryParamValue(scientificName)
                + " AND /institutionCode:" + escapeQueryParamValue(institutionCode)
                + " AND /physicalSpecimenId:" + escapeQueryParamValue(physicalSpecimenId);
        List<DigitalObject> searchResults = this.searchAll(query);
        if (searchResults.size()==1){
            return searchResults.get(0);
        } else{
            return null;
        }
    }

    /***
     * Function that returns all the digital specimens in the repository that has the property received as parameter
     * @param propertyName property to retrieve those digital specimens with this property, eg "catOfLifeReference"
     * @return list of all the digital specimens in the repository that has the property received as parameter
     * @throws DigitalObjectRepositoryException
     */
    public  List<DigitalObject> getDigitalSpecimensWithProperty(String propertyName) throws DigitalObjectRepositoryException {
        String query = "type:DigitalSpecimen AND /"+propertyName + ":[* TO *]";
        return this.searchAll(query);
    }

    /***
     * Function that returns all the digital specimens that match the gbifID passed as parameter
     * @param gbifId
     * @return List all the digital specimens that match the gbifID passed as parameter
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getDigitalSpecimensByGbifId(String gbifId) throws DigitalObjectRepositoryException {
        String query = "type:DigitalSpecimen AND /gbifId:" + escapeQueryParamValue(gbifId);
        return this.searchAll(query);
    }

    /**
     * Function that returns all the digital specimens created by the user received as parameter
     * @param username username
     * @return List of all the digital specimens created by the user received as parameter
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getDigitalSpecimensCreatedBy(String username) throws DigitalObjectRepositoryException{
        return this.getDigitalSpecimensByUserTypeProperty("metadata/createdBy",username);
    }

    /**
     * Function that returns all the digital specimens modified by the user received as parameter
     * @param username username
     * @return List of all the digital specimens modified by the user received as parameter
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getDigitalSpecimensModifiedBy(String username) throws DigitalObjectRepositoryException{
        return this.getDigitalSpecimensByUserTypeProperty("metadata/modifiedBy",username);
    }

    /***
     * Return all digital specimens created between the period range defined in the parameters
     * @param startDatetime start of the search period
     * @param endDatetime end of the search period
     * @return List of all digital specimens created in the period range defined in the parameters
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getDigitalSpecimensCreatedBetweenDateRange(ZonedDateTime startDatetime, ZonedDateTime endDatetime) throws DigitalObjectRepositoryException {
        String query = getQueryDigitalSpecimensByDateTypeProperty("metadata/createdOn",startDatetime,endDatetime);
        return this.searchAll(query);
    }

    /**
     * Function that returns all digital specimens created in the last number of days defined by parameter days
     * @param days number of days to search for digital specimens created in this period
     * @return List of all digital specimens created in the last number of days defined by parameter days
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getDigitalSpecimensCreatedRecently(Integer days) throws DigitalObjectRepositoryException {
        String query = getQueryDigitalSpecimensByDateTypeProperty("metadata/createdOn",days);
        return this.searchAll(query);
    }

    /***
     * Function that returns all the digital specimens created since the given date
     * @param startDatetime
     * @return List of all the digital specimens created since the given date
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getDigitalSpecimensCreatedSince(ZonedDateTime startDatetime) throws DigitalObjectRepositoryException {
        String query = getQueryDigitalSpecimensByDateTypeProperty("metadata/createdOn",startDatetime,null);
        return this.searchAll(query);
    }

    /***
     * Function that returns all the digital specimens created until the given date
     * @param endDatetime
     * @return List of all the digital specimens created upto the given date
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getDigitalSpecimensCreatedUntil(ZonedDateTime endDatetime) throws DigitalObjectRepositoryException {
        String query = getQueryDigitalSpecimensByDateTypeProperty("metadata/createdOn",null,endDatetime);
        return this.searchAll(query);
    }

    /***
     * Return all digital specimens modified between the period range defined in the parameters
     * @param startDatetime start of the search period
     * @param endDatetime end of the search period
     * @return List of all digital specimens modified in the period range defined in the parameters
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getDigitalSpecimensModifiedBetweenDateRange(ZonedDateTime startDatetime, ZonedDateTime endDatetime) throws DigitalObjectRepositoryException {
        String query = getQueryDigitalSpecimensByDateTypeProperty("metadata/modifiedOn",startDatetime,endDatetime);
        return this.searchAll(query);
    }

    /**
     * Return all digital specimens modified in the last number of days defined by parameter days
     * @param days number of days to search for digital specimens modified in this period
     * @return List of all digital specimens modified in the last number of days defined by parameter days
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getDigitalSpecimensModifiedRecently(Integer days) throws DigitalObjectRepositoryException {
        String query = getQueryDigitalSpecimensByDateTypeProperty("metadata/modifiedOn",days);
        return this.searchAll(query);
    }

    /***
     * Function that returns all the digital specimens modified since the given date
     * @param startDatetime
     * @return List of all the digital specimens modified since the given date
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getDigitalSpecimensModifiedSince(ZonedDateTime startDatetime) throws DigitalObjectRepositoryException {
        String query = getQueryDigitalSpecimensByDateTypeProperty("metadata/modifiedOn",startDatetime,null);
        return this.searchAll(query);
    }

    /***
     * Function that returns all the digital specimens modified until the given date
     * @param endDatetime
     * @return List of all the digital specimens modified until the given date
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getDigitalSpecimensModifiedUntil(ZonedDateTime endDatetime) throws DigitalObjectRepositoryException {
        String query = getQueryDigitalSpecimensByDateTypeProperty("metadata/modifiedOn",null,endDatetime);
        return this.searchAll(query);
    }

    /***
     * Function that returns a list with all digital objects in the repository that satisfy the query criteria
     * @param query query using Lucene Query Syntax https://lucene.apache.org/core/2_9_4/queryparsersyntax.html
     *              Make sure characters are escaped similarly to the following example
     *              String query = "type:Schema AND /name:" + escapeQueryParamValue(name);
     * @return List of digital object that match search criteria
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> searchAll(String query) throws DigitalObjectRepositoryException{
        return searchAll(query,0,this.getDigitalObjectRepositoryInfo().getPageSize());
    }


    /***
     * Function to call the hello operation of the repository
     * @return Digital object with the hello response from the repository
     * @throws DigitalObjectRepositoryException
     */
    public DigitalObject hello() throws DigitalObjectRepositoryException {
        return this.hello(this.getDigitalObjectRepositoryInfo().getServiceId());
    }

    /***
     * Funtion that list the operations available in the digital repository
     * @return list the operations available in the digital repository
     * @throws DigitalObjectRepositoryException
     */
    public  List<String> listOperations() throws DigitalObjectRepositoryException {
        return this.listOperations(this.getDigitalObjectRepositoryInfo().getServiceId());
    }

    /***
     * Function that saves a digital specimen in the repository if the digital specimen passed as parameter is valid
     * according to the latest digital specimen schema.
     * If the digital specimen doesn't exist in the repository, this function will create it, and if the digital specimen
     * exits in the repository and it has changed, this function will update it.
     * @param ds
     * @return The digital specimen saved in the repository
     * @throws DigitalObjectRepositoryException
     */
    public DigitalObject saveDigitalSpecimen(DigitalObject ds) throws DigitalObjectRepositoryException {
        String institutionCode= DigitalSpecimenUtils.getStringPropertyFromDS(ds,"institutionCode");
        String physicalSpecimenId=DigitalSpecimenUtils.getStringPropertyFromDS(ds,"physicalSpecimenId");
        String scientificName=DigitalSpecimenUtils.getStringPropertyFromDS(ds,"scientificName");

        //Check if the digital specimen exist in the repository
        DigitalObject dsInRepository = this.getDigitalSpecimen(scientificName,institutionCode,physicalSpecimenId);

        //Check if the digital specimen is valid according to the latest schema before sending it to the repository
        if (!this.validateDigitalSpecimenAgainstSchema(ds,false)){
            throw new DigitalObjectRepositoryException("Warn","Digital specimen [" + scientificName + " || " + institutionCode
                    + " || "+ physicalSpecimenId + "] is not valid according to the schema");
        }

        DigitalObject dsSaved=null;
        if (dsInRepository==null) {
            //The ds is valid and it is not found yet in the repository => create it
            dsSaved = this.create(ds);
            dsSaved.attributes.addProperty("operation", DIGITAL_OBJECT_OPERATION.INSERT.name());
        } else{
            //The ds exists in repository => compare its contents with the one in the repository and if there are differences => update ds
            if (this.haveDigitalSpecimensGotSameContent(ds,dsInRepository)){
                throw new DigitalObjectRepositoryException("Warn","Content for digital specimen [" + scientificName
                        + " || " + institutionCode + " || "+ physicalSpecimenId + "] is identical to the content " +
                        "for digital specimen found in the repository " + dsInRepository.id + ". No operation will be performed");
            } else{
                dsInRepository.attributes.remove("content");
                dsInRepository.attributes.add("content",ds.attributes.getAsJsonObject("content"));
                dsInRepository.attributes.getAsJsonObject("content").addProperty("id",dsInRepository.id);
                dsSaved = this.update(dsInRepository);
                dsSaved.attributes.addProperty("operation", DIGITAL_OBJECT_OPERATION.UPDATE.name());
            }
        }

        return dsSaved;
    }


    public boolean haveDigitalSpecimensGotSameContent(DigitalObject leftDs, DigitalObject rightDs){
        MapDifference<String, Object> comparisonResult = this.compareContentDigitalObjects(leftDs,rightDs);
        return comparisonResult.areEqual();
    }

    /***
     * Function that creates a digital specimen in the repository if the digital specimen pass as parameter is valid
     * according to the latest digital specimen schema and there isn't already a digital specimen object in the repository
     * with the same institution code and physical specimen Id
     * @param ds
     * @return The digital specimen created
     * @throws DigitalObjectRepositoryException
     */
    public DigitalObject createDigitalSpecimen(DigitalObject ds) throws DigitalObjectRepositoryException {
        String institutionCode=DigitalSpecimenUtils.getStringPropertyFromDS(ds,"institutionCode");
        String physicalSpecimenId=DigitalSpecimenUtils.getStringPropertyFromDS(ds,"physicalSpecimenId");
        String scientificName=DigitalSpecimenUtils.getStringPropertyFromDS(ds,"scientificName");

        //Check if the digital specimen doesn't exist yet in the repository
        DigitalObject dsInRepository = this.getDigitalSpecimen(scientificName,institutionCode,physicalSpecimenId);
        if (dsInRepository!=null) {
            throw new DigitalObjectRepositoryException("Warn","Digital specimen [" + scientificName + " || "+ institutionCode + " || "+ physicalSpecimenId + "]" +
                    " already found in repository dsId: " + dsInRepository.id);
        }

        //Check if the digital specimen is valid according to the latest schema before sending it to the repository
        if (!this.validateDigitalSpecimenAgainstSchema(ds,false)){
            throw new DigitalObjectRepositoryException("Warn","Digital specimen [" + scientificName + " || " + institutionCode + " || "+ physicalSpecimenId + "]" +
                    " is not valid according to the schema");
        }

        //The ds is valid and it not stored yet in the repository => create it
        DigitalObject dsCreated = this.create(ds);

        return dsCreated;
    }

    /***
     * Function that update the digital specimen in the repository
     * @param ds
     * @return
     * @throws DigitalObjectRepositoryException
     */
    public DigitalObject updateDigitalSpecimen(DigitalObject ds) throws DigitalObjectRepositoryException {
        String institutionCode=DigitalSpecimenUtils.getStringPropertyFromDS(ds,"institutionCode");
        String physicalSpecimenId=DigitalSpecimenUtils.getStringPropertyFromDS(ds,"physicalSpecimenId");
        String scientificName=DigitalSpecimenUtils.getStringPropertyFromDS(ds,"scientificName");

        //Get digital specimen already stored in repository
        DigitalObject dsInRepository = this.getDigitalSpecimen(scientificName,institutionCode,physicalSpecimenId);
        if (dsInRepository==null){
            throw new DigitalObjectRepositoryException("Warn","Digital specimen [" + scientificName + " || " + institutionCode + " || "+ physicalSpecimenId + "] " +
                    " doesn't exist in the repository");
        }

        //Check if the digital specimen is valid according to the latest schema before sending it to the repository
        if (!this.validateDigitalSpecimenAgainstSchema(ds,false)){
            throw new DigitalObjectRepositoryException("Warn","Digital specimen [" + scientificName + " || " + institutionCode + " || "+ physicalSpecimenId + "] " +
                    "is not valid according to the schema");
        }

        //Compare the content with the digital specimen already stored in the repository and f there are differences => update ds
        if (this.haveDigitalSpecimensGotSameContent(ds,dsInRepository)){
            throw new DigitalObjectRepositoryException("Warn","Digital specimen [" + scientificName + " || " + institutionCode + " || "+ physicalSpecimenId + "] is identical" +
                    " to the digital specimen found in the repository " + dsInRepository.id);
        }

        dsInRepository.attributes.remove("content");
        dsInRepository.attributes.add("content",ds.attributes.getAsJsonObject("content"));
        dsInRepository.attributes.getAsJsonObject("content").addProperty("id",dsInRepository.id);
        DigitalObject dsUpdated = this.update(dsInRepository);

        return dsUpdated;
    }

    /***
     * Function that validates if the digital specimen passed as parameter matches the current digital specimen schema
     * @param ds digital specimen to check if it is valid according to the current digital specimen schema
     * @param checkRequiredId boolean to indicate if we should check if the digital specimen has or not the property "id"
     * @return boolean indicating if the digital specimen is valid according to the current digital specimen schema
     * @throws DigitalObjectRepositoryException
     */
    public boolean validateDigitalSpecimenAgainstSchema(DigitalObject ds, boolean checkRequiredId) throws DigitalObjectRepositoryException {
        return validateDigitalObjectAgainstSchema(ds,"DigitalSpecimen",checkRequiredId);
    }

    /**
     * Function that validates if the digital object passed as parameter matches the schema defined as second parameter
     * @param digitalObject digital object to check if it is valid according to the current schema
     * @param schemaName name of the schema to be used in the validation
     * @param checkRequiredId boolean to indicate if we should check if the digital object has or not the property "id"
     * @return boolean indicating if the digital object is valid according to the given schema
     * @throws DigitalObjectRepositoryException
     */
    public boolean validateDigitalObjectAgainstSchema(DigitalObject digitalObject, String schemaName, boolean checkRequiredId) throws DigitalObjectRepositoryException {
        DigitalObject dsSchema = this.getSchemaByName(schemaName);
        JsonObject dsSchemaJsonContent = dsSchema.attributes.getAsJsonObject("content").getAsJsonObject("schema");
        JsonObject dsJsonContent = digitalObject.attributes.getAsJsonObject("content");

        return JsonUtils.validateJsonAgainstSchema(dsJsonContent,dsSchemaJsonContent,checkRequiredId);
    }

    /***
     * Function that get the list of version of a given object
     * Note: This function use the CORDRA REST API as this functionality is not provided in DOIP yet
     * @param objectId
     * @return List of versions (digital objects) of a given object. The list of versions is sorted from the oldest to the most recent
     * @throws DigitalObjectRepositoryException
     */
    public List<DigitalObject> getVersionsOfObject(String objectId) throws DigitalObjectRepositoryException{
        try {
            List<DigitalObject> listDigitalObjects = null;
            List<VersionInfo> versions = this.getRestClient().getVersionsFor(objectId);
            if (versions!=null && versions.size()>0){
                versions.sort(Comparator.comparing(v -> v.publishedOn, Comparator.nullsLast(Long::compareTo)));
                listDigitalObjects = new ArrayList<>();
                DigitalObject previousVersion = null;
                for (VersionInfo version:versions) {
                    DigitalObject digitalObject = this.retrieve(version.id);
                    MapDifference<String, Object> comparisonResult = null;
                    if (previousVersion!=null){
                        comparisonResult = this.compareContentDigitalObjects(previousVersion,digitalObject);
                    }
                    digitalObject.attributes.add("comparisonAgainstPreviousVersion",JsonUtils.convertObjectToJsonElement(comparisonResult));
                    previousVersion=digitalObject;
                    listDigitalObjects.add(digitalObject);
                }
            }
            return listDigitalObjects;
        } catch (CordraException e) {
            throw DigitalObjectRepositoryException.convertCordraException(e);
        }
    }


    /***
     * Function that get the object as it was the the time specified
     * Note: This function use the CORDRA REST API as this functionality is not provided in DOIP yet
     * @param objectId
     * @param zonedDateTime datetime from when we want to retrieve the status of the digital object
     * @return Version of the object at the given time
     * @throws DigitalObjectRepositoryException
     */
    public DigitalObject getVersionOfObjectAtGivenTime(String objectId, ZonedDateTime zonedDateTime) throws DigitalObjectRepositoryException{
        if (zonedDateTime==null){
            zonedDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault());
        }
        Long datetimeEpoch = zonedDateTime.toInstant().toEpochMilli();
        return this.getVersionOfObjectAtGivenTime(objectId,datetimeEpoch);
    }

    /**
     * Function that get the object as it was the the time specified
     * @param objectId
     * @param utcDatetime string of utc time in ISO 8601
     * @return Version of the object at the given time
     * @throws DigitalObjectRepositoryException
     */
    public DigitalObject getVersionOfObjectAtGivenTime(String objectId, String utcDatetime) throws DigitalObjectRepositoryException {
        Long timestamp;
        if (StringUtils.isNotBlank(utcDatetime)){
            timestamp = Instant.parse(utcDatetime).toEpochMilli();
        } else{
            timestamp = Instant.now().toEpochMilli();
        }
        return this.getVersionOfObjectAtGivenTime(objectId,timestamp);
    }

    /***
     * Function that get the object as it was the the time specified
     * Note: This function use the CORDRA REST API as this functionality is not provided in DOIP yet
     * @param objectId
     * @param datetimeEpoch datetime from when we want to retrieve the status of the digital object
     * @return Version of the object at the given time
     * @throws DigitalObjectRepositoryException
     */
    public DigitalObject getVersionOfObjectAtGivenTime(String objectId, Long datetimeEpoch) throws DigitalObjectRepositoryException{
        try {
            DigitalObject digitalObjectAtGivenTime = null;
            List<VersionInfo> versions = this.getRestClient().getVersionsFor(objectId);

            if (versions!=null && versions.size()>0){
                //The list of versions is sorted from the oldest to the most recent
                versions.sort(Comparator.comparing(v -> v.publishedOn, Comparator.nullsLast(Long::compareTo)));
                int versionPos=-1;
                for (VersionInfo version:versions) {
                    if (version.publishedOn!=null && version.publishedOn>datetimeEpoch){
                        break;
                    } else{
                        versionPos++;
                    }
                }

                if (versionPos!=-1){
                    digitalObjectAtGivenTime = this.retrieve(versions.get(versionPos).id);
                } else{
                    //The search date is before the first version was created. Although it is still possible that the object
                    // existed at that time, as we only create the version just before the first modification is done
                    DigitalObject firstVersionObject = this.retrieve(versions.get(0).id);
                    if (firstVersionObject.attributes.getAsJsonObject("metadata").get("createdOn").getAsLong()<=datetimeEpoch){
                        digitalObjectAtGivenTime = firstVersionObject;
                    }
                }

                if (digitalObjectAtGivenTime!=null){
                    //Calculate differences with current version
                    if (!digitalObjectAtGivenTime.id.equalsIgnoreCase(objectId)){
                        DigitalObject currentObject = this.retrieve(objectId);
                        MapDifference<String, Object> mapDifference =  this.compareContentDigitalObjects(digitalObjectAtGivenTime,currentObject);
                        JsonObject comparisonResult = (JsonObject)JsonUtils.convertObjectToJsonElement(mapDifference);
                        comparisonResult.remove("onBoth");
                        digitalObjectAtGivenTime.attributes.add("comparisonAgainstCurrentVersion",JsonUtils.convertObjectToJsonElement(comparisonResult));
                    }
                }
            }
            return digitalObjectAtGivenTime;
        } catch (CordraException e) {
            throw DigitalObjectRepositoryException.convertCordraException(e);
        }
    }

    /**
     * Function that creates a version of the digital object id received as parameter
     * Note: Note: This function use the CORDRA REST API as this functionality is not provided in DOIP yet
     * @param objectId
     * @return Digital object resulting of the versioning
     * @throws DigitalObjectRepositoryException
     */
    public DigitalObject publishVersion(String objectId) throws DigitalObjectRepositoryException {
        try {
            DigitalObject digitalObject=null;
            VersionInfo version = this.getRestClient().publishVersion(objectId,null,false);
            if (version!=null) digitalObject = this.retrieve(version.id);
            return digitalObject;
        } catch (CordraException e) {
            throw DigitalObjectRepositoryException.convertCordraException(e);
        }
    }

    /**
     * Function that get all entries that match the query. It using recursion to iterate through all the pages
     * returned by the Digital Object repository
     * @param query query to do the search
     * @param pageNumber page to get it
     * @param pageSize number of element to get per page
     * @return All digital objects in the repository entries that match the query
     * @throws DigitalObjectRepositoryException
     */
    private List<DigitalObject> searchAll(String query, Integer pageNumber, Integer pageSize) throws DigitalObjectRepositoryException{
        List<DigitalObject> results = new ArrayList<DigitalObject>();
        QueryParams queryParams = new QueryParams(pageNumber, pageSize);
        String digitalObjectRepositoryServiceId = this.getDigitalObjectRepositoryInfo().getServiceId();
        SearchResults<DigitalObject> searchResults = this.search(digitalObjectRepositoryServiceId,query, queryParams);
        searchResults.iterator().forEachRemaining(results::add);
        if (results.size()==pageSize){
            results.addAll(searchAll(query,++pageNumber,pageSize));
        }
        return results;
    }

    /**
     * Get all the digital specimens that its userTypePropertyName passed as parameter (eg: metadata/createdBy)
     * correspond to the user name
     * @param userTypePropertyName metadataPropertyName (createdBy, modifiedBy)
     * @param username username
     * @return List of digital specimens that match the search criteria
     * @throws DigitalObjectRepositoryException
     */
    private List<DigitalObject> getDigitalSpecimensByUserTypeProperty(String userTypePropertyName, String username) throws DigitalObjectRepositoryException{
        List<DigitalObject> results = new ArrayList<DigitalObject>();
        String userId=null;
        if (username.equals("admin")){
            userId=username;
        } else{
            DigitalObject user = this.getUserByUsername(username);
            if (user!=null){
                userId = user.id;
            }
        }
        if (userId!=null){
            String query = "type:DigitalSpecimen AND " + userTypePropertyName + ":" + escapeQueryParamValue(userId);
            results = this.searchAll(query);
        }

        return results;
    }

    /**
     * Get all the digital specimens that its dateTimePropertyName passed as first parameter is inside the datetime range
     * defined by the second and third parameter
     * @param dateTimePropertyName name of the property that we want to use to filter the result by datetime range
     * @param startDatetime start date time
     * @param endDatetime end date time
     * @return List of digital specimens that match the search criteria
     */
    private String getQueryDigitalSpecimensByDateTypeProperty(String dateTimePropertyName, ZonedDateTime startDatetime, ZonedDateTime endDatetime){
        String startEpoch="*";
        String endEpoch="*";

        if (startDatetime!=null){
            startEpoch = Long.toString(startDatetime.toInstant().toEpochMilli());
        }
        if (endDatetime!=null){
            endEpoch = Long.toString(endDatetime.toInstant().toEpochMilli());
        }

        return "type:DigitalSpecimen AND " + dateTimePropertyName  + ":[" + startEpoch + " TO " + endEpoch +"]";
    }

    /**
     * Get all the digital specimens that its dateTimePropertyName passed as first parameter is inside the datetime range
     * defined by the number of days before the current date and the current date
     * @param dateTimePropertyName name of the property that we want to use to filter the result by datetime range
     * @param days number of days before the current data to be use as the startDateTime
     * @return List of digital specimens that match the search criteria
     */
    private String getQueryDigitalSpecimensByDateTypeProperty(String dateTimePropertyName, Integer days){
        ZonedDateTime endDatetime = LocalDateTime.now().atZone(ZoneId.systemDefault());
        ZonedDateTime startDatetime = LocalDateTime.now().minusDays(days).atZone(ZoneId.systemDefault());

        return getQueryDigitalSpecimensByDateTypeProperty(dateTimePropertyName,startDatetime,endDatetime);
    }

    /**
     * Function to escape a query param value
     * @param paramValue value to escape
     * @return value escaped
     */
    private String escapeQueryParamValue(String paramValue){
        return "\"" + QueryParserBase.escape(paramValue) + "\"";
    }

    /**
     * Function that get the difference in the content of 2 digital specimens
     * Note: it removes their "id" for comparison
     * @param leftDs left digital specimen
     * @param rightDs left digital specimen
     * @return MapDifference with the result of the comparison
     */
    private MapDifference<String, Object> compareContentDigitalObjects(DigitalObject leftDs, DigitalObject rightDs){
        JsonObject leftDsContent = leftDs.attributes.getAsJsonObject("content");
        JsonObject rightDsContent = rightDs.attributes.getAsJsonObject("content");
        //Exclude DS ids for comparison
        String leftDsId = leftDsContent.has("id")?leftDsContent.get("id").getAsString():null;
        String rightDsId = rightDsContent.has("id")?rightDsContent.get("id").getAsString():null;
        leftDsContent.remove("id");
        rightDsContent.remove("id");
        MapDifference<String, Object> comparisonResult = JsonUtils.compareJsonElements(leftDsContent,rightDsContent);
        //Add the DS ids back to the content
        if (StringUtils.isNotBlank(leftDsId)) leftDsContent.addProperty("id",leftDsId);
        if (StringUtils.isNotBlank(rightDsId)) rightDsContent.addProperty("id",rightDsId);
        return comparisonResult;
    }


    /*****************************************************************************************************************/
    /* Methods to act as facade for DOIP client in order to avoid passing all the times the authInfo and serviceInfo */
    /*****************************************************************************************************************/

    /**
     * Function that release the resource taken by the digital object repository client
     */
    public synchronized void close() {
        this.getDoipClient().close();
    }


    public DoipClientResponse performOperation(String targetId, String operationId, JsonObject attributes) throws DigitalObjectRepositoryException {
        try{
            return this.getDoipClient().performOperation(targetId,operationId,this.getAuthInfo(),attributes,this.getServiceInfo());
        } catch (DoipException e){
            throw DigitalObjectRepositoryException.convertDoipException(e);
        }

    }

    public DoipClientResponse performOperation(String targetId, String operationId, JsonObject attributes, JsonElement input) throws DigitalObjectRepositoryException {
        try{
            return this.getDoipClient().performOperation(targetId,operationId,this.getAuthInfo(),attributes,input,this.getServiceInfo());
        } catch (DoipException e){
            throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public DoipClientResponse performOperation(String targetId, String operationId, JsonObject attributes, InDoipMessage input) throws DigitalObjectRepositoryException {
        try{
            return this.getDoipClient().performOperation(targetId,operationId,this.getAuthInfo(),attributes,input,this.getServiceInfo());
        } catch (DoipException e){
            throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public DoipClientResponse performOperation(DoipRequestHeaders headers, InDoipMessage input) throws DigitalObjectRepositoryException {
        try {
            return this.getDoipClient().performOperation(headers, input, this.getServiceInfo());
        } catch (DoipException e){
            throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public DigitalObject create(DigitalObject dobj) throws DigitalObjectRepositoryException {
        try {
            return this.getDoipClient().create(dobj,this.getAuthInfo(),this.getServiceInfo());
        } catch (DoipException e){
            throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public DigitalObject update(DigitalObject dobj) throws DigitalObjectRepositoryException {
        try {
            return this.getDoipClient().update(dobj,this.getAuthInfo(),this.getServiceInfo());
        } catch (DoipException e) {
           throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public DigitalObject retrieve(String targetId) throws DigitalObjectRepositoryException {
        try {
            return this.getDoipClient().retrieve(targetId, false, this.getAuthInfo(), this.getServiceInfo());
        } catch (DoipException e) {
           throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public DigitalObject retrieve(String targetId, boolean includeElementData) throws DigitalObjectRepositoryException {
        try {
            return this.getDoipClient().retrieve(targetId,includeElementData,this.getAuthInfo(),this.getServiceInfo());
        } catch (DoipException e) {
           throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public void delete(String targetId) throws DigitalObjectRepositoryException {
        try {
            this.getDoipClient().delete(targetId,this.getAuthInfo(),this.getServiceInfo());
        } catch (DoipException e) {
           throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public List<String> listOperations(String targetId) throws DigitalObjectRepositoryException {
        try {
            return this.getDoipClient().listOperations(targetId,this.getAuthInfo(),this.getServiceInfo());
        } catch (DoipException e) {
           throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public SearchResults<String> searchIds(String targetId, String query, QueryParams params) throws DigitalObjectRepositoryException {
        try {
            return this.getDoipClient().searchIds(targetId,query,params,this.getAuthInfo(),this.getServiceInfo());
        } catch (DoipException e) {
           throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public SearchResults<DigitalObject> search(String targetId, String query, QueryParams params) throws DigitalObjectRepositoryException {
        try {
            return this.getDoipClient().search(targetId,query,params,this.getAuthInfo(),this.getServiceInfo());
        } catch (DoipException e) {
           throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public DigitalObject hello(String targetId) throws DigitalObjectRepositoryException {
        try {
            return this.getDoipClient().hello(targetId,this.getAuthInfo(),this.getServiceInfo());
        } catch (DoipException e) {
           throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public InputStream retrieveElement(String targetId, String elementId) throws DigitalObjectRepositoryException {
        try {
            return this.getDoipClient().retrieveElement(targetId,elementId,this.getAuthInfo(),this.getServiceInfo());
        } catch (DoipException e) {
           throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }

    public InputStream retrievePartialElement(String targetId, String elementId, Long start, Long end) throws DigitalObjectRepositoryException {
        try {
            return this.getDoipClient().retrievePartialElement(targetId,elementId,start,end,this.getAuthInfo(),this.getServiceInfo());
        } catch (DoipException e) {
           throw DigitalObjectRepositoryException.convertDoipException(e);
        }
    }
}
