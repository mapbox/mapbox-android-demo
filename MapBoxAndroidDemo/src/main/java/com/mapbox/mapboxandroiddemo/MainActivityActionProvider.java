package com.mapbox.mapboxandroiddemo;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.MenuInflater;
import android.view.SubMenu;
import android.view.View;

public class MainActivityActionProvider extends ActionProvider
{
	public MainActivityActionProvider(Context context)
	{
		super(context);
	}

	@Override
	public View onCreateActionView()
	{
		return null;
	}


	@Override
	public boolean hasSubMenu()
	{
		return true;
	}

	@Override
	public void onPrepareSubMenu(SubMenu subMenu)
	{
		if (subMenu.size() < 1)
		{
			MenuInflater menuInflater = new MenuInflater(getContext());
			menuInflater.inflate(R.menu.menu_activity_main_map_spinner, subMenu);
		}
	}
}
