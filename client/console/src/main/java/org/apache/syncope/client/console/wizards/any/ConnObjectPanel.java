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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.syncope.client.console.commons.ConnIdSpecialName;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.lib.to.ConnObjectTO;
import org.apache.syncope.client.console.wicket.markup.html.form.AjaxTextFieldPanel;
import org.apache.syncope.client.console.wicket.markup.html.form.MultiFieldPanel;
import org.apache.syncope.common.lib.EntityTOUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;

public class ConnObjectPanel extends Panel {

    private static final long serialVersionUID = -6469290753080058487L;

    public ConnObjectPanel(
            final String id,
            final Pair<IModel<?>, IModel<?>> titles,
            final Pair<ConnObjectTO, ConnObjectTO> connObjectTOs,
            final boolean hideLeft) {

        super(id);

        final IModel<List<String>> formProps = new LoadableDetachableModel<List<String>>() {

            private static final long serialVersionUID = 5275935387613157437L;

            @Override
            protected List<String> load() {
                List<AttrTO> right = new ArrayList<>(connObjectTOs == null || connObjectTOs.getRight() == null
                        ? Collections.<AttrTO>emptyList()
                        : connObjectTOs.getRight().getAttrs());
                List<AttrTO> left = new ArrayList<>(connObjectTOs == null || connObjectTOs.getLeft() == null
                        ? Collections.<AttrTO>emptyList()
                        : connObjectTOs.getLeft().getAttrs());

                List<String> schemas = ListUtils.sum(
                        CollectionUtils.collect(right, new Transformer<AttrTO, String>() {

                            @Override
                            public String transform(final AttrTO input) {
                                return input.getSchema();
                            }
                        }, new ArrayList<String>()),
                        CollectionUtils.collect(left, new Transformer<AttrTO, String>() {

                            @Override
                            public String transform(final AttrTO input) {
                                return input.getSchema();
                            }
                        }, new ArrayList<String>()));
                Collections.sort(schemas);
                return schemas;
            }
        };

        add(new Label("leftTitle", titles.getLeft()).setOutputMarkupPlaceholderTag(true).setVisible(!hideLeft));
        add(new Label("rightTitle", titles.getRight()));

        final Map<String, AttrTO> leftProfile = connObjectTOs == null || connObjectTOs.getLeft() == null
                ? null
                : EntityTOUtils.buildAttrMap(connObjectTOs.getLeft().getAttrs());
        final Map<String, AttrTO> rightProfile = connObjectTOs == null || connObjectTOs.getRight() == null
                ? null
                : EntityTOUtils.buildAttrMap(connObjectTOs.getRight().getAttrs());
        ListView<String> propView = new ListView<String>("propView", formProps) {

            private static final long serialVersionUID = 3109256773218160485L;

            @Override
            protected void populateItem(final ListItem<String> item) {
                final String prop = item.getModelObject();

                final Fragment valueFragment;
                final AttrTO left = leftProfile == null ? null : leftProfile.get(prop);
                final AttrTO right = rightProfile == null ? null : rightProfile.get(prop);

                valueFragment = new Fragment("value", "doubleValue", ConnObjectPanel.this);
                valueFragment.add(getValuePanel("leftAttribute", prop, left).
                        setOutputMarkupPlaceholderTag(true).setVisible(!hideLeft));
                valueFragment.add(getValuePanel("rightAttribute", prop, right));

                if (left == null || right == null
                        || (CollectionUtils.isNotEmpty(right.getValues())
                        && CollectionUtils.isEmpty(left.getValues()))
                        || (CollectionUtils.isEmpty(right.getValues())
                        && CollectionUtils.isNotEmpty(left.getValues()))
                        || (CollectionUtils.isNotEmpty(right.getValues())
                        && CollectionUtils.isNotEmpty(left.getValues())
                        && right.getValues().size() != left.getValues().size())
                        || (CollectionUtils.isNotEmpty(right.getValues())
                        && CollectionUtils.isNotEmpty(left.getValues())
                        && !right.getValues().equals(left.getValues()))) {

                    valueFragment.add(new Behavior() {

                        private static final long serialVersionUID = 3109256773218160485L;

                        @Override
                        public void onComponentTag(final Component component, final ComponentTag tag) {
                            tag.put("class", "highlight");
                        }
                    });
                }
                item.add(valueFragment);
            }
        };
        add(propView);
    }

    /**
     * Get panel for attribute value (not remote status).
     *
     * @param id component id to be replaced with the fragment content.
     * @param attrTO remote attribute.
     * @return fragment.
     */
    private Panel getValuePanel(final String id, final String schemaName, final AttrTO attrTO) {
        Panel field;
        if (attrTO == null) {
            field = new AjaxTextFieldPanel(id, schemaName, new Model<String>());
        } else if (CollectionUtils.isEmpty(attrTO.getValues())) {
            field = new AjaxTextFieldPanel(id, schemaName, new Model<String>());
        } else if (ConnIdSpecialName.PASSWORD.equals(schemaName)) {
            field = new AjaxTextFieldPanel(id, schemaName, new Model<>("********"));
        } else if (attrTO.getValues().size() == 1) {
            field = new AjaxTextFieldPanel(id, schemaName, new Model<>(attrTO.getValues().get(0)));
        } else {
            field = new MultiFieldPanel.Builder<>(new ListModel<>(attrTO.getValues())).build(
                    id,
                    schemaName,
                    new AjaxTextFieldPanel("panel", schemaName, new Model<String>()));
        }

        field.setEnabled(false);
        return field;
    }
}