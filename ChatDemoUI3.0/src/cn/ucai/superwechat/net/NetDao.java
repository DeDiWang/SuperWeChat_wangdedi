package cn.ucai.superwechat.net;

import android.app.Activity;
import android.content.Context;

import java.io.File;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.utils.MD5;


public class NetDao {

    //用户注册
    public static void register(Context context, String userName,
                                String nick, String password,
                                OkHttpUtils.OnCompleteListener<Result> listener){
        OkHttpUtils<Result> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_REGISTER)
                .addParam(I.User.USER_NAME,userName)
                .addParam(I.User.NICK,nick)
                .addParam(I.User.PASSWORD, MD5.getMessageDigest(password))
                .targetClass(Result.class)
                .post()
                .execute(listener);
    }
    //取消注册
    public static void unregister(Context context, String userName,
                                OkHttpUtils.OnCompleteListener<Result> listener){
        OkHttpUtils<Result> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_UNREGISTER)
                .addParam(I.User.USER_NAME,userName)
                .targetClass(Result.class)
                .execute(listener);
    }
    //用户登录
    public static void login(Context context,String userName,String password,
                             OkHttpUtils.OnCompleteListener<String> listener){
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_LOGIN)
                .addParam(I.User.USER_NAME,userName)
                .addParam(I.User.PASSWORD,MD5.getMessageDigest(password))
                .targetClass(String.class)
                .execute(listener);
    }

    //更新用户昵称
    public static void updateNick(Activity context, String muserName,String newNick,OkHttpUtils.OnCompleteListener<String> listener){
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_UPDATE_USER_NICK)
                .addParam(I.User.USER_NAME,muserName)
                .addParam(I.User.NICK,newNick)
                .targetClass(String.class)
                .execute(listener);
    }
    //更新头像的方法
    ///storage/emulated/0/Android/data/cn.ucai.fulicenter/files/Pictures/
    // storage/emulated/0/Android/data/cn.ucai.fulicenter/files/Pictures/user_avatar/wangdedi123.jpg
    //http://101.251.196.90:8000/FuLiCenterServerV2.0/updateAvatar?name_or_hxid=a952702&avatarType=user_avatar
    public static void updateAvatar(Activity context, String muserName, File file, OkHttpUtils.OnCompleteListener<String> listener){
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_UPDATE_AVATAR)
                .addParam(I.NAME_OR_HXID,muserName)
                .addParam(I.AVATAR_TYPE,I.AVATAR_TYPE_USER_PATH)
                .addFile2(file)
                .targetClass(String.class)
                .post()
                .execute(listener);
    }

}
