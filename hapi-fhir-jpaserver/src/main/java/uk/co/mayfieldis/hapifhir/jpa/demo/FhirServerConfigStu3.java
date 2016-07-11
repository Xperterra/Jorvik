package uk.co.mayfieldis.hapifhir.jpa.demo;

import java.sql.Driver;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ca.uhn.fhir.jpa.config.BaseJavaConfigDstu3;
import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.util.SubscriptionsRequireManualActivationInterceptorDstu3;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;

public class FhirServerConfigStu3 extends BaseJavaConfigDstu3 {
	@Configuration
	@EnableTransactionManagement()
	@PropertySource("classpath:HAPIJPA.properties")
	public class FhirServerConfigDstu3 extends BaseJavaConfigDstu3 {

		/**
		 * Configure FHIR properties around the the JPA server via this bean
		 */
		
		@Autowired
		protected Environment env;
		
		@Bean()
		public DaoConfig daoConfig() {
			DaoConfig retVal = new DaoConfig();
			retVal.setSubscriptionEnabled(true);
			retVal.setSubscriptionPollDelay(5000);
			retVal.setSubscriptionPurgeInactiveAfterMillis(DateUtils.MILLIS_PER_HOUR);
			retVal.setAllowMultipleDelete(true);
			return retVal;
		}

		/**
		 * The following bean configures the database connection. The 'url' property value of "jdbc:derby:directory:jpaserver_derby_files;create=true" indicates that the server should save resources in a
		 * directory called "jpaserver_derby_files".
		 * 
		 * A URL to a remote database could also be placed here, along with login credentials and other properties supported by BasicDataSource.
		 */
		@Bean
		public DataSource dataSource() {
			
			/*
			BasicDataSource retVal = new BasicDataSource();
			
			try {
				retVal.setDriver(new com.mysql.jdbc.Driver());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			
			SimpleDriverDataSource retVal = new SimpleDriverDataSource();
		    
		    try {
		     	 @SuppressWarnings("unchecked")
		         Class<? extends Driver> driverClass = (Class<? extends Driver>) Class.forName(env.getProperty("jdbc.Driver"));
		     	retVal.setDriverClass(driverClass);	      
		    } catch (Exception e) {
		     //  log.error("Error loading driver class", e);
		    }
			
			retVal.setUrl(env.getProperty("jdbc.url"));
			
			retVal.setUsername(env.getProperty("jdbc.username"));
			retVal.setPassword(env.getProperty("jdbc.password"));
			
			return retVal;
		}


		@Bean()
		public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
			LocalContainerEntityManagerFactoryBean retVal = new LocalContainerEntityManagerFactoryBean();
			retVal.setPersistenceUnitName("HAPI_PU");
			retVal.setDataSource(dataSource());
			retVal.setPackagesToScan("ca.uhn.fhir.jpa.entity");
			retVal.setPersistenceProvider(new HibernatePersistenceProvider());
			retVal.setJpaProperties(jpaProperties());
			return retVal;
		}

		private Properties jpaProperties() {
			Properties extraProperties = new Properties();
			extraProperties.put("hibernate.dialect", org.hibernate.dialect.MySQLDialect.class.getName());
			extraProperties.put("hibernate.format_sql", "true");
			extraProperties.put("hibernate.show_sql", "false");
			extraProperties.put("hibernate.hbm2ddl.auto", "update");
			extraProperties.put("hibernate.jdbc.batch_size", "20");
			extraProperties.put("hibernate.cache.use_query_cache", "false");
			extraProperties.put("hibernate.cache.use_second_level_cache", "false");
			extraProperties.put("hibernate.cache.use_structured_entries", "false");
			extraProperties.put("hibernate.cache.use_minimal_puts", "false");
			extraProperties.put("hibernate.search.default.directory_provider", "filesystem");
			// needed to set properties of this directory sudo chmod -R 777 .
			extraProperties.put("hibernate.search.default.indexBase", "/Development/lucene/indexes");
			extraProperties.put("hibernate.search.lucene_version", "LUCENE_CURRENT");
//			extraProperties.put("hibernate.search.default.worker.execution", "async");
			return extraProperties;
		}

		/**
		 * Do some fancy logging to create a nice access log that has details about each incoming request.
		 */
		public IServerInterceptor loggingInterceptor() {
			LoggingInterceptor retVal = new LoggingInterceptor();
			retVal.setLoggerName("fhirtest.access");
			retVal.setMessageFormat(
					"Path[${servletPath}] Source[${requestHeader.x-forwarded-for}] Operation[${operationType} ${operationName} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] ResponseEncoding[${responseEncodingNoDefault}]");
			retVal.setLogExceptions(true);
			retVal.setErrorMessageFormat("ERROR - ${requestVerb} ${requestUrl}");
			return retVal;
		}

		/**
		 * This interceptor adds some pretty syntax highlighting in responses when a browser is detected
		 */
		@Bean(autowire = Autowire.BY_TYPE)
		public IServerInterceptor responseHighlighterInterceptor() {
			ResponseHighlighterInterceptor retVal = new ResponseHighlighterInterceptor();
			return retVal;
		}

		@Bean(autowire = Autowire.BY_TYPE)
		public IServerInterceptor subscriptionSecurityInterceptor() {
			SubscriptionsRequireManualActivationInterceptorDstu3 retVal = new SubscriptionsRequireManualActivationInterceptorDstu3();
			return retVal;
		}

		@Bean()
		public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
			JpaTransactionManager retVal = new JpaTransactionManager();
			retVal.setEntityManagerFactory(entityManagerFactory);
			return retVal;
		}
	}
}