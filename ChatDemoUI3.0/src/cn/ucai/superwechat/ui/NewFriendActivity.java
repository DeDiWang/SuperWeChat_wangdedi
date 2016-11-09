package cn.ucai.superwechat.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);
        ButterKnife.bind(this);
        user = (User) getIntent().getSerializableExtra(I.User.USER_NAME);
        L.e(TAG, "user==" + user);
        if (user == null) {
            MFGT.finish(this);
        }
        initView();
    }

    private void initView() {
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(getResources().getString(R.string.newFriendProfile));
        setUserInfo();
        if (SuperWeChatHelper.getInstance().getAppContactList().containsKey(user.getMUserName())) {
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
                break;
        }
    }
}
