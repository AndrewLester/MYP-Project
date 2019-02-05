package lestera.me.mypproject.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;

import lestera.me.mypproject.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link NoConnectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoConnectionFragment extends Fragment {
    public static final int REQUEST_BLUETOOTH_ENABLE = 0;

    private Button retryButton;
    private ImageView bluetoothIcon;
    private ProgressBar progressBar;
    private Timer timer;

    public NoConnectionFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NoConnectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NoConnectionFragment newInstance() {
        NoConnectionFragment fragment = new NoConnectionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_no_connection, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        retryButton = view.findViewById(R.id.button_retry);
        retryButton.setOnClickListener(this::onRetryButtonClick);

        progressBar = view.findViewById(R.id.connection_retry_progress);

        bluetoothIcon = view.findViewById(R.id.bluetooth_disabled_icon);
        bluetoothIcon.setOnClickListener(this::interaction);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (timer != null) {
            timer.cancel();
        }

        progressBar.clearAnimation();
        progressBar.setVisibility(View.GONE);
        retryButton.setVisibility(View.VISIBLE);
    }

    public void onRetryButtonClick(View view) {
        retryButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.animate();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(() -> {
                    progressBar.clearAnimation();
                    progressBar.setVisibility(View.GONE);
                    retryButton.setVisibility(View.VISIBLE);
                });
                this.cancel();
            }
        }, 1500);

        interaction(view);
    }

    private void interaction(View view) {
        switch (view.getId()) {
            case R.id.bluetooth_disabled_icon:
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                getActivity().startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE);
                break;
            case R.id.button_retry:
                if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.bluetooth_constraint_layout, new BluetoothItemListFragment());
                    fragmentTransaction.commit();
                }
                break;
        }
    }
}
