package com.avoprojects.avoidxline.database;

import com.avoprojects.avoidxline.model.PortoWatchlist;
import com.avoprojects.avoidxline.model.TopWatchlist;
import com.avoprojects.avoidxline.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DbSvc {
    @Autowired
    private Dao mDao;

    public boolean isUserExist(String userid){
        return mDao.getUser(userid) != null;
    }
    public int createUser(String uid,String username, String userstatus){
        int self = mDao.regUser(uid,username,userstatus);
        mDao.newPorto(uid);
        mDao.newWlist(uid);
        return self;
    }
    public int insertPorto(String uid,String simbol){
        PortoWatchlist plist = mDao.findPortoSimbol(uid,simbol);
        int psize=mDao.getUserPorto(uid).size();
        if(plist == null){
            if(psize<10) {
                return mDao.insPorto(uid, simbol);
            }else{
                return -2; //size >= 10
            }
        }else{
            return -1; //simbol udh ada
        }
    }
    public int insertWlist(String uid,String simbol){
        PortoWatchlist wlist = mDao.findPortoSimbol(uid,simbol);
        int wsize=mDao.getUserPorto(uid).size();
        if(wlist == null){
            if(wsize<20) {
                return mDao.insWlist(uid, simbol);
            }else{
                return -2; //size >= 20
            }
        }else{
            return -1; //simbol udh ada
        }
    }
    public int deletePorto(String uid,String simbol){
        PortoWatchlist plist = mDao.findPortoSimbol(uid,simbol);
        int psize=mDao.getUserPorto(uid).size();
        if(psize>0){
            if(plist != null){
                return mDao.delPortoSimbol(uid,simbol);
            }else{
                return -2; //tidak ada simbol tsb
            }
        }else{
            return -1; //porto kosong
        }
    }
    public int deleteWlist(String uid,String simbol){
        PortoWatchlist wlist = mDao.findPortoSimbol(uid,simbol);
        int wsize=mDao.getUserPorto(uid).size();
        if(wsize>0){
            if(wlist != null){
                return mDao.delWlistSimbol(uid,simbol);
            }else{
                return -2; //tidak ada simbol tsb
            }
        }else{
            return -1; //porto kosong
        }
    }
    public int updateUsername(String uid,String newname){
        return mDao.updUserName(uid,newname);
    }
    public int updateUserbio(String uid,String newbio){
        return mDao.updUserStatus(uid,newbio);
    }
    public int updateUser(String uid,String newname, String newbio){
        return mDao.updUser(uid,newname,newbio);
    }
    public User getUserProfile(String uid){
        return mDao.getUser(uid);
    }
    public List<PortoWatchlist> getUserPorto(String uid){
        return mDao.getUserPorto(uid);
    }
    public List<PortoWatchlist> getUserWlist(String uid){
        return mDao.getUserWlist(uid);
    }
    public List<TopWatchlist> getTopWatchlist(){
        return mDao.getTopList();
    }
}
