package roukaya.chelly.findfriends.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import roukaya.chelly.findfriends.Constants;
import roukaya.chelly.findfriends.R;
import roukaya.chelly.findfriends.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Set up the send button click listener
        binding.btnsend.setOnClickListener(view -> sendLocationRequest());

        return binding.getRoot();
    }

    private void sendLocationRequest() {
        // Get context safely
        Context context = getContext();
        if (context == null) {
            return; // Fragment not attached to Activity
        }

        // Get the phone number from input with null safety
        Editable editable = binding.editTextNumber.getText();
        String phoneNumber = editable != null ? editable.toString().trim() : "";

        // Validate phone number
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(context, R.string.enter_phone_number, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if we have the required permissions
        if (Constants.GPS_SMS_PERMISSION_STATS) {
            try {
                // Send SMS to request location
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, Constants.MSG_SendMePosition, null, null);
                Toast.makeText(context, R.string.sms_sent_success, Toast.LENGTH_SHORT).show();

                // Clear the input field after sending
                binding.editTextNumber.setText("");
            } catch (Exception e) {
                // Null-safe access to string resources
                String errorMsg = context.getString(R.string.sms_failed);
                String errorDetails = e.getMessage();
                String fullErrorMsg = errorDetails != null ? errorMsg + ": " + errorDetails : errorMsg;

                Toast.makeText(context, fullErrorMsg, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, R.string.permission_not_granted, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}