package com.duckblade.osrs.toa.util;

import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RaidStateTracker implements PluginLifecycleComponent
{

	private static final int WIDGET_PARENT_ID = 481;
	private static final int WIDGET_CHILD_ID = 40;

	private final Client client;
	private final EventBus eventBus;

	@Getter
	private boolean inRaid;

	@Getter
	private RaidRoom currentRoom;

	private int lastRegion = -1;

	@Override
	public void startUp()
	{
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		Widget w = client.getWidget(WIDGET_PARENT_ID, WIDGET_CHILD_ID);
		this.inRaid = w != null && !w.isHidden();

		if (!inRaid)
		{
			this.lastRegion = -1;
			this.currentRoom = null;
			return;
		}

		LocalPoint lp = client.getLocalPlayer().getLocalLocation();
		if (lp == null || !client.isInInstancedRegion())
		{
			this.lastRegion = -1;
			this.currentRoom = null;
			return;
		}

		int region = WorldPoint.fromLocalInstance(client, lp).getRegionID();
		if (this.lastRegion != (this.lastRegion = region))
		{
			this.currentRoom = RaidRoom.forRegionId(region);
		}
	}
}