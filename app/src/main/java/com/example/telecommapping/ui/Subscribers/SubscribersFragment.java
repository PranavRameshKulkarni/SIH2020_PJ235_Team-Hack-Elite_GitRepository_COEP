package com.example.telecommapping.ui.Subscribers;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.example.telecommapping.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SubscribersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubscribersFragment extends Fragment  {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    AnyChartView anyChartView;
    String[] company= {"Jio","Vodafone Idea Limited","Airtel","BSNL","MTNL"};
    int[] values={32,45,12,24,32,67,23};

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SubscribersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SubscribersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SubscribersFragment newInstance(String param1, String param2) {
        SubscribersFragment fragment = new SubscribersFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_subscribers, container, false);
        anyChartView= root.findViewById(R.id.any_chart_view);
//        setupPiechart();
        getData();
        return  root;
    }
   public void setupPiechart()
   {

       Pie pie= AnyChart.pie();
//       getData();
       List<DataEntry> dataEntries=new ArrayList<>();
       for(int i=0;i<company.length;i++)
       {
          dataEntries.add(new ValueDataEntry(company[i], values[i]));
       }
        pie.data(dataEntries);
       anyChartView.setChart(pie);
   }

   public void getData(){
       Pie pie= AnyChart.pie();
       AsyncHttpClient client = new AsyncHttpClient();
       client.addHeader("Authorization","Token e37a46932ad72db1a61f972d96c4d5ab85f96b9199ac8bf977" );
       Toast.makeText(getContext(), "Fetching data", Toast.LENGTH_LONG).show();
       client.get("https://hackelite.herokuapp.com/telecom", new AsyncHttpResponseHandler() {
           @Override
           public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
               try {
                   JSONObject jsonObject = new JSONObject(new String(responseBody));
                    Iterator<String> str = jsonObject.keys();
                   List<DataEntry> dataEntries=new ArrayList<>();
                   int i = 0;
                    while (str.hasNext()){
                        String key = str.next();
                        dataEntries.add(new ValueDataEntry(key, jsonObject.getDouble(key)));
                    }
                   pie.data(dataEntries);
                    anyChartView.setChart(pie);
               } catch (JSONException e) {
                   e.printStackTrace();
               }
           }

           @Override
           public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
               setupPiechart();
           }
       });
   }

}