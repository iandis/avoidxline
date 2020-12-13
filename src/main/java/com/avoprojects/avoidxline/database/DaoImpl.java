package com.avoprojects.avoidxline.database;
import com.avoprojects.avoidxline.model.TopWatchlist;
import com.avoprojects.avoidxline.model.User;
import com.avoprojects.avoidxline.model.PortoWatchlist;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
public class DaoImpl implements Dao {
    //tabel user
    private final static String tabel_user = "Tuser";
    private final static String search_all = "select * from " + tabel_user + ";";
    private final static String reg_user = "insert into " + tabel_user + " values('%s','%s','%s');"; //userid, uname, ustatus
    private final static String search_user = "select * from " + tabel_user + " where userid = '%s';";

    private final static String upd_user = "update "+tabel_user+" set uname = '%s', ustatus = '%s' where userid='%s';";
    private final static String upd_user_name = "update "+tabel_user+" set uname = '%s' where userid='%s';";
    private final static String upd_user_status = "update "+tabel_user+" set ustatus = '%s' where userid='%s';";
    //tabel porto
    private final static String tabel_porto = "Tportof";
    private final static String add_porto_symbol = "insert into " + tabel_porto + " (userid, p_simbol) values('%s','%s');"; //tambah data ke tabel
    private final static String add_view_porto = "create view up_%s as select p_simbol from " + tabel_porto + " where userid = '%s';";
    private final static String search_userporto = "select * from up_%s;";
    private final static String search_porto_symbol = "select * from up_%s where p_simbol = '%s';";
    private final static String delete_porto_symbol = "delete from " +tabel_porto+" where userid='%s' and p_simbol='%s';";

    private final static String delete_puser = "delete from "+ tabel_porto +" where userid='%s';";

    //tabel watchlist
    private final static String tabel_wlist = "Twlist";
    private final static String add_wlist_symbol = "insert into " + tabel_wlist + " (userid, w_simbol) values('%s','%s');";
    private final static String add_view_wlist = "create view uw_%s as select w_simbol from " + tabel_wlist + " where userid = '%s';";
    private final static String search_userwlist = "select * from uw_%s;";
    private final static String search_wlist_symbol = "select * from uw_%s where w_simbol = '%s';";
    private final static String delete_wlist_symbol = "delete from " +tabel_wlist+" where userid='%s' and w_simbol='%s';";

    private final static String delete_wuser = "delete from "+ tabel_wlist +" where userid='%s';";

    //tabel view top watchlist
    private final static String tabel_topwlist = "topWatchlist";
    private final static String view_toplist = "select * from " + tabel_topwlist + " order by count desc;";

    private JdbcTemplate mJdbc;

