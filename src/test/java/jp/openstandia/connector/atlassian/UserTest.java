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

import jp.openstandia.connector.atlassian.testutil.AbstractTest;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static jp.openstandia.connector.atlassian.AtlassianGuardUserHandler.USER_OBJECT_CLASS;
import static jp.openstandia.connector.util.Utils.toZoneDateTimeForISO8601OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserTest extends AbstractTest {

    @Test
    void addUser() {
        // Given
        String userId = "12345";
        String userName = "foo";
        String formatted = "Foo Bar";
        String familyName = "Bar";
        String givenName = "Foo";
        String middleName = "Hoge";
        String honorificPrefix = "Dr";
        String honorificSuffix = "Jr";
        String displayName = "Foo Hoge Bar";
        String nickName = "foobar";
        String title = "CEO";
        String preferredLanguage = "ja";
        String timezone = "Asia/Tokyo";
        String primaryEmail = "foo@example.com";
        String primaryPhoneNumber = "012-3456-7890";
        String primaryPhoneNumberType = "work";
        boolean active = true;

        Set<Attribute> attrs = new HashSet<>();
        attrs.add(new Name(userName));
        attrs.add(AttributeBuilder.build("name.formatted", formatted));
        attrs.add(AttributeBuilder.build("name.familyName", familyName));
        attrs.add(AttributeBuilder.build("name.givenName", givenName));
        attrs.add(AttributeBuilder.build("name.middleName", middleName));
        attrs.add(AttributeBuilder.build("name.honorificPrefix", honorificPrefix));
        attrs.add(AttributeBuilder.build("name.honorificSuffix", honorificSuffix));
        attrs.add(AttributeBuilder.build("displayName", displayName));
        attrs.add(AttributeBuilder.build("nickName", nickName));
        attrs.add(AttributeBuilder.build("title", title));
        attrs.add(AttributeBuilder.build("preferredLanguage", preferredLanguage));
        attrs.add(AttributeBuilder.build("timezone", timezone));
        attrs.add(AttributeBuilder.build("primaryEmail", primaryEmail));
        attrs.add(AttributeBuilder.build("primaryPhoneNumber", primaryPhoneNumber + "/" + primaryPhoneNumberType));
        attrs.add(AttributeBuilder.buildEnabled(active));

        AtomicReference<AtlassianGuardUserModel> created = new AtomicReference<>();
        mockClient.createUser = ((user) -> {
            created.set(user);

            return new Uid(userId, new Name(userName));
        });

        // When
        Uid uid = connector.create(USER_OBJECT_CLASS, attrs, new OperationOptionsBuilder().build());

        // Then
        assertEquals(userId, uid.getUidValue());
        assertEquals(userName, uid.getNameHintValue());

        AtlassianGuardUserModel newUser = created.get();
        assertEquals(userName, newUser.userName);
        assertEquals(formatted, newUser.name.formatted);
        assertEquals(familyName, newUser.name.familyName);
        assertEquals(givenName, newUser.name.givenName);
        assertEquals(middleName, newUser.name.middleName);
        assertEquals(honorificPrefix, newUser.name.honorificPrefix);
        assertEquals(honorificSuffix, newUser.name.honorificSuffix);
        assertEquals(displayName, newUser.displayName);
        assertEquals(nickName, newUser.nickName);
        assertEquals(title, newUser.title);
        assertEquals(preferredLanguage, newUser.preferredLanguage);
        assertEquals(timezone, newUser.timezone);
        assertEquals(1, newUser.emails.size());
        assertEquals(primaryEmail, newUser.emails.get(0).value);
        assertTrue(newUser.emails.get(0).primary);
        assertEquals(1, newUser.phoneNumbers.size());
        assertEquals(primaryPhoneNumber, newUser.phoneNumbers.get(0).value);
        assertEquals(primaryPhoneNumberType, newUser.phoneNumbers.get(0).type);
        assertTrue(newUser.phoneNumbers.get(0).primary);
    }

    @Test
    void addUserButAlreadyExists() {
        // Given
        String userName = "foo";

        Set<Attribute> attrs = new HashSet<>();
        attrs.add(new Name(userName));

        mockClient.createUser = ((user) -> {
            throw new AlreadyExistsException("");
        });

        // When
        Throwable expect = null;
        try {
            Uid uid = connector.create(USER_OBJECT_CLASS, attrs, new OperationOptionsBuilder().build());
        } catch (Throwable t) {
            expect = t;
        }

        // Then
        assertNotNull(expect);
        assertTrue(expect instanceof AlreadyExistsException);
    }

    @Test
    void updateUser() {
        // Given
        String currentUserName = "hoge";

        String userId = "12345";
        String userName = "foo";
        String formatted = "Foo Bar";
        String familyName = "Bar";
        String givenName = "Foo";
        String middleName = "Hoge";
        String honorificPrefix = "Dr";
        String honorificSuffix = "Jr";
        String displayName = "Foo Hoge Bar";
        String nickName = "foobar";
        String title = "CEO";
        String preferredLanguage = "ja";
        String timezone = "Asia/Tokyo";
        String primaryEmail = "foo@example.com";
        String primaryPhoneNumber = "012-3456-7890";
        String primaryPhoneNumberType = "work";
        boolean active = false;

        Set<AttributeDelta> modifications = new LinkedHashSet<>();
        modifications.add(AttributeDeltaBuilder.build(Name.NAME, userName));
        modifications.add(AttributeDeltaBuilder.build("name.formatted", formatted));
        modifications.add(AttributeDeltaBuilder.build("name.familyName", familyName));
        modifications.add(AttributeDeltaBuilder.build("name.givenName", givenName));
        modifications.add(AttributeDeltaBuilder.build("name.middleName", middleName));
        modifications.add(AttributeDeltaBuilder.build("name.honorificPrefix", honorificPrefix));
        modifications.add(AttributeDeltaBuilder.build("name.honorificSuffix", honorificSuffix));
        modifications.add(AttributeDeltaBuilder.build("displayName", displayName));
        modifications.add(AttributeDeltaBuilder.build("nickName", nickName));
        modifications.add(AttributeDeltaBuilder.build("title", title));
        modifications.add(AttributeDeltaBuilder.build("preferredLanguage", preferredLanguage));
        modifications.add(AttributeDeltaBuilder.build("timezone", timezone));
        modifications.add(AttributeDeltaBuilder.build("primaryEmail", primaryEmail));
        modifications.add(AttributeDeltaBuilder.build("primaryPhoneNumber", primaryPhoneNumber + "/" + primaryPhoneNumberType));
        modifications.add(AttributeDeltaBuilder.buildEnabled(active));

        AtomicReference<Uid> targetUid = new AtomicReference<>();
        AtomicReference<PatchOperationsModel> updated = new AtomicReference<>();
        mockClient.patchUser = ((u, operations) -> {
            targetUid.set(u);
            updated.set(operations);
        });

        // When
        Set<AttributeDelta> affected = connector.updateDelta(USER_OBJECT_CLASS, new Uid(userId, new Name(currentUserName)), modifications, new OperationOptionsBuilder().build());

        // Then
        assertNull(affected);

        assertEquals(userId, targetUid.get().getUidValue());
        assertEquals(currentUserName, targetUid.get().getNameHintValue());

        PatchOperationsModel operation = updated.get();
        assertNotNull(operation.operations);
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("userName") && op.value.equals(userName)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("name.formatted") && op.value.equals(formatted)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("name.familyName") && op.value.equals(familyName)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("name.givenName") && op.value.equals(givenName)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("name.middleName") && op.value.equals(middleName)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("name.honorificPrefix") && op.value.equals(honorificPrefix)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("name.honorificSuffix") && op.value.equals(honorificSuffix)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("displayName") && op.value.equals(displayName)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("nickName") && op.value.equals(nickName)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("title") && op.value.equals(title)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("preferredLanguage") && op.value.equals(preferredLanguage)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("timezone") && op.value.equals(timezone)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("emails") && ((List<AtlassianGuardUserModel.Email>) op.value).size() == 1));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("emails") && ((List<AtlassianGuardUserModel.Email>) op.value).get(0).value.equals(primaryEmail)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("emails") && ((List<AtlassianGuardUserModel.Email>) op.value).get(0).primary.equals(true)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("phoneNumbers") && ((List<AtlassianGuardUserModel.PhoneNumber>) op.value).size() == 1));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("phoneNumbers") && ((List<AtlassianGuardUserModel.PhoneNumber>) op.value).get(0).value.equals(primaryPhoneNumber)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("phoneNumbers") && ((List<AtlassianGuardUserModel.PhoneNumber>) op.value).get(0).primary.equals(true)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("phoneNumbers") && ((List<AtlassianGuardUserModel.PhoneNumber>) op.value).get(0).type.equals(primaryPhoneNumberType)));
        assertTrue(operation.operations.stream().anyMatch(op -> op.path.equals("active") && op.value.equals(active)));
    }

    @Test
    void updateUserWithNoValues() {
        ConnectorFacade connector = newFacade(configuration);

        // Given
        String currentUserName = "foo";

        String userId = "12345";
        String userName = "foo";

        Set<AttributeDelta> modifications = new HashSet<>();
        // IDM sets empty list to remove the single value
        modifications.add(AttributeDeltaBuilder.build("name.formatted", Collections.emptyList()));
        modifications.add(AttributeDeltaBuilder.build("name.familyName", Collections.emptyList()));
        modifications.add(AttributeDeltaBuilder.build("name.givenName", Collections.emptyList()));

        AtomicReference<Uid> targetUid = new AtomicReference<>();
        AtomicReference<PatchOperationsModel> updated = new AtomicReference<>();
        mockClient.patchUser = ((u, operations) -> {
            targetUid.set(u);
            updated.set(operations);
        });

        // When
        Set<AttributeDelta> affected = connector.updateDelta(USER_OBJECT_CLASS, new Uid(userId, new Name(currentUserName)), modifications, new OperationOptionsBuilder().build());

        // Then
        assertNull(affected);

        assertEquals(userId, targetUid.get().getUidValue());
        assertEquals(userName, targetUid.get().getNameHintValue());

        PatchOperationsModel operation = updated.get();
        assertNotNull(operation.operations);
        // Atlassian Guard API treats empty string as removing the value, but name.* attributes are not supported removing with empty string
        assertTrue(operation.operations.stream().anyMatch(op -> op.op.equals("replace") && op.path.equals("name.formatted") && op.value.equals("")));
        assertTrue(operation.operations.stream().anyMatch(op -> op.op.equals("replace") & op.path.equals("name.familyName") && op.value.equals("")));
        assertTrue(operation.operations.stream().anyMatch(op -> op.op.equals("replace") & op.path.equals("name.givenName") && op.value.equals("")));
    }

    @Test
    void updateUserButNotFound() {
        // Given
        String currentUserName = "foo";

        String userId = "12345";
        String formatted = "Foo Bar";

        Set<AttributeDelta> modifications = new HashSet<>();
        modifications.add(AttributeDeltaBuilder.build("name.formatted", formatted));

        mockClient.patchUser = ((u, operations) -> {
            throw new UnknownUidException();
        });

        // When
        Throwable expect = null;
        try {
            connector.updateDelta(USER_OBJECT_CLASS, new Uid(userId, new Name(currentUserName)), modifications, new OperationOptionsBuilder().build());
        } catch (Throwable t) {
            expect = t;
        }

        // Then
        assertNotNull(expect);
        assertTrue(expect instanceof UnknownUidException);
    }

    @Test
    void getUserByUid() {
        // Given
        String userId = "12345";
        String userName = "foo";
        String formatted = "Foo Bar";
        String familyName = "Bar";
        String givenName = "Foo";
        String middleName = "Hoge";
        String honorificPrefix = "Dr";
        String honorificSuffix = "Jr";
        String displayName = "Foo Hoge Bar";
        String nickName = "foobar";
        String title = "CEO";
        String preferredLanguage = "ja";
        String timezone = "Asia/Tokyo";
        String primaryEmail = "foo@example.com";
        String primaryPhoneNumber = "012-3456-7890";
        String primaryPhoneNumberType = "work";
        boolean active = false;

        String createdDate = "2024-11-14T05:56:39.79755Z";
        String updatedDate = "2024-11-14T05:56:40.212208Z";

        AtomicReference<Uid> targetUid = new AtomicReference<>();
        mockClient.getUserByUid = ((u) -> {
            targetUid.set(u);

            AtlassianGuardUserModel result = new AtlassianGuardUserModel();
            result.id = userId;
            result.userName = userName;
            result.name = new AtlassianGuardUserModel.Name();
            result.name.formatted = formatted;
            result.name.familyName = familyName;
            result.name.givenName = givenName;
            result.name.middleName = middleName;
            result.name.honorificPrefix = honorificPrefix;
            result.name.honorificSuffix = honorificSuffix;
            result.displayName = displayName;
            result.nickName = nickName;
            result.title = title;
            result.preferredLanguage = preferredLanguage;
            result.timezone = timezone;

            List<AtlassianGuardUserModel.Email> emails = new ArrayList<>();
            AtlassianGuardUserModel.Email email = new AtlassianGuardUserModel.Email();
            email.value = primaryEmail;
            email.primary = true;
            emails.add(email);
            result.emails = emails;

            List<AtlassianGuardUserModel.PhoneNumber> phoneNumbers = new ArrayList<>();
            AtlassianGuardUserModel.PhoneNumber phoneNumber = new AtlassianGuardUserModel.PhoneNumber();
            phoneNumber.value = primaryPhoneNumber;
            phoneNumber.primary = true;
            phoneNumber.type = primaryPhoneNumberType;
            phoneNumbers.add(phoneNumber);
            result.phoneNumbers = phoneNumbers;

            result.active = active;
            result.meta = new AtlassianGuardUserModel.Meta();
            result.meta.created = createdDate;
            result.meta.lastModified = updatedDate;

            return result;
        });
        AtomicReference<String> targetName = new AtomicReference<>();
        AtomicReference<Integer> targetPageSize = new AtomicReference<>();

        // When
        ConnectorObject result = connector.getObject(USER_OBJECT_CLASS, new Uid(userId, new Name(userName)), defaultGetOperation());

        // Then
        assertEquals(USER_OBJECT_CLASS, result.getObjectClass());
        assertEquals(userId, result.getUid().getUidValue());
        assertEquals(userName, result.getName().getNameValue());
        assertEquals(formatted, singleAttr(result, "name.formatted"));
        assertEquals(familyName, singleAttr(result, "name.familyName"));
        assertEquals(givenName, singleAttr(result, "name.givenName"));
        assertEquals(middleName, singleAttr(result, "name.middleName"));
        assertEquals(givenName, singleAttr(result, "name.givenName"));
        assertEquals(honorificPrefix, singleAttr(result, "name.honorificPrefix"));
        assertEquals(honorificSuffix, singleAttr(result, "name.honorificSuffix"));
        assertEquals(displayName, singleAttr(result, "displayName"));
        assertEquals(nickName, singleAttr(result, "nickName"));
        assertEquals(title, singleAttr(result, "title"));
        assertEquals(preferredLanguage, singleAttr(result, "preferredLanguage"));
        assertEquals(timezone, singleAttr(result, "timezone"));
        assertEquals(primaryEmail, singleAttr(result, "primaryEmail"));
        assertEquals(primaryPhoneNumber + "/" + primaryPhoneNumberType, singleAttr(result, "primaryPhoneNumber"));
        assertEquals(active, singleAttr(result, OperationalAttributes.ENABLE_NAME));
        assertEquals(toZoneDateTimeForISO8601OffsetDateTime(createdDate), singleAttr(result, "meta.created"));
        assertEquals(toZoneDateTimeForISO8601OffsetDateTime(updatedDate), singleAttr(result, "meta.lastModified"));

        assertNull(targetName.get());
        assertNull(targetPageSize.get());
    }

    @Test
    void getUserByUidWithEmpty() {
        ConnectorFacade connector = newFacade(configuration);

        // Given
        String userId = "12345";
        String userName = "foo";
        String createdDate = "2024-11-14T05:56:39.79755Z";
        String updatedDate = "2024-11-14T05:56:40.212208Z";

        AtomicReference<Uid> targetUid = new AtomicReference<>();
        mockClient.getUserByUid = ((u) -> {
            targetUid.set(u);

            AtlassianGuardUserModel result = new AtlassianGuardUserModel();
            result.id = userId;
            result.userName = userName;
            result.name = new AtlassianGuardUserModel.Name(); // Empty name
            result.meta = new AtlassianGuardUserModel.Meta();
            result.meta.created = createdDate;
            result.meta.lastModified = updatedDate;
            return result;
        });

        // When
        ConnectorObject result = connector.getObject(USER_OBJECT_CLASS, new Uid(userId, new Name(userName)), defaultGetOperation());

        // Then
        assertEquals(USER_OBJECT_CLASS, result.getObjectClass());
        assertEquals(userId, result.getUid().getUidValue());
        assertEquals(userName, result.getName().getNameValue());
        assertNull(result.getAttributeByName("name.formatted"));
        assertNull(result.getAttributeByName("name.familyName"));
        assertNull(result.getAttributeByName("name.givenName"));
    }

    @Test
    void getUserByUidWithNotFound() {
        ConnectorFacade connector = newFacade(configuration);

        // Given
        String userId = "12345";
        String userName = "foo";

        AtomicReference<Uid> targetUid = new AtomicReference<>();
        mockClient.getUserByUid = ((u) -> {
            targetUid.set(u);
            return null;
        });

        // When
        ConnectorObject result = connector.getObject(USER_OBJECT_CLASS, new Uid(userId, new Name(userName)), defaultGetOperation());

        // Then
        assertNull(result);
        assertNotNull(targetUid.get());
    }

    @Test
    void getUserByName() {
        // Given
        String userId = "12345";
        String userName = "foo";
        String formatted = "Foo Bar";
        String givenName = "Foo";
        String familyName = "Bar";
        String createdDate = "2024-11-14T05:56:39.79755Z";
        String updatedDate = "2024-11-14T05:56:40.212208Z";

        AtomicReference<Name> targetName = new AtomicReference<>();
        mockClient.getUserByName = ((u) -> {
            targetName.set(u);

            AtlassianGuardUserModel result = new AtlassianGuardUserModel();
            result.id = userId;
            result.userName = userName;
            result.name = new AtlassianGuardUserModel.Name();
            result.name.formatted = formatted;
            result.name.givenName = givenName;
            result.name.familyName = familyName;
            result.meta = new AtlassianGuardUserModel.Meta();
            result.meta.created = createdDate;
            result.meta.lastModified = updatedDate;
            return result;
        });

        // When
        List<ConnectorObject> results = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            results.add(connectorObject);
            return true;
        };
        connector.search(USER_OBJECT_CLASS, FilterBuilder.equalTo(new Name(userName)), handler, defaultSearchOperation());

        // Then
        assertEquals(1, results.size());
        ConnectorObject result = results.get(0);
        assertEquals(USER_OBJECT_CLASS, result.getObjectClass());
        assertEquals(userId, result.getUid().getUidValue());
        assertEquals(userName, result.getName().getNameValue());
        assertEquals(formatted, singleAttr(result, "name.formatted"));
        assertEquals(givenName, singleAttr(result, "name.givenName"));
        assertEquals(familyName, singleAttr(result, "name.familyName"));
        assertEquals(toZoneDateTimeForISO8601OffsetDateTime(createdDate), singleAttr(result, "meta.created"));
        assertEquals(toZoneDateTimeForISO8601OffsetDateTime(updatedDate), singleAttr(result, "meta.lastModified"));
    }

    @Test
    void getUsers() {
        // Given
        String userId = "12345";
        String userName = "foo";
        String formatted = "Foo Bar";
        String createdDate = "2024-11-14T05:56:39.79755Z";
        String updatedDate = "2024-11-14T05:56:40.212208Z";

        AtomicReference<Integer> targetPageSize = new AtomicReference<>();
        AtomicReference<Integer> targetOffset = new AtomicReference<>();
        mockClient.getUsers = ((h, size, offset) -> {
            targetPageSize.set(size);
            targetOffset.set(offset);

            AtlassianGuardUserModel result = new AtlassianGuardUserModel();
            result.id = userId;
            result.userName = userName;
            result.name = new AtlassianGuardUserModel.Name();
            result.name.formatted = formatted;
            result.meta = new AtlassianGuardUserModel.Meta();
            result.meta.created = createdDate;
            result.meta.lastModified = updatedDate;
            h.handle(result);

            return 1;
        });

        // When
        List<ConnectorObject> results = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            results.add(connectorObject);
            return true;
        };
        connector.search(USER_OBJECT_CLASS, null, handler, defaultSearchOperation());

        // Then
        assertEquals(1, results.size());
        ConnectorObject result = results.get(0);
        assertEquals(USER_OBJECT_CLASS, result.getObjectClass());
        assertEquals(userId, result.getUid().getUidValue());
        assertEquals(userName, result.getName().getNameValue());
        assertEquals(formatted, singleAttr(result, "name.formatted"));
        assertEquals(toZoneDateTimeForISO8601OffsetDateTime(createdDate), singleAttr(result, "meta.created"));
        assertEquals(toZoneDateTimeForISO8601OffsetDateTime(updatedDate), singleAttr(result, "meta.lastModified"));

        assertEquals(20, targetPageSize.get(), "Not page size in the operation option");
        assertEquals(1, targetOffset.get());
    }

    @Test
    void getUsersWithGroups() {
        // Given
        String userId = "12345";
        String userName = "foo";
        String formatted = "Foo Bar";
        String createdDate = "2024-11-14T05:56:39.79755Z";
        String updatedDate = "2024-11-14T05:56:40.212208Z";
        String currentGroup1 = "d138e7b8-fd26-45b2-bff9-34d11b29aff1";
        String currentGroup2 = "592af241-65c3-4dc3-83b7-f3f82f6c28bd";

        AtomicReference<Integer> targetPageSize = new AtomicReference<>();
        AtomicReference<Integer> targetOffset = new AtomicReference<>();
        mockClient.getUsers = ((h, size, offset) -> {
            targetPageSize.set(size);
            targetOffset.set(offset);

            AtlassianGuardUserModel result = new AtlassianGuardUserModel();
            result.id = userId;
            result.userName = userName;
            result.name = new AtlassianGuardUserModel.Name();
            result.name.formatted = formatted;
            result.meta = new AtlassianGuardUserModel.Meta();
            result.meta.created = createdDate;
            result.meta.lastModified = updatedDate;


            AtlassianGuardUserModel.Group group1 = new AtlassianGuardUserModel.Group();
            group1.value = currentGroup1;
            group1.type = "Group";
            group1.ref = "https://example.com/scim/directory/test/Group/group001";

            AtlassianGuardUserModel.Group group2 = new AtlassianGuardUserModel.Group();
            group2.value = currentGroup2;
            group2.type = "Group";
            group2.ref = "https://example.com/scim/directory/test/Group/group002";

            List<AtlassianGuardUserModel.Group> groups = new ArrayList<>();
            groups.add(group1);
            groups.add(group2);
            result.groups = groups;

            h.handle(result);

            return 1;
        });

        // When
        List<ConnectorObject> results = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            results.add(connectorObject);
            return true;
        };
        connector.search(USER_OBJECT_CLASS, null, handler, defaultSearchOperation());

        // Then
        assertEquals(1, results.size());
        ConnectorObject result = results.get(0);
        assertEquals(USER_OBJECT_CLASS, result.getObjectClass());
        assertEquals(userId, result.getUid().getUidValue());
        assertEquals(userName, result.getName().getNameValue());
        assertEquals(formatted, singleAttr(result, "name.formatted"));
        assertEquals(toZoneDateTimeForISO8601OffsetDateTime(createdDate), singleAttr(result, "meta.created"));
        assertEquals(toZoneDateTimeForISO8601OffsetDateTime(updatedDate), singleAttr(result, "meta.lastModified"));

        Attribute groupsAttribute = result.getAttributeByName("groups");
        assertNotNull(groupsAttribute);
        List<Object> groups = groupsAttribute.getValue();
        assertEquals(2, groups.size());
        assertEquals(currentGroup1, groups.get(0));
        assertEquals(currentGroup2, groups.get(1));

        assertEquals(20, targetPageSize.get(), "Not page size in the operation option");
        assertEquals(1, targetOffset.get());
    }

    @Test
    void getUsersZero() {
        // Given
        AtomicReference<Integer> targetPageSize = new AtomicReference<>();
        AtomicReference<Integer> targetOffset = new AtomicReference<>();
        mockClient.getUsers = ((h, size, offset) -> {
            targetPageSize.set(size);
            targetOffset.set(offset);

            return 0;
        });

        // When
        List<ConnectorObject> results = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            results.add(connectorObject);
            return true;
        };
        connector.search(USER_OBJECT_CLASS, null, handler, defaultSearchOperation());

        // Then
        assertEquals(0, results.size());
        assertEquals(20, targetPageSize.get(), "Not default page size in the configuration");
        assertEquals(1, targetOffset.get());
    }

    @Test
    void getUsersTwo() {
        // Given
        AtomicReference<Integer> targetPageSize = new AtomicReference<>();
        AtomicReference<Integer> targetOffset = new AtomicReference<>();
        mockClient.getUsers = ((h, size, offset) -> {
            targetPageSize.set(size);
            targetOffset.set(offset);

            AtlassianGuardUserModel result = new AtlassianGuardUserModel();
            result.id = "1";
            result.userName = "a";
            result.name = new AtlassianGuardUserModel.Name();
            result.name.formatted = "A";
            result.meta = new AtlassianGuardUserModel.Meta();
            result.meta.created = "2024-11-14T05:56:39.79755Z";
            result.meta.lastModified = "2024-11-14T05:56:40.212208Z";
            h.handle(result);

            result = new AtlassianGuardUserModel();
            result.id = "2";
            result.userName = "b";
            result.name = new AtlassianGuardUserModel.Name();
            result.name.formatted = "B";
            result.meta = new AtlassianGuardUserModel.Meta();
            result.meta.created = "2024-11-14T05:56:39.79755Z";
            result.meta.lastModified = "2024-11-14T05:56:40.212208Z";
            h.handle(result);

            return 2;
        });

        // When
        List<ConnectorObject> results = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            results.add(connectorObject);
            return true;
        };
        connector.search(USER_OBJECT_CLASS, null, handler, defaultSearchOperation());

        // Then
        assertEquals(2, results.size());

        ConnectorObject result = results.get(0);
        assertEquals(USER_OBJECT_CLASS, result.getObjectClass());
        assertEquals("1", result.getUid().getUidValue());
        assertEquals("a", result.getName().getNameValue());

        result = results.get(1);
        assertEquals(USER_OBJECT_CLASS, result.getObjectClass());
        assertEquals("2", result.getUid().getUidValue());
        assertEquals("b", result.getName().getNameValue());

        assertEquals(20, targetPageSize.get(), "Not default page size in the configuration");
        assertEquals(1, targetOffset.get());
    }

    @Test
    void count() {
        // Given
        AtomicReference<Integer> targetPageSize = new AtomicReference<>();
        AtomicReference<Integer> targetOffset = new AtomicReference<>();
        mockClient.getUsers = ((h, size, offset) -> {
            targetPageSize.set(size);
            targetOffset.set(offset);

            AtlassianGuardUserModel result = new AtlassianGuardUserModel();
            result.id = "1";
            result.userName = "a";
            result.name = new AtlassianGuardUserModel.Name();
            result.name.formatted = "A";
            result.meta = new AtlassianGuardUserModel.Meta();
            result.meta.created = "2024-11-14T05:56:39.79755Z";
            result.meta.lastModified = "2024-11-14T05:56:40.212208Z";
            h.handle(result);

            return 10;
        });

        // When
        List<ConnectorObject> results = new ArrayList<>();
        AtomicReference<SearchResult> searchResult = new AtomicReference<>();
        SearchResultsHandler handler = new SearchResultsHandler() {
            @Override
            public void handleResult(SearchResult result) {
                searchResult.set(result);
            }

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                results.add(connectorObject);
                return true;
            }
        };
        connector.search(USER_OBJECT_CLASS, null, handler, countSearchOperation());

        // Then
        assertEquals(1, results.size());
        assertEquals(1, targetPageSize.get());
        assertEquals(1, targetOffset.get());
        assertEquals(9, searchResult.get().getRemainingPagedResults());
        assertTrue(searchResult.get().isAllResultsReturned());
        assertNull(searchResult.get().getPagedResultsCookie());
    }

    @Test
    void pagedSearch() {
        // Given
        AtomicReference<Integer> targetPageSize = new AtomicReference<>();
        AtomicReference<Integer> targetOffset = new AtomicReference<>();
        mockClient.getUsers = ((h, size, offset) -> {
            targetPageSize.set(size);
            targetOffset.set(offset);

            AtlassianGuardUserModel result = new AtlassianGuardUserModel();
            result.id = "1";
            result.userName = "a";
            result.name = new AtlassianGuardUserModel.Name();
            result.name.formatted = "A";
            result.meta = new AtlassianGuardUserModel.Meta();
            result.meta.created = "2024-11-14T05:56:39.79755Z";
            result.meta.lastModified = "2024-11-14T05:56:40.212208Z";
            h.handle(result);

            return 6;
        });

        // When
        List<ConnectorObject> results = new ArrayList<>();
        AtomicReference<SearchResult> searchResult = new AtomicReference<>();
        SearchResultsHandler handler = new SearchResultsHandler() {
            @Override
            public void handleResult(SearchResult result) {
                searchResult.set(result);
            }

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                results.add(connectorObject);
                return true;
            }
        };
        connector.search(USER_OBJECT_CLASS, null, handler, pagedSearchOperation(6, 1));

        // Then
        assertEquals(1, results.size());
        assertEquals(1, targetPageSize.get());
        assertEquals(6, targetOffset.get());
        assertEquals(0, searchResult.get().getRemainingPagedResults());
        assertTrue(searchResult.get().isAllResultsReturned());
        assertNull(searchResult.get().getPagedResultsCookie());
    }

    @Test
    void deleteUser() {
        // Given
        String userId = "12345";
        String userName = "foo";

        AtomicReference<Uid> deleted = new AtomicReference<>();
        mockClient.deleteUser = ((uid) -> {
            deleted.set(uid);
        });

        // When
        connector.delete(USER_OBJECT_CLASS, new Uid(userId, new Name(userName)), new OperationOptionsBuilder().build());

        // Then
        assertEquals(userId, deleted.get().getUidValue());
        assertEquals(userName, deleted.get().getNameHintValue());
    }

    @Test
    void deleteUserButNotFound() {
        // Given
        String userId = "12345";
        String userName = "foo";

        AtomicReference<Uid> deleted = new AtomicReference<>();
        mockClient.deleteUser = ((uid) -> {
            throw new UnknownUidException();
        });

        // When
        Throwable expect = null;
        try {
            connector.delete(USER_OBJECT_CLASS, new Uid(userId, new Name(userName)), new OperationOptionsBuilder().build());
        } catch (Throwable t) {
            expect = t;
        }

        // Then
        assertNotNull(expect);
        assertTrue(expect instanceof UnknownUidException);
    }
}
