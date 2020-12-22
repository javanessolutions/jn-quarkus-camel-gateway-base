/*
 * Copyright (c) 2017 Javanes Solutions S.A. de C.V. All rights reserved.
 *
 * Licensed under the GNU General Public License, Version 3 (the 
 * "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.javanes.micro.quarkus.camel.gateway.processor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import io.quarkus.runtime.annotations.RegisterForReflection;

@ApplicationScoped
@Named("prepareCallProcessor")
@RegisterForReflection
public class AppPrepareCallProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.getMessage().setHeader("exchangeId", exchange.getExchangeId());
        Integer prefix = exchange.getIn().getHeader("jn-prefix",Integer.class);
        String path = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
        if(prefix != null){
            path = path.substring(prefix);
        }
        exchange.getMessage().setHeader(Exchange.HTTP_PATH, path);
    }
    
}
