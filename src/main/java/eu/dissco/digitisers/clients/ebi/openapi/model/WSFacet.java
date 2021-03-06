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
import eu.dissco.digitisers.clients.ebi.openapi.model.WSFacetValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * WSFacet
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2019-11-01T16:03:02.863Z[GMT]")
public class WSFacet {
  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private String id;

  public static final String SERIALIZED_NAME_LABEL = "label";
  @SerializedName(SERIALIZED_NAME_LABEL)
  private String label;

  public static final String SERIALIZED_NAME_TOTAL = "total";
  @SerializedName(SERIALIZED_NAME_TOTAL)
  private Double total;

  public static final String SERIALIZED_NAME_FACET_VALUES = "facetValues";
  @SerializedName(SERIALIZED_NAME_FACET_VALUES)
  private List<WSFacetValue> facetValues = null;


  public WSFacet id(String id) {
    
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


  public WSFacet label(String label) {
    
    this.label = label;
    return this;
  }

   /**
   * Get label
   * @return label
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public String getLabel() {
    return label;
  }


  public void setLabel(String label) {
    this.label = label;
  }


  public WSFacet total(Double total) {
    
    this.total = total;
    return this;
  }

   /**
   * Get total
   * @return total
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public Double getTotal() {
    return total;
  }


  public void setTotal(Double total) {
    this.total = total;
  }


  public WSFacet facetValues(List<WSFacetValue> facetValues) {
    
    this.facetValues = facetValues;
    return this;
  }

  public WSFacet addFacetValuesItem(WSFacetValue facetValuesItem) {
    if (this.facetValues == null) {
      this.facetValues = new ArrayList<WSFacetValue>();
    }
    this.facetValues.add(facetValuesItem);
    return this;
  }

   /**
   * Get facetValues
   * @return facetValues
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<WSFacetValue> getFacetValues() {
    return facetValues;
  }


  public void setFacetValues(List<WSFacetValue> facetValues) {
    this.facetValues = facetValues;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WSFacet wsFacet = (WSFacet) o;
    return Objects.equals(this.id, wsFacet.id) &&
        Objects.equals(this.label, wsFacet.label) &&
        Objects.equals(this.total, wsFacet.total) &&
        Objects.equals(this.facetValues, wsFacet.facetValues);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, label, total, facetValues);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WSFacet {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    facetValues: ").append(toIndentedString(facetValues)).append("\n");
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

