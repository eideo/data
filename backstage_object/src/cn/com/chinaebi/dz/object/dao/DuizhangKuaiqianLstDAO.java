package cn.com.chinaebi.dz.object.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;


import cn.com.chinaebi.dz.object.DuizhangKuaiqianLst;
import cn.com.chinaebi.dz.object.base.BaseDuizhangKuaiqianLstDAO;
import cn.com.chinaebi.dz.util.ConnectionUtil;


public class DuizhangKuaiqianLstDAO extends BaseDuizhangKuaiqianLstDAO implements cn.com.chinaebi.dz.object.dao.iface.DuizhangKuaiqianLstDAO {

	private Log log =LogFactory.getLog(getClass());
	public DuizhangKuaiqianLstDAO () {}
	
	public DuizhangKuaiqianLstDAO (Session session) {
		super(session);
	}

	@Override
	public List<DuizhangKuaiqianLst> findDuizhangKuaiqianLst(
			String deduct_stlm_date, boolean whetherTk) {
		Session session = null;
		List<DuizhangKuaiqianLst> kuaiqianLsts = null;
		try {
			session = this.getSession();
			Query query = session.createQuery("from DuizhangKuaiqianLst where DeductStlmDate = ? and WhetherTk = ?");
			query.setString(0, deduct_stlm_date);
			query.setBoolean(1, whetherTk);
			List list = query.list();
			if(list != null && list.size() > 0){
				kuaiqianLsts = list;
			}
		} catch (Exception e) {
			log.error(e);
		}finally{
			closeSession(session);
		}
		return kuaiqianLsts;
	}

	@Override
	public boolean findDuizhangKuaiqianLstCount(String deduct_stlm_date,
			boolean whetherTk) {
		Session session = null;
		boolean flag = false;
		try {
			session = this.getSession();
			SQLQuery query = session.createSQLQuery("select count(*) from duizhang_kuaiqian_lst where deduct_stlm_date = ? and whetherTk = ?");
			query.setString(0, deduct_stlm_date);
			query.setBoolean(1, whetherTk);
			Integer count = Integer.valueOf(query.uniqueResult().toString());
			if(count > 0){
				flag = true;
			}
		} catch (Exception e) {
			log.error(e);
		}finally{
			closeSession(session);
		}
		return flag;
	}

	/**
	 * 批量插入数据
	 * @param sql
	 * @return
	 */
	@Override
	public boolean insertBankData(String insertSql,String deduct_stlm_date) throws Exception{
		Session session = null;
		Connection conn = null;
		String[] sql_arr = insertSql.split(";");
		try{
			session = this.getSession();
			conn = ConnectionUtil.getConnecttion();
			conn.setAutoCommit(false);
			saveBankData(sql_arr,conn);
			conn.commit();
			return true;
		}catch(Exception e){
			try {
				if(conn != null){
					conn.rollback();
				}
			} catch (SQLException e1) {
				log.info("事务回滚异常:"+e1);
			}
			log.error(e);
			throw e;
		}finally{
			if(conn != null){
				try {  
					ConnectionUtil.closeConnection(conn);  
	             } catch (SQLException e) {  
	            	 log.error(e);  
	             } 
			}
			if(session != null){
				session.close();
			}
		}
	}
	/**
	 *保存快钱对账数据到数据库中
	 * 
	 */
	public void saveBankData(String[] sqlArr,Connection conn) throws Exception{
		Statement stmt = null;
		try{
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			if(sqlArr != null){
				for(int index = 0;index<sqlArr.length;index++){
					if(sqlArr[index] != null && !"".equals(sqlArr[index])){
						stmt.addBatch(sqlArr[index]);
					}
				}
			}
			stmt.executeBatch();
		}catch(Exception e){
			log.error(e);
			throw e;
		}
	}
	
	@Override
	public boolean updateDzBkChk(String reqStance,String oid,String deduct_stlm_date,int bk_chk,int flag) {
		Session session = null;
		Transaction transaction = null;
		boolean update_flag = false;
		try {
			session = this.getSession();
			transaction = session.beginTransaction();
			StringBuffer sb = new StringBuffer("update duizhang_kuaiqian_lst set bk_chk = ? where deduct_stlm_date = ?");
			SQLQuery query = null;
			switch(flag){
			 case 0:
				 sb.append(" and reqSysStance = ?");
				 query = session.createSQLQuery(sb.toString());
				 query.setInteger(0, bk_chk);
				 query.setString(1, deduct_stlm_date);
				 query.setString(2, reqStance);
				 break;
			 case 1:
				 sb.append(" and orderId = ?");
				 query = session.createSQLQuery(sb.toString());
				 query.setInteger(0, bk_chk);
				 query.setString(1, deduct_stlm_date);
				 query.setString(2, oid);
				 break;
			 case 2:
				 sb.append(" and (reqSysStance = ? or orderId = ?)");
				 query = session.createSQLQuery(sb.toString());
				 query.setInteger(0, bk_chk);
				 query.setString(1, deduct_stlm_date);
				 query.setString(2, reqStance);
				 query.setString(3, oid);
				 break;
			}
			if(query != null){
				int count = query.executeUpdate();
				if(count > -1){
					transaction.commit();
					update_flag = true;
				}
			}
		} catch (Exception e) {
			transaction.rollback();
			log.error(e);
		}finally{
			closeSession(session);
		}
		return update_flag;
	}

}