/*
 * The EBI Search: RESTful Web services
 * This is an API documentation for [EBI Search](https://www.ebi.ac.uk/ebisearch) RESTful Web services.
 *
 * The version of the OpenAPI document: all
 * Contact: www-prod@ebi.ac.uk
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package eu.dissco.digitisers.clients.ebi.openapi.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import eu.dissco.digitisers.clients.ebi.openapi.model.WSFacet;
import eu.dissco.digitisers.clients.ebi.openapi.model.WSField;
import eu.dissco.digitisers.clients.ebi.openapi.model.WSHighlight;
import eu.dissco.digitisers.clients.ebi.openapi.model.WSURL;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WSEntry
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2019-11-01T16:03:02.863Z[GMT]")
public class WSEntry {
  public static final String SERIALIZED_NAME_ACC = "acc";
  @SerializedName(SERIALIZED_NAME_ACC)
  private String acc;

  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private String id;

  public static final String SERIALIZED_NAME_SOURCE = "source";
  @SerializedName(SERIALIZED_NAME_SOURCE)
  private String source;

  public static final String SERIALIZED_NAME_SCORE = "score";
  @SerializedName(SERIALIZED_NAME_SCORE)
  private Float score;

  public static final String SERIALIZED_NAME_FIELDS = "fields";
  @SerializedName(SERIALIZED_NAME_FIELDS)
  private List<WSField> fields = null;

  public static final String SERIALIZED_NAME_FIELD_U_R_LS = "fieldURLs";
  @SerializedName(SERIALIZED_NAME_FIELD_U_R_LS)
  private List<WSURL> fieldURLs = null;

  public static final String SERIALIZED_NAME_VIEW_U_R_LS = "viewURLs";
  @SerializedName(SERIALIZED_NAME_VIEW_U_R_LS)
  private List<WSURL> viewURLs = null;

  public static final String SERIALIZED_NAME_REFERENCE_COUNT = "referenceCount";
  @SerializedName(SERIALIZED_NAME_REFERENCE_COUNT)
  private Long referenceCount;

  public static final String SERIALIZED_NAME_REFERENCES = "references";
  @SerializedName(SERIALIZED_NAME_REFERENCES)
  private List<WSEntry> references = null;

  public static final String SERIALIZED_NAME_VIEW_U_R_L_LIST = "viewURLList";
  @SerializedName(SERIALIZED_NAME_VIEW_U_R_L_LIST)
  private List<WSURL> viewURLList = null;

  public static final String SERIALIZED_NAME_FIELD_MAP = "fieldMap";
  @SerializedName(SERIALIZED_NAME_FIELD_MAP)
  private Map<String, WSField> fieldMap = null;

  public static final String SERIALIZED_NAME_VEIW_U_R_LS = "veiwURLs";
  @SerializedName(SERIALIZED_NAME_VEIW_U_R_LS)
  private List<WSURL> veiwURLs = null;

  public static final String SERIALIZED_NAME_FIRSTFIELDURL = "firstfieldurl";
  @SerializedName(SERIALIZED_NAME_FIRSTFIELDURL)
  private WSURL firstfieldurl;

  public static final String SERIALIZED_NAME_ENTRY_URL = "entryUrl";
  @SerializedName(SERIALIZED_NAME_ENTRY_URL)
  private WSURL entryUrl;

  public static final String SERIALIZED_NAME_HIGHLIGHTS = "highlights";
  @SerializedName(SERIALIZED_NAME_HIGHLIGHTS)
  private List<WSHighlight> highlights = null;

  public static final String SERIALIZED_NAME_REFERENCE_FACETS = "referenceFacets";
  @SerializedName(SERIALIZED_NAME_REFERENCE_FACETS)
  private List<WSFacet> referenceFacets = null;


  public WSEntry acc(String acc) {
    
    this.acc = acc;
    return this;
  }

   /**
   * Get acc
   * @return acc
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public String getAcc() {
    return acc;
  }


  public void setAcc(String acc) {
    this.acc = acc;
  }


  public WSEntry id(String id) {
    
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public String getId() {
    return id;
  }


  public void setId(String id) {
    this.id = id;
  }


  public WSEntry source(String source) {
    
    this.source = source;
    return this;
  }

   /**
   * Get source
   * @return source
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public String getSource() {
    return source;
  }


  public void setSource(String source) {
    this.source = source;
  }


  public WSEntry score(Float score) {
    
    this.score = score;
    return this;
  }

   /**
   * Get score
   * @return score
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public Float getScore() {
    return score;
  }


  public void setScore(Float score) {
    this.score = score;
  }


  public WSEntry fields(List<WSField> fields) {

    this.fields = fields;
    return this;
  }

  public WSEntry addFieldsItem(WSField fieldsItem) {
    if (this.fields == null) {
      this.fields = new ArrayList<WSField>();
    }
    this.fields.add(fieldsItem);
    return this;
  }

   /**
   * Get fields
   * @return fields
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<WSField> getFields() {
    return fields;
  }


  public void setFields(List<WSField> fields) {
    this.fields = fields;
  }


  public WSEntry fieldURLs(List<WSURL> fieldURLs) {
    
    this.fieldURLs = fieldURLs;
    return this;
  }

  public WSEntry addFieldURLsItem(WSURL fieldURLsItem) {
    if (this.fieldURLs == null) {
      this.fieldURLs = new ArrayList<WSURL>();
    }
    this.fieldURLs.add(fieldURLsItem);
    return this;
  }

   /**
   * Get fieldURLs
   * @return fieldURLs
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<WSURL> getFieldURLs() {
    return fieldURLs;
  }


  public void setFieldURLs(List<WSURL> fieldURLs) {
    this.fieldURLs = fieldURLs;
  }


  public WSEntry viewURLs(List<WSURL> viewURLs) {
    
    this.viewURLs = viewURLs;
    return this;
  }

  public WSEntry addViewURLsItem(WSURL viewURLsItem) {
    if (this.viewURLs == null) {
      this.viewURLs = new ArrayList<WSURL>();
    }
    this.viewURLs.add(viewURLsItem);
    return this;
  }

   /**
   * Get viewURLs
   * @return viewURLs
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<WSURL> getViewURLs() {
    return viewURLs;
  }


  public void setViewURLs(List<WSURL> viewURLs) {
    this.viewURLs = viewURLs;
  }


  public WSEntry referenceCount(Long referenceCount) {
    
    this.referenceCount = referenceCount;
    return this;
  }

   /**
   * Get referenceCount
   * @return referenceCount
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public Long getReferenceCount() {
    return referenceCount;
  }


  public void setReferenceCount(Long referenceCount) {
    this.referenceCount = referenceCount;
  }


  public WSEntry references(List<WSEntry> references) {
    
    this.references = references;
    return this;
  }

  public WSEntry addReferencesItem(WSEntry referencesItem) {
    if (this.references == null) {
      this.references = new ArrayList<WSEntry>();
    }
    this.references.add(referencesItem);
    return this;
  }

   /**
   * Get references
   * @return references
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<WSEntry> getReferences() {
    return references;
  }


  public void setReferences(List<WSEntry> references) {
    this.references = references;
  }


  public WSEntry viewURLList(List<WSURL> viewURLList) {
    
    this.viewURLList = viewURLList;
    return this;
  }

  public WSEntry addViewURLListItem(WSURL viewURLListItem) {
    if (this.viewURLList == null) {
      this.viewURLList = new ArrayList<WSURL>();
    }
    this.viewURLList.add(viewURLListItem);
    return this;
  }

   /**
   * Get viewURLList
   * @return viewURLList
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<WSURL> getViewURLList() {
    return viewURLList;
  }


  public void setViewURLList(List<WSURL> viewURLList) {
    this.viewURLList = viewURLList;
  }


  public WSEntry fieldMap(Map<String, WSField> fieldMap) {
    
    this.fieldMap = fieldMap;
    return this;
  }

  public WSEntry putFieldMapItem(String key, WSField fieldMapItem) {
    if (this.fieldMap == null) {
      this.fieldMap = new HashMap<String, WSField>();
    }
    this.fieldMap.put(key, fieldMapItem);
    return this;
  }

   /**
   * Get fieldMap
   * @return fieldMap
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public Map<String, WSField> getFieldMap() {
    return fieldMap;
  }


  public void setFieldMap(Map<String, WSField> fieldMap) {
    this.fieldMap = fieldMap;
  }


  public WSEntry veiwURLs(List<WSURL> veiwURLs) {
    
    this.veiwURLs = veiwURLs;
    return this;
  }

  public WSEntry addVeiwURLsItem(WSURL veiwURLsItem) {
    if (this.veiwURLs == null) {
      this.veiwURLs = new ArrayList<WSURL>();
    }
    this.veiwURLs.add(veiwURLsItem);
    return this;
  }

   /**
   * Get veiwURLs
   * @return veiwURLs
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<WSURL> getVeiwURLs() {
    return veiwURLs;
  }


  public void setVeiwURLs(List<WSURL> veiwURLs) {
    this.veiwURLs = veiwURLs;
  }


  public WSEntry firstfieldurl(WSURL firstfieldurl) {
    
    this.firstfieldurl = firstfieldurl;
    return this;
  }

   /**
   * Get firstfieldurl
   * @return firstfieldurl
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public WSURL getFirstfieldurl() {
    return firstfieldurl;
  }


  public void setFirstfieldurl(WSURL firstfieldurl) {
    this.firstfieldurl = firstfieldurl;
  }


  public WSEntry entryUrl(WSURL entryUrl) {
    
    this.entryUrl = entryUrl;
    return this;
  }

   /**
   * Get entryUrl
   * @return entryUrl
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public WSURL getEntryUrl() {
    return entryUrl;
  }


  public void setEntryUrl(WSURL entryUrl) {
    this.entryUrl = entryUrl;
  }


  public WSEntry highlights(List<WSHighlight> highlights) {
    
    this.highlights = highlights;
    return this;
  }

  public WSEntry addHighlightsItem(WSHighlight highlightsItem) {
    if (this.highlights == null) {
      this.highlights = new ArrayList<WSHighlight>();
    }
    this.highlights.add(highlightsItem);
    return this;
  }

   /**
   * Get highlights
   * @return highlights
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<WSHighlight> getHighlights() {
    return highlights;
  }


  public void setHighlights(List<WSHighlight> highlights) {
    this.highlights = highlights;
  }


  public WSEntry referenceFacets(List<WSFacet> referenceFacets) {
    
    this.referenceFacets = referenceFacets;
    return this;
  }

  public WSEntry addReferenceFacetsItem(WSFacet referenceFacetsItem) {
    if (this.referenceFacets == null) {
      this.referenceFacets = new ArrayList<WSFacet>();
    }
    this.referenceFacets.add(referenceFacetsItem);
    return this;
  }

   /**
   * Get referenceFacets
   * @return referenceFacets
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<WSFacet> getReferenceFacets() {
    return referenceFacets;
  }


  public void setReferenceFacets(List<WSFacet> referenceFacets) {
    this.referenceFacets = referenceFacets;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WSEntry wsEntry = (WSEntry) o;
    return Objects.equals(this.acc, wsEntry.acc) &&
        Objects.equals(this.id, wsEntry.id) &&
        Objects.equals(this.source, wsEntry.source) &&
        Objects.equals(this.score, wsEntry.score) &&
        Objects.equals(this.fields, wsEntry.fields) &&
        Objects.equals(this.fieldURLs, wsEntry.fieldURLs) &&
        Objects.equals(this.viewURLs, wsEntry.viewURLs) &&
        Objects.equals(this.referenceCount, wsEntry.referenceCount) &&
        Objects.equals(this.references, wsEntry.references) &&
        Objects.equals(this.viewURLList, wsEntry.viewURLList) &&
        Objects.equals(this.fieldMap, wsEntry.fieldMap) &&
        Objects.equals(this.veiwURLs, wsEntry.veiwURLs) &&
        Objects.equals(this.firstfieldurl, wsEntry.firstfieldurl) &&
        Objects.equals(this.entryUrl, wsEntry.entryUrl) &&
        Objects.equals(this.highlights, wsEntry.highlights) &&
        Objects.equals(this.referenceFacets, wsEntry.referenceFacets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(acc, id, source, score, fields, fieldURLs, viewURLs, referenceCount, references, viewURLList, fieldMap, veiwURLs, firstfieldurl, entryUrl, highlights, referenceFacets);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WSEntry {\n");
    sb.append("    acc: ").append(toIndentedString(acc)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    score: ").append(toIndentedString(score)).append("\n");
    sb.append("    fields: ").append(toIndentedString(fields)).append("\n");
    sb.append("    fieldURLs: ").append(toIndentedString(fieldURLs)).append("\n");
    sb.append("    viewURLs: ").append(toIndentedString(viewURLs)).append("\n");
    sb.append("    referenceCount: ").append(toIndentedString(referenceCount)).append("\n");
    sb.append("    references: ").append(toIndentedString(references)).append("\n");
    sb.append("    viewURLList: ").append(toIndentedString(viewURLList)).append("\n");
    sb.append("    fieldMap: ").append(toIndentedString(fieldMap)).append("\n");
    sb.append("    veiwURLs: ").append(toIndentedString(veiwURLs)).append("\n");
    sb.append("    firstfieldurl: ").append(toIndentedString(firstfieldurl)).append("\n");
    sb.append("    entryUrl: ").append(toIndentedString(entryUrl)).append("\n");
    sb.append("    highlights: ").append(toIndentedString(highlights)).append("\n");
    sb.append("    referenceFacets: ").append(toIndentedString(referenceFacets)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

