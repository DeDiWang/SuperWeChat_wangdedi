package cn.ucai.superwechat.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.utils.EaseUserUtils;

import java.security.spec.MGF1ParameterSpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.utils.MFGT;

public class FriendConfirmActivity extends AppCompatActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.etConfirmMessage)
    EditText etConfirmMessage;
    @BindView(R.id.tvFind)
    TextView tvFind;
    String confirmMessage;
    String toAddUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_confirm);
        ButterKnife.bind(this);
        toAddUsername=getIntent().getStringExtra(I.User.USER_NAME);
        initView();
    }

    private void initView() {
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(getResources().getString(R.string.confirm_message));
        tvFind.setVisibility(View.VISIBLE);
        tvFind.setText(getResources().getString(R.string.send));
        etConfirmMessage.setText(getString(R.string.addcontact_send_msg_prefix)
                + EaseUserUtils.getCurrentUserInfo().getMUserNick());
    }

    @OnClick({R.id.iv_back, R.id.tvFind})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                MFGT.finish(this);
                break;
            case R.id.tvFind:
                sendConfirmMessage();
                break;
        }
    }
    ProgressDialog progressDialog;
    private void sendConfirmMessage() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.addcontact_adding));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    //demo use a hardcode reason here, you need let user to input if you like
                    String s = etConfirmMessage.getText().toString().trim();
                    EMClient.getInstance().contactManager().addContact(toAddUsername, s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
                            MFGT.finish(FriendConfirmActivity.this);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
                            MFGT.finish(FriendConfirmActivity.this);
                        }
                    });
                }
            }
        }).start();
    }
}
