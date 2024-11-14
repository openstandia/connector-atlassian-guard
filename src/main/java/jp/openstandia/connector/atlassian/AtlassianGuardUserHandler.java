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

import jp.openstandia.connector.util.ObjectHandler;
import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;

import java.util.ArrayList;
import java.util.Set;

import static jp.openstandia.connector.util.Utils.toZoneDateTimeForISO8601OffsetDateTime;
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.*;

public class AtlassianGuardUserHandler implements ObjectHandler {

    public static final ObjectClass USER_OBJECT_CLASS = new ObjectClass("User");

    private static final Log LOGGER = Log.getLog(AtlassianGuardUserHandler.class);

    protected final AtlassianGuardConfiguration configuration;
    protected final AtlassianGuardRESTClient client;
    protected final SchemaDefinition schema;

    public AtlassianGuardUserHandler(AtlassianGuardConfiguration configuration, AtlassianGuardRESTClient client,
                                     SchemaDefinition schema) {
        this.configuration = configuration;
        this.client = client;
        this.schema = schema;
    }

    public static SchemaDefinition.Builder createSchema(AtlassianGuardConfiguration configuration, AtlassianGuardRESTClient client) {
        SchemaDefinition.Builder<AtlassianGuardUserModel, PatchOperationsModel, AtlassianGuardUserModel> sb
                = SchemaDefinition.newBuilder(USER_OBJECT_CLASS, AtlassianGuardUserModel.class, PatchOperationsModel.class, AtlassianGuardUserModel.class);

        return createSchema(sb);
    }

