/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.bvt.filter.wall;

import junit.framework.TestCase;

import org.junit.Assert;

import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallUtils;

/**
 * 这个场景，检测
 * 
 * @author wenshao
 */
public class WallUpdateTest3 extends TestCase {

    private String     sql    = "UPDATE T_USER SET FNAME = ? WHERE 1 = 1";
    private String     sql2   = "UPDATE T_USER SET FNAME = ? WHERE id = 1 or1 = 1";

    private WallConfig config = new WallConfig();

    protected void setUp() throws Exception {
        config.setUpdateAllow(true);
    }

    public void testMySql() throws Exception {
        Assert.assertTrue(WallUtils.isValidateMySql(sql, config));
        Assert.assertFalse(WallUtils.isValidateMySql(sql2, config));
    }

    public void testORACLE() throws Exception {
        Assert.assertTrue(WallUtils.isValidateOracle(sql, config));
        Assert.assertFalse(WallUtils.isValidateOracle(sql2, config));
    }
}
