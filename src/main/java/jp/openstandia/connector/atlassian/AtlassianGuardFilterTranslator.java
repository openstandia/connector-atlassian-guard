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

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;

public class AtlassianGuardFilterTranslator extends AbstractFilterTranslator<AtlassianGuardFilter> {

    private static final Log LOG = Log.getLog(AtlassianGuardFilterTranslator.class);

    private final OperationOptions options;
    private final ObjectClass objectClass;

    public AtlassianGuardFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        this.objectClass = objectClass;
        this.options = options;
    }

    @Override
    protected AtlassianGuardFilter createEqualsExpression(EqualsFilter filter, boolean not) {
        if (not) {
            return null;
        }
        Attribute attr = filter.getAttribute();

        if (attr instanceof Uid) {
            Uid uid = (Uid) attr;
            AtlassianGuardFilter uidFilter = new AtlassianGuardFilter(uid.getName(),
                    AtlassianGuardFilter.FilterType.EXACT_MATCH,
                    uid);
            return uidFilter;
        }
        if (attr instanceof Name) {
            Name name = (Name) attr;
            AtlassianGuardFilter nameFilter = new AtlassianGuardFilter(name.getName(),
                    AtlassianGuardFilter.FilterType.EXACT_MATCH,
                    name);
            return nameFilter;
        }

        // Not supported searching by other attributes
        return null;
    }

    @Override
    protected AtlassianGuardFilter createContainsAllValuesExpression(ContainsAllValuesFilter filter, boolean not) {
        if (not) {
            return null;
        }
        Attribute attr = filter.getAttribute();

        // Unfortunately, Atlassian Guard doesn't support "groups" attribute in User schema.
        // So IDM try to fetch the groups which the user belongs to by using ContainsAllValuesFilter.
        if (objectClass.equals(AtlassianGuardGroupHandler.GROUP_OBJECT_CLASS) &&
                attr.getName().equals("members.User.value")) {
            AtlassianGuardFilter filterGroupByMember = new AtlassianGuardFilter(attr.getName(),
                    AtlassianGuardFilter.FilterType.EXACT_MATCH,
                    attr);
            return filterGroupByMember;
        }

        // Not supported searching by other attributes
        return null;
    }
}
