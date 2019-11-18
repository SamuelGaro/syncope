/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.console.wizards.any;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.syncope.client.console.rest.UserRestClient;
import org.apache.syncope.client.console.wicket.markup.html.bootstrap.dialog.BaseModal.ModalEvent;
import org.apache.syncope.client.console.wizards.AjaxWizard;
import org.apache.syncope.client.console.wizards.AjaxWizardBuilder;
import org.apache.syncope.common.lib.patch.LinkedAccountPatch;
import org.apache.syncope.common.lib.patch.UserPatch;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.lib.to.LinkedAccountTO;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.wizard.WizardModel;

/**
 * Accounts wizard builder.
 */
public class LinkedAccountWizardBuilder extends AjaxWizardBuilder<LinkedAccountTO> {

    private static final long serialVersionUID = -9142332740863374891L;

    private final UserRestClient userRestClient = new UserRestClient();

    private UserTO userTO;

    private final String userKey;

    public LinkedAccountWizardBuilder(final String userKey, final PageReference pageRef) {
        super(new LinkedAccountTO(), pageRef);
        this.userKey = userKey;
        this.userTO = userRestClient.read(userKey);
    }

    @Override
    public AjaxWizard<LinkedAccountTO> build(final String id, final AjaxWizard.Mode mode) {
        this.userTO = userRestClient.read(userKey);
        return super.build(id, mode);
    }

    @Override
    protected WizardModel buildModelSteps(final LinkedAccountTO modelObject, final WizardModel wizardModel) {
        wizardModel.add(new LinkedAccountDetailsPanel(modelObject));
        wizardModel.add(new LinkedAccountCredentialsPanel(modelObject));
        wizardModel.add(new LinkedAccountPlainAttrsPanel(new EntityWrapper<>(modelObject), userTO));
        wizardModel.add(new LinkedAccountPrivilegesPanel(modelObject));
        return wizardModel;
    }

    @Override
    protected Serializable onApplyInternal(final LinkedAccountTO modelObject) {
        fixPlainAttrs(modelObject);

        LinkedAccountPatch linkedAccountPatch = new LinkedAccountPatch.Builder().linkedAccountTO(modelObject).build();
        linkedAccountPatch.setLinkedAccountTO(modelObject);
        UserPatch patch = new UserPatch();
        patch.setKey(userTO.getKey());
        patch.getLinkedAccounts().add(linkedAccountPatch);
        userRestClient.update(userTO.getETagValue(), patch);

        return modelObject;
    }

    private void fixPlainAttrs(final LinkedAccountTO linkedAccountTO) {
        Set<AttrTO> validAttrs = new HashSet<>(linkedAccountTO.getPlainAttrs().stream().
                filter(attr -> !attr.getValues().isEmpty()).
                collect(Collectors.toSet()));
        linkedAccountTO.getPlainAttrs().clear();
        linkedAccountTO.getPlainAttrs().addAll(validAttrs);
    }

    @Override
    protected Serializable getCreateCustomPayloadEvent(final Serializable afterObject, final AjaxRequestTarget target) {
        LinkedAccountTO linkedAccountTO = LinkedAccountTO.class.cast(afterObject);
        return new CreateEvent(
                linkedAccountTO.getConnObjectKeyValue(),
                userTO,
                target);
    }

    private static class CreateEvent extends ModalEvent {

        private static final long serialVersionUID = 6416834092156281986L;

        private final String key;

        private final UserTO userTO;

        CreateEvent(
                final String key,
                final UserTO userTO,
                final AjaxRequestTarget target) {

            super(target);
            this.key = key;
            this.userTO = userTO;
        }

        public String getKey() {
            return key;
        }

        public UserTO getUserTO() {
            return userTO;
        }
    }

}