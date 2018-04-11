/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.fcm;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.parse.InternalParseFCMParseAccess;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;

/**
 * Handles saving the FCM token to the {@link ParseInstallation} in the background
 */
public class ParseFirebaseJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters job) {
        InternalParseFCMParseAccess.logVerbose("Updating FCM token");
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        String token = FirebaseInstanceId.getInstance().getToken();
        if (installation != null && token != null) {
            InternalParseFCMParseAccess.setToken(installation, token);
            installation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        InternalParseFCMParseAccess.logVerbose("FCM token saved to installation");
                        jobFinished(job, false);
                    } else {
                        InternalParseFCMParseAccess.logError("FCM token upload failed", e);
                        jobFinished(job, true);
                    }
                }
            });
            return true;
        }
        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true; // Answers the question: "Should this job be retried?"
    }
}
