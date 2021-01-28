package au.com.crowtech.quarkus.nest.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.ServiceRegistry;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;


@SuppressWarnings("unused")
@ApplicationScoped
@RegisterForReflection
public class HibernateUtils {

	private static SessionFactory sessionFactory;
	private String username;
	private String password;
	

	public HibernateUtils() {}
	
	public HibernateUtils(final String username, final String password) {
		this.username = username;
		this.password = password;
		
	}
	
	@Transactional
	static public List<Object[]> query(EntityManager em,final String sql) {
		
		
		List<Object[]> rows = em.createNativeQuery(sql).getResultList();

		
//		int count = 0;
//		List<ResultSet> rows = new ArrayList<ResultSet>();
//		try {
//			Connection testCon = null;
//			PreparedStatement testPstmt = null;
//
//			testCon = oldDataSource.getConnection();
//
//			Connection con = null;
//			PreparedStatement pstmt = null;
//
//			try {
//				con = oldDataSource.getConnection();
//				pstmt = con.prepareStatement(sql);
//				ResultSet rs = pstmt.executeQuery();
//				while (rs.next()) {
//					rows.add(rs);
//				}
//				pstmt.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			} finally {
//				if (con != null) {
//					try {
//						con.close();
//					} catch (SQLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		return rows;
	}
	
	 public static SessionFactory buildSessionFactory(final String jdbcUrl,final String username, final String password,Class... clazz) {
	        try {
	            Properties props = new Properties();
	            props.setProperty("hibernate.connection.url", jdbcUrl/*"jdbc:mysql://[db-host]:[db-port]/db_name"*/);
	            props.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
	            props.setProperty("hibernate.connection.username", username);
	            props.setProperty("hibernate.connection.password", password);

	            props.setProperty("hibernate.current_session_context_class", "thread");
	            props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");

	            Configuration configuration = new Configuration();
	            configuration.addProperties(props);
	            for (Class claz : clazz) {
	            	configuration.addAnnotatedClass(claz);
	            }
	            //configuration.addAnnotatedClass(ConfigurationsEntity.class);
	            System.out.println("Hibernate Configuration loaded");

	            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
	            System.out.println("Hibernate serviceRegistry created");

	            SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);

	            return sessionFactory;
	        }
	        catch (Throwable ex) {
	            // Make sure you log the exception, as it might be swallowed
	            System.err.println("Initial SessionFactory creation failed." + ex);
	            throw new ExceptionInInitializerError(ex);
	        }
	    }

//	    public static SessionFactory getSessionFactory() {
//	        if(sessionFactory == null) sessionFactory = buildSessionFactory();
//	        return sessionFactory;
//	    }
}
