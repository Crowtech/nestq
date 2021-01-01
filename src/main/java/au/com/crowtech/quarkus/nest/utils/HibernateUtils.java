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

@SuppressWarnings("unused")
@ApplicationScoped
@RegisterForReflection
public class HibernateUtils {




	public HibernateUtils() {}
	
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
}
