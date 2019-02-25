package lestera.me.mypproject.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Optional;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import lestera.me.mypproject.ImagePicker;
import lestera.me.mypproject.R;
import lestera.me.mypproject.viewmodel.PlantViewModel;

public class PlantFragment extends Fragment {

    public static final String PLANT_SAVE_NAME = "plant_save_name";
    public static final String PLANT_SAVE_DESCRIPTION = "plant_save_description";
    public static final String PLANT_SAVE_URI = "plant_save_uri";
    private static final int IMAGE_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 0;

    public static PlantFragment newInstance() {
        return new PlantFragment();
    }

    private ImageView mainImage;
    private TextView name;
    private TextView description;
    private Uri imageUri;
    private Button saveButton;
    private PlantFragmentUpdateListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PlantFragmentUpdateListener) {
            listener = (PlantFragmentUpdateListener) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.plant_fragment, container, false);
        mainImage = view.findViewById(R.id.plant_fragment_image);
        mainImage.setOnClickListener(this::requestImageAccess);

        name = view.findViewById(R.id.plant_fragment_heading);
        description = view.findViewById(R.id.plant_fragment_description);
        saveButton = view.findViewById(R.id.plant_save_button);
        saveButton.setOnClickListener(v -> {
            savePlant();
            Toast.makeText(getContext(), "Plant Saved!", Toast.LENGTH_SHORT).show();
        });

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Add Plant");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //mViewModel = ViewModelProviders.of(this).get(PlantViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE) {
            Pair<Uri, Bitmap> image = ImagePicker.getImageFromResult(getActivity(), resultCode, data);
            if (image.first != null && image.second != null) {
                mainImage.setImageDrawable(null);
                mainImage.setImageURI(null);
                mainImage.setImageURI(image.first);
                imageUri = image.first;
                mainImage.setImageBitmap(image.second);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length != 2) {
                return;
            }

            requestImage();
        }
    }

    private void savePlant() {
        String nameText = name.getText().toString();
        String descriptionText = description.getText().toString();
        String uriString = null;
        if (imageUri != null) {
            uriString = imageUri.toString();
        }

        if (nameText.trim().isEmpty() || descriptionText.trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter a name and description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (listener != null) {
            Bundle args = new Bundle();
            args.putString(PLANT_SAVE_NAME, nameText);
            args.putString(PLANT_SAVE_DESCRIPTION, descriptionText);
            if (uriString != null) {
                args.putString(PLANT_SAVE_URI, uriString);
            }
            listener.onFragmentSave(args);
        }
    }

    public boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestImageAccess(View view) {
        if (!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);

        } else {
            requestImage();
        }
    }

    private void requestImage() {
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(getActivity());
        startActivityForResult(chooseImageIntent, IMAGE_REQUEST_CODE);
    }

    public interface PlantFragmentUpdateListener {
        boolean onFragmentSave(Bundle arguments);
    }
}
