/*
 *  Copyright Nomura Research Institute, Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package jp.openstandia.connector.atlassian;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AtlassianGuardUserModel {
    protected static final String USER = "urn:ietf:params:scim:schemas:core:2.0:User";

    public List<String> schemas = Collections.singletonList(USER);
    public String id; // auto generated
    public String userName;
    public Name name;
    public String displayName;
    public String nickName;
    public String title;
    public String preferredLanguage;
    public String timezone;
    public Boolean active;
    public List<Email> emails;
    public List<PhoneNumber> phoneNumbers;
    public List<Group> groups;
    public Meta meta;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Name {
        public String formatted;
        public String familyName;
        public String givenName;
        public String middleName;
        public String honorificPrefix;
        public String honorificSuffix;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Email {
        public String value;
        public String display;
        public String type;
        public Boolean primary;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PhoneNumber {
        public String value;
        public String display;
        public String type;
        public Boolean primary;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Group {
        public String value;
        @JsonProperty("$ref")
        public String ref;
        public String display;
        public String type;
        public Boolean primary;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        public String resourceType;
        public String created;
        public String lastModified;
        public String location;
    }
}
