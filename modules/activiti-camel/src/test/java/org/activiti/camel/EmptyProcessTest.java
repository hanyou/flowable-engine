/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.activiti.camel;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class EmptyProcessTest extends SpringActivitiTestCase {

  @Autowired
  CamelContext camelContext;
  
  static boolean configured = false;
	
  @BeforeClass
  public void  setUp() throws Exception {
	  if  (configured)
		  return;
	  configured = true;
      camelContext.addRoutes(new RouteBuilder() {

		@Override
		public void configure() throws Exception {
		      from("direct:startEmpty").to("activiti:emptyProcess");
		      from("direct:startEmptyWithHeader").setHeader("MyVar", constant("Foo")).to("activiti:emptyProcess?copyVariablesFromHeader=true");
		      from("direct:startEmptyBodyAsString").to("activiti:emptyProcess?copyBodyToCamelBodyAsString=true");
		}
	});
 }
  
  @Deployment(resources = {"process/empty.bpmn20.xml"})
  public void testRunProcessWithHeader() throws Exception {
   
    ProducerTemplate tpl = camelContext.createProducerTemplate();
    String body = "body text";
    String instanceId = (String) tpl.requestBody("direct:startEmptyWithHeader", body);
    assertProcessEnded(instanceId);
    HistoricVariableInstance var = processEngine.getHistoryService().createHistoricVariableInstanceQuery().variableName("camelBody").singleResult();
    assertNotNull(var);
    assertEquals(body, var.getValue());
    var = processEngine.getHistoryService().createHistoricVariableInstanceQuery().variableName("MyVar").singleResult();
    assertNotNull(var);
    assertEquals("Foo", var.getValue());
  }
  
  @Deployment(resources = {"process/empty.bpmn20.xml"})
  public void testObjectAsVariable() throws Exception {
    CamelContext ctx = applicationContext.getBean(CamelContext.class);
    ProducerTemplate tpl = ctx.createProducerTemplate();
    Object expectedObj = new Long(99);
    String instanceId = (String) tpl.requestBody("direct:startEmpty", expectedObj);
    assertProcessEnded(instanceId);
    HistoricVariableInstance var = processEngine.getHistoryService().createHistoricVariableInstanceQuery().variableName("camelBody").singleResult();
    assertNotNull(var);
    assertEquals(expectedObj, var.getValue());
  }
  
  @Deployment(resources = {"process/empty.bpmn20.xml"})
  public void testObjectAsStringVariable() throws Exception {
    CamelContext ctx = applicationContext.getBean(CamelContext.class);
    ProducerTemplate tpl = ctx.createProducerTemplate();
    Object expectedObj = new Long(99);

    String instanceId = (String) tpl.requestBody("direct:startEmptyBodyAsString", expectedObj);
    assertProcessEnded(instanceId);
    HistoricVariableInstance var = processEngine.getHistoryService().createHistoricVariableInstanceQuery().variableName("camelBody").singleResult();
    assertNotNull(var);
    assertEquals(expectedObj.toString(), var.getValue().toString());
  }
}
