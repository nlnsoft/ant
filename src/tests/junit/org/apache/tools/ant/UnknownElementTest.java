/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.tools.ant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UnknownElementTest extends BuildFileTest {
    public void setUp() {
        configureProject("src/etc/testcases/core/unknownelement.xml");
    }
    public void testMaybeConfigure() {
        // make sure we do not get a NPE
        executeTarget("testMaybeConfigure");
    }

    /**
     * Not really a UnknownElement test but rather one of "what
     * information is available in taskFinished".
     * @see https://issues.apache.org/bugzilla/show_bug.cgi?id=26197
     */
    public void XtestTaskFinishedEvent() {
        getProject().addBuildListener(new BuildListener() {
                public void buildStarted(BuildEvent event) {}
                public void buildFinished(BuildEvent event) {}
                public void targetStarted(BuildEvent event) {}
                public void targetFinished(BuildEvent event) {}
                public void taskStarted(BuildEvent event) {
                    assertTaskProperties(event.getTask());
                }
                public void taskFinished(BuildEvent event) {
                    assertTaskProperties(event.getTask());
                }
                public void messageLogged(BuildEvent event) {}
                private void assertTaskProperties(Task ue) {
                    assertNotNull(ue);
                    assertTrue(ue instanceof UnknownElement);
                    Task t = ((UnknownElement) ue).getTask();
                    assertNotNull(t);
                    assertEquals("org.apache.tools.ant.taskdefs.Echo",
                                 t.getClass().getName());
                }
            });
        executeTarget("echo");
    }

    public static class Child extends Task {
        Parent parent;
        public void injectParent(Parent parent) {
            this.parent = parent;
        }
        public void execute() {
            parent.fromChild();
        }
    }

    public static class Parent extends Task implements TaskContainer {
        List children = new ArrayList();
        public void addTask(Task t) {
            children.add(t);
        }

        public void fromChild() {
            log("fromchild");
        }

        public void execute() {
            for (Iterator i = children.iterator(); i.hasNext();) {
                UnknownElement el = (UnknownElement) i.next();
                el.maybeConfigure();
                Child child = (Child) el.getRealThing();
                child.injectParent(this);
                child.perform();
            }
        }
    }
}


