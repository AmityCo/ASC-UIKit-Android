package com.ekoapp.ekosdk.uikit.community.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.community.ui.view.EkoSelectMembersListActivity
import com.ekoapp.ekosdk.uikit.utils.EkoConstants

class EkoSelectMemberContract :
        ActivityResultContract<ArrayList<SelectMemberItem>, ArrayList<SelectMemberItem>>() {
    override fun createIntent(context: Context, input: ArrayList<SelectMemberItem>): Intent {
        return Intent(context, EkoSelectMembersListActivity::class.java).apply {
            putParcelableArrayListExtra(EkoConstants.MEMBERS_LIST, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ArrayList<SelectMemberItem>? {
        return if (resultCode == Activity.RESULT_OK) intent?.getParcelableArrayListExtra(
                EkoConstants.MEMBERS_LIST
        )
        else null
    }
}