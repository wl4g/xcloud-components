/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.component.integration.sharding.util;

import static com.wl4g.component.common.lang.StringUtils2.eqIgnCase;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import java.net.InetAddress;

import com.wl4g.component.common.log.SmartLogger;

/**
 * {@link HostUtil}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-20 v1.0.0
 * @since v1.0.0
 */
public abstract class HostUtil {
    protected static final SmartLogger log = getLogger(HostUtil.class);

    public static boolean isSameHost(String host1, String host2) {
        if (eqIgnCase(host1, host2)) {
            return true;
        }
        try {
            InetAddress h1 = InetAddress.getByName(host1);
            InetAddress h2 = InetAddress.getByName(host2);
            if (eqIgnCase(h1.getHostName(), h2.getHostName())) {
                return true;
            } else if (eqIgnCase(h1.getCanonicalHostName(), h2.getCanonicalHostName())) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            // log.error(format("Unable to compare hosts. '%s' and
            // '%s'",host1,host2),e);
            throw new IllegalStateException(e);
        }
    }

}
