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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.hyphenate.chat.EMGroupManager.EMGroupOptions;
import com.hyphenate.chat.EMGroupManager.EMGroupStyle;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.utils.MFGT;

import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.exceptions.HyphenateException;

public class NewGroupActivity extends BaseActivity {
	private EditText etGroupName;
	private ProgressDialog dialog;
	private EditText etIntro;
	private CheckBox cbPublic;
	private CheckBox cbMember;
	private TextView secondTextView;
	private ImageView ivGroupAvatar;

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
			startActivityForResult(new Intent(this, GroupPickContactsActivity.class).putExtra("groupName", name), 0);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
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
					String[] members = data.getStringArrayExtra("newmembers");
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
                        EMClient.getInstance().groupManager().createGroup(groupName, desc, members, reason, option);

						createAppGroup();

						runOnUiThread(new Runnable() {
							public void run() {
								dialog.dismiss();
								setResult(RESULT_OK);
								finish();
							}
						});
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

	}

	public void back(View view) {
		MFGT.finish(this);
	}
}
