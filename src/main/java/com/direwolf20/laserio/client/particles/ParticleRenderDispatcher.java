package com.direwolf20.laserio.client.particles;

import com.direwolf20.laserio.client.particles.fluidparticle.FluidFlowParticle;
import com.direwolf20.laserio.client.particles.itemparticle.ItemFlowParticle;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.integration.mekanism.MekanismIntegration;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ChemicalFlowParticle;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.MekanismModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;


@EventBusSubscriber(modid = LaserIO.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ParticleRenderDispatcher {

    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent evt) {
        evt.registerSpecial(ModParticles.ITEMFLOWPARTICLE.get(), ItemFlowParticle.FACTORY);
        evt.registerSpecial(ModParticles.FLUIDFLOWPARTICLE.get(), FluidFlowParticle.FACTORY);
        if (MekanismIntegration.isLoaded()) {
            evt.registerSpecial(MekanismModParticles.CHEMICALFLOWPARTICLE.get(), ChemicalFlowParticle.FACTORY);
        }
    }
}
