/*
 * Copyright (c) 2014-2016, Dries007 & DoubleDoorDevelopment
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of DoubleDoorDevelopment nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.doubledoordev.d3core.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.HashMap;
import java.util.UUID;

import static net.doubledoordev.d3core.util.CoreConstants.MODID;

/**
 * @author Dries007
 */
@Mod.EventBusSubscriber(modid = CoreConstants.MODID)
public final class VoidRefunds
{
    private static int[] voidRefundDimensions;

    private static final HashMap<UUID, InventoryPlayer> map = new HashMap<>();

    private VoidRefunds()
    {
    }

    public static void config(Configuration configuration)
    {
        final String catVoidDeaths = MODID + ".VoidDeaths";
        configuration.addCustomCategoryComment(catVoidDeaths, "In these dimensions, when you die to void damage, you will keep your items.");
        voidRefundDimensions = configuration.get(catVoidDeaths, "refundDimensions", new int[] {}).getIntList();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void livingDeathEvent(LivingDeathEvent event)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return;
        if (event.getSource() != DamageSource.OUT_OF_WORLD || !(event.getEntity() instanceof EntityPlayer)) return;
        if (event.getEntityLiving().lastDamage >= (Float.MAX_VALUE / 2)) return; // try to ignore /kill command
        for (int dim : voidRefundDimensions)
        {
            if (dim != event.getEntity().dimension) continue;
            event.setCanceled(true);

            //noinspection ConstantConditions
            InventoryPlayer tempCopy = new InventoryPlayer(null);
            tempCopy.copyInventory(((EntityPlayer) event.getEntity()).inventory);
            map.put(event.getEntity().getPersistentID(), tempCopy);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerRespawnEvent(PlayerEvent.PlayerRespawnEvent event)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return;
        InventoryPlayer oldInventory = map.get(event.player.getPersistentID());
        if (oldInventory == null) return;
        event.player.inventory.copyInventory(oldInventory);
        map.remove(event.player.getPersistentID());
    }
}
