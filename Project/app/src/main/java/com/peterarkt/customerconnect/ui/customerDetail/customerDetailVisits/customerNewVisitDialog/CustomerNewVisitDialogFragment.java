package com.peterarkt.customerconnect.ui.customerDetail.customerDetailVisits.customerNewVisitDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.provider.CustomerDBUtils;

import java.util.Date;

// Source: https://developer.android.com/reference/android/app/DialogFragment.html
public class CustomerNewVisitDialogFragment extends DialogFragment {

    private static final String CUSTOMER_ID = "CUSTOMER_ID";

    private int mCustomerId;

    private EditText commentaryInputText;

    private DialogInterface.OnDismissListener onDismissListener;

    /*
    * Launch helpers
    * */
    public static CustomerNewVisitDialogFragment newInstance(int customerId) {
        CustomerNewVisitDialogFragment fragment = new CustomerNewVisitDialogFragment();
        Bundle args = new Bundle();
        args.putInt(CUSTOMER_ID, customerId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * OnDismissListener setter:
     * This in order to do something when the Dialog is going to be dismissed.\
     * Source: https://stackoverflow.com/questions/9853430/refresh-fragment-when-dialogfragment-is-dismissed
     */
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }


    /* -------------------------------------------------------------------
    * OnCreateDialog
    * -------------------------------------------------------------------*/
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the CustomerId from the Arguments
        mCustomerId = getArguments().getInt(CUSTOMER_ID,0);

        // Get the View that will be set in the alert dialog and get the reference to the "Commentary" input.
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_customer_new_visit_dialog, (ViewGroup) getView(), false);
        commentaryInputText = (EditText) dialogView.findViewById(R.id.visit_commentary_text_view);

        // Building the dialog (Source: https://stackoverflow.com/questions/10903754/input-text-dialog-android)
        final AlertDialog newVisitAlertDialog = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.new_visit))
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Set Listener on POSITIVE Button. We use this method to force to dismiss
        // the Dialog only when the visit in successfully saved.
        // Source: https://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
        newVisitAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) newVisitAlertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Get the commentary
                        String commentary = commentaryInputText.getText().toString().trim();

                        // if commentary specified is empty, then return...
                        if(commentary.isEmpty()) {
                            Toast.makeText(getActivity(), R.string.commentary_required, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // If commentary is valid, then save it...
                        new CustomerNewVisitDialogFragment.SaveNewVisitAsyncTask().execute(commentary);
                    }
                });
            }
        });

        return newVisitAlertDialog;
    }

    /* -------------------------------------------------------------------
    * OnDismiss
    * -------------------------------------------------------------------*/
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }


    /* ------------------------------------------------------------------------------------------------
    *  AsyncTask for New Visits
      ------------------------------------------------------------------------------------------------*/
    private class SaveNewVisitAsyncTask extends AsyncTask<String,Void,Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... args) {

            if(args == null || args.length == 0) return false;

            Context context = getActivity();
            if (context == null) return false;

            long now = new Date().getTime();
            String visitCommentary = args[0];

            return CustomerDBUtils.insertNewCustomerVisit(context,mCustomerId,now,visitCommentary);
        }

        @Override
        protected void onPostExecute(Boolean savedSuccessfully) {
            super.onPostExecute(savedSuccessfully);

            if(savedSuccessfully){
                // Show Succesful message Toast.
                Toast.makeText(getActivity(),R.string.visit_created_successfully,Toast.LENGTH_SHORT).show();

                // Dismiss the dialog
                dismiss();
            }
            else
                Toast.makeText(getActivity(),R.string.an_error_has_ocurred,Toast.LENGTH_SHORT).show();
        }
    }
}
