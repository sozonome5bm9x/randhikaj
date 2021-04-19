package com.fun.fouresquarevenuefinder.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.foursquare.android.nativeoauth.model.AccessTokenResponse;
import com.foursquare.android.nativeoauth.model.AuthCodeResponse;
import com.fun.fouresquarevenuefinder.R;
import com.fun.fouresquarevenuefinder.foursquare.FourSquareAPIHandler;
import com.fun.fouresquarevenuefinder.utilities.NetworkUtil;

public class LoginActivity extends FragmentActivity {
	Button loginBtn;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.login_layout);
		loginBtn = (Button) findViewById(R.id.login_with_foursquare);
	}

	public void loginFunction(View view) {
		loginBtn.setEnabled(false);
		Intent intent = FoursquareOAuth.getConnectIntent(this,
				FourSquareAPIHandler.CLIENT_ID);
		if (NetworkUtil.haveInternet(getApplicationContext())) {
			if (!FoursquareOAuth.isPlayStoreIntent(intent)) {
				startActivityForResult(intent,
						FourSquareAPIHandler.REQUEST_CODE_FSQ_CONNECT);
			} else {
				toastMessage(LoginActivity.this,
						getString(R.string.please_install_foursquare_app));
				startActivity(intent);
			}
		} else {
			Toast.makeText(getApplicationContext(),
					"Please check internet connection", Toast.LENGTH_LONG)
					.show();
			loginBtn.setEnabled(false);
		}

	}

	public static void toastMessage(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case FourSquareAPIHandler.REQUEST_CODE_FSQ_CONNECT:
			AuthCodeResponse codeResponse = FoursquareOAuth
					.getAuthCodeFromResult(resultCode, data);
			String codeString = codeResponse.getCode();
			Exception exception = codeResponse.getException();
			if (exception == null) {
				if (NetworkUtil.haveInternet(getApplicationContext())) {
					Intent intent = FoursquareOAuth.getTokenExchangeIntent(
							this, FourSquareAPIHandler.CLIENT_ID,
							FourSquareAPIHandler.CLIENT_SECRET, codeString);
					startActivityForResult(
							intent,
							FourSquareAPIHandler.REQUEST_CODE_FSQ_TOKEN_EXCHANGE);
					loginBtn.setEnabled(true);

				} else {
					Toast.makeText(getApplicationContext(),
							"Please check internet connection",
							Toast.LENGTH_LONG).show();
				}

				Log.v("LoginActivity", "CodeString: " + codeString);
			} else {
				Toast.makeText(getApplicationContext(),
						"Authentication not successful.", Toast.LENGTH_LONG)
						.show();
			}

			break;
		case FourSquareAPIHandler.REQUEST_CODE_FSQ_TOKEN_EXCHANGE:
			AccessTokenResponse accessTokenResponse = FoursquareOAuth
					.getTokenFromResult(resultCode, data);
			String accessToken = accessTokenResponse.getAccessToken();
			Exception accessTokenException = accessTokenResponse.getException();
			if (accessTokenException == null) {
				Log.v("LoginActivity", "AccessToken" + accessToken);
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				intent.putExtra(MainActivity.SESSION_ACCESS_TOKEN, accessToken);
				startActivity(intent);
			}
			break;
		default:
			break;
		}
	}
}
