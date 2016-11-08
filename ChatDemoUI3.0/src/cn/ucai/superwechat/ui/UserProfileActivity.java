package cn.ucai.superwechat.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseImageUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.net.NetDao;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

public class UserProfileActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = UserProfileActivity.class.getSimpleName();
    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.ivAvatar)
    ImageView ivAvatar;
    @BindView(R.id.tvNick)
    TextView tvNick;
    @BindView(R.id.tvUserName)
    TextView tvUserName;
    @BindView(R.id.ivCode)
    ImageView ivCode;
    @BindView(R.id.layout_nick)
    LinearLayout layoutNick;
    @BindView(R.id.layout_username)
    LinearLayout layoutUsername;

    private ProgressDialog dialog;
    User user;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.em_activity_user_profile);
        ButterKnife.bind(this);
        initView();
        String userName = EMClient.getInstance().getCurrentUser();
        user = EaseUserUtils.getAppUserInfo(userName);
    }

    private void initView() {
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.title_user_profile));
        EaseUserUtils.setCurrentAppUserAvatar(this, ivAvatar);
        EaseUserUtils.setCurrentAppUserNick(tvNick);
        EaseUserUtils.setCurrentAppUserName(tvUserName);
    }

    public void asyncFetchUserInfo(String username) {
        SuperWeChatHelper.getInstance().getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<EaseUser>() {

            @Override
            public void onSuccess(EaseUser user) {
                if (user != null) {
                    SuperWeChatHelper.getInstance().saveContact(user);
                    if (isFinishing()) {
                        return;
                    }
                    tvNick.setText(user.getNick());
                    if (!TextUtils.isEmpty(user.getAvatar())) {
                        Glide.with(UserProfileActivity.this).load(user.getAvatar()).placeholder(R.drawable.em_default_avatar).into(ivAvatar);
                    } else {
                        Glide.with(UserProfileActivity.this).load(R.drawable.em_default_avatar).into(ivAvatar);
                    }
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
            }
        });
    }


    private void uploadHeadPhoto() {
        Builder builder = new Builder(this);
        builder.setTitle(R.string.dl_title_upload_photo);
        builder.setItems(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Toast.makeText(UserProfileActivity.this, getString(R.string.toast_no_support),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }


    private void updateRemoteNick(final String nickName) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_nick), getString(R.string.dl_waiting));
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean updatenick = SuperWeChatHelper.getInstance().getUserProfileManager().updateCurrentUserNickName(nickName);
                if (UserProfileActivity.this.isFinishing()) {
                    return;
                }
                if (!updatenick) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT)
                                    .show();
                            dialog.dismiss();
                        }
                    });
                } else {
                    updateAppUserNick(nickName);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_success), Toast.LENGTH_SHORT)
                                    .show();
                            tvNick.setText(nickName);
                        }
                    });
                }
            }
        }).start();
    }

    //更新自己服务器用户昵称
    private void updateAppUserNick(String nickName) {
        NetDao.updateNick(this, user.getMUserName(), nickName, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (s != null) {
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    if (result != null && result.isRetMsg()) {
                        User u = (User) result.getRetData();
                        updateLocalUser(u);
                    } else {
                        CommonUtils.showShortToast(getString(R.string.toast_updatenick_fail));
                        dialog.dismiss();
                    }
                } else {
                    CommonUtils.showShortToast(getString(R.string.toast_updatenick_fail));
                    dialog.dismiss();
                }
            }

            @Override
            public void onError(String error) {
                CommonUtils.showShortToast(getString(R.string.toast_updatenick_fail));
                dialog.dismiss();
            }
        });
    }

    //更新本地用户信息
    private void updateLocalUser(User u) {
        //更新本地数据库中用户信息
        SuperWeChatHelper.getInstance().saveAppContact(u);
        //更新内存中用户昵称
        EaseUserUtils.setCurrentAppUserNick(tvNick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    updateAppUserAvatar(data);
                    //setPicToView(data);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //更新本地数据库中用户头像
    private void updateAppUserAvatar(final Intent data) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
        dialog.show();
        File file = saveBitmapFile(data);
        NetDao.updateAvatar(this, user.getMUserName(), file, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (s != null) {
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    User user = (User) result.getRetData();
                    SuperWeChatHelper.getInstance().saveAppContact(user);
                    if (result != null && result.isRetMsg()) {
                        setPicToView(data);
                    } else {
                        dialog.dismiss();
                        CommonUtils.showShortToast(getString(R.string.toast_updatephoto_fail));
                    }
                } else {
                    dialog.dismiss();
                    CommonUtils.showShortToast(getString(R.string.toast_updatephoto_fail));
                }
            }

            @Override
            public void onError(String error) {
                dialog.dismiss();
                L.e(TAG, "error==" + error);
                CommonUtils.showShortToast(getString(R.string.toast_updatephoto_fail));
            }
        });
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     * save the picture data
     *
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(getResources(), photo);
            ivAvatar.setImageDrawable(drawable);
            //uploadUserAvatar(Bitmap2Bytes(photo));
            dialog.dismiss();
            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_success),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_fail),
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadUserAvatar(final byte[] data) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                final String avatarUrl = SuperWeChatHelper.getInstance().getUserProfileManager().uploadUserAvatar(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        if (avatarUrl != null) {
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_success),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_fail),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        }).start();
    }


    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    @OnClick({R.id.iv_back, R.id.ivAvatar, R.id.layout_nick, R.id.layout_username})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                MFGT.finish(this);
                break;
            case R.id.ivAvatar:
                uploadHeadPhoto();
                break;
            case R.id.layout_nick:
                final EditText editText = new EditText(this);
                editText.setText(user.getMUserNick());
                new Builder(this).setTitle(R.string.setting_nickname).setIcon(android.R.drawable.ic_dialog_info).setView(editText)
                        .setPositiveButton(R.string.dl_ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newNick = editText.getText().toString().trim();
                                if (TextUtils.isEmpty(newNick)) {
                                    Toast.makeText(UserProfileActivity.this, getString(R.string.toast_nick_not_isnull), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (newNick.equals(user.getMUserNick())) {
                                    CommonUtils.showShortToast(getString(R.string.nick_not_modify));
                                    editText.requestFocus();
                                    return;
                                }
                                updateRemoteNick(newNick);
                            }
                        }).setNegativeButton(R.string.dl_cancel, null).show();
                break;
            case R.id.layout_username:
                CommonUtils.showShortToast(getString(R.string.user_name_cannot_be_modify));
                break;
            case R.id.ivCode:

                break;
        }
    }

    public File saveBitmapFile(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap bitmap = extras.getParcelable("data");
            String imagePath = EaseImageUtils.getImagePath(user.getMUserName() + I.AVATAR_SUFFIX_JPG);
            File file = new File(imagePath);
            L.e(TAG, "file path==" + file.getAbsolutePath());
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return file;
        }
        return null;
    }
}
