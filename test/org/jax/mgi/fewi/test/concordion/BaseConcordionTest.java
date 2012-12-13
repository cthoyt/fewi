package org.jax.mgi.fewi.test.concordion;

import org.concordion.api.ResultSummary;
import org.concordion.internal.ConcordionBuilder;
import org.jax.mgi.fewi.test.data.DynamicTestData;
import org.jax.mgi.fewi.test.mock.MockQuery;
import org.jax.mgi.fewi.test.mock.MockQueryFactory;
import org.jax.mgi.fewi.test.mock.MockRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:applicationContext-ci.xml"})
@TransactionConfiguration
@Transactional
public class BaseConcordionTest {

    // The classes are required for mocking http requests (as of Spring 3.1) and are autowired via spring's DI
	@Autowired
    protected RequestMappingHandlerAdapter handler;
    @Autowired
    protected RequestMappingHandlerMapping handlerMapping;
    
    /**
     * 
     * Use spring test runner to setup and run concordion unit tests
     * 
     * @throws Exception
     */
	@Test
    public void run() throws Exception {
		// kicks off concordion process that runs your actual tests
		// Make custom extensions available to concordion
	    System.setProperty("concordion.extensions", "org.jax.mgi.fewi.test.concordion.FewiConcordionExtension");
	    
        ResultSummary resultSummary = new ConcordionBuilder().build().process(this);
        resultSummary.print(System.out, this);
        resultSummary.assertIsSatisfied(this);
		//ConcordionUtils.initConcordionTestCase(this);
    }
	
	// Makes the dynamic test data available to concordion tests
	public static String get(String id)
	{
		return DynamicTestData.get(id);
	}
	
	/*
	 * A pre-initialised, but blank mockRequest object to play with
	 */
	public MockRequest mockRequest() throws Exception
	{
		return new MockRequest(handlerMapping,handler);
	}
	/*
	 * Use when you need to get a MAV object from a particular fewi url
	 * Usage:
	 * mockRequest("/alelle/phenotable/MGI:123345");
	 */
	public MockRequest mockRequest(String url) throws Exception
	{
		return new MockRequest(handlerMapping,handler,url);
	}
	/* Convenience method */
	public Object mockRequestGet(String url, String attributeName) throws Exception
	{
		return this.mockRequest(url).get(attributeName);
	}
	
	/*
	 * The preferred way of creating mock query objects.
	 * All the different types can be accessed through the factory, only needing to pass in
	 * an autowired controller if necessary.
	 * Usage:
	 * getMockQuery.gxdHttpQuery();
	 * getMockQuery.gxdControllerQuery(gxdController);
	 */
	public MockQueryFactory getMockQuery() throws Exception
	{
		return new MockQueryFactory(new MockRequest(handlerMapping,handler));
	}
}
