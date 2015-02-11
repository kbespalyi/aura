/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.test.root.parser.handler;

import javax.xml.stream.XMLStreamReader;

import org.auraframework.def.ComponentDef;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.ThemeDef;
import org.auraframework.impl.AuraImplTestCase;
import org.auraframework.impl.root.component.ComponentDefImpl;
import org.auraframework.impl.root.parser.XMLParser;
import org.auraframework.impl.root.parser.handler.ComponentDefHandler;
import org.auraframework.impl.source.StringSource;
import org.auraframework.impl.system.DefDescriptorImpl;
import org.auraframework.system.Parser.Format;
import org.auraframework.system.Source;
import org.auraframework.throwable.quickfix.InvalidDefinitionException;
import org.auraframework.throwable.quickfix.QuickFixException;

public class ComponentDefHandlerTest extends AuraImplTestCase {
    XMLStreamReader xmlReader;
    ComponentDefHandlerOverride cdHandler;
    XMLParser parser = XMLParser.getInstance();

    public ComponentDefHandlerTest(String name) {
        super(name);
    }

    private static class ComponentDefHandlerOverride extends ComponentDefHandler {
        public ComponentDefHandlerOverride(DefDescriptor<ComponentDef> componentDefDescriptor,
                Source<?> source, XMLStreamReader xmlReader) {
            super(componentDefDescriptor, source, xmlReader);
        }

        @Override
        public void readAttributes() throws QuickFixException {
            super.readAttributes();
        }

        @Override
        public ComponentDefImpl createDefinition() throws QuickFixException {
            return (ComponentDefImpl)super.createDefinition();
        }
    };

    @Override
    public void setUp() throws Exception {
        super.setUp();
        StringSource<ComponentDef> source = new StringSource<>(vendor.getComponentDefDescriptor(),
                "<aura:component controller='" + vendor.getControllerDescriptor().getQualifiedName() + "' extends='"
                        + vendor.getParentComponentDefDescriptor() + "' implements='"
                        + vendor.getInterfaceDefDescriptor()
                        + "' abstract='true'>Child Text<aura:foo/></aura:component>", "myID", Format.XML);
        xmlReader = XMLParser.getInstance().createXMLStreamReader(source.getHashingReader());
        xmlReader.next();
        cdHandler = new ComponentDefHandlerOverride(vendor.getComponentDefDescriptor(), source, xmlReader);
    }

    public void testReadAttributes() throws Exception {
        cdHandler.readAttributes();
        ComponentDefImpl cd = cdHandler.createDefinition();
        assertEquals(vendor.getParentComponentDefDescriptor(), cd.getExtendsDescriptor());
        assertEquals(vendor.getInterfaceDefDescriptor(), cd.getInterfaces().iterator().next());
        assertTrue(cd.isAbstract());
        assertTrue(cd.isExtensible());
        assertFalse(cd.isTemplate());
    }

    public void testGetHandledTag() {
        assertEquals("aura:component", cdHandler.getHandledTag());
    }

    public void testDuplicateAttributeNames() throws Exception {
        DefDescriptor<ComponentDef> descriptor = DefDescriptorImpl.getInstance("test:fakeparser", ComponentDef.class);
        StringSource<ComponentDef> source = new StringSource<>(descriptor,
                "<aura:component><aura:attribute name=\"implNumber\" type=\"String\"/>"
                        + "<aura:attribute name=\"implNumber\" type=\"String\"/></aura:component>", "myID", Format.XML);
        ComponentDef cd = parser.parse(descriptor, source);
        try {
            cd.validateDefinition();
            fail("Should have thrown Exception. Two attributes with the same name cannot exist");
        } catch (InvalidDefinitionException expected) {
            checkExceptionContains(expected, InvalidDefinitionException.class,
                    "There is already an attribute named 'implNumber' on component 'test:fakeparser'.");
        }
    }

    /**
     * An attribute cannot be assigned multiple times on a System tag.
     * 
     * @throws Exception
     */
    public void testDuplicateAttributeOnSystemTag() throws Exception {
        DefDescriptor<ComponentDef> descriptor = DefDescriptorImpl.getInstance("test:fakeparser", ComponentDef.class);
        StringSource<ComponentDef> source = new StringSource<>(descriptor,
                "<aura:component extends='test:fakeAbstract' extends='test:fakeAbstractParent'></aura:component>",
                "myID", Format.XML);
        ComponentDef cd = parser.parse(descriptor, source);
        try {
            cd.validateDefinition();
            fail("Should have thrown Exception. Same attribute specified twice on aura:component tag.");
        } catch (InvalidDefinitionException expected) {
        }
    }

    /**
     * Verify that an attribute cannot be assigned a blank value.
     */
    public void testBlankValueForSystemTag() throws Exception {
        DefDescriptor<ComponentDef> descriptor = DefDescriptorImpl.getInstance("test:fakeparser", ComponentDef.class);
        StringSource<ComponentDef> source = new StringSource<>(descriptor,
                "<aura:component extends=''></aura:component>", "myID", Format.XML);
        ComponentDef cd = parser.parse(descriptor, source);
        try {
            cd.validateDefinition();
            fail("Should have thrown Exception. Attribute value cannot be blank.");
        } catch (InvalidDefinitionException expected) {
        }
    }

    /** tests that themes in the component bundle are correctly associated */
    public void testLocalComponentTheme() throws Exception {
        DefDescriptor<ThemeDef> themeDesc = addSourceAutoCleanup(ThemeDef.class, "<aura:theme/>");
        String fmt = String.format("%s:%s", themeDesc.getNamespace(), themeDesc.getName());
        DefDescriptor<ComponentDef> cmpDesc = DefDescriptorImpl.getInstance(fmt, ComponentDef.class);
        addSourceAutoCleanup(cmpDesc, "<aura:component/>");
        assertEquals(themeDesc, cmpDesc.getDef().getCmpTheme());
    }
}
