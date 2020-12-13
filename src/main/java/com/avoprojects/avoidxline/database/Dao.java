package com.avoprojects.avoidxline.database;
import com.avoprojects.avoidxline.model.User;
import com.avoprojects.avoidxline.model.PortoWatchlist;
import com.avoprojects.avoidxline.model.TopWatchlist;

import java.util.List;

public interface Dao {
    //user
    public List<User> getAllUser();
    public User getUser(String aUserId);
    public int regUser(String aUserId, String aUname, String aUstatus);
    public int updUserName(String aUserId,String aUname);
    public int updUserStatus(String aUserId,String aUstatus);
    public int updUser(String aUserId, String aUname,String aUstatus);
    //portofolio
    public void newPorto(String aUserId);
    public int insPorto(String aUserId, String aSimbol);
    public PortoWatchlist findPortoSimbol(String aUserId, String aSimbol);
    public int delPortoSimbol(String aUserId, String aSimbol);
    public List<PortoWatchlist> getUserPorto(String aUserId);
    public int delUserPorto(String aUserId);

    //watchlist
    public void newWlist(String aUserId);
    public int insWlist(String aUserId, String aSimbol);
    public PortoWatchlist findWlistSimbol(String aUserId, String aSimbol);
    public int delWlistSimbol(String aUserId, String aSimbol);
    public List<PortoWatchlist> getUserWlist(String aUserId);
    public int delUserWlist(String aUserId);

    //topwatchlist
    public List<TopWatchlist> getTopList();
}