    public static SchemaDefinition.Builder createSchema(SchemaDefinition.Builder<? extends AtlassianGuardUserModel, PatchOperationsModel, ? extends AtlassianGuardUserModel> sb) {
        // Atlassian Guard supports SCIM v2.0.
        // https://support.atlassian.com/provisioning-users/docs/understand-user-provisioning/

        // __UID__
        // The id for the user. Must be unique and unchangeable.
        sb.addUid("userId",
                SchemaDefinition.Types.STRING_CASE_IGNORE,
                null,
                (source) -> source.id,
                "id",
                NOT_CREATABLE, NOT_UPDATEABLE
        );

        // code (__NAME__)
        // The name for the user. Must be unique and changeable.
        // Also, it's case-sensitive.
        sb.addName("userName",
                SchemaDefinition.Types.STRING,
                (source, dest) -> dest.userName = source,
                (source, dest) -> dest.replace("userName", source),
                (source) -> source.userName,
                null,
                REQUIRED
        );

        // __ENABLE__ attribute
        sb.add(OperationalAttributes.ENABLE_NAME,
                SchemaDefinition.Types.BOOLEAN,
                (source, dest) -> dest.active = source,
                (source, dest) -> dest.replace("active", source),
                (source) -> source.active,
                "active"
        );

        // Attributes
        sb.add("name.formatted",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    if (dest.name == null) {
                        dest.name = new AtlassianGuardUserModel.Name();
                    }
                    dest.name.formatted = source;
                },
                (source, dest) -> dest.replace("name.formatted", source),
                (source) -> source.name != null ? source.name.formatted : null,
                null
        );
        sb.add("name.familyName",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    if (dest.name == null) {
                        dest.name = new AtlassianGuardUserModel.Name();
                    }
                    dest.name.familyName = source;
                },
                (source, dest) -> dest.replace("name.familyName", source),
                (source) -> source.name != null ? source.name.familyName : null,
                null
        );
        sb.add("name.givenName",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    if (dest.name == null) {
                        dest.name = new AtlassianGuardUserModel.Name();
                    }
                    dest.name.givenName = source;
                },
                (source, dest) -> dest.replace("name.givenName", source),
                (source) -> source.name != null ? source.name.givenName : null,
                null
        );
        sb.add("name.middleName",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    if (dest.name == null) {
                        dest.name = new AtlassianGuardUserModel.Name();
                    }
                    dest.name.middleName = source;
                },
                (source, dest) -> dest.replace("name.middleName", source),
                (source) -> source.name != null ? source.name.middleName : null,
                null
        );
        sb.add("name.honorificPrefix",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    if (dest.name == null) {
                        dest.name = new AtlassianGuardUserModel.Name();
                    }
                    dest.name.honorificPrefix = source;
                },
                (source, dest) -> dest.replace("name.honorificPrefix", source),
                (source) -> source.name != null ? source.name.honorificPrefix : null,
                null
        );
        sb.add("name.honorificSuffix",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    if (dest.name == null) {
                        dest.name = new AtlassianGuardUserModel.Name();
                    }
                    dest.name.honorificSuffix = source;
                },
                (source, dest) -> dest.replace("name.honorificSuffix", source),
                (source) -> source.name != null ? source.name.honorificSuffix : null,
                null
        );
        sb.add("displayName",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    dest.displayName = source;
                },
                (source, dest) -> dest.replace("displayName", source),
                (source) -> source.displayName,
                null
        );
        sb.add("nickName",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    dest.nickName = source;
                },
                (source, dest) -> dest.replace("nickName", source),
                (source) -> source.nickName,
                null
        );
        sb.add("title",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    dest.title = source;
                },
                (source, dest) -> dest.replace("title", source),
                (source) -> source.title,
                null
        );
        sb.add("preferredLanguage",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    dest.preferredLanguage = source;
                },
                (source, dest) -> dest.replace("preferredLanguage", source),
                (source) -> source.preferredLanguage,
                null
        );
        sb.add("timezone",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    dest.timezone = source;
                },
                (source, dest) -> dest.replace("timezone", source),
                (source) -> source.timezone,
                null
        );
        sb.add("active",
                SchemaDefinition.Types.BOOLEAN,
                (source, dest) -> {
                    dest.active = source;
                },
                (source, dest) -> dest.replace("active", source),
                (source) -> source.active,
                null
        );
        sb.add("primaryEmail",
                SchemaDefinition.Types.STRING_CASE_IGNORE,
                (source, dest) -> {
                    if (source == null) {
                        return;
                    }
                    AtlassianGuardUserModel.Email email = new AtlassianGuardUserModel.Email();
                    email.value = source;
                    email.primary = true;

                    dest.emails = new ArrayList<>();
                    dest.emails.add(email);
                },
                (source, dest) -> {
                    if (source == null) {
                        dest.replace((AtlassianGuardUserModel.Email) null);
                        return;
                    }
                    AtlassianGuardUserModel.Email newEmail = new AtlassianGuardUserModel.Email();
                    newEmail.value = source;
                    newEmail.primary = true;
                    dest.replace(newEmail);
                },
                (source) -> {
                    if (source.emails == null || source.emails.isEmpty()) {
                        return null;
                    }
                    return source.emails.stream()
                            .filter(x -> x.primary)
                            .findFirst()
                            .map(x -> x.value)
                            .orElse(null);
                },
                "emails"
        );
        sb.add("primaryPhoneNumber",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    if (source == null) {
                        return;
                    }

                    String[] split = source.split("/");
                    if (split.length != 2) {
                        throw new InvalidAttributeValueException("Invalid primaryPhoneNumber: " + source);
                    }

                    AtlassianGuardUserModel.PhoneNumber phoneNumber = new AtlassianGuardUserModel.PhoneNumber();
                    phoneNumber.value = split[0];
                    phoneNumber.primary = true;
                    phoneNumber.type = split[1];

                    dest.phoneNumbers = new ArrayList<>();
                    dest.phoneNumbers.add(phoneNumber);
                },
                (source, dest) -> {
                    if (source == null) {
                        dest.replace((AtlassianGuardUserModel.PhoneNumber) null);
                        return;
                    }

                    String[] split = source.split("/");
                    if (split.length != 2) {
                        throw new InvalidAttributeValueException("Invalid primaryPhoneNumber: " + source);
                    }

                    AtlassianGuardUserModel.PhoneNumber newPhoneNumber = new AtlassianGuardUserModel.PhoneNumber();
                    newPhoneNumber.value = split[0];
                    newPhoneNumber.primary = true;
                    newPhoneNumber.type = split[1];
                    dest.replace(newPhoneNumber);
                },
                (source) -> {
                    if (source.phoneNumbers == null || source.phoneNumbers.isEmpty()) {
                        return null;
                    }
                    return source.phoneNumbers.stream()
                            .filter(x -> x.primary)
                            .findFirst()
                            .map(x -> x.value + "/" + x.type)
                            .orElse(null);
                },
                "phoneNumbers"
        );

        // Association
        // Atlassian Guard supports "groups" attributes
        sb.addAsMultiple("groups",
                SchemaDefinition.Types.UUID,
                null,
                null,
                null,
                (source) -> source.groups != null ? source.groups.stream().filter(x -> x.type != null && x.type.equals("Group")).map(x -> x.value) : null,
                null,
                NOT_CREATABLE, NOT_UPDATEABLE
        );

        // Metadata (readonly)
        sb.add("meta.created",
                SchemaDefinition.Types.DATETIME,
                null,
                (source) -> toZoneDateTimeForISO8601OffsetDateTime(source.meta.created),
                null,
                NOT_CREATABLE, NOT_UPDATEABLE
        );
        sb.add("meta.lastModified",
                SchemaDefinition.Types.DATETIME,
                null,
                (source) -> toZoneDateTimeForISO8601OffsetDateTime(source.meta.lastModified),
                null,
                NOT_CREATABLE, NOT_UPDATEABLE
        );

        LOGGER.ok("The constructed user schema");

        return sb;
    }

    @Override
    public SchemaDefinition getSchema() {
        return schema;
    }

    @Override
    public Uid create(Set<Attribute> attributes) {
        AtlassianGuardUserModel user = new AtlassianGuardUserModel();
        AtlassianGuardUserModel mapped = schema.apply(attributes, user);

        Uid newUid = client.createUser(mapped);

        return newUid;
    }

    @Override
    public Set<AttributeDelta> updateDelta(Uid uid, Set<AttributeDelta> modifications, OperationOptions options) {
        PatchOperationsModel dest = new PatchOperationsModel();

        schema.applyDelta(modifications, dest);

        if (dest.hasAttributesChange()) {
            client.patchUser(uid, dest);
        }

        return null;
    }

    @Override
    public void delete(Uid uid, OperationOptions options) {
        client.deleteUser(uid);
    }

    @Override
    public int getByUid(Uid uid, ResultsHandler resultsHandler, OperationOptions options,
                        Set<String> returnAttributesSet, Set<String> fetchFieldsSet,
                        boolean allowPartialAttributeValues, int pageSize, int pageOffset) {
        AtlassianGuardUserModel user = client.getUser(uid, options, fetchFieldsSet);

        if (user != null) {
            resultsHandler.handle(toConnectorObject(schema, user, returnAttributesSet, allowPartialAttributeValues));
            return 1;
        }
        return 0;
    }

    @Override
    public int getByName(Name name, ResultsHandler resultsHandler, OperationOptions options,
                         Set<String> returnAttributesSet, Set<String> fetchFieldsSet,
                         boolean allowPartialAttributeValues, int pageSize, int pageOffset) {
        AtlassianGuardUserModel user = client.getUser(name, options, fetchFieldsSet);

        if (user != null) {
            resultsHandler.handle(toConnectorObject(schema, user, returnAttributesSet, allowPartialAttributeValues));
            return 1;
        }
        return 0;
    }

    @Override
    public int getAll(ResultsHandler resultsHandler, OperationOptions options,
                      Set<String> returnAttributesSet, Set<String> fetchFieldsSet,
                      boolean allowPartialAttributeValues, int pageSize, int pageOffset) {
        return client.getUsers((u) -> resultsHandler.handle(toConnectorObject(schema, u, returnAttributesSet, allowPartialAttributeValues)),
                options, fetchFieldsSet, pageSize, pageOffset);
    }
}
