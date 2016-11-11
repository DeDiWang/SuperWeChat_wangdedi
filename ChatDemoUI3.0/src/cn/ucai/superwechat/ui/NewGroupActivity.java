/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager.EMGroupOptions;
import com.hyphenate.chat.EMGroupManager.EMGroupStyle;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.net.NetDao;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

import com.hyphenate.easeui.domain.Group;
import com.hyphenate.easeui.utils.EaseImageUtils;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.exceptions.HyphenateException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class NewGroupActivity extends BaseActivity {
	private static final String TAG = NewGroupActivity.class.getSimpleName();
	private static final int REQUESTCODE_PICK = 1;
	private static final int REQUESTCODE_CUTTING = 2;
	private static final int REQUESTCODE_PICK_MEMBER = 3;
	private EditText etGroupName;
	private ProgressDialog dialog;
	private EditText etIntro;
	private CheckBox cbPublic;
	private CheckBox cbMember;
	private TextView secondTextView;
	private ImageView ivGroupAvatar;
	private File groupAvatarFile = null;
	EMGroup emGroup;
	String[] members;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_activity_new_group);
		etGroupName = (EditText) findViewById(R.id.edit_group_name);
		etIntro = (EditText) findViewById(R.id.edit_group_introduction);
		cbPublic = (CheckBox) findViewById(R.id.cb_public);
		cbMember = (CheckBox) findViewById(R.id.cb_member_inviter);
		secondTextView = (TextView) findViewById(R.id.second_desc);
		ivGroupAvatar = (ImageView) findViewById(R.id.iv_group_avatar);

		cbPublic.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if(isChecked){
		            secondTextView.setText(R.string.join_need_owner_approval);
		        }else{
                    secondTextView.setText(R.string.Open_group_members_invited);
		        }
		    }
		});
	}

	/**
	 * @param v
	 */
	public void save(View v) {
		String name = etGroupName.getText().toString();
		if (TextUtils.isEmpty(name)) {
		    new EaseAlertDialog(this, R.string.Group_name_cannot_be_empty).show();
		} else {
			// select from contact list
			startActivityForResult(new Intent(this, GroupPickContactsActivity.class).putExtra("groupName", name), REQUESTCODE_PICK_MEMBER);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

		switch (requestCode) {
			case REQUESTCODE_PICK:
				if (data == null || data.getData() == null) {
					return;
				}
				startPhotoZoom(data.getData());
				break;
			case REQUESTCODE_CUTTING:
				if (data != null) {
					setPicToView(data);
					saveBitmapFile(data);
				}
				break;
			case REQUESTCODE_PICK_MEMBER:
					createEMGroup(resultCode,data);
				break;
			default:
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void createEMGroup(int resultCode, final Intent data) {
		String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
		final String st2 = getResources().getString(R.string.Failed_to_create_groups);
		if (resultCode == RESULT_OK) {
			//new group
			dialog = new ProgressDialog(this);
			dialog.setMessage(st1);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();

			new Thread(new Runnable() {
				@Override
				public void run() {
					final String groupName = etGroupName.getText().toString().trim();
					String desc = etIntro.getText().toString();
					members = data.getStringArrayExtra("newmembers");
					try {
						EMGroupOptions option = new EMGroupOptions();
						option.maxUsers = 200;

						String reason = NewGroupActivity.this.getString(R.string.invite_join_group);
						reason  = EMClient.getInstance().getCurrentUser() + reason + groupName;

						if(cbPublic.isChecked()){
							option.style = cbMember.isChecked() ? EMGroupStyle.EMGroupStylePublicJoinNeedApproval : EMGroupStyle.EMGroupStylePublicOpenJoin;
						}else{
							option.style = cbMember.isChecked()?EMGroupStyle.EMGroupStylePrivateMemberCanInvite:EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite;
						}
						emGroup = EMClient.getInstance().groupManager().createGroup(groupName, desc, members, reason, option);

						createAppGroup();

					} catch (final HyphenateException e) {
						runOnUiThread(new Runnable() {
							public void run() {
								dialog.dismiss();
								Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
							}
						});
					}

				}
			}).start();
		}
	}

	private void createAppGroup() {
		if(groupAvatarFile == null){
			NetDao.createGroup(this, emGroup, listener);
		}else{
			NetDao.createGroup(this, emGroup, groupAvatarFile,listener);
		}
	}

	OkHttpUtils.OnCompleteListener<String> listener =new OkHttpUtils.OnCompleteListener<String>(){
		@Override
		public void onSuccess(String s) {
			L.e(TAG,"s=="+s);
			if(s!=null){
				Result result = ResultUtils.getResultFromJson(s, Group.class);
				if(result!=null && result.isRetMsg()){
					if(emGroup!=null && emGroup.getMembers()!=null && emGroup.getMembers().size()>1){
						addGroupMembers();
					}else{
						afterCreateGroupSuccess();
					}
				}else{
					CommonUtils.showShortToast(getResources().getString(R.string.create_group_fail));
					dialog.dismiss();
				}
			}else{
				CommonUtils.showShortToast(getResources().getString(R.string.create_group_fail));
				dialog.dismiss();
			}
		}

		@Override
		public void onError(String error) {
			CommonUtils.showShortToast(getResources().getString(R.string.create_group_fail));
			dialog.dismiss();
		}
	};

	private void addGroupMembers() {
		NetDao.addGroupMember(this, emGroup, new OkHttpUtils.OnCompleteListener<String>() {
			@Override
			public void onSuccess(String s) {
				if(s!=null){
					Result result = ResultUtils.getResultFromJson(s, Group.class);
					L.e(TAG,"addGroupMembers,result=="+result);
					if(result!=null &&result.isRetMsg()){
						Group group = (Group) result.getRetData();
						afterCreateGroupSuccess();
					}else{
						CommonUtils.showShortToast(getResources().getString(R.string.create_group_fail));
						dialog.dismiss();
					}
				}else{
					CommonUtils.showShortToast(getResources().getString(R.string.create_group_fail));
					dialog.dismiss();
				}
			}

			@Override
			public void onError(String error) {
				CommonUtils.showShortToast(getResources().getString(R.string.create_group_fail));
				dialog.dismiss();
			}
		});
	}

	private void afterCreateGroupSuccess(){
		runOnUiThread(new Runnable() {
			public void run() {
				CommonUtils.showShortToast(getResources().getString(R.string.create_group_success));
				dialog.dismiss();
				setResult(RESULT_OK);
				finish();
			}
		});
	}
	public void back(View view) {
		MFGT.finish(this);
	}

	public void onGroupAvatar(View view) {
		uploadGroupAvatar();
	}

	private void uploadGroupAvatar() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dl_title_upload_photo);
		builder.setItems(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)},
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch (which) {
							case 0:
								Toast.makeText(NewGroupActivity.this, getString(R.string.toast_no_support),
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
			ivGroupAvatar.setImageDrawable(drawable);
		}
	}

	public void saveBitmapFile(Intent data) {
		Bundle extras = data.getExtras();
		if (extras != null) {
			Bitmap bitmap = extras.getParcelable("data");
			String imagePath = EaseImageUtils.getImagePath(System.currentTimeMillis() + I.AVATAR_SUFFIX_JPG);
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
			groupAvatarFile = file;
		}
	}
}