    private final static ResultSetExtractor<User> SINGLE_USER_EXTRACTOR = new ResultSetExtractor<User>()
    {
        @Override
        public User extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
            while(resultSet.next()) {
                User user= new User(
                        resultSet.getString("userid"),
                        resultSet.getString("uname"),
                        resultSet.getString("ustatus")
                );
                return user;
            }
            return null;
        }
    };

    private final static ResultSetExtractor<List<User>> MULTI_USER_EXTRACTOR =new ResultSetExtractor< List<User> >()
    {
        @Override
        public List<User> extractData(ResultSet aRs)
                throws SQLException, DataAccessException
        {
            List<User> list=new Vector<User>();
            while(aRs.next())
            {
                User p=new User(
                        aRs.getString("userid"),
                        aRs.getString("uname"),
                        aRs.getString("ustatus"));
                list.add(p);
            }
            return list;
        }
    };

    private final static ResultSetExtractor<PortoWatchlist> SINGLE_PORTO_EXTRACTOR = new ResultSetExtractor<PortoWatchlist>()
    {
        @Override
        public PortoWatchlist extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
            while(resultSet.next()){
                PortoWatchlist Porto = new PortoWatchlist(
                        resultSet.getString("userid"),
                        resultSet.getString("p_simbol"));
                return Porto;
            }
            return null;
        }
    };

    private final static ResultSetExtractor< List<PortoWatchlist> > MULTI_PORTO_EXTRACTOR =new ResultSetExtractor<List<PortoWatchlist>>()
    {
        @Override
        public List<PortoWatchlist> extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
            List<PortoWatchlist> list=new Vector<>();
            while(resultSet.next())
            {
                PortoWatchlist portolist = new PortoWatchlist(
                        resultSet.getString("userid"),
                        resultSet.getString("p_simbol"));
                list.add(portolist);
            }
            return list;
        }
    };

    private final static ResultSetExtractor<PortoWatchlist> SINGLE_WLIST_EXTRACTOR = new ResultSetExtractor<PortoWatchlist>()
    {
        @Override
        public PortoWatchlist extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
            while(resultSet.next()){
                PortoWatchlist Wlist =new PortoWatchlist(
                        resultSet.getString("userid"),
                        resultSet.getString("w_simbol"));
                return Wlist;
            }
            return null;
        }
    };

    private final static ResultSetExtractor< List<PortoWatchlist> > MULTI_WLIST_EXTRACTOR =new ResultSetExtractor<List<PortoWatchlist>>()
    {
        @Override
        public List<PortoWatchlist> extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
            List<PortoWatchlist> list=new Vector<>();
            while(resultSet.next())
            {
                PortoWatchlist watchlist = new PortoWatchlist(
                        resultSet.getString("userid"),
                        resultSet.getString("w_simbol"));
                list.add(watchlist);
            }
            return list;
        }
    };

    private final static ResultSetExtractor< List<TopWatchlist> > TOP_WLIST_EXTRACTOR =new ResultSetExtractor<List<TopWatchlist>>()
    {
        @Override
        public List<TopWatchlist> extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
            List<TopWatchlist> list=new Vector<>();
            while(resultSet.next())
            {
                TopWatchlist portolist = new TopWatchlist(
                        resultSet.getString("w_simbol"),
                        resultSet.getInt("count"));
                list.add(portolist);
            }
            return list;
        }
    };

    public List<User> getAllUser()
    {
        return mJdbc.query(search_all, MULTI_USER_EXTRACTOR);
    }
    public User getUser(String aUserId)
    {
        return mJdbc.query(String.format(search_user,aUserId), SINGLE_USER_EXTRACTOR);
    }
    public int regUser(String aUserId, String aUname, String aUstatus)
    {
        return mJdbc.update(String.format(reg_user,aUserId,aUname,aUstatus));
    }
    public int updUserName(String aUserId,String aUname)
    {
        return mJdbc.update(String.format(upd_user_name,aUname,aUserId));
    }
    public int updUserStatus(String aUserId,String aUstatus)
    {
        return mJdbc.update(String.format(upd_user_status,aUstatus,aUserId));
    }
    public int updUser(String aUserId,String aUname, String aUstatus)
    {
        return mJdbc.update(String.format(upd_user,aUname,aUstatus,aUserId));
    }
    public void newPorto(String aUserId)
    {
        mJdbc.execute(String.format(add_view_porto,aUserId,aUserId));
    }
    public int insPorto(String aUserId, String aSimbol){
        return mJdbc.update(String.format(add_porto_symbol,aUserId,aSimbol));
    }
    public PortoWatchlist findPortoSimbol(String aUserId, String aSimbol){
        return mJdbc.query(String.format(search_porto_symbol,aUserId,aSimbol),SINGLE_PORTO_EXTRACTOR);
    }
    public int delPortoSimbol(String aUserId, String aSimbol){
        return mJdbc.update(String.format(delete_porto_symbol,aUserId,aSimbol));
    }
    public List<PortoWatchlist> getUserPorto(String aUserId){
        return mJdbc.query(String.format(search_userporto,aUserId),MULTI_PORTO_EXTRACTOR);
    }
    public int delUserPorto(String aUserId){
        return mJdbc.update(String.format(delete_puser,aUserId));
    }

    public void newWlist(String aUserId)
    {
        mJdbc.execute(String.format(add_view_wlist,aUserId,aUserId));
    }
    public int insWlist(String aUserId, String aSimbol){
        return mJdbc.update(String.format(add_wlist_symbol,aUserId,aSimbol));
    }
    public PortoWatchlist findWlistSimbol(String aUserId, String aSimbol){
        return mJdbc.query(String.format(search_wlist_symbol,aUserId,aSimbol),SINGLE_WLIST_EXTRACTOR);
    }
    public int delWlistSimbol(String aUserId, String aSimbol){
        return mJdbc.update(String.format(delete_wlist_symbol,aUserId,aSimbol));
    }
    public List<PortoWatchlist> getUserWlist(String aUserId){
        return mJdbc.query(String.format(search_userwlist,aUserId),MULTI_WLIST_EXTRACTOR);
    }
    public int delUserWlist(String aUserId){
        return mJdbc.update(String.format(delete_wuser,aUserId));
    }

    public List<TopWatchlist> getTopList(){
        return mJdbc.query(view_toplist,TOP_WLIST_EXTRACTOR);
    }
    public DaoImpl(DataSource aDataSource)
    {
        mJdbc=new JdbcTemplate(aDataSource);
    }
}
