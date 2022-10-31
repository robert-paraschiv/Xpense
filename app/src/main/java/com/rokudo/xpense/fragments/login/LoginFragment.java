package com.rokudo.xpense.fragments.login;

import static com.rokudo.xpense.utils.DatabaseUtils.usersRef;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rokudo.xpense.activities.MainActivity;
import com.rokudo.xpense.data.viewmodels.WalletsViewModel;
import com.rokudo.xpense.databinding.FragmentLoginBinding;
import com.rokudo.xpense.models.User;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.PrefsUtils;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";

    private FragmentLoginBinding binding;

    private FirebaseAuth mAuth;

    private TextInputEditText edtPhone, edtOTP;
    private MaterialButton generateOTPBtn;

    private String verificationId;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();

        edtPhone = binding.idEdtPhoneNumber;
        edtOTP = binding.idEdtOtp;
        generateOTPBtn = binding.idBtnGetOtp;


        generateOTPBtn.setOnClickListener(v -> {
            generateOTPBtn.setEnabled(false);
            if (TextUtils.isEmpty(Objects.requireNonNull(edtPhone.getText()).toString())) {
                Toast.makeText(requireActivity(), "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
            } else {
                String phone = "" + edtPhone.getText().toString();
                sendVerificationCode(phone);
                binding.phoneNrLayout.setVisibility(View.GONE);
                generateOTPBtn.setVisibility(View.GONE);
                binding.otpLayout.setVisibility(View.VISIBLE);
                binding.idBtnVerify.setVisibility(View.VISIBLE);
                binding.idEdtOtp.requestFocus();
            }
        });

        binding.idBtnVerify.setOnClickListener(v -> {
            if (TextUtils.isEmpty(Objects.requireNonNull(edtOTP.getText()).toString())) {
                Toast.makeText(requireActivity(), "Please enter OTP", Toast.LENGTH_SHORT).show();
            } else {
                verifyCode(edtOTP.getText().toString());
            }
        });

        return binding.getRoot();
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getAdditionalUserInfo() != null) {
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                User user = new User();
                                user.setUid(Objects.requireNonNull(task.getResult().getUser()).getUid());
                                user.setPhoneNumber(task.getResult().getUser().getPhoneNumber());

                                usersRef.document(Objects.requireNonNull(edtPhone.getText()).toString()).set(user)
                                        .addOnSuccessListener(unused -> Log.d(TAG, "signInWithCredential: added user to db"));

                                Wallet defaultWallet = new Wallet();
                                defaultWallet.setId(DatabaseUtils.walletsRef.document().getId());
                                defaultWallet.setAmount(0.0);
                                defaultWallet.setCreation_date(new Date());
                                defaultWallet.setCurrency("RON");
                                defaultWallet.setTitle("Default Wallet");
                                defaultWallet.setUsers(Collections.singletonList(user.getUid()));
                                defaultWallet.setCreator_id(user.getUid());

                                PrefsUtils.setSelectedWalletId(requireContext(), defaultWallet.getId());
                                WalletsViewModel walletsViewModel = new ViewModelProvider(requireActivity()).get(WalletsViewModel.class);
                                walletsViewModel.addWallet(defaultWallet).observe(getViewLifecycleOwner(), s -> {
                                    if (s.equals("Success")) {
                                        Navigation.findNavController(binding.getRoot()).popBackStack();
                                    }
                                });

                                DatabaseUtils.walletsRef.document(defaultWallet.getId())
                                        .set(defaultWallet)
                                        .addOnSuccessListener(documentReference
                                                -> Log.d(TAG, "onSuccess: added default wallet"));

                                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token ->
                                        usersRef.document(Objects.requireNonNull(edtPhone.getText()).toString())
                                                .update("token", token)
                                                .addOnSuccessListener(unused -> Log.d(TAG, "signInWithCredential: added user to db")));

                                Navigation.findNavController(binding.getRoot())
                                        .navigate(LoginFragmentDirections.actionLoginFragmentToUserDetailsFragment());
                            } else {
                                Log.d(TAG, "signInWithCredential: user exists already");
                                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token ->
                                        usersRef.document(Objects.requireNonNull(edtPhone.getText()).toString())
                                                .update("token", token)
                                                .addOnSuccessListener(unused -> Log.d(TAG, "signInWithCredential: added user to db")));

                                Intent i = new Intent(requireActivity(), MainActivity.class);
                                startActivity(i);
                                requireActivity().finish();
                            }
                        }
                    } else {
                        generateOTPBtn.setEnabled(true);
                        Log.e(TAG, "signInWithCredential: ", task.getException());
                        Toast.makeText(requireActivity(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendVerificationCode(String number) {
        // this method is used for getting
        // OTP on user phone number.
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)            // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(requireActivity())                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)           // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // callback method is called on Phone auth provider.
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks

            // initializing our callbacks for on
            // verification callback method.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // below method is used when
        // OTP is sent from Firebase
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            // when we receive the OTP it
            // contains a unique id which
            // we are storing in our string
            // which we have already created.
            verificationId = s;
        }

        // this method is called when user
        // receive OTP from Firebase.
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // below line is used for getting OTP code
            // which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();

            if (code != null) {
                // if the code is not null then
                // we are setting that code to
                // our OTP edittext field.
                edtOTP.setText(code);

                // after setting this code
                // to OTP edittext field we
                // are calling our verifycode method.
                verifyCode(code);
            }
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // displaying error message with firebase exception.
            Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "onVerificationFailed: ", e);

            generateOTPBtn.setEnabled(true);
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            Log.d(TAG, "onCodeAutoRetrievalTimeOut: Auto Retrieval timed out ");

            generateOTPBtn.setEnabled(true);
        }
    };

    // below method is use to verify code from Firebase.
    private void verifyCode(String code) {
        // below line is used for getting getting
        // credentials from our verification id and code.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential);
    }

}