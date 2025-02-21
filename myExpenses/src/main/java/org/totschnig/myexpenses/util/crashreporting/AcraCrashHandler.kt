package org.totschnig.myexpenses.util.crashreporting

import android.content.Context
import org.acra.ACRA.errorReporter
import org.acra.ReportField
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.totschnig.myexpenses.BuildConfig
import org.totschnig.myexpenses.MyApplication
import org.totschnig.myexpenses.R
import org.totschnig.myexpenses.util.Utils

class AcraCrashHandler : CrashHandler() {
    override fun onAttachBaseContext(application: MyApplication) {
        application.initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.KEY_VALUE_LIST
            setReportField(ReportField.APP_VERSION_CODE, true)
            setReportField(ReportField.USER_CRASH_DATE, true)
            excludeMatchingSharedPreferencesKeys = arrayOf("planner_calendar_path", "password")
            dialog {
                withResText(R.string.crash_dialog_text)
                title = Utils.getTextWithAppName(application, R.string.crash_dialog_title).toString()
                withResCommentPrompt(R.string.crash_dialog_comment_prompt)
                withResPositiveButtonText(android.R.string.ok)
                reportDialogClass
            }
            mailSender {
                reportAsFile = false
                mailTo = "bug-reports@myexpenses.mobi"
            }
        }
    }

    public override fun setupLoggingDo(context: Context) {
        setKeys(context)
    }

    override fun putCustomData(key: String, value: String?) {
        value?.let { errorReporter.putCustomData(key, it) }
    }
}