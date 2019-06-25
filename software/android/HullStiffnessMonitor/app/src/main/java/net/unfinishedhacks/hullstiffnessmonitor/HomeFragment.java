package net.unfinishedhacks.hullstiffnessmonitor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HomeFragment extends Fragment {
    private Controller controller;
    private DataView dataView;
    private TextView mTextMessage;
    private TextView accX;
    private TextView accY;
    private TextView accZ;
    private TextView dotX;
    private TextView dotY;
    private TextView batV;


    public static HomeFragment newInstance(Controller c) {
        HomeFragment f = new HomeFragment();

        // Supply index input as an argument.

        return f;
    }


    public HomeFragment()
    {
        controller = ((MainActivity)getActivity()).getController();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        dataView = (DataView) getActivity().findViewById(R.id.dataView);
        dataView.setBackgroundColor(Color.WHITE);
        //setContentView(dataView);


        mTextMessage = (TextView) getActivity().findViewById(R.id.message);
        accX = (TextView) getActivity().findViewById(R.id.acc_x);
        accY = (TextView) getActivity().findViewById(R.id.acc_y);
        accZ = (TextView) getActivity().findViewById(R.id.acc_z);
        dotX = (TextView) getActivity().findViewById(R.id.dot_x);
        dotY = (TextView) getActivity().findViewById(R.id.dot_y);
        batV = (TextView) getActivity().findViewById(R.id.bat_v);

        return inflater.inflate(R.layout.home_fragment, container, false);
    }
}
