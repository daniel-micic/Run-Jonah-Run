/*
 * Share Manager
 * Inspired by examples
 */

package com.jodabrothers.jonah.manager;

import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.jodabrothers.jonah.GameActivity;

public class ShareManager {
	
	public Session mySession;
	private GameActivity activity;
	
    private UiLifecycleHelper uiHelper;
    private GraphUser user;
    private PendingAction pendingAction = PendingAction.NONE;
    private boolean canPresentShareDialog;
    
    
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }
    
    public ShareManager(GameActivity pActivity) {
    	this.activity = pActivity;
        canPresentShareDialog = FacebookDialog.canPresentShareDialog(activity,
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
    	uiHelper = new UiLifecycleHelper(activity, callback);
        }

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException)) {
                new AlertDialog.Builder(activity)
                    .setTitle("cancelled")
                    .setMessage("permission_not_granted")
                    .setPositiveButton("ok", null)
                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }
    }
	
	@SuppressWarnings("incomplete-switch")
    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {            
            case POST_STATUS_UPDATE:
                shareFacebook();
                break;
        }
    }
	
	private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }

    

    private FacebookDialog.ShareDialogBuilder createShareDialogBuilderForLink() {
        return new FacebookDialog.ShareDialogBuilder(activity)
                .setName("Run Jonah Run")                             
        		.setLink("https://play.google.com/store/apps/details?id=com.jodabrothers.jonah");
    }  
    

    public void shareFacebook() {
        if (canPresentShareDialog) {
            FacebookDialog shareDialog = createShareDialogBuilderForLink().build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
            System.out.println("oi");
        } else if (user != null && hasPublishPermission()) {
        	System.out.println("cant present share dialog");
            final String message = ResourcesManager.getInstance().getShareString() + " https://play.google.com/store/apps/details?id=com.jodabrothers.jonah";
            Request request = Request
                    .newStatusUpdateRequest(Session.getActiveSession(), message, new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {
                            showPublishResult(message, response.getGraphObject(), response.getError());
                        }
                    });                    
            request.executeAsync();
        } else {
        	System.out.println("setting pending action");        	
            String tweetUrl = "http://www.facebook.com/sharer.php?" +
            		"u=https://play.google.com/store/apps/details?id=com.jodabrothers.jonah";
            Uri uri = Uri.parse(tweetUrl);
            Intent shareIntent = new Intent(Intent.ACTION_VIEW, uri).putExtra(Intent.EXTRA_TEXT, ResourcesManager.getInstance().getShareString());
            activity.startActivity(shareIntent);
            pendingAction = PendingAction.POST_STATUS_UPDATE;
        }
    }
    
    private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
        String title = null;
        String alertMessage = null;
        if (error == null) {
            
        } else {
            
            alertMessage = error.getErrorMessage();
        }

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(alertMessage)
                .setPositiveButton("ok", null)
                .show();
    }
    
    public void shareTwitter()
    {
        Intent shareIntent;
        
        shareIntent = new Intent(Intent.ACTION_SEND);
        
        PackageManager packManager = activity.getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(shareIntent,  PackageManager.MATCH_DEFAULT_ONLY);

        for(ResolveInfo resolveInfo: resolvedInfoList){
            if(resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")){
            	System.out.println("nasiel twitter app");
            	shareIntent.setClassName(
                    resolveInfo.activityInfo.packageName, 
                    resolveInfo.activityInfo.name );
                break;
            }
        }
        
        shareIntent.setType("text/*");           
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, ResourcesManager.getInstance().getShareString() + " https://play.google.com/store/apps/details?id=com.jodabrothers.jonah");
        activity.startActivity(shareIntent);
        

   }
	
}
