package com.avoprojects.avoidxline.database;

import com.avoprojects.avoidxline.model.PortoWatchlist;
import com.avoprojects.avoidxline.model.TopWatchlist;
import com.avoprojects.avoidxline.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DbSvc {
    @Autowired
    private Dao mDao;

    private String uid;
    private int psize, wsize;
    private User uProfile;
    private List<PortoWatchlist> uPorto, uWlist;
    private boolean userExist;
    public void init(String uid){
        this.uid = uid;
        try{
            uProfile=mDao.getUser(uid);
            userExist= uProfile!=null;
        }catch(Exception e){
            userExist= false;
        }
    }
    public void initUser(){
        uPorto=mDao.getUserPorto(uid);
        uWlist=mDao.getUserWlist(uid);
        psize=uPorto.size();
        wsize=uWlist.size();
    }
    public boolean isUserExist(){
        return userExist;
    }
    public int createUser(String username, String userstatus){
        int self = mDao.regUser(uid,username,userstatus);
        uProfile.uname=username;
        uProfile.userid=uid;
        uProfile.ustatus=userstatus;
        mDao.newPorto(uid);
        mDao.newWlist(uid);
        return self;
    }
    public int insertPorto(String simbol){
        int plist = 1;
        for(PortoWatchlist p: uPorto){
            if(simbol.equals(p.simbol)){
                plist=1;
                break;
            }
            plist=0;
        }
        if(plist == 0){
            if(psize<10) {
                return mDao.insPorto(uid, simbol);
            }else{
                return -2; //size >= 10
            }
        }else{
            return -1; //simbol udh ada
        }
    }
    public int insertWlist(String simbol){
        int wlist = 1;
        for(PortoWatchlist w: uWlist){
            if(simbol.equals(w.simbol)){
                wlist=1;
                break;
            }
            wlist=0;
        }
        if(wlist == 0){
            if(wsize<20) {
                return mDao.insWlist(uid, simbol);
            }else{
                return -2; //size >= 20
            }
        }else{
            return -1; //simbol udh ada
        }
    }
    public int deletePorto(String simbol){
        int plist = 1;
        for(PortoWatchlist p: uPorto){
            if(simbol.equals(p.simbol)){
                plist=1;
                break;
            }
            plist=0;
        }
        if(psize>0){
            if(plist != 0){
                return mDao.delPortoSimbol(uid,simbol);
            }else{
                return -2; //tidak ada simbol tsb
            }
        }else{
            return -1; //porto kosong
        }
    }
    public int deleteWlist(String simbol){
        int wlist = 1;
        for(PortoWatchlist p: uWlist){
            if(simbol.equals(p.simbol)){
                wlist=1;
                break;
            }
            wlist=0;
        }
        if(wsize>0){
            if(wlist != 0){
                return mDao.delWlistSimbol(uid,simbol);
            }else{
                return -2; //tidak ada simbol tsb
            }
        }else{
            return -1; //porto kosong
        }
    }
    public int updateUsername(String newname){
        return mDao.updUserName(uid,newname);
    }
    public int updateUserbio(String newbio){
        return mDao.updUserStatus(uid,newbio);
    }
    public int updateUser(String newname, String newbio){
        return mDao.updUser(uid,newname,newbio);
    }
    public User getUserProfile(){
        return uProfile;
    }
    public List<PortoWatchlist> getUserPorto(){
        return uPorto;
    }
    public List<PortoWatchlist> getUserWlist(){
        return uWlist;
    }
    public List<TopWatchlist> getTopWatchlist(){
        return mDao.getTopList();
    }
}
