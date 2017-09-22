package at.shockbytes.apps.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import at.shockbytes.apps.R;
import at.shockbytes.apps.adapter.AppsAdapter;
import at.shockbytes.apps.core.AppsApp;
import at.shockbytes.apps.drive.model.LocalShockApp;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainFragment extends Fragment {

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Bind(R.id.fragment_main_rv)
    protected RecyclerView recyclerView;

    private AppsAdapter appsAdapter;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppsApp) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, v);
        setupRecyclerView();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void setApps(List<LocalShockApp> apps) {
        setData(apps);
    }

    private void setData(List<LocalShockApp> apps) {
        appsAdapter.setData(apps);
    }

    public void addApp(LocalShockApp app) {
        appsAdapter.addEntityAtLast(app);
    }

    private void setupRecyclerView() {

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        appsAdapter = new AppsAdapter(getContext(), new ArrayList<LocalShockApp>());
        recyclerView.setAdapter(appsAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.recylcer_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

}
