package uk.nhs.leedsth.hapifhir;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.rest.server.EncodingEnum;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;

import ca.uhn.fhir.rest.server.IResourceProvider;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
public class JorvikServlet extends RestfulServer {
	
	private static final long serialVersionUID = 1L;

	// Need to add in features from https://github.com/jamesagnew/hapi-fhir/blob/master/hapi-fhir-jpaserver-example/src/main/java/ca/uhn/fhir/jpa/demo/JpaServerDemo.java
	
	//private DataSource dataSource;
	/*
	public DaoConfig daoConfig;
	
	@Autowired
	public void setDaoConfig( DaoConfig daoConfig ) {
		this.daoConfig = daoConfig;
		//this.jdbcTemplate = new JdbcTemplate(dataSource);
	 }
	*/
	
	@Autowired
	private EntityManagerFactory entityManagerFactory;

	
	@Autowired
	private SessionFactory sessionFactory;
	
	
	private WebApplicationContext myAppCtx;
	/**
	 * Constructor
	 */
	public JorvikServlet() {
		super(FhirContext.forDstu3()); // Support DSTU2
	}
	private static final Logger log = LoggerFactory.getLogger(uk.nhs.leedsth.hapifhir.JorvikServlet.class);
	/**
	 * This method is called automatically when the
	 * servlet is initializing.
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize() throws ServletException {
		super.initialize();
		
		FhirVersionEnum fhirVersion = FhirVersionEnum.DSTU3;
		setFhirContext(new FhirContext(fhirVersion));
		
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		
		
		String resourceProviderBeanName = "";
		if (fhirVersion == FhirVersionEnum.DSTU3) {
			resourceProviderBeanName = "myResourceProvidersDstu3";
		} else {
			throw new IllegalStateException();
		}
		List<IResourceProvider> beans = myAppCtx.getBean(resourceProviderBeanName, List.class);
		setResourceProviders(beans);
		
		if (beans==null)
		{
			log.info("HEINZ 57 Error");
		}
		else
		{
			log.info("HEINZ 57 = "+beans.toString());
		}
		if (sessionFactory != null)
		{
			log.info("session");
		}
		else
		{
			log.info("session NULL");
			sessionFactory = myAppCtx.getBean(SessionFactory.class);
			if (sessionFactory != null)
			{
				log.info("session 2nd Attempt");
			}
		}
		if (entityManagerFactory != null)
		{
			log.info("entityManagerFactory");
		}
		else
		{
			log.info("entityManagerFactory NULL");
		}
		
		
		/*
		IFhirSystemDao<org.hl7.fhir.dstu3.model.Bundle, Meta> systemDao = myAppCtx.getBean("mySystemDaoDstu3", IFhirSystemDao.class);
		JpaConformanceProviderDstu3 confProvider = new JpaConformanceProviderDstu3(this, systemDao, myAppCtx.getBean(DaoConfig.class));
		confProvider.setImplementationDescription("Example Server");
		setServerConformanceProvider(confProvider);
		*/
		
		/*
		 * Use a narrative generator. This is a completely optional step, 
		 * but can be useful as it causes HAPI to generate narratives for
		 * resources which don't otherwise have one.
		 */
		INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
		FhirContext ctx = getFhirContext();
		
		ctx.setNarrativeGenerator(narrativeGen);
		setDefaultResponseEncoding(EncodingEnum.JSON);
		/*
		 * This server interceptor causes the server to return nicely
		 * formatter and coloured responses instead of plain JSON/XML if
		 * the request is coming from a browser window. It is optional,
		 * but can be nice for testing.
		 */
		registerInterceptor(new ResponseHighlighterInterceptor());
		
		/*
		 * Tells the server to return pretty-printed responses by default
		 */
		setDefaultPrettyPrint(true);
		
	}
	
}
