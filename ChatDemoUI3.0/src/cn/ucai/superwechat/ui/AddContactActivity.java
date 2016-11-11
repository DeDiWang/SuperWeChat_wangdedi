package cn.ucai.superwechat.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.widget.EaseAlertDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.net.NetDao;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

public class AddContactActivity extends BaseActivity {
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.edit_note)
    EditText etUserName;

    @BindView(R.id.tvFind)
    TextView tvFind;

    private String toAddUsername;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_add_contact);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(getResources().getString(R.string.add_friend));
        tvFind.setVisibility(View.VISIBLE);
        tvFind.setText(getResources().getString(R.string.search));
    }

    @OnClick(R.id.tvFind)
    public void findContact() {
        final String name = etUserName.getText().toString();
        toAddUsername = name;
        if (TextUtils.isEmpty(name)) {
            new EaseAlertDialog(this, R.string.Please_enter_a_username).show();
            return;
        }
        progressDialog = new ProgressDialog(this);
        String stri = getResources().getString(R.string.searching);
        progressDialog.setMessage(stri);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        findAppContact();
    }

    private void findAppContact() {
        NetDao.findContact(this, toAddUsername, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                progressDialog.dismiss();
                if(s!=null){
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    if(result!=null && result.isRetMsg()){
                        User user = (User) result.getRetData();
                        if(user!=null) {
                            MFGT.gotoNewFriendActivity(AddContactActivity.this, toAddUsername);
                        }
                    }else{
                        CommonUtils.showShortToast(getResources().getString(R.string.no_such_user));
                    }
                }else{
                    CommonUtils.showShortToast(getResources().getString(R.string.no_such_user));
                }
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                CommonUtils.showShortToast(getResources().getString(R.string.no_such_user));
            }
        });
    }

    /**
     * add contact
     *
     * @param view
     */
    public void addContact(View view) {
        if (EMClient.getInstance().getCurrentUser().equals(etUserName.getText().toString())) {
            new EaseAlertDialog(this, R.string.not_add_myself).show();
            return;
        }

        if (SuperWeChatHelper.getInstance().getContactList().containsKey(etUserName.getText().toString())) {
            //let the user know the contact already in your contact list
            if (EMClient.getInstance().contactManager().getBlackListUsernames().contains(etUserName.getText().toString())) {
                new EaseAlertDialog(this, R.string.user_already_in_contactlist).show();
                return;
            }
            new EaseAlertDialog(this, R.string.This_user_is_already_your_friend).show();
            return;
        }



        new Thread(new Runnable() {
            public void run() {

                try {
                    //demo use a hardcode reason here, you need let user to input if you like
                    String s = getResources().getString(R.string.Add_a_friend);
                    EMClient.getInstance().contactManager().addContact(toAddUsername, s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    @OnClick(R.id.iv_back)
    public void onClick() {
        MFGT.finish(this);
    }
}
