package cn.ucai.superwechat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.hyphenate.easeui.domain.User;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.ui.AddContactActivity;
import cn.ucai.superwechat.ui.ChatActivity;
import cn.ucai.superwechat.ui.FriendConfirmActivity;
import cn.ucai.superwechat.ui.GuideActivity;
import cn.ucai.superwechat.ui.LoginActivity;
import cn.ucai.superwechat.ui.MainActivity;
import cn.ucai.superwechat.ui.NewFriendActivity;
import cn.ucai.superwechat.ui.NewFriendsMsgActivity;
import cn.ucai.superwechat.ui.RegisterActivity;
import cn.ucai.superwechat.ui.SettingsActivity;
import cn.ucai.superwechat.ui.UserProfileActivity;

public class MFGT {
    public static void finish(Activity activity){
        activity.finish();
        activity.overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
    }
    public static void gotoMainActivity(Activity context){
        startActivity(context, MainActivity.class);
    }
    public static void startActivity(Activity context,Class<?> cls){
        Intent intent = new Intent();
        intent.setClass(context,cls);
        startActivity(context,intent);
    }
    public static void startActivity(Context context,Intent intent){
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
    }
    public static void startActivityForResult(Activity context,Intent intent,int requestCode){
        context.startActivityForResult(intent,requestCode);
        context.overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
    }
    public static void gotoLoginActivity(Activity context){
        startActivity(context, LoginActivity.class);
    }
    public static void gotoRegisterActivity(Activity context){
        startActivity(context, RegisterActivity.class);
    }

    public static void gotoGuideActivity(Activity context) {
        startActivity(context, GuideActivity.class);
    }

    public static void gotoSettingActivity(Activity context) {
        startActivity(context, SettingsActivity.class);
    }

    public static void gotoUserProfileActivity(Activity context) {
        startActivity(context, UserProfileActivity.class);
    }

    public static void gotoAddFriend(MainActivity mainActivity) {
        startActivity(mainActivity, AddContactActivity.class);
    }

    public static void gotoNewFriendActivity(Activity context, User user) {
        Intent intent = new Intent();
        intent.setClass(context,NewFriendActivity.class);
        intent.putExtra(I.User.USER_NAME,user);
        startActivity(context,intent);
    }

    public static void gotoFriendConfrimActivity(NewFriendActivity context, String mUserName) {
        Intent intent = new Intent();
        intent.setClass(context, FriendConfirmActivity.class);
        intent.putExtra(I.User.USER_NAME,mUserName);
        startActivity(context,intent);
    }

    public static void gotoNewFriendMsg(FragmentActivity activity) {
        startActivity(activity, NewFriendsMsgActivity.class);
    }

    public static void gotoChatActivity(Activity context, String mUserName) {
        Intent intent = new Intent();
        intent.setClass(context, ChatActivity.class);
        intent.putExtra("userId",mUserName);
        startActivity(context,intent);
    }
}
