package com.example.telecommapping.ui.Addplaces;



import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.telecommapping.R;
import com.example.telecommapping.ui.PickerActivity;

import static com.mapbox.mapboxsdk.MapmyIndia.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link com.example.telecommapping.ui.Profile.ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddPlacesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Button button;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddPlacesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static com.example.telecommapping.ui.Addplaces.AddPlacesFragment newInstance(String param1, String param2) {
        com.example.telecommapping.ui.Addplaces.AddPlacesFragment fragment = new com.example.telecommapping.ui.Addplaces.AddPlacesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
   /* public void sendMessage(View view) {
        // Do something in response to button click
    } */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_addplaces, container, false);
        button=root.findViewById(R.id.button_send);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                Intent i = new Intent(getApplicationContext(), PickerActivity.class);
                startActivity(i);



            }
        });
        return root;
    }
}