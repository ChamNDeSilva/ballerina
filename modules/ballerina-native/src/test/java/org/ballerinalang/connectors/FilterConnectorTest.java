/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.connectors;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test the functionality of filter connectors
 */
public class FilterConnectorTest {
    private ProgramFile programFile;

    @BeforeClass()
    public void setup() {
        //programFile = BTestUtils.getProgramFile("samples/connectors/filter-connector-test.bal");
    }

    @Test(description = "Test TestConnector action1")
    public void testConnectorAction1() {
        programFile = BTestUtils.getProgramFile("samples/connectors/filter-connector-test.bal");
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testAction1");
        Assert.assertEquals(returns.length, 1);
        BMessage actionReturned = (BMessage) returns[0];
        String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(actionReturned.stringValue(), expected);
    }

    @Test(description = "Test passing arguments for filter connectors")
    public void testFilterConnectorArgumentPassing() {
        programFile = BTestUtils.getProgramFile("samples/connectors/filter-connector-argument-passing-test.bal");
        BValue[] args = {new BString("WSO2")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testArgumentPassing", args);
        Assert.assertEquals(returns.length, 1);
        BInteger actionReturned = (BInteger) returns[0];
        int expected = 100;
        Assert.assertEquals(actionReturned.intValue(), expected);
    }
}
