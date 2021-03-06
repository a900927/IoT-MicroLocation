package com.maxinghua.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.maxinghua.application.App;
import com.maxinghua.main.R;

/**
 *  Login fragment for authentication
 */

public class LoginFragment extends Fragment {
    CallbackManager callbackManager;
    LoginButton loginButton;

    public LoginFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onStart() {
        super.onStart();
        //FaceBook
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton)getActivity().findViewById(R.id.facebook_login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                ((App) getActivity().getApplication().getApplicationContext()).setLogin(true);
                Fragment fragment = DialogFragment.newInstance("Login Successful!","Welcome, #User Name");
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.content_frame, fragment).commit();
//                info.setText(
//                        "User ID: "
//                                + loginResult.getAccessToken().getUserId()
//                                + "\n" +
//                                "Auth Token: "
//                                + loginResult.getAccessToken().getToken()
//                );
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
                Fragment fragment = DialogFragment.newInstance("Login failed!","try to login again.");
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.content_frame, fragment).commit();
            }
        });

        Button button = (Button) getActivity().findViewById(R.id.loginButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(),
                "Login", Toast.LENGTH_LONG)
                .show();
                // set login status to true
                ((App) getActivity().getApplication().getApplicationContext()).setLogin(true);
                Fragment fragment = DialogFragment.newInstance("Login Successful!","Welcome, #User Name");
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.content_frame, fragment).commit();
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
