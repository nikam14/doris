// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.analysis;

import org.apache.doris.common.AnalysisException;
import org.apache.doris.common.Config;
import org.apache.doris.common.UserException;
import org.apache.doris.mysql.privilege.AccessControllerManager;
import org.apache.doris.mysql.privilege.MockedAuth;
import org.apache.doris.qe.ConnectContext;

import mockit.Mocked;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AlterClusterStmtTest {

    private static Analyzer analyzer;

    @Mocked
    private AccessControllerManager accessManager;
    @Mocked
    private ConnectContext ctx;

    @Before()
    public void setUp() {
        Config.disable_cluster_feature = false;
        analyzer = AccessTestUtil.fetchAdminAnalyzer(true);
        MockedAuth.mockedAccess(accessManager);
        MockedAuth.mockedConnectContext(ctx, "root", "192.168.1.1");
    }

    @Test
    public void testAnalyzeNormal() throws UserException, AnalysisException {
        final Map<String, String> properties = new HashMap();
        properties.put("instance_num", "2");
        final AlterClusterStmt stmt = new AlterClusterStmt("testCluster", properties);
        stmt.analyze(analyzer);
        Assert.assertEquals("testCluster", stmt.getAlterClusterName());
        String sql = "ALTER CLUSTER " + "testCluster" + " PROPERTIES(\"instance_num\"=" + "\"" + "2" + "\")";
        Assert.assertEquals(sql, stmt.toSql());
    }

    @Test(expected = AnalysisException.class)
    public void testNoPropertiesFail() throws UserException, AnalysisException {
        final AlterClusterStmt stmt = new AlterClusterStmt("testCluster", null);
        stmt.analyze(analyzer);
        Assert.fail("no exception");
    }

    @Test(expected = AnalysisException.class)
    public void testParamNumberFormatError() throws UserException, AnalysisException {
        final Map<String, String> properties = new HashMap();
        properties.put("instance_num", "0xfffffff");
        final AlterClusterStmt stmt = new AlterClusterStmt("testCluster", properties);
        stmt.analyze(analyzer);
        Assert.fail("no exception");
    }

}
