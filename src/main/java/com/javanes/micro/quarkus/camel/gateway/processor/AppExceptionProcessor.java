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

import com.javanes.micro.quarkus.camel.gateway.pojo.AppExceptionResponse;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import io.quarkus.runtime.annotations.RegisterForReflection;

@ApplicationScoped
@Named("exceptionProcessor")
@RegisterForReflection
public class AppExceptionProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Throwable caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);

        AppExceptionResponse response = new AppExceptionResponse();
        response.setCode(500);
        response.setExchangeId(exchange.getExchangeId());

        if (caused != null) {
            response.setMessage(caused.getMessage());
        } else {
            response.setMessage("Sin información del error.");
        }

        exchange.getMessage().setBody(response);
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
    }

}
