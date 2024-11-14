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
package jp.openstandia.connector.atlassian.testutil;

import jp.openstandia.connector.atlassian.AtlassianGuardGroupModel;
import jp.openstandia.connector.atlassian.AtlassianGuardRESTClient;
import jp.openstandia.connector.atlassian.AtlassianGuardUserModel;
import jp.openstandia.connector.atlassian.PatchOperationsModel;
import jp.openstandia.connector.util.QueryHandler;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Uid;

import java.util.Set;

public class MockClient extends AtlassianGuardRESTClient {

    private static MockClient INSTANCE = new MockClient();

    // User
    public MockFunction<AtlassianGuardUserModel, Uid> createUser;
    public MockBiConsumer<Uid, PatchOperationsModel> patchUser;
    public MockFunction<Uid, AtlassianGuardUserModel> getUserByUid;
    public MockFunction<Name, AtlassianGuardUserModel> getUserByName;
    public MockTripleFunction<QueryHandler<AtlassianGuardUserModel>, Integer, Integer, Integer> getUsers;
    public MockConsumer<Uid> deleteUser;

    // Group
    public MockFunction<AtlassianGuardGroupModel, Uid> createGroup;
    public MockBiConsumer<Uid, PatchOperationsModel> patchGroup;
    public MockBiConsumer<Uid, String> renameGroup;
    public MockFunction<Uid, AtlassianGuardGroupModel> getGroupByUid;
    public MockFunction<Name, AtlassianGuardGroupModel> getGroupByName;
    public MockTripleFunction<QueryHandler<AtlassianGuardGroupModel>, Integer, Integer, Integer> getGroups;
    public MockConsumer<Uid> deleteGroup;

    public boolean closed = false;

    public void init() {
        INSTANCE = new MockClient();
    }

    private MockClient() {
    }

    public static MockClient instance() {
        return INSTANCE;
    }

    @Override
    public void test() {
    }

    @Override
    public void close() {
        closed = true;
    }

    // User

    @Override
    public Uid createUser(AtlassianGuardUserModel newUser) throws AlreadyExistsException {
        return createUser.apply(newUser);
    }

    @Override
    public void patchUser(Uid uid, PatchOperationsModel operations) {
        patchUser.accept(uid, operations);
    }

    @Override
    public AtlassianGuardUserModel getUser(Uid uid, OperationOptions options, Set<String> fetchFieldsSet) throws UnknownUidException {
        return getUserByUid.apply(uid);
    }

    @Override
    public AtlassianGuardUserModel getUser(Name name, OperationOptions options, Set<String> fetchFieldsSet) throws UnknownUidException {
        return getUserByName.apply(name);
    }

    @Override
    public int getUsers(QueryHandler<AtlassianGuardUserModel> handler, OperationOptions options, Set<String> fetchFieldsSet, int pageSize, int pageOffset) {
        return getUsers.apply(handler, pageSize, pageOffset);
    }

    @Override
    public void deleteUser(Uid uid) {
        deleteUser.accept(uid);
    }

    // Group

    @Override
    public Uid createGroup(AtlassianGuardGroupModel group) throws AlreadyExistsException {
        return createGroup.apply(group);
    }

    @Override
    public void patchGroup(Uid uid, PatchOperationsModel operations) {
        patchGroup.accept(uid, operations);
    }

    @Override
    public AtlassianGuardGroupModel getGroup(Uid uid, OperationOptions options, Set<String> fetchFieldsSet) {
        return getGroupByUid.apply(uid);
    }

    @Override
    public AtlassianGuardGroupModel getGroup(Name name, OperationOptions options, Set<String> fetchFieldsSet) {
        return getGroupByName.apply(name);
    }

    @Override
    public int getGroups(QueryHandler<AtlassianGuardGroupModel> handler, OperationOptions options, Set<String> fetchFieldsSet, int pageSize, int pageOffset) {
        return getGroups.apply(handler, pageSize, pageOffset);
    }

    @Override
    public void deleteGroup(Uid uid) {
        deleteGroup.accept(uid);
    }

    // Mock Interface

    @FunctionalInterface
    public interface MockFunction<T, R> {
        R apply(T t);
    }

    @FunctionalInterface
    public interface MockBiFunction<T, U, R> {
        R apply(T t, U u);
    }

    @FunctionalInterface
    public interface MockTripleFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    @FunctionalInterface
    public interface MockConsumer<T> {
        void accept(T t);
    }

    @FunctionalInterface
    public interface MockBiConsumer<T, U> {
        void accept(T t, U u);
    }

    @FunctionalInterface
    public interface MockTripleConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }
}
