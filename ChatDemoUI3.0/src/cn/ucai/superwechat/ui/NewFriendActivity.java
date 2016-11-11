package cn.ucai.superwechat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.net.NetDao;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

public class NewFriendActivity extends AppCompatActivity {
    private final String TAG = NewFriendActivity.class.getSimpleName();
    @BindView(R.id.ivNewFriendAvatar)
    ImageView ivNewFriendAvatar;
    @BindView(R.id.tvNewFriendNick)
    TextView tvNewFriendNick;
    @BindView(R.id.tvNewFriendUserName)
    TextView tvNewFriendUserName;
    @BindView(R.id.btnAddToContact)
    Button btnAddToContact;
    @BindView(R.id.layout_have)
    LinearLayout layoutHave;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btnSendMessage)
    Button btnSendMessage;
    @BindView(R.id.btnVideoChat)
    Button btnVideoChat;
    User user;
    String username;
    boolean isFriend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);
        ButterKnife.bind(this);
        initView();
        username = getIntent().getStringExtra(I.User.USER_NAME);
        if(username==null){
            MFGT.finish(this);
            return;
        }
        user = SuperWeChatHelper.getInstance().getAppContactList().get(username);
        L.e(TAG, "user==" + user);
        if(user!=null){
            isFriend = true;
            setUserInfo();
        }else{
            isFriend = false;
        }
        isFriend(isFriend);
        findUserInfo();
    }

    private void findUserInfo() {
        NetDao.findContact(this, username, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                if(s!=null){
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    if(result!=null && result.isRetMsg()){
                        user = (User) result.getRetData();
                        L.e(TAG,"findUserInfo user=="+user);
                        setUserInfo();
                    }else{
                        syncFail();
                    }
                }else{
                    syncFail();
                }
            }

            @Override
            public void onError(String error) {
                syncFail();
            }
        });
    }

    private void syncFail() {
        if(!isFriend){
            MFGT.finish(this);
            return;
        }
    }

    private void initView() {
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(getResources().getString(R.string.newFriendProfile));
    }
    private void isFriend(boolean isFriend){
        if (isFriend) {
            layoutHave.setVisibility(View.VISIBLE);
        } else {
            btnAddToContact.setVisibility(View.VISIBLE);
        }
    }
    private void setUserInfo() {
        EaseUserUtils.setAppUserAvatar(this, user.getMUserName(), ivNewFriendAvatar);
        EaseUserUtils.setAppUserNick(user.getMUserNick(), tvNewFriendNick);
        EaseUserUtils.setAppUserNameWithNo(user.getMUserName(), tvNewFriendUserName);
    }

    @OnClick({R.id.btnAddToContact, R.id.btnSendMessage, R.id.btnVideoChat,R.id.iv_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                MFGT.finish(this);
                break;
            case R.id.btnAddToContact:
                MFGT.gotoFriendConfrimActivity(this,user.getMUserName());
                break;
            case R.id.btnSendMessage:
                MFGT.gotoChatActivity(this,user.getMUserName());
                break;
            case R.id.btnVideoChat:
                if (!EMClient.getInstance().isConnected())
                    Toast.makeText(this, R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
                else {
                    startActivity(new Intent(this, VideoCallActivity.class).putExtra("username", user.getMUserName())
                            .putExtra("isComingCall", false));
                }
                break;
        }
    }
}
