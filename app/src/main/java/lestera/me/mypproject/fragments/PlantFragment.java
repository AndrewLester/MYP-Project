package lestera.me.mypproject.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import lestera.me.mypproject.ImagePicker;
import lestera.me.mypproject.R;
import lestera.me.mypproject.model.Plant;
import lestera.me.mypproject.viewmodel.PlantViewModel;

public class PlantFragment extends Fragment {

    public static final int NO_PLANT = -1;
    public static final String PLANT_SAVE_NAME = "plant_save_name";
    public static final String PLANT_SAVE_DESCRIPTION = "plant_save_description";
    public static final String PLANT_SAVE_URI = "plant_save_uri";
    public static final String PLANT_SAVE_ID = "plant_save_id";
    private static final int IMAGE_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 0;

    public static PlantFragment newInstance(int id, Plant plant) {
        PlantFragment plantFragment = new PlantFragment();

        Bundle arguments = new Bundle();
        arguments.putInt(PLANT_SAVE_ID, id == NO_PLANT ? NO_PLANT : plant.getId());
        if (id != NO_PLANT) {
            arguments.putString(PLANT_SAVE_NAME, plant.getName());
            arguments.putString(PLANT_SAVE_DESCRIPTION, plant.getDescription());
            if (plant.getImageUri() != null)
                arguments.putString(PLANT_SAVE_URI, plant.getImageUri().toString());
        }

        plantFragment.setArguments(arguments);
        return plantFragment;
    }

    private int plantId = NO_PLANT;

    private ImageView mainImage;
    private EditText name;
    private ImageView nameEditButton;
    private EditText description;
    private ImageView descriptionEditButton;
    private Uri imageUri;
    private Button saveButton;
    private PlantViewModel plantViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.plant_fragment, container, false);
        Bundle arguments = getArguments();
        if (arguments != null) {
            plantId = arguments.getInt(PLANT_SAVE_ID);
        }

        mainImage = view.findViewById(R.id.plant_fragment_image);
        mainImage.setOnClickListener(this::requestImageAccess);

        name = view.findViewById(R.id.plant_fragment_heading);
        nameEditButton = view.findViewById(R.id.plant_name_edit_button);
        description = view.findViewById(R.id.plant_fragment_description);
        descriptionEditButton = view.findViewById(R.id.plant_description_edit_button);
        saveButton = view.findViewById(R.id.plant_save_button);

        if (arguments != null && arguments.getInt(PLANT_SAVE_ID, NO_PLANT) != NO_PLANT) {
            name.setText(arguments.getString(PLANT_SAVE_NAME));
            description.setText(arguments.getString(PLANT_SAVE_DESCRIPTION));
            if (arguments.containsKey(PLANT_SAVE_URI) && !arguments.getString(PLANT_SAVE_URI).equals(""))
                mainImage.setImageURI(Uri.parse(arguments.getString(PLANT_SAVE_URI)));
        }

        saveButton.setOnClickListener(v -> savePlant());

        if (plantId == NO_PLANT) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Add Plant");
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("View Plant");
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && plantId != NO_PLANT) {
            plantId = savedInstanceState.getInt(PLANT_SAVE_ID);
            name.setText(savedInstanceState.getString(PLANT_SAVE_NAME));
            description.setText(savedInstanceState.getString(PLANT_SAVE_DESCRIPTION));
            if (savedInstanceState.getString(PLANT_SAVE_URI).equals("")) {
                imageUri = null;
            } else {
                imageUri = Uri.parse(savedInstanceState.getString(PLANT_SAVE_URI));
            }
        }

        plantViewModel = ViewModelProviders.of(getActivity()).get(PlantViewModel.class);

        if (plantId == NO_PLANT) {
            saveButton.setVisibility(View.VISIBLE);
        }

        makeEditableTextField(name, nameEditButton);
        makeEditableTextField(description, descriptionEditButton);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(PLANT_SAVE_ID, plantId);
        outState.putString(PLANT_SAVE_NAME, name.getText().toString());
        outState.putString(PLANT_SAVE_DESCRIPTION, description.getText().toString());
        outState.putString(PLANT_SAVE_URI, imageUri == null ? "" : imageUri.toString());
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

        Toast.makeText(getContext(), "Plant Saved!", Toast.LENGTH_SHORT).show();

        if (plantId == NO_PLANT) {
            getActivity().onBackPressed();
        }

        onFragmentSave(nameText, descriptionText, uriString);
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestImageAccess(View view) {
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

    private void makeEditableTextField(EditText textView, ImageView button) {
        ((View) textView.getParent()).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                textView.setMaxWidth(
                        ((View) textView.getParent().getParent()).getWidth() - button.getWidth() -
                                ((ViewGroup.MarginLayoutParams) ((View) textView.getParent().getParent()).getLayoutParams()).getMarginEnd());
                textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        textView.setFocusable(true);
        textView.setClickable(true);
        textView.setFocusableInTouchMode(true);

        button.setClickable(true);
        button.setOnClickListener(v -> {
            textView.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT);
        });
        textView.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            public void afterTextChanged(Editable s) {
                saveButton.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onFragmentSave(String title, String description, String stringUri) {
        if (plantId == NO_PLANT) {
            Plant plant = new Plant(title, description);
            if (stringUri != null && !stringUri.equals("")) {
                plant.setImageUri(Uri.parse(stringUri));
            }
            plantViewModel.insert(plant);
        } else {
            Plant plant = new Plant(title, description);
            if (stringUri != null && !stringUri.equals("")) {
                plant.setImageUri(Uri.parse(stringUri));
            }
            plant.setId(plantId);
            plantViewModel.update(plant);
        }
    }
}
